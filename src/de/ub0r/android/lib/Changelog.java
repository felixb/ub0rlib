/*
 * Copyright (C) 2009-2011 Felix Bechstein
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

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Display change log {@link ListActivity}.
 * 
 * @author flx
 */
public final class Changelog extends ListActivity implements OnClickListener {
	/** Tag for output. */
	public static final String TAG = "cl";

	/** Prefs: name for last version run. */
	private static final String PREFS_LAST_RUN = "lastrun";
	/** Prefs: name for never show changelog again. */
	private static final String PREFS_HIDE = "changelog_hide";
	/** Prefs: name for last read notes. */
	private static final String PREFS_LAST_READ = "notes_lastread";

	/** {@link Intent}'s extra for additional text on top. */
	private static final String EXTRA_TEXT = "extra_text";
	/** {@link Intent}'s extra for additional button on bottom. */
	private static final String EXTRA_BUTTON = "extra_button";
	/** {@link Intent}'s extra for additional button's intent. */
	private static final String EXTRA_INTENT = "extra_intent";
	/** {@link Intent}'s extra for mode. */
	private static final String EXTRA_MODE = "extra_mode";
	/** {@link Intent}'s mode: change log. */
	private static final int MODE_CHANGELOG = 1;
	/** {@link Intent}'s mode: notes from dev. */
	private static final int MODE_NOTES = 2;

	/** Notification id: change log. */
	private static final int NOTIFICATION_CHANGELOG = 134;
	/** Notification id: notes. */
	private static final int NOTIFICATION_NOTES = 135;

	/** Mode of operating. */
	private int mode = 0;

	/**
	 * Check if a new version of this app is running.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return true on first run after update.
	 */
	public static boolean isNewVersion(final Context context) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String v0 = p.getString(PREFS_LAST_RUN, "");
		final String v1 = context.getString(R.string.app_version);
		if (v0.equals(v1)) {
			return false;
		}
		return true;
	}

	/**
	 * Show change log {@link ListActivity} if needed.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public static void showChangelog(final Context context) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String v0 = p.getString(PREFS_LAST_RUN, "");
		final String v1 = context.getString(R.string.app_version);

		p.edit().putString(PREFS_LAST_RUN, v1).commit();
		if (p.getBoolean(PREFS_HIDE, false)) {
			Log.d(TAG, "hide changelog");
			return;
		}
		if (v0.length() == 0) {
			Log.d(TAG, "first boot, skip changelog");
			return;
		}
		if (v0.equals(v1)) {
			Log.d(TAG, "no changes");
			return;
		}

		final String appv = context.getString(R.string.app_name) + " v"
				+ context.getString(R.string.app_version);
		final String t = context.getString(R.string.changelog_notification_)
				+ " " + appv;
		final String tt = context
				.getString(R.string.changelog_notification_text)
				+ " " + appv;

		final Intent i = new Intent(context, Changelog.class);
		i.setAction("changelog");
		i.putExtra(EXTRA_MODE, MODE_CHANGELOG);
		final PendingIntent pi = PendingIntent.getActivity(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		final NotificationManager nmgr = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		final Notification n = new Notification(
				android.R.drawable.stat_sys_warning, t, 0);
		n.setLatestEventInfo(context, t, tt, pi);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nmgr.notify(NOTIFICATION_CHANGELOG, n);
	}

	/**
	 * Show notes from developer {@link ListActivity} if needed.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param btnText
	 *            text of the extra {@link Button}
	 * @param extraText
	 *            text shown on top of the list
	 * @param btnIntent
	 *            {@link Intent} fired when pressing the {@link Button}
	 */
	public static void showNotes(final Context context, final String btnText,
			final String extraText, final Intent btnIntent) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final int lastRead = p.getInt(PREFS_LAST_READ, 0);
		final String[] notes = context.getResources().getStringArray(
				R.array.notes_from_dev);
		final int l = notes.length;
		if (l == 0 || lastRead == l) {
			// all notes are read
			Log.d(TAG, "all news read: " + l);
			return;
		}

		final String t = context.getString(R.string.notes_from_developer_);
		final String tt = context.getString(R.string.notes_from_developer_text);

		final Intent i = new Intent(context, Changelog.class);
		i.setAction("notes");
		i.putExtra(EXTRA_MODE, MODE_NOTES);
		if (!TextUtils.isEmpty(btnText)) {
			i.putExtra(EXTRA_BUTTON, btnText);
			i.putExtra(EXTRA_INTENT, btnIntent);
		}
		if (!TextUtils.isEmpty(extraText)) {
			i.putExtra(EXTRA_TEXT, extraText);
		}
		final PendingIntent pi = PendingIntent.getActivity(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		final NotificationManager nmgr = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		final Notification n = new Notification(
				android.R.drawable.stat_sys_warning, t, 0);
		n.setLatestEventInfo(context, t, tt, pi);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nmgr.notify(NOTIFICATION_NOTES, n);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (Utils.isApi(Build.VERSION_CODES.HONEYCOMB)) {
			this.setTheme(android.R.style.Theme_Holo_Light);
		} else {
			this.setTheme(android.R.style.Theme_Light);
		}
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.list);
		final Intent intent = this.getIntent();

		this.findViewById(R.id.ok).setOnClickListener(this);
		final Button btnExtra = (Button) this.findViewById(R.id.extra);
		btnExtra.setOnClickListener(this);
		String s = intent.getStringExtra(EXTRA_BUTTON);
		if (TextUtils.isEmpty(s)) {
			btnExtra.setVisibility(View.GONE);
		} else {
			btnExtra.setText(s);
			btnExtra.setVisibility(View.VISIBLE);
		}
		s = null;

		this.mode = intent.getIntExtra(EXTRA_MODE, 0);
		if (this.mode == MODE_CHANGELOG) {
			final String appn = this.getString(R.string.app_name);
			this.setTitle(this.getString(R.string.changelog_) + ": " + appn
					+ " v" + this.getString(R.string.app_version));

			final String[] changes = this.getResources().getStringArray(
					R.array.updates);
			final int l = changes.length;
			final Spanned[] schanges = new Spanned[l];
			for (int i = 0; i < l; i++) {
				s = changes[i];
				s = "<b>" + appn + " " + s;
				s = s.replaceFirst(": ", "</b><br>\n* ");
				s = s.replaceAll(", ", "<br>\n* ");
				schanges[i] = Html.fromHtml(s);
			}
			final ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
					this, R.layout.list_item, schanges);
			this.setListAdapter(adapter);
		} else if (this.mode == MODE_NOTES) {
			this.findViewById(R.id.hide).setVisibility(View.GONE);
			this.setTitle(R.string.notes_from_developer_);

			final String[] notes = this.getResources().getStringArray(
					R.array.notes_from_dev);
			final int l = notes.length;
			final ArrayList<Spanned> snotes = new ArrayList<Spanned>();
			s = intent.getStringExtra(EXTRA_TEXT);
			if (!TextUtils.isEmpty(s)) {
				s = "<b>" + s;
				s = s.replaceFirst(": ", "</b><br>\n");
				snotes.add(Html.fromHtml(s));
			}
			for (int i = 0; i < l; i++) {
				s = notes[i];
				if (s.trim().length() == 0) {
					continue;
				}
				s = "<b>" + s;
				s = s.replaceFirst(": ", "</b><br>\n");
				snotes.add(0, Html.fromHtml(s));
			}
			final ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
					this, R.layout.list_item, snotes);
			this.setListAdapter(adapter);
		} else {
			this.finish();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.ok:
			final SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(this);
			final Editor ed = p.edit();
			if (this.mode == MODE_CHANGELOG) {
				ed.putBoolean(PREFS_HIDE, ((CheckBox) this
						.findViewById(R.id.hide)).isChecked());
			} else if (this.mode == MODE_NOTES) {
				ed.putInt(PREFS_LAST_READ, this.getResources().getStringArray(
						R.array.notes_from_dev).length);
			}
			ed.commit();
			this.finish();
			break;
		case R.id.extra:
			final Intent i = (Intent) this.getIntent().getParcelableExtra(
					EXTRA_INTENT);
			try {
				this.startActivity(i);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "could not launch intent: " + i);
				Toast.makeText(this, "could not launch activity",
						Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	/**
	 *{@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected(" + item.getItemId() + ")");
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return false;
		}
	}
}
