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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
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
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Display send IMEI hash, read signature..
 * 
 * @author flx
 */
@SuppressWarnings("deprecation")
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

	/** Bitcoin address. */
	private static final String DONATE_BITCOIN = "1LvQe3ARrdKeMrzxkFn1MW3YrvbQKZsq5P";

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
	 * Common {@link OnCheckedChangeListener}.
	 * 
	 * @author flx
	 */
	static class InnerOnCheckedChangeListener implements OnCheckedChangeListener {
		/** {@link OnClickListener}'s target. */
		private final Activity activity;

		/**
		 * Default Constructor.
		 * 
		 * @param target
		 *            target {@link Activity}
		 */
		public InnerOnCheckedChangeListener(final Activity target) {
			this.activity = target;
		}

		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			if (isChecked) {
				this.activity.findViewById(R.id.layout_nonmarket).setVisibility(View.VISIBLE);
				this.activity.findViewById(R.id.did_paypal_donation).setVisibility(View.GONE);
			} else {
				this.activity.findViewById(R.id.layout_nonmarket).setVisibility(View.GONE);
			}
		}

	}

	/**
	 * Common {@link OnClickListener}.
	 * 
	 * @author flx
	 */
	static class InnerOnClickListener implements OnClickListener {
		/** {@link OnClickListener}'s target. */
		private final Activity activity;

		/**
		 * Default Constructor.
		 * 
		 * @param target
		 *            target {@link Activity}
		 */
		public InnerOnClickListener(final Activity target) {
			this.activity = target;
		}

		/**
		 * {@inheritDoc}
		 */
		public final void onClick(final View v) {
			if (v.getId() == R.id.donate_paypal) {
				try {
					this.activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(this.activity.getString(R.string.donate_url))));
				} catch (Exception e) {
					Log.e(TAG, "error launching paypal", e);
					Toast.makeText(this.activity, e.getMessage(), Toast.LENGTH_LONG).show();
				}
				return;
			} else if (v.getId() == R.id.donate_market) {
				Market.installApp(this.activity, DONATOR_PACKAGE, null);
				return;
			} else if (v.getId() == R.id.send) {
				if (TextUtils.isEmpty(((EditText) this.activity.findViewById(R.id.paypalid))
						.getText())) {
					Toast.makeText(this.activity, R.string.donator_id_, Toast.LENGTH_LONG).show();
					return;
				}
				final CheckBox cb = (CheckBox) this.activity.findViewById(R.id.accept);
				if (!cb.isChecked()) {
					Toast.makeText(this.activity, R.string.accept_missing, Toast.LENGTH_LONG)
							.show();
					return;
				}
				new InnerTask(this.activity, this.activity, false).execute((Void[]) null);
				return;
			} else {
				return;
			}
		}
	}

	/**
	 * Do all the IO.
	 * 
	 * @author flx
	 */
	static class InnerTask extends AsyncTask<Void, Void, Void> {
		/** Mail address used. */
		private String mail;
		/** The progress dialog. */
		private ProgressDialog dialog = null;
		/** Did an error occurred? */
		private boolean error = true;
		/** Message to the user. */
		private String msg = null;
		/** Run in recheck mode. */
		private final boolean doRecheck;
		/** Instance of DonationHelper. */
		private final Activity dh;
		/** Context. */
		private final Context ctx;

		/**
		 * Default Constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 * @param helper
		 *            {@link DonationHelper}
		 * @param recheck
		 *            run in recheck mode
		 */
		public InnerTask(final Context context, final Activity helper, final boolean recheck) {
			this.ctx = context;
			this.dh = helper;
			this.doRecheck = recheck;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onPreExecute() {
			final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this.ctx);
			if (this.doRecheck) {
				this.mail = p.getString(PREFS_DONATEMAIL, "no@mail.local");
			} else {
				this.mail = ((EditText) this.dh.findViewById(R.id.paypalid)).getText().toString()
						.trim().toLowerCase();
				this.dialog = ProgressDialog.show(this.dh, "",
						this.ctx.getString(R.string.load_hash_) + "...", true, false);
				p.edit().putString(PREFS_DONATEMAIL, this.mail).commit();
				this.dh.findViewById(R.id.send).setEnabled(false);
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
			if (this.dh != null) {
				if (!this.error) {
					this.dh.finish();
				}
				this.dh.findViewById(R.id.send).setEnabled(true);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void doInBackground(final Void... params) {
			String url = URL + "?mail=" + Uri.encode(this.mail) + "&hash=" + getImeiHash(this.ctx)
					+ "&lang=" + this.ctx.getString(R.string.lang);
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

				int text = R.string.sig_loaded;
				if (!ret) {
					text = R.string.sig_failed;
				}
				this.msg = this.ctx.getString(text);
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
	 * Common onCreate().
	 * 
	 * @param target
	 *            {@link Activity}
	 */
	static void onCreate(final Activity target) {
		target.setContentView(R.layout.donation);

		InnerOnClickListener ocl = new InnerOnClickListener(target);

		CheckBox cb = (CheckBox) target.findViewById(R.id.did_paypal_donation);
		cb.setOnCheckedChangeListener(new InnerOnCheckedChangeListener(target));

		final Intent marketIntent = Market.getInstallAppIntent(target, DONATOR_PACKAGE, null);
		boolean isMarketInstalled = marketIntent != null
				&& marketIntent.getDataString().startsWith("market://");
		if (isMarketInstalled) {
			((TextView) target.findViewById(R.id.predonate)).setText(R.string.predonate_market);
			target.findViewById(R.id.donate_market).setOnClickListener(ocl);
			target.findViewById(R.id.donate_paypal).setVisibility(View.GONE);
			if (!cb.isChecked()) {
				target.findViewById(R.id.layout_nonmarket).setVisibility(View.GONE);
			}
		} else {
			((TextView) target.findViewById(R.id.predonate)).setText(R.string.predonate);
			target.findViewById(R.id.donate_market).setVisibility(View.GONE);
			target.findViewById(R.id.donate_paypal).setOnClickListener(ocl);
			target.findViewById(R.id.layout_nonmarket).setVisibility(View.VISIBLE);
			cb.setVisibility(View.GONE);
		}

		target.findViewById(R.id.send).setOnClickListener(ocl);
		final String mail = PreferenceManager.getDefaultSharedPreferences(target).getString(
				PREFS_DONATEMAIL, "");
		((EditText) target.findViewById(R.id.paypalid)).setText(mail);
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
		Log.d(TAG, "ri: " + ri);
		int match = PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
		if (ri != null) {
			Log.d(TAG, "TEST: found package: " + ri.serviceInfo.packageName);
			ComponentName cn = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
			Log.d(TAG, "component name: " + cn);
			int i = pm.getComponentEnabledSetting(cn);
			Log.d(TAG, "component status: " + i);
			Log.w(TAG, "package status: " + ri.serviceInfo.enabled);
			if (i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
					|| i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
					&& ri.serviceInfo.enabled) {
				match = pm.checkSignatures(context.getPackageName(), ri.serviceInfo.packageName);
			} else {
				Log.w(TAG, ri.serviceInfo.packageName + ": " + ri.serviceInfo.enabled);
			}
		}

		Log.d(TAG, "TEST: match=" + match);
		if (match != PackageManager.SIGNATURE_UNKNOWN_PACKAGE) {
			if (Math.random() < CHECK_DONATOR_LIC * 100f) { // FIXME
				// verify donator license
				ComponentName cn = context.startService(donationCheck);
				Log.d(TAG, "Started service: " + cn);
				if (cn == null) {
					return false;
				}
			}
			if (match == PackageManager.SIGNATURE_MATCH) {
				return true;
			}
			return false;
		}
		pm = null;

		// no donator app installed, check donation traditionally
		// TODO: remove this!
		// TODO: return false;
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean ret = p.getBoolean(PREFS_HIDEADS, false);
		if (ret && p.getString(PREFS_DONATEMAIL, null) != null) {
			final long period = p.getLong(PREFS_PERIOD, INIT_PERIOD);
			final long lastCheck = p.getLong(PREFS_LASTCHECK, 0);
			final long nextCheck = lastCheck + period - System.currentTimeMillis();
			if (nextCheck < 0) {
				Log.i(TAG, "recheck donation");
				new InnerTask(context, null, true).execute((Void[]) null);
			} else {
				Log.d(TAG, "next recheck: " + nextCheck);
			}
		}
		return ret;
	}

	/**
	 * Start the Donation {@link Activity}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param icsStyle
	 *            use HC/ICS Style
	 */
	public static void startDonationActivity(final Context context, final boolean icsStyle) {
		if (icsStyle) {
			context.startActivity(new Intent(context, DonationFragmentActivity.class));
		} else {
			context.startActivity(new Intent(context, DonationActivity.class));
		}
	}
}
