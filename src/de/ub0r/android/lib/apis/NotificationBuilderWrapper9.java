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
import de.ub0r.android.lib.apis.NotificationBuilderWrapper3.Builder3;

/**
 * Notification.Builder implementation for API9+.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public class NotificationBuilderWrapper9 extends NotificationBuilderWrapper {
	/**
	 * API3+ wrapper around {@link Notification.Builder}.
	 * 
	 * @author flx
	 */
	static class Builder9 extends Builder3 implements Builder {
		/**
		 * Default constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public Builder9(final Context context) {
			super(context);
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setFullScreenIntent(final PendingIntent intent,
				final boolean highPriority) {
			this.getNotification().fullScreenIntent = intent;
			return this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Builder getBuilder(final Context context) {
		return new Builder9(context);
	}
}