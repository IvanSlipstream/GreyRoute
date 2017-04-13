package com.gmsworldwide.kharlamov.greyroute.firebase;

import android.content.ContentValues;
import android.util.Log;

import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Slipstream-DESKTOP on 13.04.2017.
 */

class FirebaseAdapter {

    private static final String TAG = "adapter";
    private DatabaseReference mReference;
    // TODO remove Singleton and make attaching to Activity

    private static final FirebaseAdapter ourInstance = new FirebaseAdapter();

    static FirebaseAdapter getInstance() {
        return ourInstance;
    }

    private FirebaseAdapter() {
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    private void loadAggregatorSmscs () {
        mReference.child("aggregator_smsc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot smscPattern: dataSnapshot.getChildren()){
                    KnownSmsc smsc = new KnownSmsc(KnownSmsc.LEGALITY_AGGREGATOR, smscPattern.getValue().toString(), smscPattern.getKey());
                    ContentValues cv = smsc.makeContentValues();
                    // TODO call to content provider
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
}
