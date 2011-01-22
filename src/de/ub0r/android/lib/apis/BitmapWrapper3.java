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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Wrap around Bitmap Class.
 * 
 * @version 3
 * @author flx
 */
public final class BitmapWrapper3 extends BitmapWrapper {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BitmapDrawable getBitmapDrawable(final Resources res,
			final Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}
}
