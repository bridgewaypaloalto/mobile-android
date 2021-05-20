package com.bcsv.mobile.Service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.bcsv.mobile.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    NotificationManagerCompat MyNotificationManager;
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getChannelId());
    }

    public void showNotification(String title, String message, String channelID){
        NotificationCompat.Builder builder;
        int notificationPriorityId = 0;

        try{
            if(channelID.equals("channel1")){
                builder = new NotificationCompat.Builder(this, channelID)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentText(message);
            }else{
                builder = new NotificationCompat.Builder(this, channelID)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentText(message);
                notificationPriorityId = 1;
            }

            MyNotificationManager = NotificationManagerCompat.from(this);
            MyNotificationManager.notify(notificationPriorityId, builder.build());
        } catch (NullPointerException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }
}
