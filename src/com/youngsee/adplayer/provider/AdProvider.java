package com.youngsee.adplayer.provider;

import com.youngsee.adplayer.util.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AdProvider extends ContentProvider {

	private static Logger mLogger = new Logger();
	
	private static final int URL_SYSPARAM = 1;
	private static final int URL_SYSPARAM_ID = 2;
	private static final int URL_CHARGEINFO = 3;
	private static final int URL_CHARGEINFO_ID = 4;

	private AdDbHelper mDbHelper = null;

	private static final UriMatcher s_urlMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "sysparam", URL_SYSPARAM);
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "sysparam/#", URL_SYSPARAM_ID);
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "chargeinfo", URL_CHARGEINFO);
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "chargeinfo/#", URL_CHARGEINFO_ID);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		Uri deleteUri;

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			count = db.delete(DbConstants.TABLE_SYSPARAM, where, whereArgs);
			deleteUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		case URL_SYSPARAM_ID:
			count = db.delete(DbConstants.TABLE_SYSPARAM,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			deleteUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		case URL_CHARGEINFO:
			count = db.delete(DbConstants.TABLE_CHARGEINFO, where, whereArgs);
			deleteUri = DbConstants.CONTENTURI_CHARGEINFO;
			break;

		case URL_CHARGEINFO_ID:
			count = db.delete(DbConstants.TABLE_CHARGEINFO,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			deleteUri = DbConstants.CONTENTURI_CHARGEINFO;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(deleteUri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri url) {
		switch (s_urlMatcher.match(url)) {
		case URL_SYSPARAM:
		case URL_CHARGEINFO:
			return DbConstants.CONTENT_TYPE;

		case URL_SYSPARAM_ID:
		case URL_CHARGEINFO_ID:
			return DbConstants.CONTENT_TYPE_ITME;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId;
		Uri insertUri = null;

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			if (!values.containsKey(DbConstants.SPT_SCREENWIDTH)) {
				values.put(DbConstants.SPT_SCREENWIDTH, -1);
			}
			if (!values.containsKey(DbConstants.SPT_SCREENHEIGHT)) {
				values.put(DbConstants.SPT_SCREENHEIGHT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_CHARGEREPORTPERIOD)) {
				values.put(DbConstants.SPT_CHARGEREPORTPERIOD, -1);
			}
			if (!values.containsKey(DbConstants.SPT_HEARTBEATPERIOD)) {
				values.put(DbConstants.SPT_HEARTBEATPERIOD, -1);
			}
			if (!values.containsKey(DbConstants.SPT_FTPPORT)) {
				values.put(DbConstants.SPT_FTPPORT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_IADSPORT)) {
				values.put(DbConstants.SPT_IADSPORT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_SMSPORT)) {
				values.put(DbConstants.SPT_SMSPORT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_AMPSPORT)) {
				values.put(DbConstants.SPT_AMPSPORT, -1);
			}

			rowId = db.insert(DbConstants.TABLE_SYSPARAM, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_SYSPARAM, rowId);
			}
			break;

		case URL_CHARGEINFO:
			if (!values.containsKey(DbConstants.CIT_PUBLISHID)) {
				values.put(DbConstants.CIT_PUBLISHID, DbConstants.NOTSET);
			}
			if (!values.containsKey(DbConstants.CIT_PLAYDATE)) {
				values.put(DbConstants.CIT_PLAYDATE, DbConstants.NOTSET);
			}
			if (!values.containsKey(DbConstants.CIT_TOTALPLAYTIMES)) {
				values.put(DbConstants.CIT_TOTALPLAYTIMES, -1);
			}
			if (!values.containsKey(DbConstants.CIT_CURRENTPLAYTIMES)) {
				values.put(DbConstants.CIT_CURRENTPLAYTIMES, -1);
			}

			rowId = db.insert(DbConstants.TABLE_CHARGEINFO, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_CHARGEINFO, rowId);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(insertUri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new AdDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			qb.setTables(DbConstants.TABLE_SYSPARAM);
			break;

		case URL_SYSPARAM_ID:
			qb.setTables(DbConstants.TABLE_SYSPARAM);
			qb.appendWhere(DbConstants._ID + "=" + uri.getPathSegments().get(1));
			break;

		case URL_CHARGEINFO:
			qb.setTables(DbConstants.TABLE_CHARGEINFO);
			break;

		case URL_CHARGEINFO_ID:
			qb.setTables(DbConstants.TABLE_CHARGEINFO);
			qb.appendWhere(DbConstants._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		Uri updateUri;

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			count = db.update(DbConstants.TABLE_SYSPARAM, values, where, whereArgs);
			updateUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		case URL_SYSPARAM_ID:
			count = db.update(DbConstants.TABLE_SYSPARAM, values,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			updateUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_SYSPARAM,
					Long.parseLong(uri.getPathSegments().get(1)));
			break;
			
		case URL_CHARGEINFO:
			count = db.update(DbConstants.TABLE_CHARGEINFO, values, where, whereArgs);
			updateUri = DbConstants.CONTENTURI_CHARGEINFO;
			break;

		case URL_CHARGEINFO_ID:
			count = db.update(DbConstants.TABLE_CHARGEINFO, values,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			updateUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_CHARGEINFO,
					Long.parseLong(uri.getPathSegments().get(1)));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(updateUri, null);
		}

		return count;
	}

}
