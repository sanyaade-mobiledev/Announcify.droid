package com.announcify.handler;

import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.announcify.error.ExceptionParser;
import com.announcify.queue.LittleQueue;
import com.announcify.queue.Queue;
import com.announcify.service.AnnouncifyService;
import com.announcify.service.ManagerService;
import com.announcify.sql.model.PluginModel;
import com.announcify.tts.Speaker;

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
	private final PluginModel model;

	public AnnouncificationHandler(final Context context, final Looper looper, final Speaker speaker) {
		super(looper);
		this.context = context;
		this.speaker = speaker;

		queue = new Queue(context, this);
		model = new PluginModel(context);
	}

	@Override
	public void handleMessage(final Message msg) {
		Log.e("smn", "what: " + msg.what);

		switch (msg.what) {
			case WHAT_PUT_QUEUE:
				LittleQueue little = msg.getData().getParcelable(AnnouncifyService.EXTRA_QUEUE);

				if (!model.getActive(little.getPluginName())) {
					little = new LittleQueue("Empty", new LinkedList<Object>(), context);
					msg.getData().putInt(AnnouncifyService.EXTRA_PRIORITY, 9);
				}

				switch (msg.getData().getInt(AnnouncifyService.EXTRA_PRIORITY, -1)) {
					// call only!
					case 0:
						queue.putFirst(little);
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
					postDelayed(new Runnable() {

						public void run() {
							speaker.speak("");
						}
					}, (Integer) msg.obj);
				}

				break;

			case WHAT_PAUSE:
				quit(null);
				break;

			case WHAT_CONTINUE:
				queue.grant();
				break;

			case WHAT_SHUTDOWN:
				quit(null);
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
					quit(new RuntimeException("Couldn't set UtteranceListener for TextToSpeech"));
				}

				queue.start();
				break;

			default:
				break;
		}
	}

	/**
	 * 
	 * @param exception
	 *            Exception to send to Server.
	 */
	public void quit(final Exception exception) {
		queue.quit();

		if (exception != null) {
			new ExceptionParser(context, exception).sendException();
		}

		context.stopService(new Intent(context, ManagerService.class));
	}
}