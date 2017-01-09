package com.gmsworldwide.kharlamov.greyroute;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ActivityInstrumentationTestCase2;

import com.gmsworldwide.kharlamov.greyroute.activities.MainActivity;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;
import com.robotium.solo.Condition;
import com.robotium.solo.Solo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class ApplicationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public ApplicationTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
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
        SmsIntentService.startActionMakeCSVReport(activity, smsList, receiver);
        solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return completed[0];
            }
        }, 50000);
    }
}