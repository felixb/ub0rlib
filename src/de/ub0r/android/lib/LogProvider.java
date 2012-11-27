package de.ub0r.android.lib;

import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

public class LogProvider extends ContentProvider {
	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		throw new IllegalStateException("Unsupported operation: delete");
	}

	@Override
	public String getType(final Uri uri) {
		return "text/plain";
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		throw new IllegalStateException("Unsupported operation: delete");
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder) {
		String fn = getLogFile(this.getContext());
		final int l = projection.length;
		Object[] retArray = new Object[l];
		for (int i = 0; i < l; i++) {
			if (projection[i].equals(OpenableColumns.DISPLAY_NAME)) {
				retArray[i] = fn;
			} else if (projection[i].equals(OpenableColumns.SIZE)) {
				retArray[i] = this.getContext().getFileStreamPath(fn).length();
			}
		}
		final MatrixCursor c = new MatrixCursor(projection, 1);
		c.addRow(retArray);
		return c;
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs) {
		throw new IllegalStateException("Unsupported operation: update");
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode)
			throws FileNotFoundException {
		String fn = getLogFile(this.getContext());

		return ParcelFileDescriptor.open(this.getContext().getFileStreamPath(fn),
				ParcelFileDescriptor.MODE_READ_ONLY);
	}

	public static String getLogFile(final Context context) {
		return context.getPackageName() + ".log";
	}
}
