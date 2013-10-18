package com.lebedevsd.timelaps;

import android.app.Application;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

public class TimeLapsApplication extends Application {

	private static Camera mCamera;
	private static boolean isRecording = false;

	public static Context getContext() {
		return getContext();
	}

	public static Camera getCameraInstance() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open(); // attempt to get a Camera instance
			} catch (Exception e) {
				// Camera is not available (in use or does not exist)
			}
		}
		return mCamera; // returns null if camera is unavailable
	}

	public static void reliaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
}
