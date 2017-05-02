package com.gmsworldwide.kharlamov.greyroute.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.gmsworldwide.kharlamov.greyroute.db.DbHelper;
import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;
import com.gmsworldwide.kharlamov.greyroute.provider.SmscContentProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SmscSyncService extends Service {
    public SmscSyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        ValueEventListener listener = new ValueEventListener() { // TODO change to ChildEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot smscPattern: dataSnapshot.getChildren()){
                    KnownSmsc smsc = new KnownSmsc(KnownSmsc.LEGALITY_AGGREGATOR, smscPattern.getValue().toString(), smscPattern.getKey());
                    Log.d("new_smsc_data", smsc.toString());
                    ContentValues cv = smsc.makeContentValues();
                    int rows = getContentResolver().update(SmscContentProvider.URI_KNOWN_SMSC, cv,
                            DbHelper.createWhereStatement(cv, DbHelper.KnownSmscFields.SMSC_PREFIX), null);
                    Log.d("new_smsc_data", String.format("%d rows to update", rows));
                    if (rows == 0) {
                        getContentResolver().insert(SmscContentProvider.URI_KNOWN_SMSC, cv);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("service", databaseError.getMessage());
                Thread.currentThread().interrupt();
            }
        };
        reference.child("aggregator_smsc").addValueEventListener(listener);
        return START_STICKY;
    }
}
