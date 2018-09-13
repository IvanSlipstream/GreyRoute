package com.gmsworldwide.kharlamov.grey_route.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.adapter.SmsListAdapter;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;
import com.gmsworldwide.kharlamov.grey_route.models.SmsBriefData;
import com.gmsworldwide.kharlamov.grey_route.provider.SmscContentProvider;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SmsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final Uri URI_SMS_INBOX = Uri.parse("content://sms/inbox");
    private static final String INBOX_SORT_ORDER = "date desc";
    private static final String INBOX_DATE_FIELD = "date";

    private static final int LOADER_ID_INBOX = 1;
    private static final int LOADER_ID_KNOWN_SMSCS = 2;

    private static final String RETAIN_INSTANCE_KEY_CHECKED_LIST = "checked_list";
    private static final String RETAIN_INSTANCE_KEY_SCROLL_Y = "scroll_y";
    private static final String KEY_SELECTION_PERIOD = "selection_period";
    private RecyclerView mRecyclerView;
    private SmsListAdapter mAdapter;
    private long mSelectionPeriod;
    private ContentObserver mKnownSmscObserver;
    private OnFragmentStateChangeListener mStateChangeListener;
    private OnFragmentInteractionListener mListener;

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
        mAdapter = new SmsListAdapter(null, mListener);
        if (getArguments() != null) {
            mSelectionPeriod = getArguments().getLong(KEY_SELECTION_PERIOD, 0);
        }
        if (savedInstanceState != null) {
            ArrayList<Long> checkedList = new ArrayList<>();
            long[] checkedArray = savedInstanceState.getLongArray(RETAIN_INSTANCE_KEY_CHECKED_LIST);
            if (checkedArray != null) {
                for (long id : checkedArray) {
                    checkedList.add(id);
                }
            }
            mAdapter.setCheckedList(checkedList);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentStateChangeListener &&
                context instanceof OnFragmentInteractionListener){
            mStateChangeListener = (OnFragmentStateChangeListener) context;
            mListener = (OnFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()+
                    "must implement OnFragmentStateChangeListener " +
                    "and OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_sms_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            mRecyclerView.setScrollY(savedInstanceState.getInt(RETAIN_INSTANCE_KEY_SCROLL_Y, 0));
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SmsListAdapter adapter = (SmsListAdapter) mRecyclerView.getAdapter();
        ArrayList<Long> checkedList = adapter.getCheckedList();
        long[] checkedArray = new long[checkedList.size()];
        for (int i=0;i<checkedList.size();i++){
            checkedArray[i] = checkedList.get(i);
        }
        int scroll = mRecyclerView.getScrollY();
        outState.putInt(RETAIN_INSTANCE_KEY_SCROLL_Y, scroll);
        outState.putLongArray(RETAIN_INSTANCE_KEY_CHECKED_LIST, checkedArray);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mKnownSmscObserver != null) {
            Objects.requireNonNull(getContext()).getContentResolver().unregisterContentObserver(mKnownSmscObserver);
            mKnownSmscObserver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mStateChangeListener.onFragmentResumed(this);
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(LOADER_ID_INBOX, null, this);
        loaderManager.initLoader(LOADER_ID_KNOWN_SMSCS, null, this);
        mKnownSmscObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mAdapter.notifyDataSetChanged();
            }
        };
        Objects.requireNonNull(getContext()).getContentResolver().registerContentObserver(SmscContentProvider.URI_KNOWN_SMSC, true, mKnownSmscObserver);
    }

    public ArrayList<SmsBriefData> getCheckedSmsBriefDataList(){
        return mAdapter.getSmsList(true);
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
                mAdapter.swapCursor(c);
                mAdapter.notifyDataSetChanged();
                break;
            case LOADER_ID_KNOWN_SMSCS:
                if (c != null) {
                    ArrayList<KnownSmsc> resultKnownSmsc = new ArrayList<>();
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        resultKnownSmsc.add(new KnownSmsc(c));
                        c.moveToNext();
                    }
                    mAdapter.setKnownSmscList(resultKnownSmsc);
                    mAdapter.notifyDataSetChanged();
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
        mAdapter.swapCursor(null);
    }

    public void checkAll() {
        ArrayList<Long> checkedList = mAdapter.getCheckedList();
        if (checkedList.size() == mAdapter.getItemCount()){
            checkedList = new ArrayList<>();
        } else {
            for (SmsBriefData smsBriefData:
                    mAdapter.getSmsList(false)){
                long id = smsBriefData.getId();
                if (!checkedList.contains(id)){
                    checkedList.add(id);
                }
            }
        }
        mAdapter.setCheckedList(checkedList);
        mAdapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener{
        void onLegalityButtonClicked(KnownSmsc knownSmsc);
    }

}
