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

import de.ub0r.android.lib.apis.NotificationBuilderWrapper.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Notification.Builder implementation for API3+.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public class NotificationBuilderWrapper3 extends NotificationBuilderWrapper {
	/**
	 * API3+ wrapper around {@link Notification.Builder}.
	 * 
	 * @author flx
	 */
	static class Builder3 implements Builder {

		/** The real {@link Notification}. */
		private Notification mNotification;

		/**
		 * Default constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public Builder3(final Context context) {
			this.mNotification = new Notification();
		}

		/**
		 * {@inheritDoc}
		 */
		public Notification getNotification() {
			return this.mNotification;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setAutoCancel(final boolean autoCancel) {
			// FIXME
			this.mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContent(final RemoteViews views) {
			this.mNotification.contentView = views;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setLatestEventInfo(Context context,
				CharSequence contentTitle, CharSequence contentText,
				PendingIntent contentIntent) {
			this.mNotification.setLatestEventInfo(context, contentTitle,
					contentText, contentIntent);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentInfo(final CharSequence info) {
			// NOT AVAILABLE FOR THIS API LEVEL
			// drop it
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentIntent(final PendingIntent intent) {
			this.mNotification.contentIntent = intent;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setDefaults(final int defaults) {
			this.mNotification.defaults = defaults;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setDeleteIntent(final PendingIntent intent) {
			this.mNotification.deleteIntent = intent;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setFullScreenIntent(final PendingIntent intent,
				final boolean highPriority) {
			// NOT AVAILABLE FOR THIS API LEVEL
			// drop it
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setLargeIcon(final Bitmap icon) {
			// NOT AVAILABLE FOR THIS API LEVEL
			// drop it
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setLights(final int argb, final int onMs, // .
				final int offMs) {
			this.mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
			this.mNotification.ledARGB = argb;
			this.mNotification.ledOnMS = onMs;
			this.mNotification.ledOffMS = offMs;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setNumber(final int number) {
			this.mNotification.number = number;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setOngoing(final boolean ongoing) {
			this.mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
			// FIXME
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setOnlyAlertOnce(final boolean onlyAlertOnce) {
			this.mNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
			// FIXME
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setProgress(final int max, final int progress,
				final boolean indeterminate) {
			// NOT AVAILABLE FOR THIS API LEVEL
			// drop it
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSmallIcon(final int icon, final int level) {
			this.mNotification.icon = icon;
			this.mNotification.iconLevel = level;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSmallIcon(final int icon) {
			this.mNotification.icon = icon;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSound(final Uri sound) {
			this.mNotification.sound = sound;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSound(final Uri sound, final int streamType) {
			this.mNotification.sound = sound;
			this.mNotification.audioStreamType = streamType;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setTicker(final CharSequence tickerText,
				final RemoteViews views) {
			this.mNotification.tickerText = tickerText;
			this.mNotification.tickerView = views;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setTicker(final CharSequence tickerText) {
			this.mNotification.tickerText = tickerText;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setVibrate(final long[] pattern) {
			this.mNotification.defaults |= Notification.DEFAULT_VIBRATE;
			this.mNotification.vibrate = pattern;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setWhen(final long when) {
			this.mNotification.when = when;
			return this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Builder getBuilder(final Context context) {
		return new Builder3(context);
	}

}
