package com.gmsworldwide.kharlamov.greyroute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsReceiver extends BroadcastReceiver {
    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsIntentService.startActionReceiveSms(context, intent);
    }
}
