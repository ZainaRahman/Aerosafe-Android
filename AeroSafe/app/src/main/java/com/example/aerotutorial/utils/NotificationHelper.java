package com.example.aerotutorial.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.aerotutorial.R;
import com.example.aerotutorial.UserDashboardActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "aerosafe_channel";
    private static final String CHANNEL_NAME = "AeroSafe Notifications";
    private static final String CHANNEL_DESC = "Notifications for air quality alerts";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendAQIAlert(String title, String message, int aqi) {
        Intent intent = new Intent(context, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Set color based on AQI
        int color = AQICalculator.getAQIColor(aqi);
        builder.setColor(color);

        notificationManager.notify(getNotificationId(), builder.build());
    }

    public void sendGovernmentAlert(String title, String message) {
        Intent intent = new Intent(context, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(getNotificationId(), builder.build());
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    private int getNotificationId() {
        return (int) System.currentTimeMillis();
    }

    public static void checkAndNotify(Context context, int aqi) {
        // Notify if AQI is unhealthy or worse
        if (aqi >= 101) {
            NotificationHelper helper = new NotificationHelper(context);
            String category = AQICalculator.getAQICategory(aqi);
            String alert = AQICalculator.getHealthAlert(aqi);
            helper.sendAQIAlert(
                "⚠️ High AQI Alert: " + category,
                "Current AQI is " + aqi + ". " + alert,
                aqi
            );
        }
    }
}

