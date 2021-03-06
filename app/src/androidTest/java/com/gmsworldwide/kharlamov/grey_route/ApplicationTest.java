package com.gmsworldwide.kharlamov.grey_route;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.test.rule.ActivityTestRule;
import android.widget.EditText;

import com.gmsworldwide.kharlamov.grey_route.activities.MainActivity;
import com.gmsworldwide.kharlamov.grey_route.fragments.SaveLocationDialog;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;
import com.gmsworldwide.kharlamov.grey_route.models.SmsBriefData;
import com.gmsworldwide.kharlamov.grey_route.service.SmsIntentService;
import com.robotium.solo.Condition;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApplicationTest extends ActivityTestRule<MainActivity> {

    private Solo solo;

    public ApplicationTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        launchActivity(new Intent(Intent.ACTION_DEFAULT));
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testPushMessage() throws Exception {
        solo.assertCurrentActivity("wrong activity", MainActivity.class);
        final MainActivity activity = getActivity();
        SmsBriefData data = new SmsBriefData("0443736994", "test", "Test");
        activity.sendSmscReport(data);
        solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return activity.isTaskSuccessful();
            }
        }, 50000);
        assertTrue("Task has failed.", activity.isTaskSuccessful());
    }

    @Test
    public void testCSVFile() throws Exception {
        final boolean[] completed = {false};
        solo.assertCurrentActivity("wrong activity", MainActivity.class);
        final MainActivity activity = getActivity();
        ArrayList<SmsBriefData> smsList = new ArrayList<>();
        SmsBriefData data = new SmsBriefData("380952823116", "test1", "Test1");
        smsList.add(data);
        data = new SmsBriefData("3806661488", "test2", "Test2");
        smsList.add(data);
        final ResultReceiver receiver = new ResultReceiver(new Handler(activity.getMainLooper())){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                try {
                    assertEquals(resultCode, SmsIntentService.RESULT_CODE_CSV_SAVED);
                    String fileName = resultData.getString(SmsIntentService.FILE_NAME_KEY);
                    assert fileName != null;
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                    FileReader fileReader = new FileReader(file);
                    String resultExpected = "SMSC;TP-OA;Text\r\n380952823116;Test1;test1\r\n3806661488;Test2;test2\r\n";
                    char[] cbuf = new char[resultExpected.length()];
                    assertEquals("error reading file", resultExpected.length(), fileReader.read(cbuf));
                    String resultActual = new String(cbuf);
                    assertEquals("file content does not match", resultExpected, resultActual);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                completed[0] = true;
            }
        };
        SmsIntentService.startActionMakeCSVReport(activity, smsList, receiver, mPathToSaveCSV);
        solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return completed[0];
            }
        }, 50000);
    }

    @Test
    public void testSmscDetailsFragment() throws Exception {
        solo.assertCurrentActivity("wrong activity", MainActivity.class);
        MainActivity activity = getActivity();
        activity.onLegalityButtonClicked(new KnownSmsc(KnownSmsc.LEGALITY_GREY, "all", "+38050000405"));
        solo.waitForView(solo.getView(R.id.btn_submit));
        solo.clickOnView(solo.getView(R.id.btn_submit));
        solo.sleep(30000);
        solo.waitForView(solo.getView(R.id.et_user_reason));
        solo.enterText((EditText) solo.getView(R.id.et_user_reason), "Vodafone UA");
        solo.clickOnView(solo.getView(R.id.btn_submit));
    }


    @Test
    public void testFileDialog() throws Exception {
        solo.assertCurrentActivity("wrong activity", MainActivity.class);
        MainActivity activity = getActivity();
        SaveLocationDialog dialog = new SaveLocationDialog();
        dialog.show(activity.getSupportFragmentManager(), "file_choose");
        solo.sleep(30000);
    }

}