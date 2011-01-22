/*
 * Copyright (C) 2010 Felix Bechstein, The Android Open Source Project
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

import android.R.drawable;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

/**
 * Wrap around Bitmap Class.
 * 
 * @author flx
 */
public abstract class BitmapWrapper {
	/** Tag for output. */
	private static final String TAG = "bw";

	/**
	 * Static singleton instance of {@link BitmapWrapper} holding the
	 * SDK-specific implementation of the class.
	 */
	private static BitmapWrapper sInstance;

	/**
	 * Get instance.
	 * 
	 * @return {@link BitmapWrapper}
	 */
	public static final BitmapWrapper getInstance() {
		if (sInstance == null) {
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
			if (sdkVersion < Build.VERSION_CODES.DONUT) {
				sInstance = new BitmapWrapper3();
			} else {
				sInstance = new BitmapWrapper4();
			}
			Log.d(TAG, "getInstance(): " + sInstance.getClass().getName());
		}
		return sInstance;
	}

	/**
	 * Create {@link drawable} from a {@link Bitmap}, setting initial target
	 * density based on the display metrics of the resources.
	 * 
	 * @param res
	 *            {@link Resources}
	 * @param bitmap
	 *            {@link Bitmap}
	 * @return {@link BitmapDrawable}
	 */
	public abstract BitmapDrawable getBitmapDrawable(final Resources res,
			final Bitmap bitmap);
}
