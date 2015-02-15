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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deprecated, please use ub0rlogger
 * @author flx
 */
@Deprecated
public final class Log {

    /**
     * Tag for output.
     */
    private static String mTag = "ub0rlib";

    /**
     * Tag for own output.
     */
    private static final String TAG = "ub0rlib";

    /**
     * Packagename of SendLog.
     */
    private static final String SENDLOG_PACKAGE_NAME = "org.l6n.sendlog";

    /**
     * Mail address sending the logs to.
     */
    private static final String TARGET_ADDRESS = "android@ub0r.de";

    /** Classname of SendLog. */
    // private static final String SENDLOG_CLASS_NAME = ".SendLog";

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = android.util.Log.ASSERT;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = android.util.Log.DEBUG;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = android.util.Log.ERROR;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = android.util.Log.INFO;

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = android.util.Log.VERBOSE;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = android.util.Log.WARN;

    /**
     * Fire a given {@link Intent}.
     *
     * @author flx
     */
    private static class FireIntent implements DialogInterface.OnClickListener {

        /**
         * {@link Activity}.
         */
        private final Activity a;

        /**
         * {@link Intent}.
         */
        private final Intent i;

        /**
         * Default Constructor.
         *
         * @param activity {@link Activity}
         * @param intent   {@link Intent}
         */
        public FireIntent(final Activity activity, final Intent intent) {
            this.a = activity;
            this.i = intent;
        }

        /**
         * {@inheritDoc}
         */
        public void onClick(final DialogInterface dialog, final int whichButton) {
            try {
                this.a.startActivity(this.i);
            } catch (ActivityNotFoundException e) {
                e(TAG, "activity not found", e);
                Toast.makeText(this.a, "no activity found", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Default Constructor.
     */
    private Log() {

    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(final String tag, final String msg) {
        android.util.Log.d(mTag, tag + ": " + msg);
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the
     *                  class or activity where the log call occurs.
     * @param msg       The message you would like logged.
     * @param startTime start of time measuring with SystemClock.elapsedRealtime()
     */
    public static void d(final String tag, final String msg, final long startTime) {
        android.util.Log.d(mTag, tag + ": " + msg + " / "
                + (SystemClock.elapsedRealtime() - startTime) + "ms");
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void d(final String tag, final String msg, final Throwable tr) {
        android.util.Log.d(mTag, tag + ": " + msg, tr);
    }

    /**
     * Send a ERROR log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(final String tag, final String msg) {
        android.util.Log.e(mTag, tag + ": " + msg);
    }

    /**
     * Send a ERROR log message.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the
     *                  class or activity where the log call occurs.
     * @param msg       The message you would like logged.
     * @param startTime start of time measuring with SystemClock.elapsedRealtime()
     */
    public static void e(final String tag, final String msg, final long startTime) {
        android.util.Log.e(mTag, tag + ": " + msg + " / "
                + (SystemClock.elapsedRealtime() - startTime) + "ms");
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void e(final String tag, final String msg, final Throwable tr) {
        android.util.Log.e(mTag, tag + ": " + msg, tr);
    }

    /**
     * Send a INFO log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(final String tag, final String msg) {
        android.util.Log.i(mTag, tag + ": " + msg);
    }

    /**
     * Send a INFO log message.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the
     *                  class or activity where the log call occurs.
     * @param msg       The message you would like logged.
     * @param startTime start of time measuring with SystemClock.elapsedRealtime()
     */
    public static void i(final String tag, final String msg, final long startTime) {
        android.util.Log.i(mTag, tag + ": " + msg + " / "
                + (SystemClock.elapsedRealtime() - startTime) + "ms");
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void i(final String tag, final String msg, final Throwable tr) {
        android.util.Log.i(mTag, tag + ": " + msg, tr);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void v(final String tag, final String msg) {
        android.util.Log.v(mTag, tag + ": " + msg);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the
     *                  class or activity where the log call occurs.
     * @param msg       The message you would like logged.
     * @param startTime start of time measuring with SystemClock.elapsedRealtime()
     */
    public static void v(final String tag, final String msg, final long startTime) {
        android.util.Log.v(mTag, tag + ": " + msg + " / "
                + (SystemClock.elapsedRealtime() - startTime) + "ms");
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void v(final String tag, final String msg, final Throwable tr) {
        android.util.Log.v(mTag, tag + ": " + msg, tr);
    }

    /**
     * Send a WARN log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(final String tag, final String msg) {
        android.util.Log.w(mTag, tag + ": " + msg);
    }

    /**
     * Send a WARN log message.
     *
     * @param tag       Used to identify the source of a log message. It usually identifies the
     *                  class or activity where the log call occurs.
     * @param msg       The message you would like logged.
     * @param startTime start of time measuring with SystemClock.elapsedRealtime()
     */
    public static void w(final String tag, final String msg, final long startTime) {
        android.util.Log.w(mTag, tag + ": " + msg + " / "
                + (SystemClock.elapsedRealtime() - startTime) + "ms");
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void w(final String tag, final String msg, final Throwable tr) {
        android.util.Log.w(mTag, tag + ": " + msg, tr);
    }

    /**
     * Init mTag etc.
     *
     * @param tag mTag
     */
    public static void init(final String tag) {
        mTag = tag;
    }

    /**
     * Collect and send Log with SendLog app.
     *
     * @param activity       {@link Activity}
     * @param titleInstall   title for install sendlog dialog
     * @param titleRun       title for run sendlog dialog
     * @param messageInstall message for install sendlog dialog
     * @param messageRun     message for run sendlog dialog
     */
    private static void collectAndSendLogWithSendlog(final Activity activity,
            final String titleInstall, final String messageInstall, final String titleRun,
            final String messageRun) {
        final PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(SENDLOG_PACKAGE_NAME);
        String title, message;
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:"
                    + SENDLOG_PACKAGE_NAME));
            title = titleInstall;
            message = messageInstall;
        } else {
            intent.putExtra("filter", mTag + ":D *:W");
            intent.setType("0||" + TARGET_ADDRESS);
            intent.putExtra(Intent.EXTRA_SUBJECT, "SendLog: " + activity.getPackageName());
            title = titleRun;
            message = messageRun;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setIcon(android.R.drawable.ic_dialog_info);
        b.setTitle(title);
        b.setMessage(message);
        b.setPositiveButton(android.R.string.ok, new FireIntent(activity, intent));
        b.setNegativeButton(android.R.string.cancel, null);
        b.show();
    }

    /**
     * Send logs with android board tools.
     *
     * @param activity {@link Activity}
     */
    @TargetApi(4)
    @SuppressWarnings("deprecation")
    private static void collectAndSendLogPlain(final Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        StringBuilder sb = new StringBuilder();
        if (Utils.isApi(Build.VERSION_CODES.DONUT)) {
            sb.append("Manufacturer: " + Build.MANUFACTURER + "\n");
        }
        sb.append("Model: " + Build.MODEL + "\n");
        sb.append("Device: " + Build.DEVICE + "\n");
        sb.append("Product: " + Build.PRODUCT + "\n");
        sb.append("Android Release: " + Build.VERSION.RELEASE + "\n");
        sb.append("SDK: " + Build.VERSION.SDK + "\n");
        sb.append("\n");
        try {
            Process p = Runtime.getRuntime().exec("logcat -d -v time");

            String fn = LogProvider.getLogFile(activity);
            activity.deleteFile(fn);
            FileOutputStream os = activity.openFileOutput(fn, Context.MODE_PRIVATE);
            InputStream is = p.getInputStream();

            byte[] buf = new byte[1024];
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                os.write(buf, 0, c);
            }
            is.close();
            os.close();
            p.destroy();
            Uri u = Uri.parse("content://" + activity.getPackageName() + ".DEBUGLOG");
            intent.putExtra(Intent.EXTRA_STREAM, u);
        } catch (IOException e) {
            String s = "IOException while reading logs";
            Log.e(TAG, s, e);
            Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            sb.append(s);
            sb.append("\n\n");
        }
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{TARGET_ADDRESS});
        intent.putExtra(Intent.EXTRA_SUBJECT, "SendLog: " + activity.getPackageName());
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            activity.startActivity(Intent.createChooser(intent, "SendLog"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "activity not found", e);
            Toast.makeText(activity, "no activity found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Collect and send Log. When using this method, make sure to add the LogProvider to your
     * AndroidManifest.xml: <provider android:name="de.ub0r.android.lib.LogProvider"
     * android:authorities="{Your app package}.DEBUGLOG" android:exported="true" />
     *
     * @param activity       {@link Activity}.
     * @param titleInstall   title for install sendlog dialog
     * @param titleRun       title for run sendlog dialog
     * @param messageInstall message for install sendlog dialog
     * @param messageRun     message for run sendlog dialog
     */
    public static void collectAndSendLog(final Activity activity, final String titleInstall,
            final String messageInstall, final String titleRun, final String messageRun) {
        if (Utils.isApi(Build.VERSION_CODES.JELLY_BEAN)) {
            collectAndSendLogPlain(activity);
        } else {
            collectAndSendLogWithSendlog(activity, titleInstall, messageInstall, titleRun,
                    messageRun);
        }
    }
}
