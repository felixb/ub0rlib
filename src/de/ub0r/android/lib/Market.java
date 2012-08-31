/*
 * Copyright (C) 2011-2012 Felix Bechstein
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

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Make intents market aware. Currently google and amazon is supported.
 * 
 * @author flx
 */
public final class Market {
	/** Tag for output. */
	private static final String TAG = "ub0rlib";

	/** Search all apps. */
	public static final String SEARCH_APPS = "Felix+Bechstein";
	/** Alternative link to all apps. */
	public static final String ALT_APPS = "http://code.google.com/u/felix.bechstein/";

	/** Alternative link to SMSdroid. */
	public static final String ALT_WEBSMS = "http://code.google.com/p"
			+ "/websmsdroid/downloads/list?can=2&q=Product%3DSMSdroid";
	/** Alternative link to WebSMS. */
	public static final String ALT_SMSDROID = "http://code.google.com/p"
			+ "/websmsdroid/downloads/list?can=2&q=Product%3WebSMS";

	/** Alternative link to Call Meter. */
	public static final String ALT_CALLMETER = "http://code.google.com/p"
			+ "/callmeter/downloads/list";

	/** Alternative link to TravelLog. */
	public static final String ALT_TRAVELLOG = "http://code.google.com/p"
			+ "/ub0rapps/downloads/list?can=2&q=Product%3DTravelLog";
	/** Alternative link to OTPdroid. */
	public static final String ALT_OTPDROID = "http://code.google.com/p"
			+ "/ub0rapps/downloads/list?can=2&q=Product%3DOTPdroid";
	/** Alternative link to MissingKeys. */
	public static final String ALT_MISSINGKEYS = "http://code.google.com/p"
			+ "/ub0rapps/downloads/list?can=2&q=Product%3DMissingKeys";
	/** Alternative link to RingRing. */
	public static final String ALT_RINGRING = "http://code.google.com/p"
			+ "/ub0rapps/downloads/list?can=2&q=Product%3DRingRing";

	/** URL used for google market to install. */
	private static final String GOOGLE_INSTALL = "market://details?id=";
	/** URL used for google market website to install. */
	private static final String GOOGLE_WEBINSTALL = "https://play.google.com/store/apps/details?id=";
	/** URL used for amazon market to install. */
	private static final String AMAZON_INSTALL = "http://www.amazon.com" + "/gp/mas/dl/android?p=";

	/** URL used for google market to search. */
	private static final String GOOGLE_SEARCH = "market://search?q=";
	/** URL used for amazon market to search. */
	private static final String AMAZON_SEARCH = "http://www.amazon.com" + "/gp/mas/dl/android?s=";

	/** Skip google market. */
	private static final boolean GOOGLE_SKIP = false;
	/** Skip amazon market. */
	private static final boolean AMAZON_SKIP = false;

	/**
	 * Hide default constructor.
	 */
	private Market() {
	}

	/**
	 * get {@link Intent} to any market app to load an app.
	 * 
	 * @param context
	 *            {@link Context} used to launch the intent
	 * @param packagename
	 *            package name of target app
	 * @param alternativeLink
	 *            link to some alternative source, if no market is available
	 * @return {@link Intent} which should be launched
	 */
	public static Intent getInstallAppIntent(final Context context, final String packagename,
			final String alternativeLink) {
		Log.i(TAG, "getInstallAppIntent(" + context + ", " + packagename + ", " + alternativeLink
				+ ")");
		final Intent i = new Intent(Intent.ACTION_VIEW);

		if (!GOOGLE_SKIP) {
			// try google market
			i.setData(Uri.parse(GOOGLE_INSTALL + packagename));
			if (i.resolveActivity(context.getPackageManager()) != null) {
				return i;
			}
			Log.w(TAG, "no google market installed");
		} else {
			Log.i(TAG, "skip google market");
		}

		if (!AMAZON_SKIP) {
			// try amazon market
			i.setData(Uri.parse(AMAZON_INSTALL + packagename));
			final List<ResolveInfo> l = context.getPackageManager().queryIntentActivities(i, 0);
			for (ResolveInfo r : l) {
				if (r.activityInfo.packageName.contains("amazon")) {
					Log.i(TAG, "use app: " + r.activityInfo.packageName);
					return i;
				} else {
					Log.i(TAG, "skipp app: " + r.activityInfo.packageName);
				}
			}
			Log.w(TAG, "no amazon market installed");
		} else {
			Log.i(TAG, "skip amazon market");
		}

		if (!TextUtils.isEmpty(alternativeLink)) {
			// try alternative link
			i.setData(Uri.parse(alternativeLink));
			if (i.resolveActivity(context.getPackageManager()) != null) {
				return i;
			}
			Log.w(TAG, "no handler installed: " + alternativeLink);
		} else if (!GOOGLE_SKIP) {
			i.setData(Uri.parse(GOOGLE_WEBINSTALL + packagename));
			return i;
		}

		Log.w(TAG, "no handler found to install package: " + packagename);
		return null;
	}

	/**
	 * Open any market app to load an app.
	 * 
	 * @param activity
	 *            {@link Activity} used to launch the intent
	 * @param packagename
	 *            package name of target app
	 * @param alternativeLink
	 *            link to some alternative source, if no market is available
	 * @return true if any intent was launched successfully
	 */
	public static boolean installApp(final Activity activity, final String packagename,
			final String alternativeLink) {
		Log.i(TAG, "installApp(" + activity + ", " + packagename + ", " + alternativeLink + ")");
		final Intent i = getInstallAppIntent(activity, packagename, alternativeLink);
		if (i != null) {
			try {
				activity.startActivity(i);
				return true;
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "activity not found", e);
				Toast.makeText(activity, "activity not found", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(activity, "no handler found to install package: " + packagename,
					Toast.LENGTH_LONG).show();
		}
		return false;
	}

	/**
	 * Get {@link Intent} to market app to search for an app.
	 * 
	 * @param context
	 *            {@link Context} used to launch the intent
	 * @param search
	 *            search string
	 * @param alternativeLink
	 *            link to some alternative source, if no market is available
	 * @return intent to content
	 */
	public static Intent getSearchAppIntent(final Context context, final String search,
			final String alternativeLink) {
		Log.i(TAG, "getSearchAppIntent(" + context + ", " + search + ", " + alternativeLink + ")");
		final Intent i = new Intent(Intent.ACTION_VIEW);

		if (!GOOGLE_SKIP) {
			// try google market
			i.setData(Uri.parse(GOOGLE_SEARCH + search));
			if (i.resolveActivity(context.getPackageManager()) != null) {
				return i;
			}
			Log.w(TAG, "no google market installed");
		} else {
			Log.i(TAG, "skip google market");
		}

		if (!AMAZON_SKIP) {
			// try amazon market
			i.setData(Uri.parse(AMAZON_SEARCH + search));
			// TODO: launch amazon app explicitly?
			final List<ResolveInfo> l = context.getPackageManager().queryIntentActivities(i, 0);
			for (ResolveInfo r : l) {
				if (r.activityInfo.packageName.contains("amazon")) {
					Log.i(TAG, "use app: " + r.activityInfo.packageName);
					return i;
				} else {
					Log.i(TAG, "skipp app: " + r.activityInfo.packageName);
				}
			}
			Log.w(TAG, "no amazon market installed");
		} else {
			Log.i(TAG, "skip amazon market");
		}

		if (!TextUtils.isEmpty(alternativeLink)) {
			// try alternative link
			i.setData(Uri.parse(alternativeLink));
			if (i.resolveActivity(context.getPackageManager()) != null) {
				return i;
			}
			Log.e(TAG, "no handler installed: " + alternativeLink);
		}

		Log.w(TAG, "no handler found to search app: " + search);
		return null;
	}

	/**
	 * Open any market app to search for an app.
	 * 
	 * @param activity
	 *            {@link Activity} used to launch the intent
	 * @param search
	 *            search string
	 * @param alternativeLink
	 *            link to some alternative source, if no market is available
	 * @return true if any intent was launched successfully
	 */
	public static boolean searchApp(final Activity activity, final String search,
			final String alternativeLink) {
		Log.i(TAG, "searchApp(" + activity + ", " + search + ", " + alternativeLink + ")");
		final Intent i = getSearchAppIntent(activity, search, alternativeLink);
		if (i != null) {
			try {
				activity.startActivity(i);
				return true;
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "activity not found", e);
				Toast.makeText(activity, "activity not found", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(activity, "no handler found to search app: " + search, Toast.LENGTH_LONG)
					.show();
		}
		return false;
	}

	/**
	 * Open any market app to search for all my app.
	 * 
	 * @param activity
	 *            {@link Activity} used to launch the intent
	 * @return true if any intent was launched successfully
	 */
	public static boolean searchMoreApps(final Activity activity) {
		Log.i(TAG, "searchMoreApps(" + activity + ")");
		return searchApp(activity, SEARCH_APPS, ALT_APPS);
	}

	/**
	 * Set a {@link OnPreferenceClickListener} to preference to launch any market app.
	 * 
	 * @param activity
	 *            {@link Activity} to launch the {@link Intent}
	 * @param preference
	 *            {@link Preference} to set the {@link OnPreferenceClickListener} to
	 * @param packagename
	 *            package name to install
	 * @param search
	 *            search to look for; ignored if packagename is not null
	 * @param alternativeLink
	 *            alternative source to get the content
	 */
	public static void setOnPreferenceClickListener(final Activity activity,
			final Preference preference, final String packagename, final String search,
			final String alternativeLink) {
		if (preference == null) {
			return;
		}
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				if (!TextUtils.isEmpty(packagename)) {
					installApp(activity, packagename, alternativeLink);
					return true;
				}
				if (!TextUtils.isEmpty(search)) {
					searchApp(activity, search, alternativeLink);
					return true;
				}
				return false;
			}
		});
	}
}
