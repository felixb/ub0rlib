/*
 * Copyright (C) 2009-2012 Felix Bechstein
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

/**
 * Display change log.
 * 
 * @author flx
 */
public final class ChangelogHelper {
	/** Tag for output. */
	public static final String TAG = "cl";

	/** Prefs: name for last version run. */
	private static final String PREFS_LAST_RUN = "lastrun";

	/**
	 * Default Constructor.
	 */
	private ChangelogHelper() {
		// nothing to do
	}

	/**
	 * Check if a new version of this app is running.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return true on first run after update.
	 */
	public static boolean isNewVersion(final Context context) {
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		final String v0 = p.getString(PREFS_LAST_RUN, "");
		String v1 = null;
		try {
			v1 = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "package not found: " + context.getPackageName(), e);
		}
		if (v0.equals(v1)) {
			return false;
		}
		return true;
	}

	/**
	 * Show change log dialog.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param title
	 *            title of dialog
	 * @param appname
	 *            name of app
	 * @param resChanges
	 *            string-array resource for changes
	 * @param resNotes
	 *            string-array resource for notes
	 */
	public static void showChangelog(final Context context, final String title,
			final String appname, final int resChanges, final int resNotes) {
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		final String v0 = p.getString(PREFS_LAST_RUN, "");
		String v1 = null;
		try {
			v1 = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "package not found: " + context.getPackageName(), e);
		}
		p.edit().putString(PREFS_LAST_RUN, v1).commit();
		if (v0.length() == 0) {
			Log.d(TAG, "first boot, skip changelog");
			return;
		}
		if (v0.equals(v1)) {
			Log.d(TAG, "no changes");
			return;
		}

		String[] changes = context.getResources().getStringArray(resChanges);
		String[] notes = resNotes > 0 ? context.getResources().getStringArray(resNotes) : null;

		final SpannableStringBuilder sb = new SpannableStringBuilder();
		for (String s : notes) {
			SpannableString ss = new SpannableString(s + "\n");
			int j = s.indexOf(":");
			if (j > 0) {
				if (!TextUtils.isEmpty(s)) {
					ss.setSpan(new StyleSpan(Typeface.BOLD), 0, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			sb.append(ss);
			sb.append("\n");
		}
		if (notes != null && notes.length > 0) {
			sb.append("\n");
		}
		for (String s : changes) {
			s = appname + " " + s.replaceFirst(": ", ":\n* ").replaceAll(", ", "\n* ") + "\n";
			SpannableString ss = new SpannableString(s);
			int j = s.indexOf(":");
			if (j > 0) {
				ss.setSpan(new StyleSpan(Typeface.BOLD), 0, j, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			sb.append(ss);
			sb.append("\n");
		}
		sb.setSpan(new RelativeSizeSpan(0.75f), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		changes = null;
		notes = null;

		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setTitle(title);
		b.setMessage(sb);
		b.setCancelable(true);
		b.setPositiveButton(android.R.string.ok, null);
		b.show();
	}
}
