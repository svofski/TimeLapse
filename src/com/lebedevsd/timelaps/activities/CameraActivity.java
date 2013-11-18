package com.lebedevsd.timelaps.activities;

import com.lebedevsd.timelaps.R;
import com.lebedevsd.timelaps.TimeLapsApplication;
import com.lebedevsd.timelaps.service.RecordingService;
import com.lebedevsd.timelaps.vidgets.CameraPreview;
import com.lebedevsd.timelaps.vidgets.OptionsFragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Context;
import android.content.Intent;
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

	private ImageView captureButton;
	private FrameLayout preview;
	private boolean isRecording;
	Messenger mService = null;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d("Activity", "message recieved");
			switch (msg.what) {
			case RecordingService.MSG_REPLY_TO:
				mService = msg.replyTo;
				isRecording = (msg.arg1 == 1);
				changeButtonState();
				break;
			case RecordingService.MSG_ERCORDING_STATE_CHANGED:
				isRecording = (msg.arg1 == 1);
				changeButtonState();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!checkCameraHardware(getApplicationContext())) {
			this.finish();
		}
		getApplicationContext().startService(
				(new Intent(getApplicationContext(), RecordingService.class))
						.putExtra("replyTo", mMessenger));

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
				Message reply = new Message();
				reply.what = RecordingService.MSG_RECORD;
				try {
					mService.send(reply);
				} catch (RemoteException e) {
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		Message reply = new Message();
		reply.what = RecordingService.MSG_ACTIVITY_PAUSED;
		try {
			mService.send(reply);
		} catch (RemoteException e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void changeButtonState() {
		if (isRecording)
			captureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_flash));
		else
			captureButton.setImageDrawable(getResources().getDrawable(
					R.drawable.ic_color_effects));
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
	protected void onDestroy() {
		super.onDestroy();
	}
}
