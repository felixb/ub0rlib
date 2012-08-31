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

import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import de.ub0r.android.lib.Log;

/**
 * Helper class to set/unset background for api5 systems.
 * 
 * @author flx
 */
@TargetApi(5)
public final class ContactsWrapper5 extends ContactsWrapper {
	/** Tag for output. */
	private static final String TAG = "cw5";

	/** Uri for getting {@link Contact} from number. */
	private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

	/** Selection for getting {@link Contact} from number. */
	// private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL("
	// + Phone.NUMBER + ",?) AND " + Data.MIMETYPE + "='"
	// + Phone.CONTENT_ITEM_TYPE + "'" + " AND " + Data.RAW_CONTACT_ID
	// + " IN " + "(SELECT raw_contact_id " + " FROM phone_lookup"
	// + " WHERE normalized_number GLOB('+*'))";
	private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
			+ ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";

	/** Projection for getting {@link Contact} from number. */
	private static final String[] CALLER_ID_PROJECTION = new String[] { Phone.NUMBER, // 0
			Phone.DISPLAY_NAME, // 1
			Phone.CONTACT_ID, // 2
			Phone.CONTACT_PRESENCE, // 3
			Phone.CONTACT_STATUS, // 4
			Contacts.LOOKUP_KEY, // 5
	};

	/** Index in CALLER_ID_PROJECTION: number. */
	private static final int INDEX_CALLER_ID_NUMBER = 0;
	/** Index in CALLER_ID_PROJECTION: name. */
	private static final int INDEX_CALLER_ID_NAME = 1;
	/** Index in CALLER_ID_PROJECTION: id. */
	private static final int INDEX_CALLER_ID_CONTACTID = 2;
	/** Index in CALLER_ID_PROJECTION: presence. */
	private static final int INDEX_CALLER_ID_PRESENCE = 3;
	/** Index in CALLER_ID_PROJECTION: status. */
	private static final int INDEX_CALLER_ID_STATUS = 4;
	/** Index in CALLER_ID_PROJECTION: lookup key. */
	private static final int INDEX_CALLER_ID_LOOKUP_KEY = 5;

	/** Projection for persons query, filter. */
	private static final String[] PROJECTION_FILTER = new String[] { Phone.LOOKUP_KEY, // 0
			Data.DISPLAY_NAME, // 1
			Phone.NUMBER, // 2
			Phone.TYPE // 3
	};

	/** Projection for persons query, show. */
	private static final String[] PROJECTION_CONTENT = new String[] { BaseColumns._ID, // 0
			Data.DISPLAY_NAME, // 1
			Phone.NUMBER, // 2
			Phone.TYPE // 3
	};

	/** SQL to select mobile numbers only. */
	private static final String MOBILES_ONLY = Phone.TYPE + " = " + Phone.TYPE_MOBILE;

	/** Sort Order. */
	private static final String SORT_ORDER = Phone.STARRED + " DESC, " + Phone.TIMES_CONTACTED
			+ " DESC, " + Phone.DISPLAY_NAME + " ASC, " + Phone.TYPE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bitmap loadContactPhoto(final Context context, final Uri contactUri) {
		Log.d(TAG, "loadContactPhoto(ctx, " + contactUri + ")");
		if (contactUri == null) {
			return null;
		}
		try {
			final ContentResolver cr = context.getContentResolver();
			InputStream is = Contacts.openContactPhotoInputStream(cr, contactUri);
			if (is == null) {
				Log.d(TAG, "no photo for: " + contactUri);
				return null;
			}
			return BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			Log.e(TAG, "error getting photo: " + contactUri, e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getContactUri(final long id) {
		return ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri getContactUri(final ContentResolver cr, final String id) {
		if (id == null) {
			return null;
		}
		Log.d(TAG, "getContactUri(cr, " + id + ")");
		try {
			final Uri ret = Contacts.lookupContact(cr,
					Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id));
			Log.d(TAG, "found uri: " + ret);
			return ret;
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
		Log.d(TAG, "getLookupUri(cr, " + id + ")");
		if (TextUtils.isEmpty(id)) {
			return null;
		}
		Uri ret = null;
		final Cursor c = cr.query(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id),
				new String[] { Contacts.LOOKUP_KEY, BaseColumns._ID }, null, null, BaseColumns._ID
						+ " ASC");
		if (c != null) {
			if (c.moveToFirst()) {
				ret = Contacts.getLookupUri(c.getLong(1), id);
			}
			c.close();
		}
		if (ret == null) {
			ret = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, id);
		}
		Log.d(TAG, "found uri: " + ret);
		return ret;
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
		Uri uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, n);
		// FIXME: this is broken in android os; issue #8255
		Log.d(TAG, "query: " + uri);
		Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
		if (c != null && c.moveToFirst()) {
			return c;
		}
		// Fallback to API3
		c = new ContactsWrapper3().getContact(cr, n);
		if (c != null && c.moveToFirst()) {
			// get orig API5 cursor for the real number
			final String where = PROJECTION_FILTER[FILTER_INDEX_NUMBER] + " = '"
					+ c.getString(FILTER_INDEX_NUMBER) + "'";
			Log.d(TAG, "query: " + Phone.CONTENT_URI + " # " + where);
			Cursor c0 = cr.query(Phone.CONTENT_URI, PROJECTION_FILTER, where, null, null);
			if (c0 != null && c0.moveToFirst()) {
				Log.d(TAG, "id: " + c0.getString(FILTER_INDEX_ID));
				Log.d(TAG, "name: " + c0.getString(FILTER_INDEX_NAME));
				Log.d(TAG, "number: " + c0.getString(FILTER_INDEX_NUMBER));
				return c0;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Cursor getContact(final ContentResolver cr, final Uri uri) {
		// FIXME: this is broken in android os; issue #8255
		Log.d(TAG, "query: " + uri);
		try {
			final Cursor c = cr.query(uri, PROJECTION_FILTER, null, null, null);
			if (c != null && c.moveToFirst()) {
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
		return Phone.CONTENT_URI;
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

	@Override
	public String getContentWhere(final String filter) {
		String f = DatabaseUtils.sqlEscapeString('%' + filter.toString() + '%');
		StringBuilder s = new StringBuilder();
		s.append("(" + ContactsContract.Data.DISPLAY_NAME + " LIKE ");
		s.append(f);
		s.append(") OR (" + Phone.DATA1 + " LIKE ");
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
		i.setType(Phone.CONTENT_ITEM_TYPE);
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Intent getInsertPickIntent(final String address) {
		final Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
		i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
		i.putExtra(ContactsContract.Intents.Insert.PHONE, address);
		i.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, Phone.TYPE_MOBILE);
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showQuickContact(final Context context, final View target, final Uri lookupUri,
			final int mode, final String[] excludeMimes) {
		ContactsContract.QuickContact.showQuickContact(context, target, lookupUri, mode,
				excludeMimes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateContactDetails(final Context context, final boolean loadOnly,
			final boolean loadAvatar, final Contact contact) {
		if (contact == null) {
			Log.w(TAG, "updateContactDetails(null)");
			return false;
		}
		Log.d(TAG, "updateContactDetails(" + contact.mRecipientId + ")");
		Log.d(TAG, "id: " + contact.mPersonId);
		Log.d(TAG, "number: " + contact.mNumber);
		Log.d(TAG, "name: " + contact.mName);
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

		// mName + mPersonId + mLookupKey + mPresenceState + mPresenceText
		if (number != null && (!loadOnly || contact.mName == null || contact.mPersonId < 0L)) {
			final String n = PhoneNumberUtils.toCallerIDMinMatch(number);
			if (!TextUtils.isEmpty(n)) {
				final String selection = CALLER_ID_SELECTION.replace("+", n);
				Log.d(TAG, "sel: " + selection);
				final Cursor cursor = cr.query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
						selection, new String[] { number }, null);
				Log.d(TAG, "result: " + cursor.getCount());

				if (cursor.moveToFirst()) {
					final long pid = cursor.getLong(INDEX_CALLER_ID_CONTACTID);
					final String lookup = cursor.getString(INDEX_CALLER_ID_LOOKUP_KEY);
					final String na = cursor.getString(INDEX_CALLER_ID_NAME);
					final String nu = cursor.getString(INDEX_CALLER_ID_NUMBER);
					final int prs = cursor.getInt(INDEX_CALLER_ID_PRESENCE);
					final String prt = cursor.getString(INDEX_CALLER_ID_STATUS);
					Log.d(TAG, "id: " + pid);
					Log.d(TAG, "name: " + na);
					Log.d(TAG, "number: " + nu);
					Log.d(TAG, "presence state: " + prs);
					Log.d(TAG, "presence text: " + prt);
					if (pid != contact.mPersonId) {
						contact.mPersonId = pid;
						changed = true;
					}
					if (lookup != null && !lookup.equals(contact.mLookupKey)) {
						contact.mLookupKey = lookup;
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
					if (prt != null && !prt.equals(contact.mPresenceText)) {
						contact.mPresenceText = prt;
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

		if (loadAvatar && contact.mAvatarData == null && contact.mAvatar == null) {
			final byte[] data = this.loadAvatarData(context, contact);
			synchronized (contact) {
				if (data != null) {
					contact.mAvatarData = data;
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Load the avatar data from the cursor into memory. Don't decode the data until someone calls
	 * for it (see getAvatar). Hang onto the raw data so that we can compare it when the data is
	 * reloaded. TODO: consider comparing a checksum so that we don't have to hang onto the raw
	 * bytes after the image is decoded.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param contact
	 *            {@link Contact}
	 * @return avatar as byte[]
	 */
	private byte[] loadAvatarData(final Context context, final Contact contact) {
		byte[] data = null;

		if (contact.mPersonId <= 0L || contact.mAvatar != null) {
			return null;
		}

		final Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact.mPersonId);

		final InputStream avatarDataStream = Contacts.openContactPhotoInputStream(
				context.getContentResolver(), contactUri);
		try {
			if (avatarDataStream != null) {
				data = new byte[avatarDataStream.available()];
				avatarDataStream.read(data, 0, data.length);
			}
		} catch (IOException e) {
			Log.e(TAG, "error recoding stream", e);
		} finally {
			try {
				if (avatarDataStream != null) {
					avatarDataStream.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "error closing stream", e);
			}
		}

		return data;
	}
}
