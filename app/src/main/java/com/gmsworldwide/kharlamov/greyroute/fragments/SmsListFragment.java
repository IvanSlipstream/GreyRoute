package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.adapter.SmsListAdapter;
import com.gmsworldwide.kharlamov.greyroute.matcher.SmscMatcher;
import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.provider.SmscContentProvider;

import java.util.ArrayList;
import java.util.Locale;

public class SmsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final Uri URI_SMS_INBOX = Uri.parse("content://sms/inbox");
    private static final String INBOX_SORT_ORDER = "date desc";
    private static final String INBOX_DATE_FIELD = "date";

    private static final int LOADER_ID_INBOX = 1;
    private static final int LOADER_ID_KNOWN_SMSCS = 2;

    private static final String RETAIN_INSTANCE_KEY_CHECKED_LIST = "checked_list";
    private static final String KEY_SELECTION_PERIOD = "selection_period";
    private RecyclerView mRecyclerView;
    private SmsListAdapter adapter;
    private long mSelectionPeriod;
    private boolean mReloadInbox = true;
    private ContentObserver mKnownSmscObserver;

    public SmsListFragment() {
    }

    public static SmsListFragment newInstance(long mSelectionPeriod) {
        
        Bundle args = new Bundle();
        args.putLong(KEY_SELECTION_PERIOD, mSelectionPeriod);
        SmsListFragment fragment = new SmsListFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SmsListAdapter();
        if (getArguments() != null) {
            mSelectionPeriod = getArguments().getLong(KEY_SELECTION_PERIOD, 0);
        }
        if (savedInstanceState != null) {
            adapter.setCheckedList(savedInstanceState.getIntegerArrayList(RETAIN_INSTANCE_KEY_CHECKED_LIST));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rv_sms_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SmsListAdapter adapter = (SmsListAdapter) mRecyclerView.getAdapter();
        outState.putIntegerArrayList(RETAIN_INSTANCE_KEY_CHECKED_LIST, adapter.getCheckedList());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mKnownSmscObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mKnownSmscObserver);
            mKnownSmscObserver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        // check if the user has set a new selection period and thus reload or not the SMS Inbox
        if (mReloadInbox) {
            restartLoader(LOADER_ID_INBOX);
        } else {
            loaderManager.initLoader(LOADER_ID_INBOX, null, this);
        }
        mReloadInbox = false;
        initLoader(LOADER_ID_KNOWN_SMSCS);
    }

    private void restartLoader(int id) {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(id, null, this);
    }

    private void initLoader(int id) {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(id, null, this);
    }

    public void addSmsBriefData(SmsBriefData data) {
        if (adapter != null) {
            adapter.addSmsBriefData(data);
            adapter.notifyDataSetChanged();
        }
    }

    public ArrayList<SmsBriefData> getCheckedSmsBriefDataList(){
        ArrayList<SmsBriefData> result = new ArrayList<>();
        ArrayList<SmsBriefData> localList = adapter.getSmsBriefDataList();
        if (adapter != null) {
            for (int index: adapter.getCheckedList()){
                result.add(localList.get(index));
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public CursorLoader onCreateLoader(final int id, Bundle args) {
        switch (id) {
            case LOADER_ID_INBOX:
                String condition = String.format(Locale.getDefault(),
                                "%s > %d", INBOX_DATE_FIELD, mSelectionPeriod);
                return new CursorLoader(getContext(), URI_SMS_INBOX, null, condition, null, INBOX_SORT_ORDER);
            case LOADER_ID_KNOWN_SMSCS:
                return new CursorLoader(getContext(), SmscContentProvider.URI_KNOWN_SMSC, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        switch (loader.getId()){
            case LOADER_ID_INBOX:
                ArrayList<SmsBriefData> resultInbox = new ArrayList<>();
                if (c != null) {
                    while (c.moveToNext()) {
                        resultInbox.add(new SmsBriefData(c));
                    }
                }
                adapter.setSmsBriefDataList(resultInbox);
                adapter.notifyDataSetChanged();
                break;
            case LOADER_ID_KNOWN_SMSCS:
                if (c != null) {
                    ArrayList<KnownSmsc> resultKnownSmsc = new ArrayList<>();
                    while (c.moveToNext()) {
                        resultKnownSmsc.add(new KnownSmsc(c));
                    }
                    adapter.setKnownSmscList(resultKnownSmsc);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (getContext() != null && mKnownSmscObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mKnownSmscObserver);
            mKnownSmscObserver = null;
        }
    }

}
