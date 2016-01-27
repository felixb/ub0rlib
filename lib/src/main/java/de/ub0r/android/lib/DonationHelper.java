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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Display send IMEI hash, read signature..
 *
 * @author flx
 */
public final class DonationHelper {

    /**
     * Tag for output.
     */
    private static final String TAG = "dh";

    /**
     * Threshold for chacking donator app license.
     */
    private static final double CHECK_DONATOR_LIC = 0.1;

    /**
     * Donator package.
     */
    public static final String DONATOR_PACKAGE = "de.ub0r.android.donator";

    public static final Uri DONATOR_URI = Uri
            .parse("https://play.google.com/store/apps/details?id=" + DONATOR_PACKAGE);

    /**
     * Check dontor Broadcast.
     */
    private static final String DONATOR_BROADCAST_CHECK = DONATOR_PACKAGE + ".CHECK";

    /**
     * Hashed IMEI.
     */
    private static String imeiHash = null;

    /**
     * Default Constructor.
     */
    private DonationHelper() {
        // nothing to do
    }

    /**
     * Get MD5 hash of the IMEI (device id).
     *
     * @param context {@link Context}
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
     * Check if ads should be hidden.
     *
     * @param context {@link Context}
     * @return true if ads should be hidden
     */
    public static boolean hideAds(final Context context) {
        PackageManager pm = context.getPackageManager();
        Intent donationCheck = new Intent(DONATOR_BROADCAST_CHECK);
        ResolveInfo ri = pm.resolveService(donationCheck, 0);
        if (ri == null) {
            return false;
        } else {
            ComponentName cn = new ComponentName(ri.serviceInfo.packageName, ri.serviceInfo.name);
            int i = pm.getComponentEnabledSetting(cn);
            if (i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    && ri.serviceInfo.enabled) {
                int match = pm
                        .checkSignatures(context.getPackageName(), ri.serviceInfo.packageName);
                if (match != PackageManager.SIGNATURE_MATCH) {
                    Log.e(TAG, "signatures do not match: " + match);
                    return false;
                }
                donationCheck.setComponent(cn);
            } else {
                Log.w(TAG, ri.serviceInfo.packageName + ": " + ri.serviceInfo.enabled);
                return false;
            }
        }

        double r = Math.random();
        if (r < CHECK_DONATOR_LIC) {
            // verify donator license
            ComponentName cn = context.startService(donationCheck);
            return cn != null;
        } else {
            return true;
        }
    }

}
