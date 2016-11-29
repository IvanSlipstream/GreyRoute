package com.gmsworldwide.kharlamov.greyroute.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.telephony.SmsMessage;

public class SmsIntentService extends IntentService {
    private static final String ACTION_RECEIVE_SMS = "com.gmsworldwide.kharlamov.greyroute.action.RECEIVE_SMS";
    private static final String ACTION_SET_LISTENER = "com.gmsworldwide.kharlamov.greyroute.action.SET_LISTENER";

    private static final String EXTRA_LISTENER = "com.gmsworldwide.kharlamov.greyroute.extra.LISTENER";
    private static final String EXTRA_SMS = "com.gmsworldwide.kharlamov.greyroute.extra.SMS";

    public static final String PDU_KEY = "pdus";
    public static final String SMSC_KEY = "smsc";
    public static final String TEXT_KEY = "text";
    public static final String TP_OA_KEY = "tp_oa";

    public SmsIntentService() {
        super("SmsIntentService");
    }

    public static void startActionSetListener(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, SmsIntentService.class);
        intent.setAction(ACTION_SET_LISTENER);
        intent.putExtra(EXTRA_LISTENER, receiver);
        context.startService(intent);
    }

    public static void startActionReceiveSms(Context context, Intent smsIntent) {
        Intent intent = new Intent(context, SmsIntentService.class);
        intent.setAction(ACTION_RECEIVE_SMS);
        intent.putExtra(EXTRA_SMS, smsIntent);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action){
                case ACTION_RECEIVE_SMS:
                    final Intent smsIntent = intent.getParcelableExtra(EXTRA_SMS);
                    handleActionReceiveSms(smsIntent);
                    break;
                case ACTION_SET_LISTENER:
                    final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_LISTENER);
                    handleActionSetListener(receiver);
                    break;
            }
        }
    }

    private void handleActionReceiveSms(Intent smsIntent) {
        ResultReceiver receiver = SmsReceiverContext.getInstance().getReceiverContext();
        if (receiver !=null){
            Bundle bundle = new Bundle();
            Object[] pduArray = (Object[]) smsIntent.getExtras().get(PDU_KEY);
            if (pduArray != null) {
                SmsMessage[] messages = new SmsMessage[pduArray.length];
                for (int i = 0; i < pduArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                    bundle.putString(SMSC_KEY, messages[i].getServiceCenterAddress());
                    bundle.putString(TEXT_KEY, messages[i].getDisplayMessageBody());
                    bundle.putString(TP_OA_KEY, messages[i].getDisplayOriginatingAddress());
                    receiver.send(1, bundle);
                }
            }
        }
    }

    private void handleActionSetListener(ResultReceiver receiver) {
        SmsReceiverContext.getInstance().setReceiverContext(receiver);
    }

    public static class SmsReceiverContext {

        private ResultReceiver mReceiverContext;

        private SmsReceiverContext() {
        }

        private static SmsReceiverContext ourInstance = new SmsReceiverContext();

        public static synchronized SmsReceiverContext getInstance() {
            return ourInstance;
        }

        public ResultReceiver getReceiverContext() {
            return mReceiverContext;
        }

        public void setReceiverContext(ResultReceiver receiverContext) {
            this.mReceiverContext = receiverContext;
        }
    }

}
