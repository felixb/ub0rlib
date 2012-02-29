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

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

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

	/**
	 * Common {@link OnClickListener}.
	 * 
	 * @author flx
	 */
	static class InnerOnClickListener implements OnClickListener {
		/** Target {@link Activity}. */
		private final Activity activity;
		/** Mode of operating. */
		private final int mode;

		/**
		 * Default Constructor.
		 * 
		 * @param target
		 *            target {@link Activity}
		 * @param m
		 *            mode
		 */
		public InnerOnClickListener(final Activity target, final int m) {
			this.activity = target;
			this.mode = m;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(final View v) {
			if (v.getId() == R.id.ok) {
				final SharedPreferences p = PreferenceManager
						.getDefaultSharedPreferences(this.activity);
				final Editor ed = p.edit();
				if (this.mode == MODE_CHANGELOG) {
					ed.putBoolean(PREFS_HIDE,
							((CheckBox) this.activity.findViewById(R.id.hide)).isChecked());
				} else if (this.mode == MODE_NOTES) {
					ed.putInt(
							PREFS_LAST_READ,
							this.activity.getResources().getStringArray(R.array.notes_from_dev).length);
				}
				ed.commit();
				this.activity.finish();
			} else if (v.getId() == R.id.extra) {
				final Intent i = (Intent) this.activity.getIntent()
						.getParcelableExtra(EXTRA_INTENT);
				try {
					this.activity.startActivity(i);
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "could not launch intent: " + i);
					Toast.makeText(this.activity, "could not launch activity", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Log.w(TAG, "unexpected id in onClick()");
			}
		}
	}

	/**
	 * Default Constructor.
	 */
	private ChangelogHelper() {
		// nothing to do
	}

	/**
	 * Get {@link Intent} to Changelog {@link Activity}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param icsStyle
	 *            use HC/ICS Style ~ @return {@link} {@link Intent}
	 */
	private static Intent getChangelogIntent(final Context context, final boolean icsStyle) {
		return new Intent(context, ChangelogActivity.class);
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
		final String v1 = context.getString(R.string.app_version);
		if (v0.equals(v1)) {
			return false;
		}
		return true;
	}

	/**
	 * Show change log if needed.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param icsStyle
	 *            use HC/ICS Style
	 */
	public static void showChangelog(final Context context, final boolean icsStyle) {
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
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
		final String t = context.getString(R.string.changelog_notification_) + " " + appv;
		final String tt = context.getString(R.string.changelog_notification_text) + " " + appv;

		final Intent i = getChangelogIntent(context, icsStyle);
		i.setAction("changelog");
		i.putExtra(EXTRA_MODE, MODE_CHANGELOG);
		final PendingIntent pi = PendingIntent.getActivity(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		final NotificationManager nmgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification n = new Notification(android.R.drawable.stat_sys_warning, t, 0);
		n.setLatestEventInfo(context, t, tt, pi);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nmgr.notify(NOTIFICATION_CHANGELOG, n);
	}

	/**
	 * Show notes from developer if needed.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param icsStyle
	 *            use HC/ICS Style
	 * @param btnText
	 *            text of the extra {@link Button}
	 * @param extraText
	 *            text shown on top of the list
	 * @param btnIntent
	 *            {@link Intent} fired when pressing the {@link Button}
	 */
	public static void showNotes(final Context context, final boolean icsStyle,
			final String btnText, final String extraText, final Intent btnIntent) {
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		final int lastRead = p.getInt(PREFS_LAST_READ, 0);
		final String[] notes = context.getResources().getStringArray(R.array.notes_from_dev);
		final int l = notes.length;
		if (l == 0 || lastRead == l) {
			// all notes are read
			Log.d(TAG, "all news read: " + l);
			p.edit().putInt(PREFS_LAST_READ, l).commit();
			return;
		}

		final String t = context.getString(R.string.notes_from_developer_);
		final String tt = context.getString(R.string.notes_from_developer_text);

		final Intent i = getChangelogIntent(context, icsStyle);
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
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification n = new Notification(android.R.drawable.stat_sys_warning, t, 0);
		n.setLatestEventInfo(context, t, tt, pi);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		nmgr.notify(NOTIFICATION_NOTES, n);
	}

	/**
	 * Common onCreate().
	 * 
	 * @param target
	 *            {@link Activity}
	 */
	static void onCreate(final Activity target) {
		target.setContentView(R.layout.changelog_list);
		final Intent intent = target.getIntent();
		final int mode = intent.getIntExtra(EXTRA_MODE, 0);

		InnerOnClickListener ocl = new InnerOnClickListener(target, mode);

		target.findViewById(R.id.ok).setOnClickListener(ocl);
		final Button btnExtra = (Button) target.findViewById(R.id.extra);
		btnExtra.setOnClickListener(ocl);
		String s = intent.getStringExtra(EXTRA_BUTTON);
		if (TextUtils.isEmpty(s)) {
			btnExtra.setVisibility(View.GONE);
		} else {
			btnExtra.setText(s);
			btnExtra.setVisibility(View.VISIBLE);
		}
		s = null;

		if (mode == MODE_CHANGELOG) {
			final String appn = target.getString(R.string.app_name);
			target.setTitle(target.getString(R.string.changelog_) + ": " + appn + " v"
					+ target.getString(R.string.app_version));

			final String[] changes = target.getResources().getStringArray(R.array.updates);
			final int l = changes.length;
			final Spanned[] schanges = new Spanned[l];
			for (int i = 0; i < l; i++) {
				s = changes[i];
				s = "<b>" + appn + " " + s;
				s = s.replaceFirst(": ", "</b><br>\n* ");
				s = s.replaceAll(", ", "<br>\n* ");
				schanges[i] = Html.fromHtml(s);
			}
			final ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(target,
					R.layout.changelog_list_item, schanges);
			((ListView) target.findViewById(android.R.id.list)).setAdapter(adapter);
		} else if (mode == MODE_NOTES) {
			target.findViewById(R.id.hide).setVisibility(View.GONE);
			target.setTitle(R.string.notes_from_developer_);

			final String[] notes = target.getResources().getStringArray(R.array.notes_from_dev);
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
			final ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(target,
					R.layout.changelog_list_item, snotes);
			((ListView) target.findViewById(android.R.id.list)).setAdapter(adapter);
		} else {
			target.finish();
		}
	}
}
