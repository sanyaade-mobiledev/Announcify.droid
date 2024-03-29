package com.announcify.background.error;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.announcify.api.background.error.ExceptionHandler;

public class ExceptionService extends IntentService {

	public ExceptionService() {
		super("Announcify - ExceptionService");
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		Log.e("smn", "logged");

		// taken from:
		// http://www.androidsnippets.org/snippets/36/

		final HttpClient client = new DefaultHttpClient();
		final HttpPost post = new HttpPost(
				"http://www.announcify.com/bug/report.php");

		try {
			final List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(ExceptionHandler.PACKAGE, intent
					.getStringExtra(ExceptionHandler.PACKAGE)));
			params.add(new BasicNameValuePair(ExceptionHandler.ANDROID_VERSION,
					intent.getStringExtra(ExceptionHandler.ANDROID_VERSION)));
			params.add(new BasicNameValuePair(ExceptionHandler.VERSION_CODE,
					intent.getStringExtra(ExceptionHandler.VERSION_CODE)));
			params.add(new BasicNameValuePair(ExceptionHandler.VERSION_NAME,
					intent.getStringExtra(ExceptionHandler.VERSION_NAME)));
			params.add(new BasicNameValuePair(ExceptionHandler.MODEL, intent
					.getStringExtra(ExceptionHandler.MODEL)));

			final Throwable error = (Throwable) intent
					.getSerializableExtra(ExceptionHandler.STACKTRACE);

			Log.e("Announcify", "Caught exception", error);

			final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			final PrintStream stream = new PrintStream(byteOutput);
			error.printStackTrace(stream);
			stream.flush();
			stream.close();
			byteOutput.flush();

			params.add(new BasicNameValuePair(ExceptionHandler.STACKTRACE,
					byteOutput.toString("UTF-8")));

			byteOutput.close();

			post.setEntity(new UrlEncodedFormEntity(params));

			client.execute(post);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
