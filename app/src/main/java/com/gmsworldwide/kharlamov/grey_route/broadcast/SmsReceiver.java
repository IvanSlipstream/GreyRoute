package com.gmsworldwide.kharlamov.grey_route.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gmsworldwide.kharlamov.grey_route.service.SmsIntentService;

public class SmsReceiver extends BroadcastReceiver {
    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsIntentService.startActionReceiveSms(context, intent);
    }
}
