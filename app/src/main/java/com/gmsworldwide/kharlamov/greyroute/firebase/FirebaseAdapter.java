package com.gmsworldwide.kharlamov.greyroute.firebase;

import android.content.ContentValues;
import android.util.Log;

import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Slipstream-DESKTOP on 13.04.2017.
 */

public class FirebaseAdapter {

    // TODO make service
    private static final String TAG = "adapter";
    private DatabaseReference mReference;
    private ValueEventListener mListener;
    private FirebaseContext mFirebaseContext;

    public FirebaseAdapter(@NotNull FirebaseContext context) {
        this.mFirebaseContext = context;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    public void attach () {
        mListener = new ValueEventListener() { // TODO change to ChildEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot smscPattern: dataSnapshot.getChildren()){
                    KnownSmsc smsc = new KnownSmsc(KnownSmsc.LEGALITY_AGGREGATOR, smscPattern.getValue().toString(), smscPattern.getKey());
                    ContentValues cv = smsc.makeContentValues();
                    if (mFirebaseContext != null) {
                        mFirebaseContext.onNewSmscData(cv);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        };
        mReference.child("aggregator_smsc").addValueEventListener(mListener);
    }

    public void detach () {
        mReference.child("aggregator_smsc").removeEventListener(mListener);
        mListener = null;
    }

    public interface FirebaseContext {
        void onNewSmscData(ContentValues cv);
    }

}
