package com.gmsworldwide.kharlamov.grey_route.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

/**
 * Created by Slipstream-DESKTOP on 13.04.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String _ID = "_id";
    private static final String DB_NAME = "smsc_inspector";

    public static class KnownSmscFields {
        public static final String TABLE_NAME = "known_smsc";
        // prefix to match against user's SMSC addresses
        public static final String SMSC_PREFIX = "prefix";
        // carrier owner of the SMSC
        public static final String CARRIER_NAME = "carrier";
        // legality of SMSC
        public static final String LEGALITY = "legality";
    }

    private static String CREATE_TABLE_KNOWN_SMSC =
            String.format("create table %s (" +
            "%s integer primary key autoincrement," +
            "%s text," +
            "%s text," +
            "%s integer)",
            KnownSmscFields.TABLE_NAME,
            _ID,
            KnownSmscFields.SMSC_PREFIX,
            KnownSmscFields.CARRIER_NAME,
            KnownSmscFields.LEGALITY);

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (String statement :
                new String[]{
                        CREATE_TABLE_KNOWN_SMSC
                }) {
            sqLiteDatabase.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Creates statement for "where" clause to pass in update operations
     * @param cv data to insert
     * @param key field name which identifies the records to be updated
     * @return statement
     */
    public static String createWhereStatement(ContentValues cv, String key){
        if (cv.containsKey(key)) {
            return String.format(Locale.getDefault(), "%s = '%s'",
                    key, cv.getAsString(key));
        } else {
            return null;
        }
    }
}
