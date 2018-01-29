package com.gmsworldwide.kharlamov.grey_route.matcher;

import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;

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

    /**
     * Matches given SMSC address against known SMSC addresses
     * @param smscAddress SMSC address to match
     * @return object representing match result
     */
    public KnownSmsc matchSmscAddress(String smscAddress) {
        KnownSmsc result = null;
        int matchingDigits = 0;
        for (KnownSmsc knownSmsc: mKnownSmscList) {
            if (smscAddress != null && smscAddress.startsWith(knownSmsc.getSmscPrefix())){
                if (matchingDigits < knownSmsc.getSmscPrefix().length()) {
                    matchingDigits = knownSmsc.getSmscPrefix().length();
                    result = knownSmsc;
                }
            }
        }
        return result;
    }
}
