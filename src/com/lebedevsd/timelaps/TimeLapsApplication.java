package com.lebedevsd.timelaps;

import android.app.Application;
import android.hardware.Camera;
import android.util.Log;

public class TimeLapsApplication extends Application {

	private static final String TAG = "TimeLapsApplication";
	private static Camera mCamera;

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
}
