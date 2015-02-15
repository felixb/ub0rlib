/*
 * Copyright (C) 2010 Felix Bechstein
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

package de.ub0r.android.lib.apis;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.People.Extensions;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.Contacts.PresenceColumns;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import de.ub0r.android.lib.Log;

/**
 * Implement {@link ContactsWrapper} for API 3 and 4.
 * 
 * @author flx
 */
@SuppressWarnings("deprecation")
public final class ContactsWrapper3 extends ContactsWrapper {
	/** Tag for output. */
	private static final String TAG = "cw3";

	/** Selection for getting {@link Contact} from number. */
	private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + PhonesColumns.NUMBER
			+ ",?)";
	/** {@link Uri} for getting {@link Contact} from number. */
	private static final Uri PHONES_WITH_PRESENCE_URI = Uri.parse(Contacts.Phones.CONTENT_URI
			+ "_with_presence");
	/** Projection for getting {@link Contact} from number. */
	private static final String[] CALLER_ID_PROJECTION = new String[] { PhonesColumns.NUMBER, // 0
			PeopleColumns.NAME, // 1
			Contacts.Phones.PERSON_ID, // 2
			PresenceColumns.PRESENCE_STATUS, // 3
	};

	/** {@link Uri} for getting {@link Contact} from number. */
	private static final Uri PHONES_WITHOUT_PRESENCE_URI = Contacts.Phones.CONTENT_URI;
	/** Projection for getting {@link Contact} from number. */
	private static final String[] CALLER_ID_PROJECTION_WITHOUT_PRESENCE = new String[] {
			PhonesColumns.NUMBER, // 0
			PeopleColumns.NAME, // 1
			Contacts.Phones.PERSON_ID, // 2
	};

	/** Index in CALLER_ID_PROJECTION: number. */
	private static final int INDEX_CALLER_ID_NUMBER = 0;
	/** Index in CALLER_ID_PROJECTION: name. */
	private static final int INDEX_CALLER_ID_NAME = 1;
	/** Index in CALLER_ID_PROJECTION: id. */
	private static final int INDEX_CALLER_ID_CONTACTID = 2;
	/** Index in CALLER_ID_PROJECTION: presence. */
	private static final int INDEX_CALLER_ID_PRESENCE = 3;

	/** Projection for persons query, filter. */
	private static final String[] PROJECTION_FILTER = new String[] { Extensions.PERSON_ID, // 0
			PeopleColumns.NAME, // 1
			PhonesColumns.NUMBER, // 2
			PhonesColumns.TYPE // 3
	};

	/** Projection for persons query, content. */
	private static final String[] PROJECTION_CONTENT = new String[] { BaseColumns._ID, // 0
			PeopleColumns.NAME, // 1
			PhonesColumns.NUMBER, // 2
			PhonesColumns.TYPE // 3
	};

	/** SQL to select mobile numbers only. */
	private static final String MOBILES_ONLY = PhonesColumns.TYPE + " = "
			+ PhonesColumns.TYPE_MOBILE;

	/** Sort Order. */
	private static final String SORT_ORDER = PeopleColumns.STARRED + " DESC, "
			+ PeopleColumns.TIMES_CONTACTED + " DESC, " + PeopleColumns.NAME + " ASC, "
			+ PhonesColumns.TYPE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getContactUri(final long id) {
		return ContentUris.withAppendedId(People.CONTENT_URI, id);
	}

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
	public Uri getLookupUri(final ContentResolver cr, final String id) {
		return this.getContactUri(null, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bitmap loadContactPhoto(final Context context, final Uri contactUri) {
		if (contactUri == null) {
			return null;
		}
		try {
			return People.loadContactPhoto(context, contactUri, -1, null);
		} catch (Exception e) {
			Log.e(TAG, "error getting photo: " + contactUri, e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor getContact(final ContentResolver cr, final String number) {
		final String n = this.cleanNumber(number);
		if (n == null || n.length() == 0) {
			return null;
		}
		final Uri uri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, n);
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
	protected Cursor getContact(final ContentResolver cr, final Uri uri) {
		Log.d(TAG, "query: " + uri);
		try {
			final String[] p = PROJECTION_FILTER;
			final Cursor c = cr.query(uri, p, null, null, null);
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
		i.putExtra(Contacts.Intents.Insert.PHONE_TYPE, Contacts.PhonesColumns.TYPE_MOBILE);
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showQuickContact(final Context context, final View target, final Uri lookupUri,
			final int mode, final String[] excludeMimes) {
		context.startActivity(new Intent(Intent.ACTION_VIEW, lookupUri));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateContactDetails(final Context context, final boolean loadOnly,
			final boolean loadAvatar, final Contact contact) {
		Log.d(TAG, "updateContactDetails(" + contact.mRecipientId + ")");
		Log.d(TAG, "id: " + contact.mPersonId + ")");
		Log.d(TAG, "number: " + contact.mNumber + ")");
		Log.d(TAG, "name: " + contact.mName + ")");
		boolean changed = false;
		final long rid = contact.mRecipientId;
		final ContentResolver cr = context.getContentResolver();

		// mNumber
		String number = contact.mNumber;
		boolean changedNameAndNumber = false;
		if (rid > 0L && (!loadOnly || number == null)) {
			final Cursor cursor = cr.query(ContentUris.withAppendedId(CANONICAL_ADDRESS, rid),
					null, null, null, null);
			if (cursor.moveToFirst()) {
				number = cursor.getString(0);
				if (number != null && !number.startsWith("000") && number.startsWith("00")) {
					number = number.replaceFirst("^00", "+");
				}
				Log.d(TAG, "found address for " + rid + ": " + number);
				contact.mNumber = number;
				changedNameAndNumber = true;
				changed = true;
			}
			cursor.close();
		}

		// mName + mPersonId + mLookupKey + mPresenceState
		if (number != null && (!loadOnly || contact.mName == null || contact.mPersonId < 0L)) {
			final String n = PhoneNumberUtils.stripSeparators(number);
			if (!TextUtils.isEmpty(n)) {
				Log.d(TAG, "lookup contact: " + n);
				boolean withpresence = true;
				Cursor cursor = null;
				try {
					cursor = cr.query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
							CALLER_ID_SELECTION, new String[] { n }, null);
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "could not query: " + PHONES_WITH_PRESENCE_URI, e);
					Log.i(TAG, "try without presence: " + PHONES_WITHOUT_PRESENCE_URI);
					cursor = cr.query(PHONES_WITHOUT_PRESENCE_URI,
							CALLER_ID_PROJECTION_WITHOUT_PRESENCE, CALLER_ID_SELECTION,
							new String[] { n }, null);
					withpresence = false;
				}
				if (cursor.moveToFirst()) {
					final long pid = cursor.getLong(INDEX_CALLER_ID_CONTACTID);
					final String na = cursor.getString(INDEX_CALLER_ID_NAME);
					final String nu = cursor.getString(INDEX_CALLER_ID_NUMBER);
					int prs = PRESENCE_STATE_UNKNOWN;
					if (withpresence) {
						cursor.getInt(INDEX_CALLER_ID_PRESENCE);
					}
					Log.d(TAG, "id: " + pid);
					Log.d(TAG, "name: " + na);
					Log.d(TAG, "number: " + nu);
					Log.d(TAG, "presence state: " + prs);
					if (pid != contact.mPersonId) {
						contact.mPersonId = pid;
						contact.mLookupKey = String.valueOf(pid);
						changed = true;
					}
					if (na != null && !na.equals(contact.mName)) {
						contact.mName = na;
						changedNameAndNumber = true;
						changed = true;
					}
					if (prs != contact.mPresenceState) {
						contact.mPresenceState = prs;
						changed = true;
					}
				}
				cursor.close();
			}
		}

		// mNameAndNumber
		if (changedNameAndNumber) {
			contact.updateNameAndNumer();
		}
		// mPresenceText;
		contact.mPresenceText = null;

		if (contact.mLookupKey == null && contact.mPersonId >= 0L) {
			contact.mLookupKey = String.valueOf(contact.mPersonId);
			changed = true;
		}

		if (loadAvatar && contact.mPersonId >= 0L) {
			// mAvatar[Data];
			Bitmap b = this.loadContactPhoto(context, this.getContactUri(contact.mPersonId));
			if (b == null) {
				contact.mAvatar = null;
				contact.mAvatarData = null;
			} else {
				contact.mAvatar = new BitmapDrawable(b);
			}
		}
		return changed;
	}
}
