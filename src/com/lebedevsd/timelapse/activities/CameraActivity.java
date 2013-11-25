package com.lebedevsd.timelapse.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lebedevsd.timelapse.R;
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

public class CameraActivity extends FragmentActivity {

	private static final String TAG = "CameraActivity";

	private CameraPreview mPreview;
	private static Camera mCamera;
	private ImageView captureButton;
	private FrameLayout preview;
	private MediaRecorder mRecorder;
	private boolean isRecording = false;
	private Fragment optionsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!checkCameraHardware(getApplicationContext())) {
			this.finish();
		}
		setContentView(R.layout.activity_camera_view);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(getApplicationContext());
		preview = (FrameLayout) findViewById(R.id.cameraView);
		preview.addView(mPreview);
		mPreview.setKeepScreenOn(true);

		// Create and add optionsFragment
		optionsFragment = new OptionsFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.optionsPlaseholder, optionsFragment).commit();

		captureButton = (ImageView) findViewById(R.id.startMotionIV);
		captureButton.setOnClickListener(new View.OnClickListener() {
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
			captureButton.setImageDrawable(getResources().getDrawable(
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
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (isRecording) {
			ft.show(optionsFragment);
			mRecorder.stop();
			releaseMediaRecorder();
			mCamera.lock();
			isRecording = false;
			captureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_color_effects));
		} else {
			ft.hide(optionsFragment);
			if (prepareVideoRecorder()) {
				mRecorder.start();
				isRecording = true;
				captureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_flash));
			} else {
				releaseMediaRecorder();
			}
		}
		ft.commit();
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
		mRecorder.setProfile(CamcorderProfile
				.get(OptionsFragment.getQuality()));
		
		mRecorder.setCaptureRate(0.3);
		
		// Step 4: Set output file
		String filePath = Environment.getExternalStorageDirectory().getPath()
				+ "/timelapsVideos";
		String fileName = (new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault())).format(new Date())
				+ ".mp4";
		File timeLapsDirectory = new File(filePath + '/');
		timeLapsDirectory.mkdirs();
		mRecorder.setOutputFile(filePath + '/' + fileName);

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

}
