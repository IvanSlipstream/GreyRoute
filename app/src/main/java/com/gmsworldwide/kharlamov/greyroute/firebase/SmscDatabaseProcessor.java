package com.gmsworldwide.kharlamov.greyroute.firebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.os.ResultReceiver;
import android.util.Log;

import com.gmsworldwide.kharlamov.greyroute.models.SmscMatch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Slipstream on 30.03.2017 in GreyRoute.
 */

public class SmscDatabaseProcessor {

    public static final String LOG_TAG = "test_match_smsc";
    public static final String KEY_DATABASE_ERROR = "db_error";
    public static final String KEY_SMSC_MATCH = "match";
    public static final int RESULT_CODE_MATCH = 1;
    public static final int RESULT_CODE_FAILURE = -1;

    private DatabaseReference mReference;
    private ResultReceiver mReceiver;

    public SmscDatabaseProcessor(@NonNull ResultReceiver receiver) {
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.mReceiver = receiver;
    }

    /**
     * Matches given SMSC addresses against patterns in database.
     * Result is sent over receiver.
     * @param smscs array of SMSC addresses
     */
    public void matchSmscAddress (final String[] smscs) {
        mReference.child("aggregator_smsc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot smscPattern: dataSnapshot.getChildren()){
                    for (String smsc: smscs){
                        if (smsc.startsWith(smscPattern.getKey())) {
                            SmscMatch match = new SmscMatch(smscPattern.getKey().length(), smscPattern.getValue().toString(), smsc);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(KEY_SMSC_MATCH, match);
                            mReceiver.send(RESULT_CODE_MATCH, bundle);
                            Log.d(LOG_TAG, match.toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DATABASE_ERROR, databaseError.getMessage());
                mReceiver.send(RESULT_CODE_FAILURE, bundle);
            }
        });
    }

    public void matchSmscAddress (final String smsc) {
        final SmscMatch match = new SmscMatch(0, null, smsc);
        mReference.child("aggregator_smsc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bundle bundle = new Bundle();
                for (DataSnapshot smscPattern: dataSnapshot.getChildren()){
                    if (smsc.startsWith(smscPattern.getKey())) {
                        SmscMatch newMatch = new SmscMatch(smscPattern.getKey().length(), smscPattern.getValue().toString(), smsc);
                        match.override(newMatch);
                    }
                }
                bundle.putParcelable(KEY_SMSC_MATCH, match);
                mReceiver.send(RESULT_CODE_MATCH, bundle);
                Log.d(LOG_TAG, match.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DATABASE_ERROR, databaseError.getMessage());
                mReceiver.send(RESULT_CODE_FAILURE, bundle);
            }
        });
    }
}
