package com.lebedevsd.timelaps.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.lebedevsd.timelaps.TimeLapsApplication;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class RecordingService extends Service {

	private static final String TAG = "RecordingService";

	private Messenger mClient = null;
	private Looper mServiceLooper;
	private IncomingHandler mServiceHandler;

	// TO SERVICE
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_RECORD = 3;
	public static final int MSG_ACTIVITY_PAUSED = 4;

	// TO ACTIVITY
	public static final int MSG_REPLY_TO = 5;
	public static final int MSG_PROGRESS = 6;
	public static final int MSG_ERCORDING_STATE_CHANGED = 7;

	private final class IncomingHandler extends Handler {

		public IncomingHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClient = msg.replyTo;
				Log.d(TAG, "Client recived");
				Message reply = mServiceHandler.obtainMessage();
				reply.what = MSG_REPLY_TO;
				reply.replyTo = mMessenger;
				reply.arg1 = isRecording ? 1 : 0;
				try {
					mClient.send(reply);
				} catch (RemoteException e) {
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				mClient = null;
				break;
			case MSG_RECORD:
				changeRecorderState();
				break;
			case MSG_ACTIVITY_PAUSED:
				if (!isRecording) {
					TimeLapsApplication.reliaseCamera();
					stopSelf();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private Camera mCamera;
	private MediaRecorder mRecorder;
	private boolean isRecording = false;
	private Messenger mMessenger = null;

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Thread.NORM_PRIORITY);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new IncomingHandler(mServiceLooper);
		mMessenger = new Messenger(mServiceHandler);
	}

	private void changeRecorderState() {
		if (isRecording) {
			mRecorder.stop();
			releaseMediaRecorder();
			mCamera.lock();
			isRecording = false;
		} else {
			
			if (prepareVideoRecorder()) {
				mRecorder.start();
				isRecording = true;
			} else {
				releaseMediaRecorder();
			}
		}
		Message reply = mServiceHandler.obtainMessage();
		reply.what = MSG_ERCORDING_STATE_CHANGED;
		reply.arg1 = isRecording ? 1 : 0;
		try {
			mClient.send(reply);
		} catch (RemoteException e) {
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Message msg = mServiceHandler.obtainMessage();
		msg.what = MSG_REGISTER_CLIENT;
		msg.replyTo = (Messenger) intent.getExtras().get("replyTo");
		mServiceHandler.sendMessage(msg);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
