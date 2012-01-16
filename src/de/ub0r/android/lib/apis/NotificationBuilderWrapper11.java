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
import android.widget.RemoteViews;

/**
 * Notification.Builder implementation for API11+.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public class NotificationBuilderWrapper11 extends NotificationBuilderWrapper {
	/**
	 * API11+ wrapper around {@link Notification.Builder}.
	 * 
	 * @author flx
	 */
	static class Builder11 implements Builder {

		/** The real {@link Notification.Builder}. */
		private Notification.Builder mBuilder;

		/**
		 * Default constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public Builder11(final Context context) {
			this.mBuilder = new Notification.Builder(context);
		}

		/**
		 * @return the real {@link Notification.Builder}
		 */
		protected Notification.Builder getBuilder() {
			return this.mBuilder;
		}

		/**
		 * {@inheritDoc}
		 */
		public Notification getNotification() {
			return this.mBuilder.getNotification();
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setAutoCancel(final boolean autoCancel) {
			this.mBuilder.setAutoCancel(autoCancel);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContent(final RemoteViews views) {
			this.mBuilder.setContent(views);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentInfo(final CharSequence info) {
			this.mBuilder.setContentInfo(info);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentIntent(final PendingIntent intent) {
			this.mBuilder.setContentIntent(intent);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentText(final CharSequence text) {
			this.mBuilder.setContentText(text);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setContentTitle(final CharSequence title) {
			this.mBuilder.setContentTitle(title);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setDefaults(final int defaults) {
			this.mBuilder.setDefaults(defaults);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setDeleteIntent(final PendingIntent intent) {
			this.mBuilder.setDeleteIntent(intent);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setFullScreenIntent(final PendingIntent intent,
				final boolean highPriority) {
			this.mBuilder.setFullScreenIntent(intent, highPriority);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setLargeIcon(final Bitmap icon) {
			this.mBuilder.setLargeIcon(icon);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setLights(final int argb, final int onMs, // .
				final int offMs) {
			this.mBuilder.setLights(argb, onMs, offMs);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setNumber(final int number) {
			this.mBuilder.setNumber(number);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setOngoing(final boolean ongoing) {
			this.mBuilder.setOngoing(ongoing);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setOnlyAlertOnce(final boolean onlyAlertOnce) {
			this.mBuilder.setOnlyAlertOnce(onlyAlertOnce);
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
			this.mBuilder.setSmallIcon(icon, level);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSmallIcon(final int icon) {
			this.mBuilder.setSmallIcon(icon);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSound(final Uri sound) {
			this.mBuilder.setSound(sound);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setSound(final Uri sound, final int streamType) {
			this.mBuilder.setSound(sound, streamType);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setTicker(final CharSequence tickerText,
				final RemoteViews views) {
			this.mBuilder.setTicker(tickerText, views);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setTicker(final CharSequence tickerText) {
			this.mBuilder.setTicker(tickerText);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setVibrate(final long[] pattern) {
			this.mBuilder.setVibrate(pattern);
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setWhen(final long when) {
			this.mBuilder.setWhen(when);
			return this;
		}

		@Override
		public Builder setLatestEventInfo(final Context context,
				final CharSequence contentTitle,
				final CharSequence contentText,
				final PendingIntent contentIntent) {
			this.mBuilder.setContentTitle(contentTitle);
			this.mBuilder.setContentText(contentText);
			this.mBuilder.setContentIntent(contentIntent);
			return this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Builder getBuilder(final Context context) {
		return new Builder11(context);
	}

}
