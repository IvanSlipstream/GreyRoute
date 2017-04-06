package com.gmsworldwide.kharlamov.greyroute.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Slipstream on 04.04.2017 in GreyRoute.
 */

public class SmscMatch implements Parcelable {

    private int mMatchingDigits;
    private String mCarrierName;
    private String mSmscAddress;

    public SmscMatch(int matchingDigits, String carrierName, String smscAddress) {
        this.mMatchingDigits = matchingDigits;
        this.mCarrierName = carrierName;
        this.mSmscAddress = smscAddress;
    }

    private SmscMatch(Parcel in) {
        mMatchingDigits = in.readInt();
        mCarrierName = in.readString();
        mSmscAddress = in.readString();
    }

    public static final Creator<SmscMatch> CREATOR = new Creator<SmscMatch>() {
        @Override
        public SmscMatch createFromParcel(Parcel in) {
            return new SmscMatch(in);
        }

        @Override
        public SmscMatch[] newArray(int size) {
            return new SmscMatch[size];
        }
    };

    public void override(SmscMatch newSmsMatch){
        if (this.mMatchingDigits < newSmsMatch.mMatchingDigits
                && this.mSmscAddress.equals(newSmsMatch.mSmscAddress)){
            this.mCarrierName = newSmsMatch.mCarrierName;
            this.mMatchingDigits = newSmsMatch.mMatchingDigits;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMatchingDigits);
        dest.writeString(mCarrierName);
        dest.writeString(mSmscAddress);
    }

    public String getCarrierName() {
        return mCarrierName;
    }

    @Override
    public String toString() {
        return "SmscMatch{" +
                "mMatchingDigits=" + mMatchingDigits +
                ", mCarrierName='" + mCarrierName + '\'' +
                ", mSmscAddress='" + mSmscAddress + '\'' +
                '}';
    }
}
