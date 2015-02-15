/*
 * Copyright (C) 2010 Felix Bechstein, The Android Open Source Project
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

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;

/**
 * Wrap around contacts API.
 * 
 * @author flx
 */
public abstract class ContactsWrapper {
	/** Tag for output. */
	private static final String TAG = "cw";

	/** Index of id/lookup key. */
	public static final int FILTER_INDEX_ID = 0;
	/** Index of name. */
	public static final int FILTER_INDEX_NAME = 1;
	/** Index of number. */
	public static final int FILTER_INDEX_NUMBER = 2;
	/** Index of type. */
	public static final int FILTER_INDEX_TYPE = 3;

	/** Index of id/lookup key. */
	public static final int CONTENT_INDEX_ID = 0;
	/** Index of name. */
	public static final int CONTENT_INDEX_NAME = 1;
	/** Index of number. */
	public static final int CONTENT_INDEX_NUMBER = 2;
	/** Index of type. */
	public static final int CONTENT_INDEX_TYPE = 3;

	/** Presence's state: unavailable. */
	public static final int PRESENCE_STATE_UNKNOWN = -1;
	/** Presence's state: available. */
	public static final int PRESENCE_STATE_AVAILABLE = 5;
	/** Presence's state: away. */
	public static final int PRESENCE_STATE_AWAY = 2;
	/** Presence's state: do not disturb. */
	public static final int PRESENCE_STATE_DO_NOT_DISTURB = 4;
	/** Presence's state: idle. */
	public static final int PRESENCE_STATE_IDLE = 3;
	/** Presence's state: invisible. */
	public static final int PRESENCE_STATE_INVISIBLE = 1;
	/** Presence's state: offline. */
	public static final int PRESENCE_STATE_OFFLINE = 0;

	/** Uri to fetch addresses. */
	protected static final Uri CANONICAL_ADDRESS = Uri.parse("content://mms-sms/canonical-address");

	/**
	 * Static singleton instance of {@link ContactsWrapper} holding the SDK-specific implementation
	 * of the class.
	 */
	private static ContactsWrapper sInstance;

	/**
	 * Get instance.
	 * 
	 * @return {@link ContactsWrapper}
	 */
	public static final ContactsWrapper getInstance() {
		if (sInstance == null) {
			if (Utils.isApi(Build.VERSION_CODES.ECLAIR)) {
				sInstance = new ContactsWrapper5();
			} else {
				sInstance = new ContactsWrapper3();
			}
			Log.d(TAG, "getInstance(): " + sInstance.getClass().getName());
		}
		return sInstance;
	}

	/**
	 * Get {@link Uri} for filter contacts by address.
	 * 
	 * @return {@link Uri}
	 */
	public abstract Uri getContentUri();

	/**
	 * Get projection for filter contacts by address.
	 * 
	 * @return projection
	 */
	public abstract String[] getContentProjection();

	/**
	 * Get sort order for filter contacts.
	 * 
	 * @return sort
	 */
	public abstract String getContentSort();

	/**
	 * Get WHERE for filter.
	 * 
	 * @param filter
	 *            filter
	 * @return WHERE
	 */
	public abstract String getContentWhere(final String filter);

	/**
	 * Get {@link String} selecting mobiles only.
	 * 
	 * @return mobiles only {@link String}
	 */
	public abstract String getMobilesOnlyString();

	/**
	 * Load ContactPhoto from database.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param contactUri
	 *            uri of contact
	 * @return {@link Bitmap}
	 */
	public abstract Bitmap loadContactPhoto(final Context context, final Uri contactUri);

	/**
	 * Get {@link Uri} to a Contact.
	 * 
	 * @param id
	 *            id of contact
	 * @return {@link Uri}
	 */
	public abstract Uri getContactUri(final long id);

	/**
	 * Get {@link Uri} to a Contact.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param id
	 *            id of contact
	 * @return {@link Uri}
	 */
	public abstract Uri getContactUri(final ContentResolver cr, final String id);

	/**
	 * Get LookUp {@link Uri} to a Contact.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param id
	 *            id/lookup key of contact
	 * @return {@link Uri}
	 */
	public abstract Uri getLookupUri(final ContentResolver cr, final String id);

	/**
	 * Get a {@link Cursor} with <id,name,number> for a given number.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param number
	 *            number to look for
	 * @return a {@link Cursor} matching the number
	 */
	public abstract Cursor getContact(final ContentResolver cr, final String number);

	/**
	 * Get a {@link Cursor} with <id,name,number> for a given number.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param uri
	 *            {@link Uri} to get the contact from
	 * @return a {@link Cursor} matching the number
	 */
	protected abstract Cursor getContact(final ContentResolver cr, final Uri uri);

	/**
	 * Pick a Contact's phone.
	 * 
	 * @return {@link Intent}
	 */
	public abstract Intent getPickPhoneIntent();

	/**
	 * Insert or pick a Contact to add this address to.
	 * 
	 * @param address
	 *            address
	 * @return {@link Intent}
	 */
	public abstract Intent getInsertPickIntent(final String address);

	/**
	 * Trigger a dialog that lists the various methods of interacting with the requested Contacts
	 * entry.
	 * 
	 * @param context
	 *            The parent Context that may be used as the parent for this dialog.
	 * @param target
	 *            Specific View from your layout that this dialog should be centered around. In
	 *            particular, if the dialog has a "callout" arrow, it will be pointed and centered
	 *            around this View.
	 * @param lookupUri
	 *            A CONTENT_LOOKUP_URI style Uri that describes a specific contact to feature in
	 *            this dialog.
	 * @param mode
	 *            Any of MODE_SMALL, MODE_MEDIUM, or MODE_LARGE, indicating the desired dialog size,
	 *            when supported.
	 * @param excludeMimes
	 *            Optional list of MIMETYPE MIME-types to exclude when showing this dialog. For
	 *            example, when already viewing the contact details card, this can be used to omit
	 *            the details entry from the dialog.
	 */
	public abstract void showQuickContact(final Context context, final View target,
			final Uri lookupUri, final int mode, final String[] excludeMimes);

	/**
	 * Trigger a dialog that lists the various methods of interacting with the requested Contacts
	 * entry. If QuickContact is unavailable, the contact app gets started.
	 * 
	 * @param context
	 *            The parent Context that may be used as the parent for this dialog.
	 * @param target
	 *            Specific View from your layout that this dialog should be centered around. In
	 *            particular, if the dialog has a "callout" arrow, it will be pointed and centered
	 *            around this View.
	 * @param lookupUri
	 *            A CONTENT_LOOKUP_URI style Uri that describes a specific contact to feature in
	 *            this dialog.
	 * @param mode
	 *            Any of MODE_SMALL, MODE_MEDIUM, or MODE_LARGE, indicating the desired dialog size,
	 *            when supported.
	 * @param excludeMimes
	 *            Optional list of MIMETYPE MIME-types to exclude when showing this dialog. For
	 *            example, when already viewing the contact details card, this can be used to omit
	 *            the details entry from the dialog.
	 */
	public final void showQuickContactFallBack(final Context context, final View target,
			final Uri lookupUri, final int mode, final String[] excludeMimes) {
		if (lookupUri == null) {
			return;
		}
		try {
			ContactsWrapper.this.showQuickContact(context, target, lookupUri, mode, null);
		} catch (Exception e) {
			Log.e(TAG, "error showing QuickContact", e);
			try {
				context.startActivity(new Intent(Intent.ACTION_VIEW, lookupUri));
			} catch (ActivityNotFoundException e1) {
				Log.e(TAG, "error showing contact", e1);
			}
		}
	}

	/**
	 * Update {@link Contact}'s details.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param loadOnly
	 *            load only data which is not available
	 * @param loadAvatar
	 *            load avatar?
	 * @param contact
	 *            {@link Contact}
	 * @return true if {@link Contact}'s details where changed
	 */
	public abstract boolean updateContactDetails(final Context context, final boolean loadOnly,
			final boolean loadAvatar, final Contact contact);

	/**
	 * Get a QuickContact dialog for a given number.
	 * 
	 * @param context
	 *            The parent Context that may be used as the parent for this dialog.
	 * @param target
	 *            Specific View from your layout that this dialog should be centered around. In
	 *            particular, if the dialog has a "callout" arrow, it will be pointed and centered
	 *            around this View.
	 * @param uri
	 *            {@link Uri} for {@link Contact}.
	 * @param mode
	 *            Any of MODE_SMALL, MODE_MEDIUM, or MODE_LARGE, indicating the desired dialog size,
	 *            when supported.
	 * @param excludeMimes
	 *            Optional list of MIMETYPE MIME-types to exclude when showing this dialog. For
	 *            example, when already viewing the contact details card, this can be used to omit
	 *            the details entry from the dialog.
	 * @return {@link OnClickListener} spawning the dialog.
	 */
	public final OnClickListener getQuickContact(final Context context, final View target,
			final Uri uri, final int mode, final String[] excludeMimes) {
		if (uri == null) {
			return null;
		}
		final OnClickListener ret = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				ContactsWrapper.this.showQuickContactFallBack(context, target, uri, mode, null);
			}
		};
		return ret;
	}

	/**
	 * Get a QuickContact dialog for a given number.
	 * 
	 * @param context
	 *            The parent Context that may be used as the parent for this dialog.
	 * @param target
	 *            Specific View from your layout that this dialog should be centered around. In
	 *            particular, if the dialog has a "callout" arrow, it will be pointed and centered
	 *            around this View.
	 * @param number
	 *            Number of contact.
	 * @param mode
	 *            Any of MODE_SMALL, MODE_MEDIUM, or MODE_LARGE, indicating the desired dialog size,
	 *            when supported.
	 * @param excludeMimes
	 *            Optional list of MIMETYPE MIME-types to exclude when showing this dialog. For
	 *            example, when already viewing the contact details card, this can be used to omit
	 *            the details entry from the dialog.
	 * @return {@link OnClickListener} spawning the dialog.
	 */
	public final OnClickListener getQuickContact(final Context context, final View target,
			final String number, final int mode, final String[] excludeMimes) {
		if (number == null) {
			return null;
		}
		final OnClickListener ret = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				Uri u = ContactsWrapper.this.getLookupKeyForNumber(context.getContentResolver(),
						number);
				ContactsWrapper.this.showQuickContactFallBack(context, target, u, mode, null);
			}
		};
		return ret;
	}

	/**
	 * Get a Name for a given number.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param number
	 *            number to look for
	 * @return name matching the number
	 */
	public final String getNameForNumber(final ContentResolver cr, final String number) {
		final Cursor c = this.getContact(cr, number);
		if (c != null) {
			final String ret = c.getString(FILTER_INDEX_NAME);
			c.close();
			return ret;
		}
		return null;
	}

	/**
	 * Get a id for a given number.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param number
	 *            number to look for
	 * @return id matching the number
	 */
	public final String getIdForNumber(final ContentResolver cr, final String number) {
		final Cursor c = this.getContact(cr, number);
		if (c != null) {
			final String ret = c.getString(FILTER_INDEX_ID);
			c.close();
			return ret;
		}
		return null;
	}

	/**
	 * Get LookUpKey for number.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param number
	 *            number
	 * @return {@link Uri} to contact
	 */
	public final Uri getLookupKeyForNumber(final ContentResolver cr, final String number) {
		Log.d(TAG, "getLookupKeyForNumber(cr, " + number + ")");
		Uri ret = null;
		final Cursor c = this.getContact(cr, number);
		if (c != null) {
			final String id = c.getString(FILTER_INDEX_ID);
			c.close();
			ret = this.getLookupUri(cr, id);
		}
		Log.d(TAG, "return: " + ret);
		return ret;
	}

	/**
	 * Get "Name <Number>" from {@link Uri}.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param uri
	 *            {@link Uri}
	 * @return "Name <Number>"
	 */
	public final String getNameAndNumber(final ContentResolver cr, final Uri uri) {
		final Cursor c = this.getContact(cr, uri);
		if (c != null) {
			final String ret = c.getString(FILTER_INDEX_NAME) + " <"
					+ c.getString(FILTER_INDEX_NUMBER) + ">";
			c.close();
			return ret;
		}
		return null;
	}

	/**
	 * Get Number from {@link Uri}.
	 * 
	 * @param cr
	 *            {@link ContentResolver}
	 * @param uri
	 *            {@link Uri}
	 * @return Number
	 */
	public final String getNumber(final ContentResolver cr, final Uri uri) {
		final Cursor c = this.getContact(cr, uri);
		if (c != null) {
			final String ret = this.cleanNumber(c.getString(FILTER_INDEX_NUMBER));
			c.close();
			return ret;
		}
		return null;
	}

	/**
	 * Clean a number from all but [+*0-9].
	 * 
	 * @param number
	 *            drity number
	 * @return clean number
	 */
	public final String cleanNumber(final String number) {
		if (number == null) {
			return null;
		}
		return number.replaceAll("[^*+0-9]", "");
	}
}
