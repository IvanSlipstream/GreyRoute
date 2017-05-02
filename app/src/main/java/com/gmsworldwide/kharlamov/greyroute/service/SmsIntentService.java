package com.gmsworldwide.kharlamov.greyroute.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.telephony.SmsMessage;
import android.util.Log;

import com.gmsworldwide.kharlamov.greyroute.db.DbHelper;
import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.provider.SmscContentProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.gmsworldwide.kharlamov.greyroute.activities.MainActivity.CSV_REPORT_HEADER;

public class SmsIntentService extends IntentService {
    private static final String ACTION_RECEIVE_SMS = "com.gmsworldwide.kharlamov.greyroute.action.RECEIVE_SMS";
    private static final String ACTION_SET_LISTENER = "com.gmsworldwide.kharlamov.greyroute.action.SET_LISTENER";
    private static final String ACTION_MAKE_CSV_REPORT = "com.gmsworldwide.kharlamov.greyroute.action.MAKE_CSV_REPORT";
    private static final String ACTION_SYNC_SMSCS = "com.gmsworldwide.kharlamov.greyroute.action.MATCH_SMSC";

    private static final String EXTRA_LISTENER = "com.gmsworldwide.kharlamov.greyroute.extra.LISTENER";
    private static final String EXTRA_SMS = "com.gmsworldwide.kharlamov.greyroute.extra.SMS";
    private static final String EXTRA_SMS_LIST = "com.gmsworldwide.kharlamov.greyroute.extra.SMS_LIST";
    private static final String EXTRA_SMSC_ADDRESS = "com.gmsworldwide.kharlamov.greyroute.extra.SMSC";

    private static final String PDU_KEY = "pdus";
    public static final String SMS_KEY = "sms";
    public static final String FILE_NAME_KEY = "file_name";
    public static final int RESULT_CODE_FAILURE = -1;
    public static final int RESULT_CODE_NEW_SMS = 1;
    public static final int RESULT_CODE_CSV_SAVED = 2;

    private static ValueEventListener sListener = null;
    private static final Object sListenerLock = new Object();

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

    public static void startActionMakeCSVReport(Context context, ArrayList<SmsBriefData> smsList, ResultReceiver receiver) {
        Intent intent = new Intent(context, SmsIntentService.class);
        intent.setAction(ACTION_MAKE_CSV_REPORT);
        intent.putExtra(EXTRA_SMS_LIST, smsList);
        intent.putExtra(EXTRA_LISTENER, receiver);
        context.startService(intent);
    }

    public static void startActionSyncSmscs(Context context) {
        Intent intent = new Intent(context, SmsIntentService.class);
        intent.setAction(ACTION_SYNC_SMSCS);
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
                case ACTION_MAKE_CSV_REPORT:
                    final ResultReceiver csvResultReceiver = intent.getParcelableExtra(EXTRA_LISTENER);
                    ArrayList<SmsBriefData> smsList = intent.getParcelableArrayListExtra(EXTRA_SMS_LIST);
                    handleActionMakeCSVReport(smsList, csvResultReceiver);
                    break;
                case ACTION_SYNC_SMSCS:
                    handleActionSyncSmscs();
                    break;
            }
        }
    }

    private void handleActionReceiveSms(Intent smsIntent) {
        ResultReceiver receiver = ServiceSinglets.getInstance().getReceiverContext();
        if (receiver !=null){
            Bundle bundle = new Bundle();
            Object[] pduArray = (Object[]) smsIntent.getExtras().get(PDU_KEY);
            if (pduArray != null) {
                SmsMessage[] messages = new SmsMessage[pduArray.length];
                for (int i = 0; i < pduArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                    SmsBriefData data = new SmsBriefData(messages[i].getServiceCenterAddress(),
                            messages[i].getDisplayMessageBody(), messages[i].getDisplayOriginatingAddress());
                    bundle.putParcelable(SMS_KEY, data);
                    receiver.send(RESULT_CODE_NEW_SMS, bundle);
                }
            }
        }
    }

    private void handleActionSetListener(ResultReceiver receiver) {
        ServiceSinglets.getInstance().setReceiverContext(receiver);
    }

    private void handleActionMakeCSVReport(ArrayList<SmsBriefData> smsList, ResultReceiver receiver){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String fileName = String.format("report_%s.csv", timeStamp);
        File reportFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(reportFile);
            fos.write(CSV_REPORT_HEADER.getBytes("utf-8"));
            for (SmsBriefData smsBriefData: smsList) {
                fos.write(String.format("%s;%s;%s\r\n", smsBriefData.getSmsc(),
                        smsBriefData.getTpOa(), smsBriefData.getText().replaceAll("\\s", " ")).getBytes("utf-8"));
            }
            fos.close();
            Log.d("test", "writeReportCSV: done "+reportFile.getAbsolutePath());
            Bundle bundle = new Bundle();
            bundle.putString(FILE_NAME_KEY, fileName);
            receiver.send(RESULT_CODE_CSV_SAVED, bundle);
        } catch (IOException e) {
            e.printStackTrace();
            receiver.send(RESULT_CODE_FAILURE, null);
        }
    }

    private void handleActionSyncSmscs() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        synchronized (sListenerLock){
            if (sListener == null){
                sListener = new ValueEventListener() { // TODO change to ChildEventListener
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot smscPattern : dataSnapshot.getChildren()) {
                            KnownSmsc smsc = new KnownSmsc(KnownSmsc.LEGALITY_AGGREGATOR, smscPattern.getValue().toString(), smscPattern.getKey());
                            Log.d("new_smsc_data", smsc.toString());
                            ContentValues cv = smsc.makeContentValues();
                            int rows = getContentResolver().update(SmscContentProvider.URI_KNOWN_SMSC, cv,
                                    DbHelper.createWhereStatement(cv, DbHelper.KnownSmscFields.SMSC_PREFIX), null);
                            Log.d("new_smsc_data", String.format("%d rows to update", rows));
                            if (rows == 0) {
                                getContentResolver().insert(SmscContentProvider.URI_KNOWN_SMSC, cv);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("service", databaseError.getMessage());
                        Thread.currentThread().interrupt();
                    }
                };
                reference.child("aggregator_smsc").addValueEventListener(sListener);
            }
        }
    }

    public static class ServiceSinglets {

        private ResultReceiver mReceiverContext;
        private static ServiceSinglets ourInstance;

        private ServiceSinglets() {
        }

        public static synchronized ServiceSinglets getInstance() {
            if (ourInstance == null) {
                ourInstance = new ServiceSinglets();
            }
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
