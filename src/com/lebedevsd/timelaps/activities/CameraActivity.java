package com.lebedevsd.timelaps.activities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.lebedevsd.timelaps.R;
import com.lebedevsd.timelaps.RecordingService;
import com.lebedevsd.timelaps.TimeLapsApplication;
import com.lebedevsd.timelaps.vidgets.CameraPreview;
import com.lebedevsd.timelaps.vidgets.OptionsFragment;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends FragmentActivity {

	private static final String TAG = "CameraActivity";

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mRecorder;

	private boolean isRecording = false;
	private ImageView captureButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!checkCameraHardware(getApplicationContext())) {
			this.finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder();
		TimeLapsApplication.reliaseCamera();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.activity_camera_view);

		mCamera = TimeLapsApplication.getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraView);
		preview.addView(mPreview);
		mPreview.setKeepScreenOn(true);

		Fragment optionsFragment = new OptionsFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.optionsPlaseholder, optionsFragment).commit();

		captureButton = (ImageView) findViewById(R.id.startMotionIV);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRecording) {
					// stop recording and release camera
					mRecorder.stop(); // stop the recording
					releaseMediaRecorder(); // release the MediaRecorder object
					mCamera.lock(); // take camera access back from
									// MediaRecorder

					// inform the user that recording has stopped
					captureButton.setImageResource(R.drawable.ic_flash);
					isRecording = false;
				} else {
					// initialize video camera
					if (prepareVideoRecorder()) {
						// Camera is available and unlocked, MediaRecorder is
						// prepared,
						// now you can start recording
						mRecorder.start();

						// inform the user that recording has started
						captureButton
								.setImageResource(R.drawable.ic_color_effects);

						isRecording = true;
					} else {
						// prepare didn't work, release the camera
						releaseMediaRecorder();
						// inform user
					}
				}
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

	private boolean prepareVideoRecorder() {

		mRecorder = new MediaRecorder();

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
		mRecorder.setOutputFile(Environment.getExternalStorageDirectory()
				.getPath()
				+ "/"
				+ (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date())
				+ ".mp4");

		// Step 5: Set the preview output
		mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

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
			mRecorder.reset(); // clear recorder configuration
			mRecorder.release(); // release the recorder object
			mRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}
}
