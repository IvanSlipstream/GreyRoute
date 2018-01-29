package com.gmsworldwide.kharlamov.grey_route.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.gmsworldwide.kharlamov.grey_route.db.DbHelper;

/**
 * Created by Slipstream on 04.04.2017 in GreyRoute.
 */

public class KnownSmsc implements Parcelable {

    public static final int LEGALITY_GREY = 1;
    public static final int LEGALITY_AGGREGATOR = 2;
    public static final int LEGALITY_UNKNOWN = 0;

    private int mLegality;
    private String mCarrierName;
    private String mSmscPrefix;

    public KnownSmsc(int legality, String carrierName, String smscAddress) {
        this.mLegality = legality;
        this.mCarrierName = carrierName;
        this.mSmscPrefix = smscAddress;
    }

    public KnownSmsc(Cursor c){
        this.mCarrierName = c.getString(c.getColumnIndex(DbHelper.KnownSmscFields.CARRIER_NAME));
        this.mSmscPrefix = c.getString(c.getColumnIndex(DbHelper.KnownSmscFields.SMSC_PREFIX));
        this.mLegality = c.getInt(c.getColumnIndex(DbHelper.KnownSmscFields.LEGALITY));
    }

    private KnownSmsc(Parcel in) {
        mLegality = in.readInt();
        mCarrierName = in.readString();
        mSmscPrefix = in.readString();
    }

    public static final Creator<KnownSmsc> CREATOR = new Creator<KnownSmsc>() {
        @Override
        public KnownSmsc createFromParcel(Parcel in) {
            return new KnownSmsc(in);
        }

        @Override
        public KnownSmsc[] newArray(int size) {
            return new KnownSmsc[size];
        }
    };

    public void override(KnownSmsc newSmsMatch){
        if (this.mLegality < newSmsMatch.mLegality
                && this.mSmscPrefix.equals(newSmsMatch.mSmscPrefix)){
            this.mCarrierName = newSmsMatch.mCarrierName;
            this.mLegality = newSmsMatch.mLegality;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLegality);
        dest.writeString(mCarrierName);
        dest.writeString(mSmscPrefix);
    }

    public String getCarrierName() {
        return mCarrierName;
    }

    public String getSmscPrefix() {
        return mSmscPrefix;
    }

    public int getLegality() {
        return mLegality;
    }

    public ContentValues makeContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.KnownSmscFields.CARRIER_NAME, mCarrierName);
        cv.put(DbHelper.KnownSmscFields.SMSC_PREFIX, mSmscPrefix);
        cv.put(DbHelper.KnownSmscFields.LEGALITY, mLegality);
        return cv;
    }

    @Override
    public String toString() {
        return "KnownSmsc{" +
                "mLegality=" + mLegality +
                ", mCarrierName='" + mCarrierName + '\'' +
                ", mSmscPrefix='" + mSmscPrefix + '\'' +
                '}';
    }
}
