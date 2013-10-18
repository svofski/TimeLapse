package com.lebedevsd.timelaps.vidgets;

import java.util.List;

import com.lebedevsd.timelaps.R;
import com.lebedevsd.timelaps.TimeLapsApplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
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

public class OptionsFragment extends Fragment implements OnClickListener {

	private static Camera.Parameters mCameraParams;

	private FrameLayout mOptionsHolder;
	private LinearLayout mOptionsLayout;

	private ImageView mFlashImageView;
	private ImageView mVideoOptionsImageView;
	private ImageView mWhiteBalanceImageView;
	private ImageView mColorEffectImageView;
	private ImageView mFocusModeImageView;
	private ImageView mISOImageView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mOptionsHolder = (FrameLayout) inflater.inflate(
				R.layout.options_layout, container, false);
		return mOptionsHolder;
	}

	@Override
	public void onResume() {
		super.onResume();
		initInnerComponents();
	}

	@SuppressLint("NewApi")
	private void initInnerComponents() {
		// init UI elements
		mOptionsLayout = (LinearLayout) mOptionsHolder
				.findViewById(R.id.options);
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

		mCameraParams = TimeLapsApplication.getCameraInstance().getParameters();

		// set clickListener to UI elements
		mFlashImageView.setOnClickListener(this);
		mVideoOptionsImageView.setOnClickListener(this);
		mWhiteBalanceImageView.setOnClickListener(this);
		mColorEffectImageView.setOnClickListener(this);
		mFocusModeImageView.setOnClickListener(this);
		mISOImageView.setOnClickListener(this);
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mCameraParams.setColorEffect(mCameraParams
									.getSupportedColorEffects().get(which));
							TimeLapsApplication.getCameraInstance()
									.stopPreview();
							TimeLapsApplication.getCameraInstance()
									.setParameters(mCameraParams);
							TimeLapsApplication.getCameraInstance()
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mCameraParams.setFocusMode(mCameraParams
									.getSupportedFocusModes().get(which));
							TimeLapsApplication.getCameraInstance()
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
			String supportedIsoValues = mCameraParams.get("video-size-values");
			array = supportedIsoValues.split(",");
			String value = mCameraParams.get("video-size");
			int indexOfActiveSetting = 0;
			for (; indexOfActiveSetting < array.length; indexOfActiveSetting++)
				if (array[indexOfActiveSetting].equals(value))
					break;
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (array.length > which) {
								mCameraParams.set("video-size", array[which]);
								TimeLapsApplication.getCameraInstance()
										.setParameters(mCameraParams);
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (array.length > which) {
								mCameraParams.set("antibanding", array[which]);
								TimeLapsApplication.getCameraInstance()
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
			builder.setTitle("Some Title");

			min = Double
					.valueOf(mCameraParams.get("min-exposure-compensation"));
			max = Double
					.valueOf(mCameraParams.get("max-exposure-compensation"));
			step = Double.valueOf(mCameraParams
					.get("exposure-compensation-step"));
			cur = Double.valueOf(mCameraParams.get("exposure-compensation"));

			minExposureValue.setText(String.valueOf(min));
			maxExposureValue.setText(String.valueOf(max));
			curExposureValue.setText(String.valueOf(cur));

			exposureValue.setProgress((int) ((cur - min) / (max - min) * 100));
			progressStep = (max - min) / step / 100;
			exposureValue
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							mCameraParams.set("exposure-compensation",
									String.valueOf(cur));
							TimeLapsApplication.getCameraInstance()
									.setParameters(mCameraParams);
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
							curExposureValue.setText(String.valueOf(cur));
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mCameraParams.setWhiteBalance(mCameraParams
									.getSupportedWhiteBalance().get(which));
							TimeLapsApplication.getCameraInstance()
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mCameraParams.setFlashMode(mCameraParams
									.getSupportedFlashModes().get(which));
							TimeLapsApplication.getCameraInstance()
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
			builder.setTitle("Some Title").setSingleChoiceItems(array,
					indexOfActiveSetting,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (array.length > which) {
								mCameraParams.set("iso", array[which]);
								TimeLapsApplication.getCameraInstance()
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
		default:
			break;
		}

	}
}
