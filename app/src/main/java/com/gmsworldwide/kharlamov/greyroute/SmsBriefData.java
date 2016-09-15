package com.gmsworldwide.kharlamov.greyroute;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;

/**
 * Created by Slipstream on 30.07.2016.
 */
public class SmsBriefData {

    private String mSmsc;
    private String mText;
    private String mTpOa;

    SmsBriefData (Bundle bundle){
        this.mSmsc = bundle.getString(SmsIntentService.SMSC_KEY);
        this.mText = bundle.getString(SmsIntentService.TEXT_KEY);
        this.mTpOa = bundle.getString(SmsIntentService.TP_OA_KEY);
    }

    SmsBriefData (Cursor cursor) {
        this.mSmsc = cursor.getString(cursor.getColumnIndex("service_center"));
        this.mText = cursor.getString(cursor.getColumnIndex("body"));
        this.mTpOa = cursor.getString(cursor.getColumnIndex("address"));
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
}
