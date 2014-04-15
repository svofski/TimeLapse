package com.lebedevsd.timelapse.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import static com.lebedevsd.timelapse.activities.CameraActivity.PROGRESS_RECORDING;
import static com.lebedevsd.timelapse.activities.CameraActivity.START_RECORDING;
import static com.lebedevsd.timelapse.activities.CameraActivity.STOP_RECORDING;

import com.lebedevsd.timelapse.vidgets.OptionsFragment;

public class TimerManager {
	private Timer mTimer;
	private int mDelay;
	private int mDuration;
	private TimerTask mDelayTask;
	private TimerTask mDurationTask;
	private TimerTask mCounterTask;
	private int count = 0;
	private Handler mActivityHandler;

	public TimerManager(Handler activityHandler) {
		mTimer = new Timer();
		mActivityHandler = activityHandler;
		mDelay = 0;
		mDuration = 0;
	}

	public void initTimers() {
		count = 0;
		mDelay = OptionsFragment.getLapseDelay();
		mDuration = OptionsFragment.getLapseDuration();
		mCounterTask = new CounterTask();
		mTimer.schedule(mCounterTask, 0, 1000L);
		mDelayTask = new DelayTask();
		mTimer.schedule(mDelayTask, mDelay);
		if (mDuration != 0)
		{
			mDurationTask = new DurationTask();
			mTimer.schedule(mDurationTask, mDelay + mDuration);
		}
	}

	public void cancel() {
		if (mDelayTask != null) {
			mDelayTask.cancel();
			mDelayTask = null;
		}
		if (mDurationTask != null) {
			mDurationTask.cancel();
			mDurationTask = null;
		}
		if (mCounterTask != null) {
			mCounterTask.cancel();
			mCounterTask = null;
		}
	}

	private class DelayTask extends TimerTask {

		@Override
		public void run() {
			mActivityHandler.sendEmptyMessage(START_RECORDING);
		}
	}

	private class DurationTask extends TimerTask {

		@Override
		public void run() {
			mActivityHandler.sendEmptyMessage(STOP_RECORDING);
		}
	}

	private class CounterTask extends TimerTask {

		@Override
		public void run() {
			count++;
			Message msg = new Message();
			msg.what = PROGRESS_RECORDING;
			if (count * 1000 < mDelay) {
				int time = mDelay - count * 1000;
				int seconds = time / 1000;
				int mins = time / 60000;
				msg.obj = ("Time before recording:" + String.format(
						"%d:%d",
						mins,seconds));
				mActivityHandler.sendMessage(msg);
			} else {
				int time = count * 1000 - mDelay;
				int seconds = time / 1000;
				int mins = time / 60000;
				msg.obj = ("Recording:" + String.format(
						"%d:%d",
						mins,seconds));
				mActivityHandler.sendMessage(msg);
			}
		}
	}

}
