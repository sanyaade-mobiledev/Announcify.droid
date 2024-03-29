package com.announcify.background.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.announcify.api.background.util.AnnouncifySettings;
import com.announcify.background.receiver.CallReceiver;
import com.announcify.background.receiver.GravityReceiver;
import com.announcify.background.receiver.HeadsetReceiver;
import com.announcify.background.receiver.ScreenReceiver;
import com.announcify.background.receiver.gravity.GravityListener;

public class ConditionManager {

	private final Context context;
	private final ScreenReceiver screenReceiver;
	private final GravityListener gravityReceiver;
	private final CallReceiver callReceiver;
	private final HeadsetReceiver headsetReceiver;

	public ConditionManager(final Context context,
			final AnnouncifySettings settings) {
		this.context = context;

		if (settings.isGravityCondition()) {
			gravityReceiver = new GravityReceiver(context);
			gravityReceiver.setAccuracy(2f);
		} else {
			gravityReceiver = null;
		}

		if (settings.isScreenCondition()) {
			screenReceiver = new ScreenReceiver(context);
			final IntentFilter screenFilter = new IntentFilter();
			screenFilter.addAction(Intent.ACTION_SCREEN_ON);
			screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
			context.registerReceiver(screenReceiver, screenFilter);
		} else {
			screenReceiver = null;
		}

		if (settings.isDiscreetCondition()) {
			headsetReceiver = new HeadsetReceiver();
			Intent headsetReceiverIntent = context.registerReceiver(
					headsetReceiver, new IntentFilter(
							Intent.ACTION_HEADSET_PLUG));
			if (headsetReceiverIntent == null)
				headsetReceiver.onReceive(context, headsetReceiverIntent);
		} else {
			headsetReceiver = null;
		}

		callReceiver = new CallReceiver(context);
		((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
				.listen(callReceiver, PhoneStateListener.LISTEN_CALL_STATE);
	}

	public boolean isScreenOn() {
		return screenReceiver != null ? screenReceiver.isScreenOn() : false;
	}

	public void quit() {
		if (screenReceiver != null) {
			context.unregisterReceiver(screenReceiver);
		}
		if (callReceiver != null) {
			((TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE)).listen(
					callReceiver,
					android.telephony.PhoneStateListener.LISTEN_NONE);
		}
		if (gravityReceiver != null) {
			gravityReceiver.stopAccelerometer();
		}
		if (headsetReceiver != null) {
			context.unregisterReceiver(headsetReceiver);
		}
	}

	public void setOnCall(final boolean onCall) {
		callReceiver.setOnCall(onCall);
	}
}
