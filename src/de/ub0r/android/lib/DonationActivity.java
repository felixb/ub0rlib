/*
 * Copyright (C) 2010-2012 Felix Bechstein
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
package de.ub0r.android.lib;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

/**
 * Shows {@link Activity} to let people donate.
 * 
 * @author flx
 */
public class DonationActivity extends Activity {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		if (Utils.isApi(Build.VERSION_CODES.HONEYCOMB)) {
			this.setTheme(android.R.style.Theme_Holo_Light);
		} else {
			this.setTheme(android.R.style.Theme_Light);
		}
		super.onCreate(savedInstanceState);
		DonationHelper.onCreate(this);
	}
}
