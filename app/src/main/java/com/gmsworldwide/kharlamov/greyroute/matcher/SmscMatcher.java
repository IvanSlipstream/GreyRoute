package com.gmsworldwide.kharlamov.greyroute.matcher;

import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;

import java.util.ArrayList;

/**
 * Created by Slipstream-DESKTOP on 09.05.2017.
 */

public class SmscMatcher {

    private ArrayList<KnownSmsc> mKnownSmscList = new ArrayList<>();

    public SmscMatcher(ArrayList<KnownSmsc> smscList) {
        this.mKnownSmscList = smscList;
    }

    public void setSmscList(ArrayList<KnownSmsc> smscList) {
        this.mKnownSmscList = smscList;
    }

    public KnownSmsc matchSmscAddress(String smscAddress) {
        KnownSmsc result = null;
        int matchingDigits = 0;
        for (KnownSmsc knownSmsc: mKnownSmscList) {
            if (smscAddress.startsWith(knownSmsc.getSmscPrefix())){
                if (matchingDigits < knownSmsc.getSmscPrefix().length()) {
                    matchingDigits = knownSmsc.getSmscPrefix().length();
                    result = knownSmsc;
                }
            }
        }
        return result;
    }
}
