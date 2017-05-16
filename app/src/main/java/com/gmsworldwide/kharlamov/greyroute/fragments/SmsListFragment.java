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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;
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
        adapter = new SmsListAdapter();
        if (getArguments() != null) {
            mSelectionPeriod = getArguments().getLong(KEY_SELECTION_PERIOD, 0);
        }
        if (savedInstanceState != null) {
            adapter.setCheckedList(savedInstanceState.getIntegerArrayList(RETAIN_INSTANCE_KEY_CHECKED_LIST));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
//            mRecyclerView.swapAdapter(adapter, false);
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
//                mRecyclerView.swapAdapter(adapter, false);
                break;
            case LOADER_ID_KNOWN_SMSCS:
                if (c != null) {
                    ArrayList<KnownSmsc> resultKnownSmsc = new ArrayList<>();
                    while (c.moveToNext()) {
                        resultKnownSmsc.add(new KnownSmsc(c));
                    }
                    adapter.setKnownSmscList(resultKnownSmsc);
                    adapter.notifyDataSetChanged();
//                    mRecyclerView.swapAdapter(adapter, false);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getContext().getContentResolver().unregisterContentObserver(mKnownSmscObserver);
        mKnownSmscObserver = null;
    }

    private class SmsHolder extends RecyclerView.ViewHolder {

        private TextView mTvSmscAddress;
        private TextView mTvText;
        private TextView mTvTpOa;
        private CheckBox mCbMarked;
        private ImageView mIvLegality;
        private boolean mMarked;

        SmsHolder(View itemView) {
            super(itemView);
            mTvSmscAddress = (TextView) itemView.findViewById(R.id.tv_smsc_address);
            mTvText = (TextView) itemView.findViewById(R.id.tv_sms_text);
            mTvTpOa = (TextView) itemView.findViewById(R.id.tv_tp_oa);
            mCbMarked = (CheckBox) itemView.findViewById(R.id.cb_check_for_report);
            mIvLegality = (ImageView) itemView.findViewById(R.id.iv_smsc_legality);
        }

        private void mark(boolean marked){
            mCbMarked.setChecked(marked);
            mMarked = marked;
        }

        private boolean isMarked() {
            return mMarked;
        }
    }

    private class SmsListAdapter extends RecyclerView.Adapter<SmsHolder> {

        private ArrayList<SmsBriefData> mSmsBriefDataList;
        private ArrayList<Integer> mCheckedList;
        private ArrayList<KnownSmsc> mKnownSmscList;
        private SmscMatcher mMatcher;

        SmsListAdapter() {
            this.mSmsBriefDataList = new ArrayList<>();
            this.mCheckedList = new ArrayList<>();
            this.mKnownSmscList = new ArrayList<>();
        }

        @Override
        public SmsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.view_holder_sms, parent, false);
            return new SmsHolder(view);
        }

        @Override
        public void onBindViewHolder(final SmsHolder holder, int position) {
            SmsBriefData smsBriefData = mSmsBriefDataList.get(position);
            holder.mTvSmscAddress.setText(smsBriefData.getSmsc());
            holder.mTvTpOa.setText(smsBriefData.getTpOa());
            holder.mTvText.setText(smsBriefData.getText());
            holder.mCbMarked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.mark(isChecked);
                    int adapterPosition = holder.getAdapterPosition();
                    if (holder.isMarked()){
                        if (!mCheckedList.contains(adapterPosition)) {
                            mCheckedList.add(adapterPosition);
                        }
                    } else {
                        mCheckedList.remove((Integer) adapterPosition);
                    }
                }
            });
            if (mMatcher != null){
                KnownSmsc knownSmsc = mMatcher.matchSmscAddress(smsBriefData.getSmsc());
                if (knownSmsc != null) {
                    holder.mIvLegality.setOnClickListener(new OnLegalityImageClickListener(knownSmsc));
                    switch (knownSmsc.getLegality()) {
                        case KnownSmsc.LEGALITY_AGGREGATOR:
                            holder.mIvLegality.setVisibility(View.VISIBLE);
                            holder.mIvLegality.setImageDrawable(getResources().getDrawable(R.mipmap.ic_aggregator_smsc));
                            break;
                    }
                }
            }
            holder.mark(mCheckedList.contains(position));
        }

        @Override
        public int getItemCount() {
            return mSmsBriefDataList.size();
        }

        private void addSmsBriefData(SmsBriefData smsBriefData) {
            mSmsBriefDataList.add(0, smsBriefData);
            for (int i=0;i<mCheckedList.size();i++){
                Integer itemIndex = mCheckedList.get(i);
                itemIndex++;
                mCheckedList.set(i, itemIndex);
            }
            mCheckedList.add(0);
        }

        private ArrayList<SmsBriefData> getSmsBriefDataList() {
            return mSmsBriefDataList;
        }

        private void setSmsBriefDataList(ArrayList<SmsBriefData> smsBriefDataList) {
            this.mSmsBriefDataList = smsBriefDataList;
        }

        ArrayList<Integer> getCheckedList() {
            return mCheckedList;
        }

        void setCheckedList(ArrayList<Integer> checkedList) {
            this.mCheckedList = checkedList;
        }

        private void setKnownSmscList(ArrayList<KnownSmsc> knownSmscList) {
            this.mKnownSmscList = knownSmscList;
            this.mMatcher = new SmscMatcher(knownSmscList);
        }
    }

    private class OnLegalityImageClickListener implements View.OnClickListener {

        private KnownSmsc mKnownSmsc;

        public OnLegalityImageClickListener(KnownSmsc knownSmsc) {
            this.mKnownSmsc = knownSmsc;
        }

        @Override
        public void onClick(View view) {
            if (mKnownSmsc != null) {
                mListener.onLegalityIconClicked(mKnownSmsc, view.getLeft(), view.getBottom());
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onLegalityIconClicked(KnownSmsc knownSmsc, float invokerX, float invokerY);
    }
}
