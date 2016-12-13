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
import com.gmsworldwide.kharlamov.greyroute.fragments.SmsListFragment;
import com.gmsworldwide.kharlamov.greyroute.fragments.PermissionExplanationDialog;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
    implements PermissionExplanationDialog.OnFragmentInteractionListener,
        AnalyzeInboxFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private static final String UNKNOWN_MCC_MNC = "UNKNOWN";
    private static final int REQUEST_CODE_PERMISSION_READ_SMS = 2;
    private static final String TAG_ANALYSIS_FORM = "analysis_form";
    private static final String TAG_SMS_LIST = "sms_list";
    private Switch mSwRegisterReceiver;
    private ResultReceiver mReceiver;
    protected boolean mTaskSuccessful = false;

    public boolean isTaskSuccessful() {
        return mTaskSuccessful;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: add "analysed" flag
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment_container, AnalyzeInboxFragment.newInstance(), TAG_ANALYSIS_FORM)
                    .commit();
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
                // TODO handle report saving
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SmsIntentService.RESULT_CODE_NEW_SMS && resultData != null) {
                    SmsListFragment fragment = (SmsListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_smsc_list);
                    assert fragment != null;
                    fragment.addSmsBriefData((SmsBriefData) resultData.getParcelable(SmsIntentService.SMS_KEY));
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

    @Override
    public void onInboxAnalyzeRequested() {
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

    private void replaceFragmentSmsList() {
        // replace a fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_smsc_list);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG_ANALYSIS_FORM)
                .replace(R.id.fl_fragment_container, SmsListFragment.newInstance(), TAG_SMS_LIST)
                .commit();
    }
}
