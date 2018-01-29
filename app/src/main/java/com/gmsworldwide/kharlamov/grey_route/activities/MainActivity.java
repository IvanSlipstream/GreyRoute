package com.gmsworldwide.kharlamov.grey_route.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.fragments.AnalyzeInboxFragment;
import com.gmsworldwide.kharlamov.grey_route.fragments.OnFragmentStateChangeListener;
import com.gmsworldwide.kharlamov.grey_route.fragments.ReportChooseDialog;
import com.gmsworldwide.kharlamov.grey_route.fragments.SmsListFragment;
import com.gmsworldwide.kharlamov.grey_route.fragments.PermissionExplanationDialog;
import com.gmsworldwide.kharlamov.grey_route.fragments.SmscAnalyzeDialog;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;
import com.gmsworldwide.kharlamov.grey_route.models.SmsBriefData;
import com.gmsworldwide.kharlamov.grey_route.provider.ReportFileProvider;
import com.gmsworldwide.kharlamov.grey_route.service.SmsIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
    implements PermissionExplanationDialog.OnFragmentInteractionListener,
        AnalyzeInboxFragment.OnFragmentInteractionListener,
        ReportChooseDialog.OnFragmentInteractionListener,
        SmsListFragment.OnFragmentInteractionListener,
        SmscAnalyzeDialog.OnFragmentInteractionListener,
        OnFragmentStateChangeListener {

    public static final String UNKNOWN_MCC_MNC = "UNKNOWN";
    public static final String CSV_REPORT_HEADER = "SMSC;Time;TP-OA;Text\r\n";

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final int REQUEST_CODE_PERMISSION_READ_SMS = 2;
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CODE_WRITE_REPORT_CSV = 12;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private static final String TAG_ANALYZE_INBOX = "analyze_inbox";
    private static final String TAG_SMS_LIST = "sms_list";
    private static final String TAG_REPORT_CHOICE_DIALOG = "report_choice";
    private static final String TAG_CUSTOM_PERIOD_DIALOG = "custom_period";
    private static final String TAG_SMSC_DETAILS = "smsc_details";
    private static final String RETAIN_INSTANCE_KEY_SELECTION_PERIOD = "selection_period";
    private static final String RETAIN_INSTANCE_KEY_FAB_VISIBILITY = "fab_visible";
    private static final String RETAIN_INSTANCE_STATE = "state";
    private static final byte CAUSE_NO_SMS_CHOSEN = 1;
    protected boolean mTaskSuccessful = false;
    private long mSelectionPeriod = 0;
    private boolean mFabVisible;
    private STATE mCurrentState;

    private enum STATE {
        GREETING, ANALYZE_INBOX, INBOX
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentState = STATE.GREETING;
        if (savedInstanceState != null) {
            mCurrentState = STATE.values()[savedInstanceState.getInt(RETAIN_INSTANCE_STATE, 0)];
        }
        switch (mCurrentState) {
            case GREETING:
                // TODO: 01.11.2017 make greeting
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.cl_main, AnalyzeInboxFragment.newInstance(), TAG_ANALYZE_INBOX)
                        .commit();
                break;
            case ANALYZE_INBOX:
                mSelectionPeriod = savedInstanceState.getLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, 0);
                mFabVisible = savedInstanceState.getBoolean(RETAIN_INSTANCE_KEY_FAB_VISIBILITY, true);
                break;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(mFabVisible ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSelectedSmsList() == null) return;
                ReportChooseDialog dialog = ReportChooseDialog.newInstance();
                dialog.show(getSupportFragmentManager(), TAG_REPORT_CHOICE_DIALOG);
            }
        });
        SmsIntentService.startActionSyncSmscs(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, mSelectionPeriod);
        outState.putBoolean(RETAIN_INSTANCE_KEY_FAB_VISIBILITY, mFabVisible);
        outState.putInt(RETAIN_INSTANCE_STATE, mCurrentState.ordinal());
    }

    @Override
    protected void onStart() {
        super.onStart();
        SmsIntentService.startActionSyncSmscs(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isPermissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        switch (requestCode) {
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
        if (hasPermission(Manifest.permission.READ_SMS)){
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
            case REQUEST_CODE_PERMISSION_READ_SMS:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_PERMISSION_READ_SMS);
                break;
        }
    }

    @Override
    public void onPushReportRequested() {
        ArrayList<SmsBriefData> smsList = getSelectedSmsList();
        if (smsList != null) {
            for (SmsBriefData smsBriefData: smsList){
                sendSmscReport(smsBriefData);
            }
        }
    }

    @Override
    public void onCSVReportRequested() {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            writeReportCSV();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onMessageSubmit(KnownSmsc knownSmsc, String reason) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String simOperator = getSimOperator();
        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Task<Void> task = reference.child("not_gray").child(simOperator).child(knownSmsc.getSmscPrefix())
                .child(dateString).setValue(reason);
        task.addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mTaskSuccessful = task.isSuccessful();
                Log.d("test", String.format("Task %s completed, %s.", task.toString(), task.isSuccessful() ? "success" : "failure"));
                showPushResult(mTaskSuccessful);
            }
        });
    }

    @Override
    public void onLegalityButtonClicked(KnownSmsc knownSmsc) {
        SmscAnalyzeDialog dialog = SmscAnalyzeDialog.newInstance(knownSmsc);
        dialog.show(getSupportFragmentManager(), TAG_SMSC_DETAILS);
    }

    @Override
    public void onFragmentResumed(Fragment fragment) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fragment instanceof SmsListFragment) {
            fab.setVisibility(View.VISIBLE);
            mCurrentState = STATE.INBOX;
        }
        if (fragment instanceof AnalyzeInboxFragment){
            fab.setVisibility(View.GONE);
            mCurrentState = STATE.ANALYZE_INBOX;
            setTitle(R.string.title_analyze_inbox);
            Log.d("test-"+getClass().getSimpleName(), "InboxAnalyzeFragment resumed, changing title");
        }
    }

    // util methods


    public boolean isTaskSuccessful() {
        return mTaskSuccessful;
    }

    private boolean hasPermission(@NonNull String permission){
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean sendSmscReport(SmsBriefData data) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (reference == null) {
            showPushResult(false);
            return false;
        }
        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        // default mcc and mnc
        String simOperator = getSimOperator();
        Task<Void> task = reference.child("new_smsc").child(simOperator)
                .child(data.getSmsc() != null ? data.getSmsc() : "null").setValue(dateString);
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

    private String getSimOperator() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // default mcc and mnc
        String simOperator = UNKNOWN_MCC_MNC;
        if (manager != null) {
            simOperator = manager.getSimOperator();
        }
        return simOperator;
    }

    @Nullable
    private ArrayList<SmsBriefData> getSelectedSmsList() {
        ArrayList<SmsBriefData> smsList;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
        if (fragment != null && fragment instanceof SmsListFragment) {
            smsList = ((SmsListFragment) fragment).getCheckedSmsBriefDataList();
            if (smsList != null && smsList.size() != 0) {
                return smsList;
            }
        }
        showReportFailure(CAUSE_NO_SMS_CHOSEN);
        return null;
    }

    private void showPushResult(boolean success) {
        int resId = success ? R.string.push_sent_success : R.string.push_sent_failure;
        Snackbar.make(findViewById(R.id.cl_main), getResources().getString(resId), Snackbar.LENGTH_LONG).show();
    }

    private void showCSVReportResult(String reportFileName) {
        String folderName = getResources().getString(R.string.downloads);
        String location = Environment.DIRECTORY_DOWNLOADS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            folderName = getResources().getString(R.string.documents);
            location = Environment.DIRECTORY_DOCUMENTS;
        }
        final String reportPath = Environment.getExternalStoragePublicDirectory(location) + File.separator + reportFileName;
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cl_main),
                getResources().getString(R.string.file_saved, reportFileName, folderName), Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.hint_open_csv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                Uri uri;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = ReportFileProvider.getUriForFile(v.getContext(),
                            v.getContext().getApplicationContext().getPackageName() + ".provider",
                            new File(reportPath));
                } else {
                    File csvFile = new File(reportPath);
                    uri = Uri.fromFile(csvFile);
                }
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeTypeString = mimeTypeMap.getMimeTypeFromExtension("csv");
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeTypeString);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(), R.string.hint_no_csv_handler, Toast.LENGTH_LONG).show();
                }
            }
        });
        snackbar.show();

    }

    private void showReportFailure(int cause) {
        switch (cause) {
            case CAUSE_NO_SMS_CHOSEN:
                Snackbar.make(findViewById(R.id.cl_main), R.string.hint_no_sms_chosen, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private void replaceFragmentSmsList() {
        // replace a fragment with SMS list fragment
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG_ANALYZE_INBOX)
                .replace(R.id.cl_main, SmsListFragment.newInstance(mSelectionPeriod), TAG_SMS_LIST)
                .commitAllowingStateLoss();
    }

    private void writeReportCSV() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.error_no_external_storage, Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<SmsBriefData> smsList = getSelectedSmsList();
        if (smsList == null) {
            return;
        }
        ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case SmsIntentService.RESULT_CODE_FAILURE:
                        break;
                    case SmsIntentService.RESULT_CODE_CSV_SAVED:
                        showCSVReportResult(resultData.getString(SmsIntentService.FILE_NAME_KEY)
                        );
                        break;
                }
            }
        };
        SmsIntentService.startActionMakeCSVReport(this, smsList, receiver);
    }
}
