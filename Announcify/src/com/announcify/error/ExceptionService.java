package com.announcify.error;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ExceptionService extends IntentService {
	public ExceptionService() {
		super("ExceptionService");
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		final List<NameValuePair> formparams = new ArrayList<NameValuePair>();

		try {
			new Thread() {
				@Override
				public void run() {
					try {
						final Exception exception = (Exception) intent.getSerializableExtra(ExceptionHandler.STACKTRACE);
						Log.e("Announcify", "Caught exception", exception);

						final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
						final PrintStream stream = new PrintStream(byteOutput);
						exception.printStackTrace(stream);
						stream.flush();
						stream.close();
						byteOutput.flush();
						final String stackTrace = byteOutput.toString("UTF-8");
						byteOutput.close();

						formparams.add(new BasicNameValuePair(ExceptionHandler.PACKAGE, intent.getStringExtra(ExceptionHandler.PACKAGE)));
						formparams.add(new BasicNameValuePair(ExceptionHandler.STACKTRACE, stackTrace));
						formparams.add(new BasicNameValuePair(ExceptionHandler.MODEL, Build.MODEL));
						formparams.add(new BasicNameValuePair(ExceptionHandler.VERSION_CODE, intent.getStringExtra(ExceptionHandler.VERSION_CODE)));
						formparams.add(new BasicNameValuePair(ExceptionHandler.ANDROID_VERSION, Build.VERSION.SDK));
						formparams.add(new BasicNameValuePair(ExceptionHandler.INFORMATION, intent.getStringExtra(ExceptionHandler.INFORMATION)));
						formparams.add(new BasicNameValuePair(ExceptionHandler.VERSION_NAME, intent.getStringExtra(ExceptionHandler.VERSION_NAME)));

						final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

						final HttpPost request = new HttpPost("http://announcify.appspot.com/exception/put");
						request.setEntity(entity);

						final HttpClient client = new DefaultHttpClient();

						System.out.println(client.execute(request, new BasicResponseHandler()));
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}