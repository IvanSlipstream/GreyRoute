package com.gmsworldwide.kharlamov.greyroute.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Slipstream on 30.07.2016.
 */
public class SmsBriefData implements Parcelable {

    private String mSmsc;
    private String mText;
    private String mTpOa;
    private long mTime;

    public SmsBriefData(String smsc, String text, String tpOa) {
        this.mSmsc = smsc;
        this.mText = text;
        this.mTpOa = tpOa;
    }

    public SmsBriefData(String smsc, String text, String tpOa, long time) {
        this.mSmsc = smsc;
        this.mText = text;
        this.mTpOa = tpOa;
        this.mTime = time;
    }

    public SmsBriefData (Cursor cursor) {
        this.mSmsc = cursor.getString(cursor.getColumnIndex("service_center"));
        this.mText = cursor.getString(cursor.getColumnIndex("body"));
        this.mTpOa = cursor.getString(cursor.getColumnIndex("address"));
        this.mTime = cursor.getLong(cursor.getColumnIndex("date"));
    }

    private SmsBriefData(Parcel in) {
        mSmsc = in.readString();
        mText = in.readString();
        mTpOa = in.readString();
        mTime = in.readLong();
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
        parcel.writeLong(mTime);
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

    public String getFormattedTime(){
        Date date = new Date(mTime);
        return DateFormat.getDateTimeInstance().format(date);
    }
}
