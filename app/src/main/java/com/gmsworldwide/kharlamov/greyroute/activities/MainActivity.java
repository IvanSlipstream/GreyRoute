package com.gmsworldwide.kharlamov.greyroute.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.fragments.AnalyzeInboxFragment;
import com.gmsworldwide.kharlamov.greyroute.fragments.ReportChooseDialog;
import com.gmsworldwide.kharlamov.greyroute.fragments.SmsListFragment;
import com.gmsworldwide.kharlamov.greyroute.fragments.PermissionExplanationDialog;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements PermissionExplanationDialog.OnFragmentInteractionListener,
        AnalyzeInboxFragment.OnFragmentInteractionListener,
        ReportChooseDialog.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final String UNKNOWN_MCC_MNC = "UNKNOWN";
    private static final int REQUEST_CODE_PERMISSION_READ_SMS = 2;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private static final String TAG_ANALYSIS_FORM = "analysis_form";
    private static final String TAG_SMS_LIST = "sms_list";
    private static final String TAG_REPORT_CHOICE_DIALOG = "report_choice";
    private static final String RETAIN_INSTANCE_KEY_SELECTION_PERIOD = "selection_period";
    private Switch mSwRegisterReceiver;
    private ResultReceiver mReceiver;
    protected boolean mTaskSuccessful = false;
    private long mSelectionPeriod = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment_container, AnalyzeInboxFragment.newInstance(), TAG_ANALYSIS_FORM)
                    .commit();
        } else {
            mSelectionPeriod = savedInstanceState.getLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, 0);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwRegisterReceiver = (Switch) findViewById(R.id.sw_register_receiver);
        mSwRegisterReceiver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Activity activity = (Activity) compoundButton.getContext();
                if (checked) {
                    if (hasPermissionReceiveSms()) {
                        // register our activity as a receiver
                        registerReceiveSmsListener(activity);
                    } else {
                        // request permissions
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.RECEIVE_SMS)){
                            PermissionExplanationDialog dialog =
                                    PermissionExplanationDialog.newInstance(getResources().getString(R.string.broadcast_sms_explanation),
                                            REQUEST_CODE_PERMISSION_READ_SMS);
                            dialog.show(getSupportFragmentManager(), TAG_EXPLANATION_DIALOG);
                        } else {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_PERMISSION_RECEIVE_SMS);
                        }
                    }
                } else {
                    // clear the listener
                    SmsIntentService.SmsReceiverContext.getInstance().setReceiverContext(null);
                }
            }
        });
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportChooseDialog dialog = ReportChooseDialog.newInstance();
                dialog.show(getSupportFragmentManager(), TAG_REPORT_CHOICE_DIALOG);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, mSelectionPeriod);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SmsIntentService.RESULT_CODE_NEW_SMS && resultData != null) {
                    SmsListFragment fragment = (SmsListFragment) getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
                    if (fragment != null) {
                        fragment.addSmsBriefData((SmsBriefData) resultData.getParcelable(SmsIntentService.SMS_KEY));
                    }
                }
            }
        };
        mSwRegisterReceiver.setChecked(hasPermissionReceiveSms() &&
                SmsIntentService.SmsReceiverContext.getInstance().getReceiverContext() != null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isPermissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_RECEIVE_SMS:
                if (isPermissionGranted) {
                    registerReceiveSmsListener(this);
                }
                mSwRegisterReceiver.setChecked(isPermissionGranted);
                break;
            case REQUEST_CODE_PERMISSION_READ_SMS:
                if (isPermissionGranted) {
                    replaceFragmentSmsList();
                }
                break;
        }
    }

    // callback methods

    @Override
    public void onInboxAnalyzeRequested(long selectionPeriod) {
        // a callback from AnalyzeInboxFragment
        // save selection period in seconds
        mSelectionPeriod = selectionPeriod;
        if (hasPermissionReadSms()){
            replaceFragmentSmsList();
        } else {
            // request permissions
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
                PermissionExplanationDialog dialog =
                        PermissionExplanationDialog.newInstance(getResources().getString(R.string.inbox_analyze_explanation),
                                REQUEST_CODE_PERMISSION_READ_SMS);
                dialog.show(getSupportFragmentManager(), TAG_EXPLANATION_DIALOG);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_PERMISSION_RECEIVE_SMS);
            }
        }
    }

    @Override
    public void onPermissionExplanationDismiss(int requestCode) {
        switch (requestCode){
            case REQUEST_CODE_PERMISSION_RECEIVE_SMS:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_PERMISSION_RECEIVE_SMS);
                break;
            case REQUEST_CODE_PERMISSION_READ_SMS:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_PERMISSION_READ_SMS);
                break;
        }
    }

    @Override
    public void onPushReportRequested() {
        ArrayList<SmsBriefData> smsList = null;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
        if (fragment != null && fragment instanceof SmsListFragment) {
            smsList = ((SmsListFragment) fragment).getSmsBriefDataList();
            for (SmsBriefData smsBriefData: smsList){
                sendSmscReport(smsBriefData);
            }
        }
        // TODO: handle error
    }

    @Override
    public void onCSVReportRequested() {

    }

    // util methods

    public boolean isTaskSuccessful() {
        return mTaskSuccessful;
    }

    private void registerReceiveSmsListener(Activity activity) {
        SmsIntentService.startActionSetListener(activity, mReceiver);
    }

    private boolean hasPermissionReceiveSms(){
        int receiveSmsPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        return (receiveSmsPermissions == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasPermissionReadSms(){
        int receiveSmsPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        return (receiveSmsPermissions == PackageManager.PERMISSION_GRANTED);
    }

    public boolean sendSmscReport(SmsBriefData data) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // default mcc and mnc
        String mSimOperator = UNKNOWN_MCC_MNC;
        if (manager != null) {
            mSimOperator = manager.getSimOperator();
        }
        Task<Void> task = mDatabase.child("new_smsc").child(mSimOperator).push().setValue(data.getSmsc());
        task.addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mTaskSuccessful = task.isSuccessful();
                Log.d("test", String.format("Task %s completed, %s.", task.toString(), task.isSuccessful() ? "success" : "failure"));
            }
        });
        return false;
    }

    private void replaceFragmentSmsList() {
        // replace a fragment with SMS list fragment
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG_ANALYSIS_FORM)
                .replace(R.id.fl_fragment_container, SmsListFragment.newInstance(mSelectionPeriod), TAG_SMS_LIST)
                .commit();
    }
}
