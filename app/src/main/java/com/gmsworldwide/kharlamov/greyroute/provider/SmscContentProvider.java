package com.gmsworldwide.kharlamov.greyroute.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

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
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Context context = getContext();
        Uri notificationUri = null;
        long id;
        switch (sUriMatcher.match(uri)){
            case CODE_KNOWN_SMSC:
                id = mDatabaseWrite.insert(DbHelper.KnownSmscFields.TABLE_NAME, null, values);
                notificationUri = ContentUris.withAppendedId(uri, id);
                break;
        }
        if (context != null && notificationUri != null) {
            context.getContentResolver().notifyChange(notificationUri, null);
        }
        return notificationUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        switch (sUriMatcher.match(uri)) {
            case CODE_KNOWN_SMSC:
                c = mDatabaseRead.query(DbHelper.KnownSmscFields.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
        }
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rows = 0;
        Context context = getContext();
        switch (sUriMatcher.match(uri)){
            case CODE_KNOWN_SMSC:
                rows = mDatabaseWrite.updateWithOnConflict(DbHelper.KnownSmscFields.TABLE_NAME, values,
                        selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
        }
        if (context != null && rows > 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}
