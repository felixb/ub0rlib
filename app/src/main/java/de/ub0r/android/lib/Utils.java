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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author flx
 */
public final class Utils {
	/** Tag for output. */
	private static final String TAG = "utls";

	/** 10. */
	public static final int N_10 = 10;
	/** 100. */
	public static final int N_100 = 100;
	/** 1000. */
	public static final int N_1000 = 1000;
	/** One hour in minutes. */
	public static final int HOUR_IN_MINUTES = 60;
	/** One hour in seconds. */
	public static final int HOUR_IN_SECONDS = 60 * 60;
	/** One hour in milliseconds. */
	public static final int HOUR_IN_MILLIS = 60 * 60 * 1000;
	/** One minutes in seconds. */
	public static final int MINUTES_IN_SECONDS = 60;
	/** One minutes in milliseconds. */
	public static final int MINUTES_IN_MILLIS = 60 * 1000;
	/** One day in seconds. */
	public static final long DAY_IN_SECONDS = 60L * 60L * 24L;
	/** One day in milliseconds. */
	public static final long DAY_IN_MILLIS = 60L * 60L * 24L * 1000L;

	/** k aka 1024. */
	public static final int K = 1024;
	/** M aka 1024 * 1024. */
	public static final int M = K * K;

	/** API level. */
	private static int iApi = -1;

	/**
	 * Default Constructor.
	 */
	private Utils() {

	}

	/**
	 * Parse {@link Boolean}.
	 * 
	 * @param value
	 *            value a {@link String}
	 * @param defValue
	 *            default value
	 * @return parsed {@link Boolean}
	 */
	public static boolean parseBoolean(final String value, final boolean defValue) {
		boolean ret = defValue;
		if (value == null || value.length() == 0) {
			return ret;
		}
		try {
			ret = Boolean.parseBoolean(value);
		} catch (NumberFormatException e) {
			Log.w(TAG, "parseBoolean(" + value + ") failed: " + e.toString());
		}
		return ret;
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
	 * Parse {@link Float}.
	 * 
	 * @param value
	 *            value a {@link String}
	 * @param defValue
	 *            default value
	 * @return parsed {@link Float}
	 */
	public static float parseFloat(final String value, final float defValue) {
		float ret = defValue;
		if (value == null || value.length() == 0) {
			return ret;
		}
		try {
			ret = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			Log.w(TAG, "parseFloat(" + value + ") failed: " + e.toString());
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
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
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
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
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
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + "jp.co.c_lis.ccl.morelocale")));
			} catch (ActivityNotFoundException e1) {
				Log.e(TAG, "no market", e1);
				Toast.makeText(context, "no market", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Copy a file from source to destination.
	 * 
	 * @param source
	 *            source
	 * @param destination
	 *            destination
	 * @throws IOException
	 *             File not found or any other IO Exception.
	 */
	public static void copyFile(final String source, final String destination) throws IOException {
		final InputStream in = new FileInputStream(source);
		final OutputStream out = new FileOutputStream(destination);
		byte[] buf = new byte[K];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Concatenate byte arrays.
	 * 
	 * @param bytes
	 *            array of byte arrays
	 * @return byte array
	 */
	public static byte[] concatByteArrays(final byte[][] bytes) {
		final int l = bytes.length;
		int rl = 0;
		for (int i = 0; i < l; i++) {
			rl += bytes[i].length;
		}
		final byte[] ret = new byte[rl];
		int pos = 0;
		for (int i = 0; i < l; i++) {
			byte[] b = bytes[i];
			int bl = b.length;
			System.arraycopy(b, 0, ret, pos, bl);
			pos += bl;
		}
		return ret;
	}

	/**
	 * Get Android's API version.
	 * 
	 * @return API version
	 */
	@SuppressWarnings("deprecation")
	public static int getApiVersion() {
		if (iApi < 0) {
			iApi = Integer.parseInt(Build.VERSION.SDK);
		}
		return iApi;
	}

	/**
	 * Is API supported?
	 * 
	 * @param api
	 *            Android's API version
	 * @return true, if api <= current API version
	 */
	@SuppressWarnings("deprecation")
	public static boolean isApi(final int api) {
		if (iApi < 0) {
			iApi = Integer.parseInt(Build.VERSION.SDK);
		}
		return iApi >= api;
	}

	/**
	 * Get the prefix from a telephone number (best approximation).
	 * 
	 * @param number
	 *            number
	 * @return prefix
	 */
	public static String getPrefixFromTelephoneNumber(final String number) {
		String prefix = null;

		if (number.startsWith("+10") || number.startsWith("+11")) {
			prefix = "+1";
		} else if (number.startsWith("+20") || number.startsWith("+27")) {
			prefix = number.substring(0, 3);
		} else if (number.startsWith("+2") || number.startsWith("+35") || number.startsWith("+37")
				|| number.startsWith("+38") || number.startsWith("+42") || number.startsWith("+50")
				|| number.startsWith("+59") || number.startsWith("+67") || number.startsWith("+68")
				|| number.startsWith("+69") || number.startsWith("+85") || number.startsWith("+88")
				|| number.startsWith("+96") || number.startsWith("+97") || number.startsWith("+99")) {
			prefix = number.substring(0, 4);
		} else if (number.startsWith("+3") || number.startsWith("+4") || number.startsWith("+5")
				|| number.startsWith("+6") || number.startsWith("+8") || number.startsWith("+9")) {
			prefix = number.substring(0, 3);
		} else if (number.startsWith("+7")) {
			prefix = number.substring(0, 2);
		} else if (number.startsWith("+1")) {
			prefix = number.substring(0, 6);
		}

		return prefix;
	}
}
