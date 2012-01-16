/*
 * Copyright (C) 2010-2012 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of ub0rlib.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.lib.apis;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import de.ub0r.android.lib.Utils;

/**
 * Wrapper around Notification.Builder.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public abstract class NotificationBuilderWrapper {

	/**
	 * Wrapper around Notification.Builder.
	 */
	public interface Builder {
		/**
		 * Combine all of the options that have been set and return a new
		 * Notification object.
		 */
		Notification getNotification();

		/**
		 * Setting this flag will make it so the notification is automatically
		 * canceled when the user clicks it in the panel.
		 * 
		 * @param autoCancel
		 * @return
		 */
		Builder setAutoCancel(boolean autoCancel);

		/**
		 * Supply a custom RemoteViews to use instead of the standard one.
		 * 
		 * @param views
		 * @return
		 */
		Builder setContent(RemoteViews views);

		Builder setLatestEventInfo(Context context, CharSequence contentTitle,
				CharSequence contentText, PendingIntent contentIntent);

		/**
		 * Set the large text at the right-hand side of the notification.
		 * 
		 * @param info
		 * @return
		 */
		Builder setContentInfo(CharSequence info);

		/**
		 * Supply a PendingIntent to send when the notification is clicked.
		 * 
		 * @param intent
		 * @return
		 */
		Builder setContentIntent(PendingIntent intent);

		/**
		 * Set the default notification options that will be used.
		 * 
		 * @param defaults
		 * @return
		 */
		Builder setDefaults(int defaults);

		/**
		 * Supply a PendingIntent to send when the notification is cleared by
		 * the user directly from the notification panel.
		 * 
		 * @param intent
		 * @return
		 */
		Builder setDeleteIntent(PendingIntent intent);

		/**
		 * An intent to launch instead of posting the notification to the status
		 * bar.
		 * 
		 * @param intent
		 * @param highPriority
		 * @return
		 */
		Builder setFullScreenIntent(PendingIntent intent, boolean highPriority);

		/**
		 * Set the large icon that is shown in the ticker and notification.
		 * 
		 * @param icon
		 * @return
		 */
		Builder setLargeIcon(Bitmap icon);

		/**
		 * Set the argb value that you would like the LED on the device to blnk,
		 * as well as the rate.
		 * 
		 * @param argb
		 * @param onMs
		 * @param offMs
		 * @return
		 */
		Builder setLights(int argb, int onMs, int offMs);

		/**
		 * Set the large number at the right-hand side of the notification.
		 * 
		 * @param number
		 * @return
		 */
		Builder setNumber(int number);

		/**
		 * Set whether this is an ongoing notification.
		 * 
		 * @param ongoing
		 * @return
		 */
		Builder setOngoing(boolean ongoing);

		/**
		 * Set this flag if you would only like the sound, vibrate and ticker to
		 * be played if the notification is not already showing.
		 * 
		 * @param onlyAlertOnce
		 * @return
		 */
		Builder setOnlyAlertOnce(boolean onlyAlertOnce);

		/**
		 * Set the progress this notification represents, which may be
		 * represented as a ProgressBar.
		 * 
		 * @param max
		 * @param progress
		 * @param indeterminate
		 * @return
		 */
		Builder setProgress(int max, int progress, boolean indeterminate);

		/**
		 * A variant of setSmallIcon(int) that takes an additional level
		 * parameter for when the icon is a LevelListDrawable.
		 * 
		 * @param icon
		 * @param level
		 * @return
		 */
		Builder setSmallIcon(int icon, int level);

		/**
		 * 
		 Set the small icon to use in the notification layouts.
		 * 
		 * @param icon
		 * @return
		 */
		Builder setSmallIcon(int icon);

		/**
		 * Set the sound to play.
		 * 
		 * @param sound
		 * @return
		 */
		Builder setSound(Uri sound);

		/**
		 * Set the sound to play.
		 * 
		 * @param sound
		 * @param streamType
		 * @return
		 */
		Builder setSound(Uri sound, int streamType);

		/**
		 * Set the text that is displayed in the status bar when the
		 * notification first arrives, and also a RemoteViews object that may be
		 * displayed instead on some devices.
		 * 
		 * @param tickerText
		 * @param views
		 * @return
		 */
		Builder setTicker(CharSequence tickerText, RemoteViews views);

		/**
		 * Set the text that is displayed in the status bar when the
		 * notification first arrives.
		 * 
		 * @param tickerText
		 * @return
		 */
		Builder setTicker(CharSequence tickerText);

		/**
		 * Set the vibration pattern to use.
		 * 
		 * @param pattern
		 * @return
		 */
		Builder setVibrate(long[] pattern);

		/**
		 * Set the time that the event occurred.
		 * 
		 * @param when
		 * @return
		 */
		Builder setWhen(long when);
	}

	/** Tag for output. */
	private static final String TAG = "nw";

	/**
	 * Static singleton instance of {@link Notification.Builder} holding the
	 * SDK-specific implementation of the class.
	 */
	private static NotificationBuilderWrapper sInstance;

	/**
	 * Get instance.
	 * 
	 * @return {@link TelephonyWrapper}
	 */
	private static NotificationBuilderWrapper getInstance() {
		if (sInstance == null) {
			if (Utils.isApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
				sInstance = new NotificationBuilderWrapper14();
			} else if (Utils.isApi(Build.VERSION_CODES.HONEYCOMB)) {
				sInstance = new NotificationBuilderWrapper11();
			} else if (Utils.isApi(Build.VERSION_CODES.GINGERBREAD)) {
				sInstance = new NotificationBuilderWrapper9();
			} else {
				sInstance = new NotificationBuilderWrapper3();
			}
			Log.d(TAG, "getInstance(): " + sInstance.getClass().getName());
		}
		return sInstance;
	}

	/**
	 * @param context
	 *            {@link Context}
	 * @return {@link Builder}
	 */
	public abstract Builder getBuilder(final Context context);

	/**
	 * @param context
	 *            {@link Context}
	 * @return {@link Builder}
	 */
	public static final Builder getNotificationBuilder(final Context context) {
		return getInstance().getBuilder(context);
	}
}
