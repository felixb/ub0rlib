/*
 * Copyright (C) 2010 Felix Bechstein
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author flx
 */
public final class Utils {
	/** Tag for output. */
	private static final String TAG = "utls";

	/**
	 * Default Constructor.
	 */
	private Utils() {

	}

	/**
	 * Parse {@link Integer}.
	 * 
	 * @param value
	 *            value a {@link String}
	 * @param defValue
	 *            default value
	 * @return parsed {@link Integer}
	 */
	public static int parseInt(final String value, final int defValue) {
		int ret = defValue;
		if (value == null || value.length() == 0) {
			return ret;
		}
		try {
			ret = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			Log.w(TAG, "parseInt(" + value + ") failed: " + e.toString());
		}
		return ret;
	}

	/**
	 * Parse {@link Long}.
	 * 
	 * @param value
	 *            value a {@link String}
	 * @param defValue
	 *            default value
	 * @return parsed {@link Long}
	 */
	public static long parseLong(final String value, final long defValue) {
		long ret = defValue;
		if (value == null || value.length() == 0) {
			return ret;
		}
		try {
			ret = Long.parseLong(value);
		} catch (NumberFormatException e) {
			Log.w(TAG, "parseLong(" + value + ") failed: " + e.toString());
		}
		return ret;
	}

	/**
	 * Calculate MD5 Hash from String.
	 * 
	 * @param s
	 *            input
	 * @return hash
	 */
	public static String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte[] messageDigest = digest.digest();
			// Create Hex String
			StringBuilder hexString = new StringBuilder(32);
			int b;
			for (int i = 0; i < messageDigest.length; i++) {
				b = 0xFF & messageDigest[i];
				if (b < 0x10) {
					hexString.append('0' + Integer.toHexString(b));
				} else {
					hexString.append(Integer.toHexString(b));
				}
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, null, e);
		}
		return "";
	}

	/**
	 * Set locale read from preferences to context.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public static void setLocale(final Context context) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String lc = p.getString("morelocale", null);
		if (TextUtils.isEmpty(lc)) {
			return;
		}
		Log.i(TAG, "set custom locale: " + lc);
		final Locale locale = new Locale(lc);
		Locale.setDefault(locale);
		final Configuration config = new Configuration();
		config.locale = locale;
		context.getResources().updateConfiguration(config, null);
	}

	/**
	 * Start MoreLocale2 or fetch it from market if unavailable.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public static void startMoreLocale(final Context context) {
		try {
			final Intent i = new Intent(Intent.ACTION_MAIN);
			i.setComponent(new ComponentName("jp.co.c_lis.ccl.morelocale",
					"com.android.settings.morelocale.ui.MainActivity"));
			context.startActivity(i);
		} catch (ActivityNotFoundException e) {
			try {
				Log.e(TAG, "no morelocale2", e);
				context.startActivity(new Intent(Intent.ACTION_VIEW, // .
						Uri.parse("market://details?id="
								+ "jp.co.c_lis.ccl.morelocale")));
			} catch (ActivityNotFoundException e1) {
				Log.e(TAG, "no market", e1);
				Toast.makeText(context, "no market", Toast.LENGTH_LONG).show();
			}
		}
	}
}
