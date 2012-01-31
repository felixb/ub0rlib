/*
 * Copyright (C) 2010-2012 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of ub0rlib.
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; If
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.lib.apis;

import android.app.Notification;
import android.content.Context;
import de.ub0r.android.lib.apis.NotificationBuilderWrapper11.Builder11;

/**
 * Notification.Builder implementation for API14+.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public class NotificationBuilderWrapper14 extends NotificationBuilderWrapper {
	/**
	 * API14+ wrapper around {@link Notification.Builder}.
	 * 
	 * @author flx
	 */
	static class Builder14 extends Builder11 implements Builder {

		/**
		 * Default constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public Builder14(final Context context) {
			super(context);
		}

		/**
		 * {@inheritDoc}
		 */
		public Builder setProgress(final int max, final int progress, final boolean indeterminate) {
			this.getBuilder().setProgress(max, progress, indeterminate);
			return this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Builder getBuilder(final Context context) {
		return new Builder14(context);
	}

}
