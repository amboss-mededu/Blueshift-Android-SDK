package com.blueshift.rich_push;

import android.util.Log;

/**
 * Created by rahul on 6/9/16.
 */
public enum NotificationType {
    AlertDialog,
    Notification,
    Unknown;

    private static final String LOG_TAG = "NotificationType";

    public static NotificationType fromString(String notificationType) {
        if (notificationType != null) {
            switch (notificationType) {
                case "alert":
                    return AlertDialog;

                case "notification":
                    return Notification;

                default:
                    Log.w(LOG_TAG, "Unknown 'notification_type' found: " + notificationType);

                    return Unknown;
            }
        } else {
            Log.w(LOG_TAG, "'notification_type' is not available inside 'message'.");

            return Unknown;
        }
    }
}
