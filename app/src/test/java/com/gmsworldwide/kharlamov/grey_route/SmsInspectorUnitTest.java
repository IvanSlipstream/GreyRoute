package com.gmsworldwide.kharlamov.grey_route;

import com.gmsworldwide.kharlamov.grey_route.matcher.SmscMatcher;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class SmsInspectorUnitTest {

    @Test
    public void testSmscMatcher (){
        ArrayList<KnownSmsc> knownSmscs = new ArrayList<>();
        for (String prefix: new String[]{"+12345", "+5678", "+1234", "+123456", "+1234568"}){
            knownSmscs.add(new KnownSmsc(KnownSmsc.LEGALITY_AGGREGATOR, "carrier "+prefix, prefix));
        }
        SmscMatcher matcher = new SmscMatcher(knownSmscs);
        assertEquals("carrier +123456", matcher.matchSmscAddress("+1234567").getCarrierName());
    }
}