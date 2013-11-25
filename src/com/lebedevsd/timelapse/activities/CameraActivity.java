package com.lebedevsd.timelapse.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lebedevsd.timelapse.R;
import com.lebedevsd.timelapse.utils.TimerManager;
import com.lebedevsd.timelapse.vidgets.CameraPreview;
import com.lebedevsd.timelapse.vidgets.OptionsFragment;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends FragmentActivity implements
		RecordingInterface {

	private static final String TAG = "CameraActivity";

	private CameraPreview mCameraPreview;
	private static Camera mCamera;
	private ImageView mCaptureButton;
	private FrameLayout mPreviewLayout;
	private MediaRecorder mRecorder;
	private boolean isRecording = false;
	private Fragment mOptionsFragment;
	private TimerManager mTimerManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!checkCameraHardware(getApplicationContext())) {
			this.finish();
		}
		setContentView(R.layout.activity_camera_view);

		mTimerManager = new TimerManager(this);

		// Create our Preview view and set it as the content of our activity.
		mCameraPreview = new CameraPreview(getApplicationContext());
		mPreviewLayout = (FrameLayout) findViewById(R.id.cameraView);
		mPreviewLayout.addView(mCameraPreview);
		mCameraPreview.setKeepScreenOn(true);

		// Create and add optionsFragment
		mOptionsFragment = new OptionsFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.optionsPlaseholder, mOptionsFragment).commit();

		mCaptureButton = (ImageView) findViewById(R.id.startMotionIV);
		mCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeRecorderState();
			}
		});
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
		if (isRecording) {
			mRecorder.stop();
			releaseMediaRecorder();
			mCamera.lock();
			isRecording = false;
			mCaptureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_color_effects));
		} else
			releaseMediaRecorder();
		reliaseCamera();
		mCamera = null;
		mRecorder = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void changeRecorderState() {
		if (isRecording) {
			stopRecording();
		} else {
			startRecording();
		}
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
				+ File.separator + R.string.lapse_folder;
		String fileName = (new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault())).format(new Date())
				+ ".mp4";
		File timeLapsDirectory = new File(filePath + File.separator);
		timeLapsDirectory.mkdirs();
		mRecorder.setOutputFile(filePath + File.separator + fileName);

		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		// Step 5: Set the preview output
		// mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

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
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (isRecording) {
			// Enabling settings
			ft.show(mOptionsFragment);
			// Free recorder
			mRecorder.stop();
			releaseMediaRecorder();
			// Camera stuff
			mCamera.lock();
			// Activity stuff
			mTimerManager.cancel();
			isRecording = false;
			mCaptureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_color_effects));
		}
		ft.commit();
	}

	@Override
	public void startRecording() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!isRecording) {
			// Disable options
			ft.hide(mOptionsFragment);
			if (prepareVideoRecorder()) {
				mRecorder.start();
				// Activity stuff
				mTimerManager.initTimers();
				isRecording = true;
				mCaptureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_flash));
			} else {
				releaseMediaRecorder();
			}
		}
		ft.commit();
	}

}
