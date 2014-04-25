package com.lebedevsd.timelapse.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;
import com.lebedevsd.timelapse.R;
import com.lebedevsd.timelapse.utils.TimerManager;
import com.lebedevsd.timelapse.vidgets.CameraPreview;
import com.lebedevsd.timelapse.vidgets.OptionsFragment;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

enum State {
	StateIdle, StatePrepared, StateRecording
}

public class CameraActivity extends Activity implements
		RecordingInterface, BillingInterface {

	private static final String TAG = "CameraActivity";

	public static final int START_RECORDING = 0xff;
	public static final int STOP_RECORDING = 0xff + 1;
	public static final int PROGRESS_RECORDING = 0xff + 2;
	public static final int PREPARE_FOR_RECORDING = 0xff + 3;

	private CameraPreview mCameraPreview;
	private static Camera mCamera;
	private ImageView mCaptureButton;
	private FrameLayout mPreviewLayout;
	private MediaRecorder mRecorder;
	private State mRecordingState;
	private OptionsFragment mOptionsFragment;
	private TimerManager mTimerManager;
	private TextView mCurrentTimer;
	private Handler mActionHandler;
	private IInAppBillingService mService;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(
				"com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);
		if (!checkCameraHardware(getApplicationContext())) {
			this.finish();
		}
		setContentView(R.layout.activity_camera_view);

		mActionHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case START_RECORDING:
					startRecording();
					break;
				case STOP_RECORDING:
					stopRecording();
					break;
				case PROGRESS_RECORDING:
					setRecordingProgress((String) msg.obj);
					break;
				case PREPARE_FOR_RECORDING:
					prepareViewForRecording();
					break;
				default:
					break;
				}
			};
		};

		mTimerManager = new TimerManager(mActionHandler);

		// Create our Preview view and set it as the content of our activity.
		mCameraPreview = new CameraPreview();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.cameraView, mCameraPreview)
				.commitAllowingStateLoss();

		mCurrentTimer = (TextView) findViewById(R.id.tvCurrentTimer);

		// Create and add optionsFragment
		mOptionsFragment = new OptionsFragment();
		mOptionsFragment.setBillindInterfaceDelegate(this);
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.optionsPlaseholder, mOptionsFragment)
				.commitAllowingStateLoss();

		mCaptureButton = (ImageView) findViewById(R.id.startMotionIV);
		mCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRecordingState == State.StateIdle) {
					mTimerManager.initTimers();
				} else {
					stopRecording();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001) {
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(purchaseData);
					String sku = jo.getString("productId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			unbindService(mServiceConn);
		}
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopRecording();
		reliaseCamera();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mOptionsFragment.setBillindInterfaceDelegate(this);
		mRecordingState = State.StateIdle;
		mCurrentTimer.setVisibility(View.GONE);
	}

	private boolean prepareVideoRecorder() {

		mRecorder = new MediaRecorder();
		mCamera = getCameraInstance();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mRecorder.setProfile(CamcorderProfile.get(OptionsFragment
				.getLapseQuality()));

		mRecorder.setCaptureRate(OptionsFragment.getLapseFrameRate());

		// Step 4: Set output file
		String filePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator
				+ getResources().getString(R.string.lapse_folder_name);
		String fileName = "TimeLapse"
				+ (new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()))
						.format(new Date()) + ".mp4";
		File timeLapsDirectory = new File(filePath + File.separator);
		timeLapsDirectory.mkdirs();
		galleryAddVideo(timeLapsDirectory);
		mRecorder.setOutputFile(filePath + File.separator + fileName);

		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		// Step 5: Set the preview output
		// mRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	public static Camera getCameraInstance() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			} catch (Exception e) {
				Log.d("TAG", e.getMessage());
			}
		}
		return mCamera;
	}

	public static void reliaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			mCamera.lock();
		}
	}

	@Override
	public void stopRecording() {
		// Activity stuff
		mTimerManager.cancel();
		mCaptureButton.setImageDrawable(getResources().getDrawable(
				R.drawable.ic_action));
		mCurrentTimer.setVisibility(View.GONE);
		if (mRecordingState == State.StateRecording) {
			// Free recorder
			mRecorder.stop();
			releaseMediaRecorder();
			// Camera stuff
			mCamera.lock();
		}
		mRecordingState = State.StateIdle;
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.show(mOptionsFragment);
		ft.commitAllowingStateLoss();
	}

	private void galleryAddVideo(File f) {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
		AppRater.app_event(this);
	}

	@Override
	public void startRecording() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (mRecordingState != State.StateRecording) {
			// Disable options
			ft.hide(mOptionsFragment);
			if (prepareVideoRecorder()) {
				mRecorder.start();
				// Activity stuff
				mRecordingState = State.StateRecording;
				mCaptureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_action_selected));
				mCurrentTimer.setVisibility(View.VISIBLE);
			} else {
				releaseMediaRecorder();
			}
		}
		ft.commitAllowingStateLoss();
	}

	private void prepareViewForRecording() {
		if (mRecordingState == State.StateIdle) {
			FragmentTransaction ft = getFragmentManager()
					.beginTransaction();
			ft.hide(mOptionsFragment);
			mRecordingState = State.StatePrepared;
			mCaptureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_action_selected));
			mCurrentTimer.setVisibility(View.VISIBLE);
			ft.commitAllowingStateLoss();
		}
	}

	public void setRecordingProgress(String progress) {
		mCurrentTimer.setText(progress);
	}

	@Override
	public void donateButtonWasPressed() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				ArrayList<String> skuList = new ArrayList<String>();
				skuList.add("donation_for_timelapse_application");
				Bundle querySkus = new Bundle();
				querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

				try {
					Log.d("", getPackageName());
					Bundle skuDetails = mService.getSkuDetails(3,
							getPackageName(), "inapp", querySkus);
					int response = skuDetails.getInt("RESPONSE_CODE");
					if (response == 0) {
						ArrayList<String> responseList = skuDetails
								.getStringArrayList("DETAILS_LIST");

						for (String thisResponse : responseList) {
							try {
								JSONObject object = new JSONObject(thisResponse);
								Bundle buyIntentBundle = mService.getBuyIntent(
										3, getPackageName(),
										object.getString("productId"), "inapp",
										null);
								PendingIntent pendingIntent = buyIntentBundle
										.getParcelable("BUY_INTENT");
								try {
									startIntentSenderForResult(
											pendingIntent.getIntentSender(),
											1001, new Intent(),
											Integer.valueOf(0),
											Integer.valueOf(0),
											Integer.valueOf(0));
								} catch (SendIntentException e) {
									e.printStackTrace();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
}
