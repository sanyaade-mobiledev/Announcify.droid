package com.announcify.handler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.announcify.queue.LittleQueue;
import com.announcify.queue.Queue;
import com.announcify.service.AnnouncifyService;
import com.announcify.service.ManagerService;
import com.announcify.tts.Speaker;
import com.announcify.util.Money;

public class AnnouncificationHandler extends Handler {
	public static final int WHAT_START = 40;
	public static final int WHAT_CONTINUE = 44;

	public static final int WHAT_PAUSE = 43;
	public static final int WHAT_SHUTDOWN = 45;

	public static final int WHAT_PUT_QUEUE = 41;
	public static final int WHAT_NEXT_ITEM = 42;

	public static final int WHAT_CHANGE_LOCALE = 46;
	public static final int WHAT_REVERT_LOCALE = 47;

	private final Context context;
	private final Queue queue;
	private final Speaker speaker;

	public AnnouncificationHandler(final Context context, final Looper looper, final Speaker speaker) {
		super(looper);
		this.context = context;
		this.speaker = speaker;

		queue = new Queue(context, this);
	}

	@Override
	public void handleMessage(final Message msg) {
		Log.e("smn", "what: " + msg.what);

		switch (msg.what) {
			case WHAT_PUT_QUEUE:
				final LittleQueue little = msg.getData().getParcelable(AnnouncifyService.EXTRA_QUEUE);

				// TODO: prohibit in-call speech here!
				switch (msg.getData().getInt(AnnouncifyService.EXTRA_PRIORITY, -1)) {
					case 0:
						queue.putFirst(little);
						break;
					case 1:
						queue.putLast(little);
						break;

					// third party plugins
					default:
						queue.putLast(little);
						break;
				}

				break;

			case WHAT_NEXT_ITEM:
				if (msg.obj instanceof String) {
					speaker.speak((String) msg.obj);
				} else if (msg.obj instanceof Integer) {
					int i = (Integer) msg.obj;
					if (i < 1000) {
						i *= 1000;
					}

					postDelayed(new Runnable() {

						public void run() {
							speaker.speak("");
						}
					}, (Integer) msg.obj);
				}

				break;

			case WHAT_PAUSE:
				if (!Money.isPaid(context)) {
					context.stopService(new Intent(context, ManagerService.class));
				} else {
					quit();
				}

				break;

			case WHAT_CONTINUE:
				queue.grant();
				break;

			case WHAT_SHUTDOWN:
				quit();
				// TODO: save queue
				break;

			case WHAT_CHANGE_LOCALE:
				// TODO:
				// speaker.applyLanguage((Speech) msg.obj);
				break;

			case WHAT_REVERT_LOCALE:
				speaker.revertLanguage();
				break;

			case WHAT_START:
				if (speaker.setOnUtteranceCompletedListener(queue) != TextToSpeech.SUCCESS) {
					// TODO: send log to server
				}

				queue.start();
				break;

			default:
				break;
		}
	}

	public void quit() {
		queue.quit();
		getLooper().getThread().interrupt();
		getLooper().quit();
	}
}