package com.lebedevsd.timelapse.vidgets;

import java.io.File;
import java.util.List;

import com.lebedevsd.timelapse.R;
import com.lebedevsd.timelapse.activities.CameraActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class OptionsFragment extends Fragment implements OnClickListener,
		MediaScannerConnectionClient {

	private static Camera.Parameters mCameraParams;
	private String SCAN_PATH;
	private static final String FILE_TYPE = "video/*";
	private MediaScannerConnection conn;
	private FrameLayout mOptionsHolder;
	private LinearLayout mOptionsLayout;

	private ImageView mFlashImageView;
	private ImageView mVideoOptionsImageView;
	private ImageView mWhiteBalanceImageView;
	private ImageView mColorEffectImageView;
	private ImageView mFocusModeImageView;
	private ImageView mISOImageView;
	private ImageView mFPSImageView;
	private ImageView mDurationImageView;
	private ImageView mDelayImageView;
	private ImageView mFolderImageView;
	private static int mLapseQuality;
	private static float mLapseFrameRate;
	private static int mSelectedFrameRate;
	private static int mLapseDuration;
	private static int mSelectedDurationOption;
	private static int mLapseDelay;
	private static int mSelectedDelayOption;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mOptionsHolder = (FrameLayout) inflater.inflate(
				R.layout.options_layout, container, false);
		mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_1080P;
		mLapseFrameRate = 20;
		mSelectedFrameRate = 0;
		mLapseDelay = 0;
		mSelectedDelayOption = 0;
		mLapseDuration = 0;
		mSelectedDurationOption = 0;
		return mOptionsHolder;
	}

	@Override
	public void onResume() {
		super.onResume();
		initInnerComponents();
	}

	public static int getLapseQuality() {
		return mLapseQuality;
	}

	public static float getLapseFrameRate() {
		return mLapseFrameRate;
	}

	public static int getLapseDuration() {
		return mLapseDuration;
	}

	public static int getLapseDelay() {
		return mLapseDelay;
	}

	private void initInnerComponents() {
		// init UI elements

		mOptionsLayout = (LinearLayout) mOptionsHolder
				.findViewById(R.id.options);
		mFolderImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.lastVideoIV);
		mFlashImageView = (ImageView) mOptionsLayout.findViewById(R.id.flashIV);
		mVideoOptionsImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.videoSizeIV);
		mWhiteBalanceImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.whiteBalanceIV);
		mColorEffectImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.colorEffectsIV);
		mFocusModeImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.focusModeIV);
		mISOImageView = (ImageView) mOptionsLayout.findViewById(R.id.isoIV);
		mFPSImageView = (ImageView) mOptionsLayout.findViewById(R.id.fpsIV);
		mDurationImageView = (ImageView) mOptionsLayout
				.findViewById(R.id.durationIV);
		mDelayImageView = (ImageView) mOptionsLayout.findViewById(R.id.delayIV);
		

		mCameraParams = CameraActivity.getCameraInstance().getParameters();

		// set clickListener to UI elements
		mFlashImageView.setOnClickListener(this);
		mVideoOptionsImageView.setOnClickListener(this);
		mWhiteBalanceImageView.setOnClickListener(this);
		mColorEffectImageView.setOnClickListener(this);
		mFocusModeImageView.setOnClickListener(this);
		mISOImageView.setOnClickListener(this);
		mFPSImageView.setOnClickListener(this);
		mDurationImageView.setOnClickListener(this);
		mDelayImageView.setOnClickListener(this);
		mFolderImageView.setOnClickListener(this);
	}

	public static class ColorEffectsDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			List<String> colorEffects = mCameraParams
					.getSupportedColorEffects();
			String[] array = colorEffects.toArray(new String[colorEffects
					.size()]);
			int indexOfActiveSetting = colorEffects.indexOf(mCameraParams
					.getColorEffect());
			builder.setTitle(
					getResources().getString(
							R.string.color_effects_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mCameraParams.setColorEffect(mCameraParams
											.getSupportedColorEffects().get(
													which));
									CameraActivity.getCameraInstance()
											.stopPreview();
									CameraActivity.getCameraInstance()
											.setParameters(mCameraParams);
									CameraActivity.getCameraInstance()
											.startPreview();
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class FocusModesDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			List<String> modes = mCameraParams.getSupportedFocusModes();
			String[] array = modes.toArray(new String[modes.size()]);
			int indexOfActiveSetting = modes.indexOf(mCameraParams
					.getFocusMode());
			builder.setTitle(
					getResources().getString(R.string.focus_mode_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mCameraParams.setFocusMode(mCameraParams
											.getSupportedFocusModes()
											.get(which));
									CameraActivity.getCameraInstance()
											.setParameters(mCameraParams);
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class VideoSizesDialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			array = getResources().getStringArray(R.array.time_lapse_quality);
			int indexOfActiveSetting = 0;
			switch (mLapseQuality) {
			case CamcorderProfile.QUALITY_TIME_LAPSE_1080P:
				indexOfActiveSetting = 0;
				break;
			case CamcorderProfile.QUALITY_TIME_LAPSE_720P:
				indexOfActiveSetting = 1;
				break;
			case CamcorderProfile.QUALITY_TIME_LAPSE_480P:
				indexOfActiveSetting = 2;
				break;
			case CamcorderProfile.QUALITY_TIME_LAPSE_HIGH:
				indexOfActiveSetting = 3;
				break;
			case CamcorderProfile.QUALITY_TIME_LAPSE_LOW:
				indexOfActiveSetting = 4;
				break;
			}
			builder.setTitle(
					getResources().getString(R.string.video_sizes_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										switch (which) {
										case 0:
											mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_1080P;
											break;
										case 1:
											mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_720P;
											break;
										case 2:
											mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_480P;
											break;
										case 3:
											mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_HIGH;
											break;
										case 4:
											mLapseQuality = CamcorderProfile.QUALITY_TIME_LAPSE_LOW;
											break;
										}
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class DurationDialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			array = getResources().getStringArray(R.array.time_lapse_duration);
			builder.setTitle(
					getResources().getString(R.string.duration_dialog_title))
					.setSingleChoiceItems(array, mSelectedDurationOption,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										mSelectedDurationOption = which;
										switch (which) {
										case 0:
											mLapseDuration = 0;
											break;
										case 1:
											mLapseDuration = 30 * 1000; // 30
																		// sec
											break;
										case 2:
											mLapseDuration = 1 * 60 * 1000; // 1
																			// min
											break;
										case 3:
											mLapseDuration = 5 * 60 * 1000; // 5
																			// min
											break;
										case 4:
											mLapseDuration = 15 * 60 * 1000; // 15
																				// min
											break;
										case 5:
											mLapseDuration = 30 * 60 * 1000; // 30
																				// min
											break;
										case 6:
											mLapseDuration = 1 * 60 * 60 * 1000; // 1
																					// hour
											break;
										}
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class DelayDialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			array = getResources().getStringArray(R.array.time_lapse_delay);
			builder.setTitle(
					getResources().getString(R.string.delay_dialog_title))
					.setSingleChoiceItems(array, mSelectedDelayOption,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										mSelectedDelayOption = which;
										switch (which) {
										case 0:
											mLapseDelay = 0;
											break;
										case 1:
											mLapseDelay = 15 * 60 * 1000; // 15
																			// min
											break;
										case 2:
											mLapseDelay = 30 * 60 * 1000; // 30
																			// min
											break;
										case 3:
											mLapseDelay = 1 * 60 * 60 * 1000; // 1
																				// hour
											break;
										case 4:
											mLapseDelay = 2 * 60 * 60 * 1000; // 2
																				// hours
											break;
										case 5:
											mLapseDelay = 3 * 60 * 60 * 1000; // 3
																				// hours
											break;
										}
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class LapseRateDialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			array = getResources()
					.getStringArray(R.array.time_lapse_frame_rate);
			builder.setTitle(
					getResources().getString(R.string.lapse_rate_dialog_title))
					.setSingleChoiceItems(array, mSelectedFrameRate,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										mSelectedFrameRate = which;
										switch (which) {
										case 0:
											mLapseFrameRate = 20;
											break;
										case 1:
											mLapseFrameRate = 15;
											break;
										case 2:
											mLapseFrameRate = 10;
											break;
										case 3:
											mLapseFrameRate = 5;
											break;
										case 4:
											mLapseFrameRate = 1;
											break;
										case 5:
											break;
										}
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class AntibandingDialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String supportedIsoValues = mCameraParams.get("antibanding-values");
			array = supportedIsoValues.split(",");
			String value = mCameraParams.get("antibanding");
			int indexOfActiveSetting = 0;
			for (; indexOfActiveSetting < array.length; indexOfActiveSetting++)
				if (array[indexOfActiveSetting].equals(value))
					break;
			builder.setTitle(
					getResources().getString(R.string.antibanding_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										mCameraParams.set("antibanding",
												array[which]);
										CameraActivity.getCameraInstance()
												.setParameters(mCameraParams);
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class ExposureDialog extends DialogFragment {
		private SeekBar exposureValue;
		private TextView curExposureValue;
		private TextView minExposureValue;
		private TextView maxExposureValue;
		double max;
		double min;
		double step;
		double cur;
		double progressStep;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.exposure_layout, null);
			builder.setView(v);
			exposureValue = (SeekBar) v.findViewById(R.id.sbExposureValue);
			curExposureValue = (TextView) v
					.findViewById(R.id.tvCurrentExposure);
			minExposureValue = (TextView) v.findViewById(R.id.tvMinExpValue);
			maxExposureValue = (TextView) v.findViewById(R.id.tvMaxExpValue);
			builder.setTitle(getResources().getString(
					R.string.exposure_dialog_title));

			min = Double
					.valueOf(mCameraParams.get("min-exposure-compensation"));
			max = Double
					.valueOf(mCameraParams.get("max-exposure-compensation"));
			step = Double.valueOf(mCameraParams
					.get("exposure-compensation-step"));
			cur = Double.valueOf(mCameraParams.get("exposure-compensation"));

			minExposureValue.setText(String.valueOf(min));
			maxExposureValue.setText(String.valueOf(max));
			curExposureValue.setText(String.format("%.3f", cur));

			exposureValue.setProgress((int) ((cur - min) / (max - min) * 100));
			progressStep = (max - min) / step / 100;
			exposureValue
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							mCameraParams.set("exposure-compensation",
									String.format("%.3f", cur));
							CameraActivity.getCameraInstance().setParameters(
									mCameraParams);
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							cur = min + ((int) (progress * progressStep))
									* step;
							curExposureValue.setText(String.format("%.3f", cur));
						}
					});

			return builder.create();
		}
	}

	public static class WhiteBalanceDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			List<String> whiteBalanceMode = mCameraParams
					.getSupportedWhiteBalance();
			String[] array = whiteBalanceMode
					.toArray(new String[whiteBalanceMode.size()]);
			int indexOfActiveSetting = whiteBalanceMode.indexOf(mCameraParams
					.getWhiteBalance());
			builder.setTitle(
					getResources().getString(
							R.string.white_balance_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mCameraParams.setWhiteBalance(mCameraParams
											.getSupportedWhiteBalance().get(
													which));
									CameraActivity.getCameraInstance()
											.setParameters(mCameraParams);
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class FlashModesDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			List<String> flashModes = mCameraParams.getSupportedFlashModes();
			String[] array = flashModes.toArray(new String[flashModes.size()]);
			int indexOfActiveSetting = flashModes.indexOf(mCameraParams
					.getFlashMode());
			builder.setTitle(
					getResources().getString(R.string.flash_modes_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mCameraParams.setFlashMode(mCameraParams
											.getSupportedFlashModes()
											.get(which));
									CameraActivity.getCameraInstance()
											.setParameters(mCameraParams);
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class ISODialog extends DialogFragment {
		private static String[] array;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String supportedIsoValues = mCameraParams.get("iso-values");
			array = supportedIsoValues.split(",");
			String value = mCameraParams.get("iso");
			int indexOfActiveSetting = 0;
			for (; indexOfActiveSetting < array.length; indexOfActiveSetting++)
				if (array[indexOfActiveSetting].equals(value))
					break;
			builder.setTitle(
					getResources().getString(R.string.iso_dialog_title))
					.setSingleChoiceItems(array, indexOfActiveSetting,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (array.length > which) {
										mCameraParams.set("iso", array[which]);
										CameraActivity.getCameraInstance()
												.setParameters(mCameraParams);
									}
									dialog.dismiss();
								}
							});
			return builder.create();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.flashIV:
			(new FlashModesDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.isoIV:
			(new ISODialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.focusModeIV:
			(new ExposureDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.colorEffectsIV:
			(new ColorEffectsDialog())
					.show(getChildFragmentManager(), getTag());
			break;
		case R.id.videoSizeIV:
			(new VideoSizesDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.whiteBalanceIV:
			(new WhiteBalanceDialog())
					.show(getChildFragmentManager(), getTag());
			break;
		case R.id.fpsIV:
			(new LapseRateDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.durationIV:
			(new DurationDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.delayIV:
			(new DelayDialog()).show(getChildFragmentManager(), getTag());
			break;
		case R.id.lastVideoIV:
			startScan();
			break;
		default:
			break;
		}
	}

	private void startScan() {
		if (conn != null) {
			conn.disconnect();
		}
		String PATH = Environment.getExternalStorageDirectory().getPath()
				+ File.separator
				+ getResources().getString(R.string.lapse_folder_name);
		File dir = new File(PATH);
		dir.mkdirs();
		String[] lapseVids = dir.list();
		if (lapseVids.length > 0) {
			SCAN_PATH = PATH + File.separator + lapseVids[lapseVids.length - 1];
			conn = new MediaScannerConnection(this.getActivity()
					.getApplicationContext(), this);
			conn.connect();
		} else {
			Toast.makeText(getActivity(), "No videos", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onMediaScannerConnected() {
		conn.scanFile(SCAN_PATH, FILE_TYPE);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		try {
			System.out.println("URI " + uri);
			if (uri != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
			}
		} finally {
			conn.disconnect();
			conn = null;
		}
	}
}
