package com.blueshift.rich_push;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.blueshift.Blueshift;
import com.blueshift.model.Configuration;
import com.blueshift.util.NotificationUtils;

/**
 * @author Rahul Raveendran V P
 *         Created on 25/2/15 @ 11:40 AM
 *         https://github.com/rahulrvp
 */
public class RichPushActionReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "RichPushActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive - " + intent.getAction());

        String action = intent.getAction();
        Message message = (Message) intent.getSerializableExtra(RichPushConstants.EXTRA_MESSAGE);
        if (action != null && message != null) {
            if (action.equals(RichPushConstants.ACTION_VIEW(context))) {
                displayProductPage(context, message);
            } else if (action.equals(RichPushConstants.ACTION_BUY(context))) {
                addToCart(context, message);
            } else if (action.equals(RichPushConstants.ACTION_OPEN_CART(context))) {
                displayCartPage(context, message);
            } else if (action.equals(RichPushConstants.ACTION_OPEN_OFFER_PAGE(context))) {
                displayOfferDisplayPage(context, message);
            } else if (action.equals(RichPushConstants.ACTION_OPEN_APP(context))) {
                openApp(context, message);
            }

            if (NotificationType.CustomNotification.equals(message.getNotificationType())) {
                // delete cached files based on category
                switch (message.getCategory()) {
                    case Carousel:
                    case AnimatedCarousel:
                        // remove cached images(if any) for this carousel notification
                        NotificationUtils.removeCachedCarouselImages(context, message);

                        break;

                    case GifNotification:
                        /**
                         * Stop if any Gif is being played
                         */
                        NotificationWorker.disableGifPlayback();

                        /**
                         * Remove any cached images and meta data used for GIF notification
                         */
                        NotificationUtils.deleteCachedGifFrameBitmaps(context, message);
                        NotificationUtils.deleteCachedGifFramesMetaData(context, message);

                        break;

                    default:
                        Log.d(LOG_TAG, "Unknown custom notification category.");
                }
            }

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getIntExtra(RichPushConstants.EXTRA_NOTIFICATION_ID, 0));

            Blueshift.getInstance(context).trackNotificationClick(message, true);

            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    public void displayProductPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getProductPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getProductPage());
            pageLauncherIntent.putExtra("sku", message.getSku());
            pageLauncherIntent.putExtra("mrp", message.getMrp());
            pageLauncherIntent.putExtra("price", message.getPrice());
            pageLauncherIntent.putExtra("data", message.getData());
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message);
        }
    }

    public void addToCart(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getCartPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getCartPage());
            pageLauncherIntent.putExtra("sku", message.getSku());
            pageLauncherIntent.putExtra("mrp", message.getMrp());
            pageLauncherIntent.putExtra("price", message.getPrice());
            pageLauncherIntent.putExtra("data", message.getData());
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message);
        }
    }

    public void displayCartPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getCartPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getCartPage());
            pageLauncherIntent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message);
        }
    }

    public void displayOfferDisplayPage(Context context, Message message) {
        Configuration configuration = Blueshift.getInstance(context).getConfiguration();
        if (configuration != null && configuration.getOfferDisplayPage() != null) {
            Intent pageLauncherIntent = new Intent(context, configuration.getOfferDisplayPage());
            pageLauncherIntent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
            pageLauncherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pageLauncherIntent);

            trackAppOpen(context, message);
        }
    }

    public void openApp(Context context, Message message) {
        if (context != null) {
            PackageManager packageManager = context.getPackageManager();
            Intent launcherIntent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            launcherIntent.putExtra(RichPushConstants.EXTRA_MESSAGE, message);
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launcherIntent);

            trackAppOpen(context, message);
        }
    }

    protected void trackAppOpen(Context context, Message message) {
        Blueshift.getInstance(context).trackNotificationPageOpen(message, true);
    }
}
