package com.gmsworldwide.kharlamov.greyroute.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.gmsworldwide.kharlamov.greyroute.Manifest;
import com.gmsworldwide.kharlamov.greyroute.db.DbHelper;

public class SmscContentProvider extends ContentProvider {
    private SQLiteDatabase mDatabaseWrite;
    private SQLiteDatabase mDatabaseRead;

    private static final int CODE_KNOWN_SMSC = 1;
    private static final String SUFFIX_KNOWN_SMSC = "/known_smsc";

    private static final String AUTHORITY = "com.gmsworldwide.kharlamov.greyroute";

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final Uri URI_KNOWN_SMSC = Uri.parse("content://"+AUTHORITY+SUFFIX_KNOWN_SMSC);

    static {
        sUriMatcher.addURI(AUTHORITY, SUFFIX_KNOWN_SMSC, CODE_KNOWN_SMSC);
    }

    public SmscContentProvider() {
    }

    @Override
    public boolean onCreate() {
        DbHelper dbHelper = new DbHelper(getContext());
        mDatabaseWrite = dbHelper.getWritableDatabase();
        mDatabaseRead = dbHelper.getReadableDatabase();
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Context context = getContext();
        int rows = mDatabaseWrite.update(DbHelper.KnownSmscFields.TABLE_NAME, values, selection, selectionArgs);
        if (context != null) {
            context.notify();
        }
        return rows;
    }
}
