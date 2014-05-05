package org.x2ools.t9apps;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class T9AppsApplication extends Application {

	private static final String TAG = "T9AppsApplication";
	public static final int NOTIFICATION_ID = 100088;

	@Override
	public void onCreate() {
		// super.onCreate();
		Log.d(TAG, "onCreate");
		Intent viewService = new Intent(this, ViewManagerService.class);
		startService(viewService);
		
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent intent = PendingIntent.getActivity(this, 0, 
				new Intent(this, ConfigActivity.class),  0);
		Notification notification = new Notification.Builder(this)
		.setAutoCancel(false)
		.setOngoing(false)
		.setContentTitle(getResources().getString(R.string.notification_title))
		.setContentText(getResources().getString(R.string.notification_text))
		.setContentIntent(intent)
		 .setSmallIcon(R.drawable.ic_launcher)
		.build();
		nm.notify(NOTIFICATION_ID, notification);
		Log.d(TAG, "notify " + NOTIFICATION_ID);

	}

}
