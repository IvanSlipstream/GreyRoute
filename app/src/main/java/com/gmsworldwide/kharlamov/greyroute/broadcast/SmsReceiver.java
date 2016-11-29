package com.gmsworldwide.kharlamov.greyroute.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;

public class SmsReceiver extends BroadcastReceiver {
    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsIntentService.startActionReceiveSms(context, intent);
    }
}
