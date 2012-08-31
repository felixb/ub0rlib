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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import de.ub0r.android.lib.Log;

/**
 * Information about a {@link Contact}.
 * 
 * @author flx
 */
public final class Contact {
	/** Tag for logging. */
	static final String TAG = "api.contacts";

	/** {@link Contact}'s number. */
	String mNumber;
	/** {@link Contact}'s name. */
	String mName;
	/**
	 * {@link Contact}'s name and number formated like "name &lt;number&gt;.
	 */
	private String mNameAndNumber;

	/** {@link Contact}'s recipient id. */
	final long mRecipientId;
	/** {@link Contact}'s person id. */
	long mPersonId;
	/** {@link Contact}'s lookup key. */
	String mLookupKey;
	/** {@link Contact}'s presence state. */
	int mPresenceState;
	/** {@link Contact}'s presence text. */
	String mPresenceText;
	/** {@link Contact}'s avatar. */
	BitmapDrawable mAvatar;
	/** {@link Contact}'s avatar data. */
	byte[] mAvatarData;

	/** {@link Uri} to {@link Contact}. **/
	private Uri mContactUri = null;
	/** Lookup {@link Uri} to {@link Contact}. **/
	private Uri mLookupUri = null;

	/**
	 * Get default {@link Drawable} resource id for {@link Contact}'s presence state.
	 * 
	 * @param presenceState
	 *            presence state
	 * @return {@link Drawable}'s resId
	 */
	public static int getPresenceRes(final int presenceState) {
		switch (presenceState) {
		case ContactsWrapper.PRESENCE_STATE_AVAILABLE:
			return android.R.drawable.presence_online;
		case ContactsWrapper.PRESENCE_STATE_AWAY:
		case ContactsWrapper.PRESENCE_STATE_IDLE:
		case ContactsWrapper.PRESENCE_STATE_INVISIBLE:
			return android.R.drawable.presence_away;
		case ContactsWrapper.PRESENCE_STATE_DO_NOT_DISTURB:
			return android.R.drawable.presence_busy;
		case ContactsWrapper.PRESENCE_STATE_OFFLINE:
			return android.R.drawable.presence_offline;
		default:
			return -1;
		}
	}

	/**
	 * Create a {@link Contact}.
	 * 
	 * @param recipientId
	 *            recipient's id
	 */
	public Contact(final long recipientId) {
		this.mPersonId = -1L;
		this.mRecipientId = recipientId;
	}

	/**
	 * Create a {@link Contact}.
	 * 
	 * @param recipientId
	 *            recipient's id
	 * @param number
	 *            number
	 */
	public Contact(final long recipientId, final String number) {
		this.mPersonId = -1L;
		this.mRecipientId = recipientId;
		this.mNumber = number;
		this.updateNameAndNumer();
	}

	/**
	 * Create a {@link Contact}.
	 * 
	 * @param number
	 *            number
	 */
	public Contact(final String number) {
		this.mPersonId = -1L;
		this.mRecipientId = -1L;
		this.mNumber = number;
		this.updateNameAndNumer();
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
	 * @return true if {@link Contact}'s details where changed
	 */
	public boolean update(final Context context, final boolean loadOnly, final boolean loadAvatar) {
		Log.d(TAG, "update()");
		return ContactsWrapper.getInstance().updateContactDetails(context, loadOnly, loadAvatar,
				this);
	}

	/**
	 * @return {@link Contact}'s number
	 */
	public String getNumber() {
		return this.mNumber;
	}

	/**
	 * Set {@link Contact}'s number.
	 * 
	 * @param number
	 *            number
	 */
	public void setNumber(final String number) {
		this.mNumber = number;
		this.updateNameAndNumer();
	}

	/**
	 * Update mNameAndNumber.
	 */
	void updateNameAndNumer() {
		final String name = this.mName;
		final String number = this.mNumber;
		if (TextUtils.isEmpty(name)) {
			if (TextUtils.isEmpty(number)) {
				this.mNameAndNumber = "...";
			} else {
				this.mNameAndNumber = PhoneNumberUtils.formatNumber(number);
			}
		} else {
			if (TextUtils.isEmpty(number)) {
				this.mNameAndNumber = name;
			} else {
				this.mNameAndNumber = name + " <" + PhoneNumberUtils.formatNumber(number) + ">";
			}
		}
	}

	/**
	 * @return {@link Contact}'s name
	 */
	public String getName() {
		return this.mName;
	}

	/**
	 * Set {@link Contact}'s name.
	 * 
	 * @param name
	 *            name
	 */
	public void setName(final String name) {
		this.mName = name;
		this.updateNameAndNumer();
	}

	/**
	 * @return {@link Contact}'s name and number formated like "name &lt;number&gt;
	 */
	public String getNameAndNumber() {
		return this.mNameAndNumber;
	}

	/**
	 * @return {@link Contact}'s name or number or "...".
	 */
	public String getDisplayName() {
		if (TextUtils.isEmpty(this.mName)) {
			if (TextUtils.isEmpty(this.mNumber)) {
				return "...";
			} else {
				return this.mNumber;
			}
		} else {
			return this.mName;
		}
	}

	/**
	 * @return recipient's id
	 */
	public long getRecipientId() {
		return this.mRecipientId;
	}

	/**
	 * @return Person's id
	 */
	public long getContactId() {
		return this.mPersonId;
	}

	/**
	 * @return LookupKey
	 */
	public String getLookUpKey() {
		return this.mLookupKey;
	}

	/**
	 * @return {@link Uri} to {@link Contact}
	 */
	public Uri getUri() {
		if (this.mContactUri == null && this.mPersonId > 0L) {
			this.mContactUri = ContactsWrapper.getInstance().getContactUri(this.mPersonId);
		}
		return this.mContactUri;
	}

	/**
	 * @param cr
	 *            {@link ContentResolver}
	 * @return {@link Uri} to {@link Contact}
	 */
	public Uri getLookUpUri(final ContentResolver cr) {
		if (this.mLookupUri == null && this.mLookupKey != null) {
			this.mLookupUri = ContactsWrapper.getInstance().getLookupUri(cr, this.mLookupKey);
		}
		return this.mLookupUri;
	}

	/**
	 * @return presence's state
	 */
	public int getPresenceState() {
		return this.mPresenceState;
	}

	/**
	 * @return presence's text
	 */
	public String getPresenceText() {
		return this.mPresenceText;
	}

	/**
	 * Get {@link Contact}'s avatar.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param defaultValue
	 *            default {@link Drawable}
	 * @return {@link Contact}'s avatar
	 */
	@TargetApi(4)
	public Drawable getAvatar(final Context context, final Drawable defaultValue) {
		Log.d(TAG, "getAvatar()");
		if (this.mAvatar == null) {
			if (this.mAvatarData != null) {
				Bitmap b = BitmapFactory.decodeByteArray(this.mAvatarData, 0,
						this.mAvatarData.length);
				this.mAvatar = new BitmapDrawable(context.getResources(), b);
			}
		}
		if (this.mAvatar == null) {
			return defaultValue;
		}
		return this.mAvatar;
	}
}
