
package com.fun.mediarenderer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.fun.mediarenderer.activity.MainActivity;

public class UpnpApp extends Application {

    private NotificationManager mNotificationManager;

    // pulsate every 1 second, indicating a relatively high degree of urgency
    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int NOTIFICATION_ARGB_COLOR = 0xff0088ff; // cyan

    @Override
    public void onCreate() {
        super.onCreate();

        // important! this sax driver is necessary for cling stack
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        initSingletons();

    }

    protected void initSingletons() {
        // Initialize the instance of MySingleton
        UpnpSingleton.initInstance(this.getApplicationContext());
    }

    public void customAppMethod() {
        // Custom application method
    }

    public void showNotification(String content) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this.getApplicationContext(), MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.media_renderer_title))
                .setContentText(content)
                .setTicker(
                        this.getResources().getQuantityString(
                                R.plurals.session_notification_ticker, 1, 2))
                .setDefaults(
                        Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE)
                .setLights(NOTIFICATION_ARGB_COLOR, NOTIFICATION_LED_ON_MS,
                        NOTIFICATION_LED_OFF_MS)
                .setSmallIcon(R.drawable.mediarenderer)
                .setContentIntent(contentIntent)
                .build();
        // .getNotification();

        CancelNotification();

        // Send the notification. We use a layout id because it is a unique
        // number. We use it later to cancel.
        mNotificationManager.notify(R.string.media_renderer_service_notification_id, notification);

    }

    public void CancelNotification() {
        mNotificationManager.cancel(R.string.media_renderer_service_notification_id);
    }

}
