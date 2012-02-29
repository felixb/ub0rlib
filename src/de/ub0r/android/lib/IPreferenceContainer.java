/*
 * Copyright (C) 2010-2012 Felix Bechstein
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
package de.ub0r.android.lib;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;

/**
 * Switch between PreferenceActivity and PreferenceFragment.
 * 
 * @author flx
 */
public interface IPreferenceContainer {
	/**
	 * Get a {@link Preference}.
	 * 
	 * @param key
	 *            key
	 * @return {@link Preference}
	 */
	Preference findPreference(CharSequence key);

	/**
	 * Get {@link Context}.
	 * 
	 * @return {@link Context}
	 */
	Context getContext();

	/**
	 * Get {@link Activity}.
	 * 
	 * @return {@link Activity}
	 */
	Activity getActivity();
}
