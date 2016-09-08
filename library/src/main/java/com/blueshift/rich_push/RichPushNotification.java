package com.blueshift.rich_push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.blueshift.Blueshift;
import com.blueshift.model.Configuration;
import com.blueshift.util.NetworkUtils;

import java.io.File;

/**
 * Created by rahul on 18/2/15.
 */
public class RichPushNotification {
    private final static String LOG_TAG = RichPushNotification.class.getSimpleName();

    public static void handleMessage(final Context context, final Message message) {
        switch (message.getNotificationType()) {
            case AlertDialog:
                buildAndShowAlertDialog(context, message);
                break;

            case Notification:
                /**
                 * The rich push rendering require network access (ex: image download)
                 * Since network operations are not allowed in main thread, we
                 * are rendering the push message in a different thread.
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        buildAndShowNotification(context, message);
                    }
                }).run();

                break;
        }

        // Tracking the notification display.
        Blueshift.getInstance(context).trackNotificationView(message.getId(), true);
    }

    private static void buildAndShowAlertDialog(Context context, Message message) {
        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        notificationIntent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(notificationIntent);
    }

    private static void buildAndShowNotification(Context context, Message message) {
        int notificationID = 0;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);

        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null) {
            builder.setSmallIcon(configuration.getAppIcon());

            switch (message.getCategory()) {
                case Buy:
                    notificationID = NotificationCategory.Buy.getNotificationId();
                    if (configuration.getProductPage() != null) {
                        Intent intent = new Intent(RichPushConstants.ACTION_VIEW(context));
                        intent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
                        intent.putExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, notificationID);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                0, intent, PendingIntent.FLAG_ONE_SHOT);

                        builder.addAction(0, "View", pendingIntent);
                    }

                    if (configuration.getCartPage() != null) {
                        Intent intent = new Intent(RichPushConstants.ACTION_BUY(context));
                        intent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
                        intent.putExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, notificationID);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                0, intent, PendingIntent.FLAG_ONE_SHOT);

                        builder.addAction(0, "Buy", pendingIntent);
                    }

                    break;

                case ViewCart:
                    notificationID = NotificationCategory.ViewCart.getNotificationId();
                    if (configuration.getCartPage() != null) {
                        Intent intent = new Intent(RichPushConstants.ACTION_OPEN_CART(context));
                        intent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
                        intent.putExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, notificationID);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                0, intent, PendingIntent.FLAG_ONE_SHOT);

                        builder.addAction(0, "Open Cart", pendingIntent);
                    }

                    break;

                case Promotion:
                    notificationID = NotificationCategory.Promotion.getNotificationId();
                    if (configuration.getOfferDisplayPage() != null) {
                        Intent intent = new Intent(RichPushConstants.ACTION_OPEN_OFFER_PAGE(context));
                        intent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
                        intent.putExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, notificationID);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                0, intent, PendingIntent.FLAG_ONE_SHOT);

                        builder.setContentIntent(pendingIntent);
                    }

                    break;

                default:
                    /**
                     * Default action is to open app and send all details as extra inside intent
                     */
                    Intent intent = new Intent(RichPushConstants.ACTION_OPEN_APP(context));
                    intent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
                    intent.putExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, notificationID);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            0, intent, PendingIntent.FLAG_ONE_SHOT);

                    builder.setContentIntent(pendingIntent);
            }
        }

        builder.setContentTitle(message.getTitle());
        builder.setContentText(message.getBody());

        if (message.getImage_url() != null && !message.getImage_url().isEmpty()) {
            String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.jpg";
            if (NetworkUtils.downloadFile(message.getImage_url(), destinationPath)) {
                Bitmap bitmap = BitmapFactory.decodeFile(destinationPath);
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                File file = new File(destinationPath);
                Log.d(LOG_TAG, "Deleting cached image " + (file.delete() ? "success." : "failed."));
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, builder.build());
    }
}