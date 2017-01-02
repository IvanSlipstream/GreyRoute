package com.gmsworldwide.kharlamov.greyroute.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
    implements PermissionExplanationDialog.OnFragmentInteractionListener,
        AnalyzeInboxFragment.OnFragmentInteractionListener,
        ReportChooseDialog.OnFragmentInteractionListener, FragmentManager.OnBackStackChangedListener {

    public static final String UNKNOWN_MCC_MNC = "UNKNOWN";
    public static final String CSV_REPORT_HEADER = "SMSC;TP-OA;Text\r\n";

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final int REQUEST_CODE_PERMISSION_READ_SMS = 2;
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 3;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private static final String TAG_ANALYSIS_FORM = "analysis_form";
    private static final String TAG_SMS_LIST = "sms_list";
    private static final String TAG_REPORT_CHOICE_DIALOG = "report_choice";
    private static final String RETAIN_INSTANCE_KEY_SELECTION_PERIOD = "selection_period";
    private static final String RETAIN_INSTANCE_KEY_FAB_VISIBILITY = "fab_visible";
    private Switch mSwRegisterReceiver;
    private ResultReceiver mReceiver;
    protected boolean mTaskSuccessful = false;
    private long mSelectionPeriod = 0;
    private boolean mFabVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment_container, AnalyzeInboxFragment.newInstance(), TAG_ANALYSIS_FORM)
                    .commit();
        } else {
            mSelectionPeriod = savedInstanceState.getLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, 0);
            mFabVisible = savedInstanceState.getBoolean(RETAIN_INSTANCE_KEY_FAB_VISIBILITY, true);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwRegisterReceiver = (Switch) findViewById(R.id.sw_register_receiver);

        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(mFabVisible ? View.VISIBLE : View.GONE);
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
        outState.putBoolean(RETAIN_INSTANCE_KEY_FAB_VISIBILITY, mFabVisible);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            case REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (isPermissionGranted) {
                    writeReportCSV();
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
        setTitle(R.string.app_name);
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
        ArrayList<SmsBriefData> smsList;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
        if (fragment != null && fragment instanceof SmsListFragment) {
            smsList = ((SmsListFragment) fragment).getCheckedSmsBriefDataList();
            for (SmsBriefData smsBriefData: smsList){
                sendSmscReport(smsBriefData);
            }
        } else {
            Toast.makeText(this, R.string.hint_no_sms_chosen, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCSVReportRequested() {
        if (hasPermissionWriteExternalStorage()){
            writeReportCSV();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
            // no need to explain permission to the user
            // as we already have a callback from a fragment explaining it
        }
    }

    @Override
    public void onInboxAnalyzeFragmentResumed(){
        setTitle(R.string.title_analyze_inbox);
    }

    @Override
    public void onBackStackChanged() {
        // show or hide our FAB depending on SmsListFragment visible or not
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab == null) {
            return;
        }
        if (isFragmentOnTop(fragment)) {
            fab.setVisibility(View.VISIBLE);
            mFabVisible = true;
        } else {
            fab.setVisibility(View.GONE);
            mFabVisible = false;
        }
    }

    // util methods

    public boolean isTaskSuccessful() {
        return mTaskSuccessful;
    }

    @Contract("null -> false")
    private boolean isFragmentOnTop(Fragment fragment) {
        return fragment != null && fragment.isVisible() && fragment.isResumed();
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

    private boolean hasPermissionWriteExternalStorage(){
        int receiveSmsPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                showPushResult(mTaskSuccessful);
            }
        });
        return false;
    }


    private void showPushResult(boolean success) {
        int resId = success ? R.string.push_sent_success : R.string.push_sent_failure;
        Snackbar.make(findViewById(R.id.cl_main), getResources().getString(resId), Snackbar.LENGTH_LONG).show();
    }

    private void showCSVReportResult(String reportFileName) {
        Snackbar.make(findViewById(R.id.cl_main), getResources().getString(R.string.file_saved, reportFileName), Snackbar.LENGTH_LONG).show();
    }

    private void replaceFragmentSmsList() {
        // replace a fragment with SMS list fragment
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG_ANALYSIS_FORM)
                .replace(R.id.fl_fragment_container, SmsListFragment.newInstance(mSelectionPeriod), TAG_SMS_LIST)
                .commit();
    }

    private void writeReportCSV() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.error_no_external_storage, Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<SmsBriefData> smsList;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
        if (fragment != null && fragment instanceof SmsListFragment) {
            smsList = ((SmsListFragment) fragment).getCheckedSmsBriefDataList();
        } else {
            Toast.makeText(this, R.string.hint_no_sms_chosen, Toast.LENGTH_SHORT).show();
            return;
        }
        ResultReceiver receiver = new ResultReceiver(new Handler()){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode){
                    case SmsIntentService.RESULT_CODE_FAILURE:
                        break;
                    case SmsIntentService.RESULT_CODE_CSV_SAVED:
                        showCSVReportResult(resultData.getString(SmsIntentService.FILE_NAME_KEY));
                        break;
                }
            }
        };
        SmsIntentService.startActionMakeCSVReport(this, smsList, receiver);
    }
}
