package com.gmsworldwide.kharlamov.greyroute;

import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ApplicationTestCase;

import com.gmsworldwide.kharlamov.greyroute.activities.MainActivity;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.robotium.solo.Condition;
import com.robotium.solo.Solo;


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

    public void testActivity() throws Exception {
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
}