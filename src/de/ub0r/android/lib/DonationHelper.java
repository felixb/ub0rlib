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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
	static final String PREFS_HIDEADS = "hideads";

	/** Standard buffer size. */
	public static final int BUFSIZE = 512;

	/** Preference: paypal id. */
	static final String PREFS_DONATEMAIL = "donate_mail";
	/** Preference: last check. */
	private static final String PREFS_LASTCHECK = "donate_lastcheck";
	/** Preference: period for next check. */
	private static final String PREFS_PERIOD = "donate_period";
	/** Initial period. */
	private static final long INIT_PERIOD = 24 * 60 * 60 * 1000; // 1 day

	/** Threshold for chacking donator app license. */
	private static final double CHECK_DONATOR_LIC = 0.05;

	/** URL for checking hash. */
	public static final String URL = "http://www.ub0r.de/donation/";

	/** Donator package. */
	private static final String DONATOR_PACKAGE = "de.ub0r.android.donator";
	/** Check dontor Broadcast. */
	private static final String DONATOR_BROADCAST_CHECK = DONATOR_PACKAGE + ".CHECK";

	/** Crypto algorithm for signing UID hashs. */
	private static final String ALGO = "RSA";
	/** Crypto hash algorithm for signing UID hashs. */
	private static final String SIGALGO = "SHA1with" + ALGO;
	/** My public key for verifying UID hashs. */
	private static final String KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNAD"
			+ "CBiQKBgQCgnfT4bRMLOv3rV8tpjcEqsNmC1OJaaEYRaTHOCC"
			+ "F4sCIZ3pEfDcNmrZZQc9Y0im351ekKOzUzlLLoG09bsaOeMd"
			+ "Y89+o2O0mW9NnBch3l8K/uJ3FRn+8Li75SqoTqFj3yCrd9IT"
			+ "sOJC7PxcR5TvNpeXsogcyxxo3fMdJdjkafYwIDAQAB";

	/** Hashed IMEI. */
	private static String imeiHash = null;

	/**
	 * Do all the IO.
	 * 
	 * @author flx
	 */
	static class InnerTask extends AsyncTask<Void, Void, Void> {
		/** The progress dialog. */
		private ProgressDialog dialog = null;
		/** Did an error occurred? */
		private boolean error = true;
		/** Message to the user. */
		private String msg = null;
		/** Run in recheck mode. */
		private final boolean doRecheck;
		/** Instance of DonationHelper. */
		private String paypalId;
		/** Context. */
		private final Context ctx;
		/** Strings. */
		private final String mLoading, mLoadCompleted, mLoadFailed;

		/**
		 * Default Constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 * @param mail
		 *            paypal id
		 * @param messagesLoad
		 *            {@link String}s for "loading", "loading completed" and "loading failed"
		 */
		public InnerTask(final Context context, final String mail, final String[] messagesLoad) {
			this.ctx = context;
			this.paypalId = mail;
			this.doRecheck = false;
			this.mLoading = messagesLoad[0];
			this.mLoadCompleted = messagesLoad[1];
			this.mLoadFailed = messagesLoad[2];
		}

		/**
		 * Default Constructor to re-check.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		public InnerTask(final Context context) {
			this.ctx = context;
			this.doRecheck = true;
			this.mLoading = null;
			this.mLoadCompleted = null;
			this.mLoadFailed = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onPreExecute() {
			final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this.ctx);
			if (this.doRecheck) {
				this.paypalId = p.getString(PREFS_DONATEMAIL, "no@mail.local");
			} else {
				this.paypalId = this.paypalId.trim().toLowerCase();
				this.dialog = ProgressDialog.show(this.ctx, "", this.mLoading + "...", true, false);
				p.edit().putString(PREFS_DONATEMAIL, this.paypalId).commit();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onPostExecute(final Void result) {
			if (this.dialog != null) {
				this.dialog.dismiss();
			}
			final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this.ctx);
			if (this.doRecheck) {
				long period = p.getLong(PREFS_PERIOD, INIT_PERIOD) * 2;
				long lastCheck = System.currentTimeMillis();
				if (!this.error) {
					p.edit().putLong(PREFS_PERIOD, period).putLong(PREFS_LASTCHECK, lastCheck)
							.commit();
				}
			} else {
				if (this.msg != null) {
					Toast.makeText(this.ctx, this.msg, Toast.LENGTH_LONG).show();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void doInBackground(final Void... params) {
			String url = URL + "?mail=" + Uri.encode(this.paypalId) + "&hash="
					+ getImeiHash(this.ctx) + "&lang=" + Locale.getDefault().getISO3Country();
			if (this.doRecheck) {
				url += "&recheck=1";
			}
			final HttpGet request = new HttpGet(url);
			try {
				Log.d(TAG, "url: " + url);
				final HttpResponse response = new DefaultHttpClient().execute(request);
				int resp = response.getStatusLine().getStatusCode();
				if (resp != HttpStatus.SC_OK) {
					this.msg = "Service is down. Retry later. Returncode: " + resp;
					// silent error on recheck
					this.error = !this.doRecheck;
					return null;
				}
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()), BUFSIZE);
				final String line = bufferedReader.readLine();
				final boolean ret = checkSig(this.ctx, line);
				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(this.ctx);
				prefs.edit().putBoolean(PREFS_HIDEADS, ret).commit();

				if (ret) {
					this.msg = this.mLoadCompleted;
				} else {
					this.msg = this.mLoadFailed;
				}
				this.error = !ret;
				if (this.error) {
					this.msg += "\n" + line;
				}
			} catch (ClientProtocolException e) {
				Log.e(TAG, "error loading sig", e);
				this.msg = e.getMessage();
				// silent error on recheck
				this.error = !this.doRecheck;
			} catch (IOException e) {
				Log.e(TAG, "error loading sig", e);
				this.msg = e.getMessage();
				// silent error on recheck
				this.error = !this.doRecheck;
			}
			return null;
		}
	}

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
	 * Check for signature updates.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param s
	 *            signature
	 * @param h
	 *            hash
	 * @return true if ads should be hidden
	 */
	public static boolean checkSig(final Context context, final String s, final String h) {
		Log.d(TAG, "checkSig(ctx, " + s + ", " + h + ")");
		boolean ret = false;
		try {
			final byte[] publicKey = Base64Coder.decode(KEY);
			final KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
			PublicKey pk = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
			Log.d(TAG, "hash: " + h);
			final String cs = s.replaceAll(" |\n|\t", "");
			Log.d(TAG, "read sig: " + cs);
			try {
				byte[] signature = Base64Coder.decode(cs);
				Signature sig = Signature.getInstance(SIGALGO);
				sig.initVerify(pk);
				sig.update(h.getBytes());
				ret = sig.verify(signature);
				Log.d(TAG, "ret: " + ret);
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "error reading signature", e);
			}
		} catch (Exception e) {
			Log.e(TAG, "error reading signatures", e);
		}
		if (!ret) {
			Log.i(TAG, "sig: " + s);
		}
		return ret;
	}

	/**
	 * Check for signature updates.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param s
	 *            signature
	 * @return true if ads should be hidden
	 */
	public static boolean checkSig(final Context context, final String s) {
		Log.d(TAG, "checkSig(ctx, " + s + ")");
		return checkSig(context, s, getImeiHash(context));
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
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://code.google.com/p/ub0rapps/downloads/list?"
									+ "can=3&q=Product%3DDonator")));
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "activity not found", e);
					Toast.makeText(context, "activity not found", Toast.LENGTH_LONG).show();
				}
			}
		});
		b.show();
	}
}
