package com.lebedevsd.timelapse.vidgets;

import java.io.IOException;

import com.lebedevsd.timelapse.R;
import com.lebedevsd.timelapse.activities.CameraActivity;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CameraPreview extends Fragment{
	private static String TAG = "CameraPreview";
	
	private FrameLayout mCameraPreviewHolder;
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder.Callback mSurfaceCallBack;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mSurfaceCallBack = new SurfaceHolder.Callback(){
			public void surfaceCreated(SurfaceHolder holder) {
				// The Surface has been created, now tell the camera where to draw the
				// preview.
				try {
					mCamera = CameraActivity.getCameraInstance();
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
				} catch (IOException e) {
					Log.d(TAG, "Error setting camera preview: " + e.getMessage());
				}
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				// empty. Take care of releasing the Camera preview in your activity.
			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				// stop preview before making changes
				try {
					mCamera.stopPreview();
				} catch (Exception e) {
					// ignore: tried to stop a non-existent preview
				}

				mSurfaceView.requestLayout();

				// start preview with new settings
				try {
					mCamera.setPreviewDisplay(mHolder);
					mCamera.startPreview();

				} catch (Exception e) {
					Log.d(TAG, "Error starting camera preview: " + e.getMessage());
				}

			}
		};
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		mCamera.stopPreview();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(mSurfaceCallBack);
		mCamera = CameraActivity.getCameraInstance();
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mSurfaceView.setKeepScreenOn(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mCameraPreviewHolder = (FrameLayout) inflater.inflate(
				R.layout.camera_preview_layout, container, false);
		mSurfaceView = (SurfaceView) mCameraPreviewHolder.findViewById(R.id.svCameraPreview);
		return mCameraPreviewHolder;
	}
}