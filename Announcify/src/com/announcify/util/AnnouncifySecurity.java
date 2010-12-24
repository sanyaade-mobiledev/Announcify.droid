package com.announcify.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;

public class AnnouncifySecurity {
	public static final String EXTRA_LICENSED = "com.announcify.LICENSED";

	private static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDsdGybFkj9/26Fpu2mNASpAC8xQDRYocvVkxbpN6mF8k4a9L5ocnyUAY7sfKb0wjEc5e+vxL21kFKvvW0zEZX8a5wSXUfD5oiaXaiMPrp7cC1YbPPAelZvFEAzriA6pyk7PPKuqtAN2tcTiJED+kpiVAyEVU42lDUqE70xlRE6dQIDAQAB";

	// Generate your own 20 random bytes, and put them here.
	private static final byte[] SALT = new byte[] {-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};

	private boolean licensed;

	private final Activity activity;
	private final LicenseChecker checker;
	private final AnnouncifyLicenseCheckerCallback callback;

	public AnnouncifySecurity(final Activity activity) {
		this.activity = activity;

		// Try to use more data here. ANDROID_ID is a single point of attack.
		final String deviceId = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
		// Library calls this when it's done.
		callback = new AnnouncifyLicenseCheckerCallback();
		// Construct the LicenseChecker with a policy.
		checker = new LicenseChecker(activity, new ServerManagedPolicy(activity, new AESObfuscator(SALT, activity.getPackageName(), deviceId)), BASE64_PUBLIC_KEY);

		checker.checkAccess(callback);
	}

	public void quit() {
		checker.onDestroy();
	}

	private class AnnouncifyLicenseCheckerCallback implements LicenseCheckerCallback {
		public void allow() {
			licensed = true;

			if (activity.isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}

			activity.dismissDialog(1);
			activity.showDialog(2);
			// Should allow user access.

			final Editor editor = activity.getSharedPreferences(AnnouncifySettings.PREFERENCES_NAME, Context.MODE_WORLD_READABLE).edit();
			editor.putBoolean("test", false);
			editor.commit();
		}

		public void dontAllow() {
			if (activity.isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			activity.dismissDialog(1);

			activity.showDialog(0);

			final Editor editor = activity.getSharedPreferences(AnnouncifySettings.PREFERENCES_NAME, Context.MODE_WORLD_READABLE).edit();
			editor.putBoolean("test", true);
			editor.commit();
		}

		public void applicationError(final ApplicationErrorCode errorCode) {
			if (activity.isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// This is a polite way of saying the developer made a mistake
			// while setting up or calling the license checker library.
			// Please examine the error code and fix the error.

			activity.dismissDialog(1);
		}
	}

	public boolean isLicensed() {
		return licensed;
	}
}