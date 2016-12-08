package com.gmsworldwide.kharlamov.greyroute.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.fragments.PermissionExplanationDialog;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
    implements PermissionExplanationDialog.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private Switch mSwRegisterReceiver;
    private ResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                                    PermissionExplanationDialog.newInstance(getResources().getString(R.string.broadcast_sms_explanation));
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

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // TODO handle receiving SMS
            }
        };
        mSwRegisterReceiver.setChecked(hasPermissionReceiveSms() &&
                SmsIntentService.SmsReceiverContext.getInstance().getReceiverContext() != null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isPermissionGranted = false;
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_RECEIVE_SMS:
                isPermissionGranted =  (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                if (isPermissionGranted) {
                    registerReceiveSmsListener(this);
                }
                mSwRegisterReceiver.setChecked(isPermissionGranted);
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

    @Override
    public void onPermissionExplanationDismiss() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_PERMISSION_RECEIVE_SMS);
    }

    public boolean sendSmscReport(SmsBriefData data) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Task<Void> task = mDatabase.child("new_smsc").push().setValue(data.getSmsc());
        task.addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("test", "Task "+task.toString()+" completed.");
            }
        });
        return false;
    }
}
