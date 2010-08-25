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

package de.ub0r.android.lib.apis;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.Contacts.People.Extensions;
import android.view.View;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.R;

/**
 * Implement {@link ContactsWrapper} for API 3 and 4.
 * 
 * @author flx
 */
@SuppressWarnings("deprecation")
public final class ContactsWrapper3 extends ContactsWrapper {
	/** Tag for output. */
	private static final String TAG = "cw3";

	/** Projection for persons query, filter. */
	private static final String[] PROJECTION_FILTER = // .
	new String[] { Extensions.PERSON_ID, // 0
			Extensions.PERSON_ID, // 1
			PeopleColumns.NAME, // 2
			PhonesColumns.NUMBER, // 3
			PhonesColumns.TYPE // 4
	};

	/** Projection for persons query, content. */
	private static final String[] PROJECTION_CONTENT = // .
	new String[] { BaseColumns._ID, // 0
			PeopleColumns.NAME, // 1
			PhonesColumns.NUMBER, // 2
			PhonesColumns.TYPE // 3
	};

	/** SQL to select mobile numbers only. */
	private static final String MOBILES_ONLY = // .
	PhonesColumns.TYPE + " = " + PhonesColumns.TYPE_MOBILE;

	/** Sort Order. */
	private static final String SORT_ORDER = PeopleColumns.STARRED + " DESC, "
			+ PeopleColumns.TIMES_CONTACTED + " DESC, " + PeopleColumns.NAME
			+ " ASC, " + PhonesColumns.TYPE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getContactUri(final ContentResolver cr, final String id) {
		try {
			return Uri.withAppendedPath(People.CONTENT_URI, id);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "unable to get uri for id: " + id, e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getLookupUri(final String id, final String rid) {
		return this.getContactUri(null, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bitmap loadContactPhoto(final Context context, // .
			final String contactId) {
		if (contactId == null || contactId.length() == 0) {
			return null;
		}
		try {
			Uri uri = Uri.withAppendedPath(People.CONTENT_URI, contactId);
			return People.loadContactPhoto(context, uri,
					R.drawable.ic_contact_picture, null);
		} catch (Exception e) {
			Log.e(TAG, "error getting photo: " + contactId, e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor getContact(final ContentResolver cr, // .
			final String number) {
		final String n = this.cleanNumber(number);
		if (n == null || n.length() == 0) {
			return null;
		}
		final Uri uri = Uri.withAppendedPath(
				Contacts.Phones.CONTENT_FILTER_URL, n);
		Log.d(TAG, "query: " + uri);
		final Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
		if (c != null && c.moveToFirst()) {
			Log.d(TAG, "id: " + c.getString(FILTER_INDEX_ID));
			Log.d(TAG, "name: " + c.getString(FILTER_INDEX_NAME));
			Log.d(TAG, "number: " + c.getString(FILTER_INDEX_NUMBER));
			return c;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Cursor getContact(final ContentResolver cr, // .
			final Uri uri) {
		Log.d(TAG, "query: " + uri);
		try {
			final Cursor c = cr.query(uri, PROJECTION_FILTER, null, null,
					null);
			if (c.moveToFirst()) {
				Log.d(TAG, "id: " + c.getString(FILTER_INDEX_ID));
				Log.d(TAG, "name: " + c.getString(FILTER_INDEX_NAME));
				Log.d(TAG, "number: " + c.getString(FILTER_INDEX_NUMBER));
				return c;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "error fetching contact: " + uri, e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getContentUri() {
		return Contacts.Phones.CONTENT_URI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getContentProjection() {
		return PROJECTION_CONTENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMobilesOnlyString() {
		return MOBILES_ONLY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentSort() {
		return SORT_ORDER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentWhere(final String filter) {
		String f = DatabaseUtils.sqlEscapeString('%' + filter.toString() + '%');
		StringBuilder s = new StringBuilder();
		s.append("(" + PeopleColumns.NAME + " LIKE ");
		s.append(f);
		s.append(") OR (" + PhonesColumns.NUMBER + " LIKE ");
		s.append(f);
		s.append(")");
		return s.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Intent getPickPhoneIntent() {
		final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType(Phones.CONTENT_ITEM_TYPE);
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Intent getInsertPickIntent(final String address) {
		final Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
		i.setType(People.CONTENT_ITEM_TYPE);
		i.putExtra(Contacts.Intents.Insert.PHONE, address);
		i.putExtra(Contacts.Intents.Insert.PHONE_TYPE,
				Contacts.PhonesColumns.TYPE_MOBILE);
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showQuickContact(final Context context, final View target,
			final Uri lookupUri, final int mode, final String[] excludeMimes) {
		context.startActivity(new Intent(Intent.ACTION_VIEW, lookupUri));
	}
}
