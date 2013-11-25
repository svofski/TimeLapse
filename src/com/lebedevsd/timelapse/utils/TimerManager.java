package com.lebedevsd.timelapse.utils;

import java.util.Timer;
import java.util.TimerTask;

import com.lebedevsd.timelapse.activities.RecordingInterface;
import com.lebedevsd.timelapse.vidgets.OptionsFragment;

public class TimerManager {
	private Timer mTimer;
	private int mDelay;
	private int mDuration;
	private TimerTask mDelayTask;
	private TimerTask mDurationTask;
	private RecordingInterface mRecorderController;
	
	public TimerManager(RecordingInterface recorderController){
		mTimer = new Timer();
		mRecorderController = recorderController;
		mDelay = 0;
		mDuration = 0;
	}
	
	public void initTimers(){
		mDelay = OptionsFragment.getLapseDelay();
		mDuration = OptionsFragment.getLapseDuration();
		if (mDelay != 0)
			mTimer.schedule(new DelayTask(), mDelay);
		if (mDuration != 0)
			mTimer.schedule(new DurationTask(), mDelay + mDuration);
	}
		
	public void cancel(){
		if (mDelayTask != null){
			mDelayTask.cancel();
			mDelayTask = null;
		}
		if (mDurationTask != null){
			mDurationTask.cancel();
			mDurationTask = null;
		}	
	}
	
	private class DelayTask extends TimerTask{

		@Override
		public void run() {
			mRecorderController.startRecording();
		}
	}
	
	private class DurationTask extends TimerTask{

		@Override
		public void run() {
			mRecorderController.stopRecording();
		}
	}
	
}
