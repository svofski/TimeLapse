package com.lebedevsd.timelaps.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lebedevsd.timelaps.R;
import com.lebedevsd.timelaps.TimeLapsApplication;
import com.lebedevsd.timelaps.vidgets.CameraPreview;
import com.lebedevsd.timelaps.vidgets.OptionsFragment;

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
	private Camera mCamera;
	private ImageView captureButton;
	private FrameLayout preview;
	private MediaRecorder mRecorder;
	private boolean isRecording = false;

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
		Fragment optionsFragment = new OptionsFragment();
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
		TimeLapsApplication.reliaseCamera();
		mCamera = null;
		mRecorder = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void changeRecorderState() {
		if (isRecording) {
			mRecorder.stop();
			releaseMediaRecorder();
			mCamera.lock();
			isRecording = false;
			captureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_color_effects));
		} else {
			if (prepareVideoRecorder()) {
				mRecorder.start();
				isRecording = true;
				captureButton.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_flash));
			} else {
				releaseMediaRecorder();
			}
		}
	}

	private boolean prepareVideoRecorder() {

		mRecorder = new MediaRecorder();
		mCamera = TimeLapsApplication.getCameraInstance();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4: Set output file
		String filePath = Environment.getExternalStorageDirectory().getPath()
				+ "/timelapsVideos";
		String fileName = (new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault())).format(new Date())
				+ ".mp4";
		File timeLapsDirectory = new File(filePath + '/');
		timeLapsDirectory.mkdirs();
		mRecorder.setOutputFile(filePath + '/' + fileName);

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

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
			mCamera.lock();
		}
	}

}
