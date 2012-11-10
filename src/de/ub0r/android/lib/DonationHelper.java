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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.widget.Toast;

/**
 * Display send IMEI hash, read signature..
 * 
 * @author flx
 */
public final class DonationHelper {
	/** Tag for output. */
	private static final String TAG = "dh";

	/** Preference's name: hide ads. */
	private static final String PREFS_HIDEADS = "hideads";

	/** Threshold for chacking donator app license. */
	private static final double CHECK_DONATOR_LIC = 0.05;

	/** Donator package. */
	private static final String DONATOR_PACKAGE = "de.ub0r.android.donator";
	/** Donator legacy package. */
	private static final String DONATOR_PACKAGE_LEGACY = "de.ub0r.android.donatorlegacy";
	/** Donator legacy alternate URL. */
	private static final String DONATOR_ALTURL_LEGACY = "http://code.google.com/p/ub0rapps/downloads/list?"
			+ "can=3&q=Product%3DDonator";
	/** Check dontor Broadcast. */
	private static final String DONATOR_BROADCAST_CHECK = DONATOR_PACKAGE + ".CHECK";

	/** Hashed IMEI. */
	private static String imeiHash = null;

	/**
	 * Default Constructor.
	 */
	private DonationHelper() {
		// nothing to do
	}

	/**
	 * Get MD5 hash of the IMEI (device id).
	 * 
	 * @param context
	 *            {@link Context}
	 * @return MD5 hash of IMEI
	 */
	public static String getImeiHash(final Context context) {
		if (imeiHash == null) {
			// get imei
			TelephonyManager mTelephonyMgr = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			final String did = mTelephonyMgr.getDeviceId();
			if (did != null) {
				imeiHash = Utils.md5(did);
			} else {
				imeiHash = Utils.md5(Build.BOARD + Build.BRAND + Build.PRODUCT + Build.DEVICE);
			}
		}
		return imeiHash;
	}

	/**
	 * Check if ads should be hidden.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return true if ads should be hidden
	 */
	public static boolean hideAds(final Context context) {
		PackageManager pm = context.getPackageManager();
		Intent donationCheck = new Intent(DONATOR_BROADCAST_CHECK);
		ResolveInfo ri = pm.resolveService(donationCheck, 0);
		// Log.d(TAG, "ri: " + ri);
		int match = PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
		if (ri != null) {
			Log.d(TAG, "found package: " + ri.serviceInfo.packageName);
			ComponentName cn = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
			// Log.d(TAG, "component name: " + cn);
			int i = pm.getComponentEnabledSetting(cn);
			// Log.d(TAG, "component status: " + i);
			// Log.d(TAG, "package status: " + ri.serviceInfo.enabled);
			if (i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
					|| i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
					&& ri.serviceInfo.enabled) {
				match = pm.checkSignatures(context.getPackageName(), ri.serviceInfo.packageName);
			} else {
				Log.w(TAG, ri.serviceInfo.packageName + ": " + ri.serviceInfo.enabled);
			}
		}

		Log.i(TAG, "signature match: " + match);
		if (match != PackageManager.SIGNATURE_UNKNOWN_PACKAGE) {
			if (Math.random() < CHECK_DONATOR_LIC) {
				// verify donator license
				ComponentName cn = context.startService(donationCheck);
				Log.d(TAG, "Started service: " + cn);
				if (cn == null) {
					return false;
				}
			}
			return match == PackageManager.SIGNATURE_MATCH;
		}
		pm = null;

		// no donator app installed, check donation traditionally
		// do not drop legacy donators
		boolean ret = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				PREFS_HIDEADS, false);
		Log.d(TAG, "legacy donation check: " + ret);
		return ret;
	}

	/**
	 * Show "donate" dialog.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param title
	 *            title
	 * @param btnDonate
	 *            button text for donate
	 * @param btnNoads
	 *            button text for "i did a donation"
	 * @param messages
	 *            messages for dialog body
	 */
	public static void showDonationDialog(final Activity context, final String title,
			final String btnDonate, final String btnNoads, final String[] messages) {
		final Intent marketIntent = Market.getInstallAppIntent(context, DONATOR_PACKAGE, null);

		String btnTitle = String.format(btnDonate, "Play Store");

		SpannableStringBuilder sb = new SpannableStringBuilder();
		for (String m : messages) {
			sb.append(m);
			sb.append("\n");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.setSpan(new RelativeSizeSpan(0.75f), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setTitle(title);
		b.setMessage(sb);
		b.setCancelable(true);
		b.setPositiveButton(btnTitle, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					context.startActivity(marketIntent);
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "activity not found", e);
					Toast.makeText(context, "activity not found", Toast.LENGTH_LONG).show();
				}
			}
		});
		b.setNeutralButton(btnNoads, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					context.startActivity(Market.getInstallAppIntent(context,
							DONATOR_PACKAGE_LEGACY, DONATOR_ALTURL_LEGACY));
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "activity not found", e);
					Toast.makeText(context, "activity not found", Toast.LENGTH_LONG).show();
				}
			}
		});
		b.show();
	}
}
