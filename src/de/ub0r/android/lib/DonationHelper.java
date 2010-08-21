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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Display send IMEI hash, read signature..
 * 
 * @author flx
 */
public class DonationHelper extends Activity implements OnClickListener {
	/** Tag for output. */
	private static final String TAG = "dh";

	/** Preference's name: hide ads. */
	static final String PREFS_HIDEADS = "hideads";

	/** Standard buffer size. */
	public static final int BUFSIZE = 512;

	/** Id of {@link Notification}. */
	private static final int NOTIFICATION_ID = 123457;
	/** Preference: donator installed. */
	static final String PREFS_DONATOR_INSTALLED = "donater_installed";
	/** Preference: paypal id. */
	static final String PREFS_DONATEMAIL = "donate_mail";
	/** Preference: last check. */
	private static final String PREFS_LASTCHECK = "donate_lastcheck";
	/** Preference: period for next check. */
	private static final String PREFS_PERIOD = "donate_period";
	/** Initial period. */
	private static final long INIT_PERIOD = 24 * 60 * 60 * 1000; // 1 day

	/** URL for checking hash. */
	public static final String URL = "http://www.4.ub0r.de/donation/";

	/** Donator package. */
	private static final String DONATOR_PACKAGE = "de.ub0r.android.donator";
	/** Donator class. */
	private static final String DONATOR_CLASS = DONATOR_PACKAGE + ".Main";
	/** Dontor Broadcast. */
	public static final String DONATOR_BROADCAST = DONATOR_PACKAGE
			+ ".REGISTERED";

	/** Uri to market:donator. */
	private static final Uri DONATOR_MARKET = Uri // .
			.parse("market://search?q=pname:" + DONATOR_PACKAGE);

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

	/** {@link EditText} for paypal id. */
	private EditText etPaypalId;

	/** Hashed IMEI. */
	private static String imeiHash = null;

	/**
	 * Do all the IO.
	 * 
	 * @author flx
	 */
	private static class InnerTask extends AsyncTask<Void, Void, Void> {
		/** Mail address used. */
		private String mail;
		/** The progress dialog. */
		private ProgressDialog dialog = null;
		/** Did an error occurred? */
		private boolean error = true;
		/** Did an http error occurred? */
		private boolean errorHttp = false;
		/** Message to the user. */
		private String msg = null;
		/** Run in recheck mode. */
		private final boolean doRecheck;
		/** Instance of DonationHelper. */
		private final DonationHelper dh;
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
		public InnerTask(final Context context, final DonationHelper helper,
				final boolean recheck) {
			this.ctx = context;
			this.dh = helper;
			this.doRecheck = recheck;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onPreExecute() {
			final SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(this.ctx);
			if (this.doRecheck) {
				this.mail = p.getString(PREFS_DONATEMAIL, "no@mail.local");
			} else {
				this.mail = this.dh.etPaypalId.getText().toString().trim()
						.toLowerCase();
				this.dialog = ProgressDialog.show(this.dh, "", this.ctx
						.getString(R.string.load_hash_)
						+ "...", true, false);
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
			if (this.doRecheck) {
				final SharedPreferences p = PreferenceManager
						.getDefaultSharedPreferences(this.ctx);
				long period = p.getLong(PREFS_PERIOD, INIT_PERIOD) * 2;
				long lastCheck = System.currentTimeMillis();
				if (this.error && !this.errorHttp) {
					p.edit().remove(PREFS_LASTCHECK).remove(PREFS_PERIOD)
							.remove(PREFS_HIDEADS).commit();
				} else if (!this.error) {
					p.edit().putLong(PREFS_PERIOD, period).putLong(
							PREFS_LASTCHECK, lastCheck).commit();
				}
			} else {
				if (this.msg != null) {
					Toast.makeText(this.ctx, this.msg, Toast.LENGTH_LONG)
							.show();
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
			String url = URL + "?mail=" + Uri.encode(this.mail) + "&hash="
					+ getImeiHash(this.ctx) + "&lang="
					+ this.ctx.getString(R.string.lang);
			if (this.doRecheck) {
				url += "&recheck=1";
			}
			final HttpGet request = new HttpGet(url);
			try {
				Log.d(TAG, "url: " + url);
				final HttpResponse response = new DefaultHttpClient()
						.execute(request);
				int resp = response.getStatusLine().getStatusCode();
				if (resp != HttpStatus.SC_OK) {
					this.msg = "Service is down. Retry later. Returncode: "
							+ resp;
					this.errorHttp = true;
					return null;
				}
				final BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(// .
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
			} catch (IOException e) {
				Log.e(TAG, "error loading sig", e);
				this.msg = e.getMessage();
			}
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.donation);

		this.findViewById(R.id.donate_paypal).setOnClickListener(this);
		this.findViewById(R.id.donate_market).setOnClickListener(this);
		this.findViewById(R.id.send).setOnClickListener(this);
		this.etPaypalId = (EditText) this.findViewById(R.id.paypalid);
		final String mail = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(PREFS_DONATEMAIL, "");
		this.etPaypalId.setText(mail);

		if (mail == null || mail.length() == 0) {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(DONATOR_PACKAGE, DONATOR_CLASS);
			try {
				this.startActivityForResult(intent, 0);
			} catch (ActivityNotFoundException e) {
				Log.d(TAG, "no donator installed");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			final String mail = data.getStringExtra(Intent.EXTRA_EMAIL);
			if (mail != null && mail.length() > 0) {
				this.etPaypalId.setText(mail);
				final SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				prefs.edit().putBoolean(PREFS_HIDEADS, true).putString(
						PREFS_DONATEMAIL, mail).commit();
				this.finish();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.donate_paypal:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(this.getString(R.string.donate_url))));
			} catch (Exception e) {
				Log.e(TAG, "error launching paypal", e);
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return;
		case R.id.donate_market:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW,
						DONATOR_MARKET));
			} catch (Exception e) {
				Log.e(TAG, "error launching market", e);
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return;
		case R.id.send:
			if (this.etPaypalId.getText().toString().length() == 0) {
				Toast.makeText(this, R.string.donator_id_, Toast.LENGTH_LONG)
						.show();
				return;
			}
			final CheckBox cb = (CheckBox) this.findViewById(R.id.accept);
			if (!cb.isChecked()) {
				Toast
						.makeText(this, R.string.accept_missing,
								Toast.LENGTH_LONG).show();
				return;
			}
			new InnerTask(this, this, false).execute((Void[]) null);
			return;
		default:
			return;
		}
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
					.getSystemService(TELEPHONY_SERVICE);
			final String did = mTelephonyMgr.getDeviceId();
			if (did != null) {
				imeiHash = Utils.md5(did);
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
	 * @return true if ads should be hidden
	 */
	public static boolean checkSig(final Context context, final String s) {
		Log.d(TAG, "checkSig(ctx, " + s + ")");
		boolean ret = false;
		try {
			final byte[] publicKey = Base64Coder.decode(KEY);
			final KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
			PublicKey pk = keyFactory.generatePublic(new X509EncodedKeySpec(
					publicKey));
			final String h = getImeiHash(context);
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
	 * Check if ads should be hidden.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return true if ads should be hidden
	 */
	public static boolean hideAds(final Context context) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final boolean ret = p.getBoolean(PREFS_HIDEADS, false);
		if (ret && p.getString(PREFS_DONATEMAIL, null) != null) {
			final long period = p.getLong(PREFS_PERIOD, INIT_PERIOD);
			final long lastCheck = p.getLong(PREFS_LASTCHECK, 0);
			final long nextCheck = lastCheck + period
					- System.currentTimeMillis();
			if (nextCheck < 0) {
				Log.i(TAG, "recheck donation");
				new InnerTask(context, null, true).execute((Void[]) null);
			} else {
				Log.d(TAG, "next recheck: " + nextCheck);
			}
		} else if (!ret && p.getBoolean(PREFS_DONATOR_INSTALLED, false)) {
			// donator installed but not yet loaded the noads code
			final CharSequence t = context.getString(R.string.notify_);
			final Notification n = new Notification(
					android.R.drawable.stat_sys_warning, t, System
							.currentTimeMillis());
			final Intent intent = new Intent(context, DonationHelper.class);
			n.setLatestEventInfo(context, t, context
					.getString(R.string.notify_text), PendingIntent
					.getActivity(context, 0, intent,
							PendingIntent.FLAG_CANCEL_CURRENT));
			n.flags |= Notification.FLAG_AUTO_CANCEL;

			final NotificationManager mNotificationMgr = // .
			(NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationMgr.notify(NOTIFICATION_ID, n);
		}
		return ret;
	}
}
