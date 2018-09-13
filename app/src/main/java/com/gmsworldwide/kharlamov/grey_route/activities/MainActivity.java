package com.gmsworldwide.kharlamov.grey_route.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.fragments.AnalyzeInboxFragment;
import com.gmsworldwide.kharlamov.grey_route.fragments.OnFragmentStateChangeListener;
import com.gmsworldwide.kharlamov.grey_route.fragments.ReportChooseDialog;
import com.gmsworldwide.kharlamov.grey_route.fragments.SaveLocationDialog;
import com.gmsworldwide.kharlamov.grey_route.fragments.SmsListFragment;
import com.gmsworldwide.kharlamov.grey_route.fragments.PermissionExplanationDialog;
import com.gmsworldwide.kharlamov.grey_route.fragments.SmscAnalyzeDialog;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;
import com.gmsworldwide.kharlamov.grey_route.models.SmsBriefData;
import com.gmsworldwide.kharlamov.grey_route.service.SmsIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        OnFragmentStateChangeListener,
        SaveLocationDialog.OnFragmentInteractionListener {

    public static final String UNKNOWN_MCC_MNC = "UNKNOWN";
    public static final String CSV_REPORT_HEADER = "Time received;GMT offset;SMSC;TP-OA;Text\r\n";

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_SMS = 1;
    private static final int REQUEST_CODE_PERMISSION_READ_SMS = 2;
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CODE_WRITE_REPORT_CSV = 12;
    private static final String TAG_EXPLANATION_DIALOG = "explanation";
    private static final String TAG_ANALYZE_INBOX = "analyze_inbox";
    private static final String TAG_SMS_LIST = "sms_list";
    private static final String TAG_REPORT_CHOICE_DIALOG = "report_choice";
    private static final String TAG_REPORT_PATH_CSV_DIALOG = "location_choice";
    private static final String TAG_CUSTOM_PERIOD_DIALOG = "custom_period";
    private static final String TAG_SMSC_DETAILS = "smsc_details";
    private static final String RETAIN_INSTANCE_KEY_SELECTION_PERIOD = "selection_period";
    private static final String RETAIN_INSTANCE_KEY_FAB_VISIBILITY = "fab_visible";
    private static final String RETAIN_INSTANCE_STATE = "state";
    private static final String RETAIN_INSTANCE_KEY_PATH_CSV = "path_csv";
    private static final byte CAUSE_NO_SMS_CHOSEN = 1;
    protected boolean mTaskSuccessful = false;
    private long mSelectionPeriod = 0;
    private boolean mFabVisible;
    private String mPathToSaveCSV = "";
    private STATE mCurrentState;
    private Toolbar mToolbar;
    private boolean mPaused = true;


    private enum STATE {
        GREETING, ANALYZE_INBOX, INBOX;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentState = STATE.GREETING;
        if (savedInstanceState != null) {
            mCurrentState = STATE.values()[savedInstanceState.getInt(RETAIN_INSTANCE_STATE, 0)];
            mPathToSaveCSV = savedInstanceState.getString(RETAIN_INSTANCE_KEY_PATH_CSV, "");
        }
        switch (mCurrentState) {
            case GREETING:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.cl_main, AnalyzeInboxFragment.newInstance(), TAG_ANALYZE_INBOX)
                        .commit();
                break;
            case ANALYZE_INBOX:
                mSelectionPeriod = savedInstanceState.getLong(RETAIN_INSTANCE_KEY_SELECTION_PERIOD, 0);
                mFabVisible = savedInstanceState.getBoolean(RETAIN_INSTANCE_KEY_FAB_VISIBILITY, true);
                break;
        }
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (mFabVisible) {
            fab.show();
        } else {
            fab.hide();
        }
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
        outState.putString(RETAIN_INSTANCE_KEY_PATH_CSV, mPathToSaveCSV);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SmsIntentService.startActionSyncSmscs(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.inflateMenu(R.menu.menu_main);
        if (mCurrentState != STATE.INBOX){
            menu.findItem(R.id.mi_check_all).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mi_check_all:
                if (mCurrentState == STATE.INBOX){
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SMS_LIST);
                    if (fragment != null && fragment instanceof SmsListFragment){
                        ((SmsListFragment) fragment).checkAll();
                    }
                }
                return true;
        }
        return false;
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
                    requestSaveLocation();
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
                        new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_PERMISSION_READ_SMS);
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
            requestSaveLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
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
    public void onPathDefined(String path) {
        mPathToSaveCSV = path;
        writeReportCSV();
    }

    @Override
    public void onFragmentResumed(Fragment fragment) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Menu menu = toolbar.getMenu();
        MenuItem menuItem = menu.findItem(R.id.mi_check_all);
        if (fragment instanceof SmsListFragment) {
            fab.show();
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
            mCurrentState = STATE.INBOX;
        }
        if (fragment instanceof AnalyzeInboxFragment){
            fab.hide();
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
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
        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(Calendar.getInstance().getTime());
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

    private void requestSaveLocation() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, R.string.error_no_external_storage, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPathToSaveCSV != null && mPathToSaveCSV.length() != 0) {
            mPathToSaveCSV = Environment.DIRECTORY_DOWNLOADS;
        }
        if (!mPaused){
            SaveLocationDialog dialog = new SaveLocationDialog();
            dialog.show(getSupportFragmentManager(), TAG_REPORT_PATH_CSV_DIALOG);
        }
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

    private void showCSVReportResult(String reportFileName, String folderName) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cl_main),
                getResources().getString(R.string.file_saved, reportFileName, folderName), Snackbar.LENGTH_LONG);
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
                        Toast.makeText(MainActivity.this, R.string.hint_unable_to_save, Toast.LENGTH_LONG).show();
                        break;
                    case SmsIntentService.RESULT_CODE_CSV_SAVED:
                        showCSVReportResult(resultData.getString(SmsIntentService.FILE_NAME_KEY),
                                resultData.getString(SmsIntentService.FILE_LOCATION_KEY, ""));
                        break;
                }
            }
        };
        SmsIntentService.startActionMakeCSVReport(this, smsList, receiver, mPathToSaveCSV);
    }
}
