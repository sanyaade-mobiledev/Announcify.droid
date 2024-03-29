package com.announcify.background.service;

import org.mailboxer.saymyname.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.announcify.api.background.error.ExceptionHandler;
import com.announcify.api.background.service.PluginService;
import com.announcify.api.background.util.AnnouncifySettings;
import com.announcify.background.handler.AnnouncificationHandler;
import com.announcify.background.receiver.ControlReceiver;
import com.announcify.background.tts.Speaker;
import com.announcify.ui.control.RemoteControlDialog;

public class ManagerService extends Service {

	private NotificationManager notificationManager;
	private ConditionManager conditionManager;
	private ControlReceiver controlReceiver;
	private AnnouncificationHandler handler;
	private HandlerThread thread;
	private Speaker speaker;

	@Override
	public IBinder onBind(final Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		super.onCreate();

		final AnnouncifySettings settings = new AnnouncifySettings(this);

		thread = new HandlerThread("Announcifications");
		thread.start();

		speaker = new Speaker(ManagerService.this, new OnInitListener() {

			public void onInit(final int status) {
				handler.sendEmptyMessage(AnnouncificationHandler.WHAT_START);
			}
		});

		handler = new AnnouncificationHandler(ManagerService.this,
				thread.getLooper(), speaker);
		handler.post(new Runnable() {

			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(
						ManagerService.this));
			}
		});

		controlReceiver = new ControlReceiver(handler);
		final IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(RemoteControlDialog.ACTION_CONTINUE);
		controlFilter.addAction(RemoteControlDialog.ACTION_PAUSE);
		controlFilter.addAction(RemoteControlDialog.ACTION_SKIP);
		registerReceiver(controlReceiver, controlFilter);

		if (settings.isShowNotification()) {
			final PendingIntent pendingIntent = PendingIntent.getActivity(this,
					1993, new Intent(this, RemoteControlDialog.class), 0);
			final Notification notification = new Notification(
					R.drawable.notification_icon, null, 0);
			notification.setLatestEventInfo(this, "Important Announcification",
					"Press here to stop it.", pendingIntent);
			startForeground(17, notification);
		}

		conditionManager = new ConditionManager(this, settings);

	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		if ((intent == null) || (intent.getExtras() == null)) {
			return START_NOT_STICKY;
		}

		if ((intent.getExtras().getInt(PluginService.EXTRA_PRIORITY, 9) > 3)
				&& conditionManager.isScreenOn()) {
			// it's not a screen-on plugin. screen is on. stop.
			return START_NOT_STICKY;
		}

		if (intent.getExtras().getInt(PluginService.EXTRA_PRIORITY, 9) <= 3) {
			// it's a screen-on plugin. disable screen-on condition temporarily.
			conditionManager.setOnCall(true);
		}

		final Message msg = handler
				.obtainMessage(AnnouncificationHandler.WHAT_PUT_QUEUE);
		msg.setData(intent.getExtras());
		handler.sendMessage(msg);

		// we don't want android to restart the service after killing us
		// (in order to prevent annoying duplicate announcements).
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.e("Announcify", "shutdown");

		if (handler != null) {
			final Message msg = handler
					.obtainMessage(AnnouncificationHandler.WHAT_SHUTDOWN);
			handler.sendMessage(msg);
		}

		if (controlReceiver != null) {
			unregisterReceiver(controlReceiver);
		}

		conditionManager.quit();

		if (speaker != null) {
			speaker.shutdown();
		}

		if ((thread != null) && thread.isAlive()) {
			thread.interrupt();
			thread.getLooper().quit();
		}

		if (notificationManager != null) {
			notificationManager.cancel(17);
		}

		super.onDestroy();
	}
}
