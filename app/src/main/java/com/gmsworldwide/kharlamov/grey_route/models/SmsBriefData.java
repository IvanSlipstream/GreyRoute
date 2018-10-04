package com.gmsworldwide.kharlamov.grey_route.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Slipstream on 30.07.2016.
 */
public class SmsBriefData implements Parcelable {

    public static final char TRAILING_CHARACTER = '*';
    private static final int END_INDEX_DEPERSONALIZED = 6;
    private String mSmsc;
    private String mText;
    private String mTpOa;
    private String mSimImsi;
    private long mTime;
    private long mSentTime;
    private long mId;

    public SmsBriefData(String smsc, String text, String tpOa) {
        this.mSmsc = smsc;
        this.mText = text;
        this.mTpOa = tpOa;
    }

    public SmsBriefData(String smsc, String text, String tpOa, long time, long id, long sentTime) {
        this.mSmsc = smsc;
        this.mText = text;
        this.mTpOa = tpOa;
        this.mTime = time;
        this.mId = id;
        this.mSentTime = sentTime;
    }

    public SmsBriefData (Cursor cursor) {
        this.mSmsc = cursor.getString(cursor.getColumnIndex("service_center"));
        this.mText = cursor.getString(cursor.getColumnIndex("body"));
        this.mTpOa = cursor.getString(cursor.getColumnIndex("address"));
        this.mTime = cursor.getLong(cursor.getColumnIndex("date"));
        this.mSentTime = cursor.getLong(cursor.getColumnIndex("date_sent"));
        this.mId = cursor.getLong(cursor.getColumnIndex("_id"));
        int imsiIndex = cursor.getColumnIndex("sim_imsi");
        this.mSimImsi = imsiIndex == -1 && cursor.getString(imsiIndex) != null
                ? ""
                : cursor.getString(imsiIndex).substring(0, END_INDEX_DEPERSONALIZED)+TRAILING_CHARACTER;
    }

    private SmsBriefData(Parcel in) {
        mSmsc = in.readString();
        mText = in.readString();
        mTpOa = in.readString();
        mSimImsi = in.readString();
        mTime = in.readLong();
        mSentTime = in.readLong();
        mId = in.readLong();
    }

    public static final Creator<SmsBriefData> CREATOR = new Creator<SmsBriefData>() {
        @Override
        public SmsBriefData createFromParcel(Parcel in) {
            return new SmsBriefData(in);
        }

        @Override
        public SmsBriefData[] newArray(int size) {
            return new SmsBriefData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSmsc);
        parcel.writeString(mText);
        parcel.writeString(mTpOa);
        parcel.writeString(mSimImsi);
        parcel.writeLong(mTime);
        parcel.writeLong(mSentTime);
        parcel.writeLong(mId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getSmsc() {
        return mSmsc;
    }

    public String getText() {
        return mText;
    }

    public String getTpOa() {
        return mTpOa;
    }

    public long getId() {
        return mId;
    }

    public long getTime() {
        return mTime;
    }

    public String getSimImsiPrefix() {
        return mSimImsi;
    }

    private String getTimeZoneName() {
        int millis = Calendar.getInstance().get(Calendar.DST_OFFSET) +
                Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        return String.format(Locale.getDefault(), "%+02d:%02d", millis / 3600000, millis % 3600000 / 60000);
    }

    public String getFormattedTime(){
        Date date = new Date(mTime);
        return DateFormat.getDateTimeInstance().format(date);
    }

    public String getFormattedSentTime(){
        Date date = new Date(mSentTime);
        return DateFormat.getDateTimeInstance().format(date);
    }
}
