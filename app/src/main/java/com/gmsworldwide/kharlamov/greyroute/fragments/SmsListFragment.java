package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;

import java.util.ArrayList;
import java.util.Locale;

public class SmsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<SmsBriefData>> {

    public static final Uri URI_SMS_INBOX = Uri.parse("content://sms/inbox");
    public static final String INBOX_SORT_ORDER = "date desc";
    public static final String INBOX_DATE_FIELD = "date";

    private static final int LOADER_ID_INBOX = 1;
    private static final String RETAIN_INSTANCE_KEY_CHECKED_LIST = "checked_list";
    private static final String KEY_SELECTION_PERIOD = "selection_period";
    private RecyclerView mRecyclerView;
    private SmsListAdapter adapter;
    private long mSelectionPeriod;
    private boolean mReloadInbox = true;

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
    public void onResume() {
        super.onResume();
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        // check if the user has set a new selection period and thus reload or not the SMS Inbox
        if (mReloadInbox) {
            loaderManager.restartLoader(LOADER_ID_INBOX, null, this);
        } else {
            loaderManager.initLoader(LOADER_ID_INBOX, null, this);
        }
        mReloadInbox = false;
    }

    public void addSmsBriefData(SmsBriefData data) {
        if (adapter != null) {
            adapter.addSmsBriefData(data);
            mRecyclerView.swapAdapter(adapter, false);
        }
    }

    public ArrayList<SmsBriefData> getSmsBriefDataList(){
        if (adapter != null) {
            return adapter.getSmsBriefDataList();
        } else {
            return null;
        }
    }

    @Override
    public Loader<ArrayList<SmsBriefData>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ArrayList<SmsBriefData>>(getContext()) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public ArrayList<SmsBriefData> loadInBackground() {
                String condition = String.format(Locale.getDefault(),
                        "%s > %d", INBOX_DATE_FIELD, mSelectionPeriod);
                ArrayList<SmsBriefData> result = new ArrayList<>();
                Cursor c = getContext().getContentResolver().query(URI_SMS_INBOX, null, condition, null, INBOX_SORT_ORDER);
                if (c != null) {
                    while (c.moveToNext()) {
                        result.add(new SmsBriefData(c));
                    }
                    c.close();
                }
                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<SmsBriefData>> loader, ArrayList<SmsBriefData> data) {
        adapter.setSmsBriefDataList(data);
        mRecyclerView.swapAdapter(adapter, false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<SmsBriefData>> loader) {

    }

    private class SmsHolder extends RecyclerView.ViewHolder {

        private TextView mTvSmscAddress;
        private TextView mTvText;
        private TextView mTvTpOa;
        private CheckBox mCbMarked;
        private boolean mMarked;

        SmsHolder(View itemView) {
            super(itemView);
            mTvSmscAddress = (TextView) itemView.findViewById(R.id.tv_smsc_address);
            mTvText = (TextView) itemView.findViewById(R.id.tv_sms_text);
            mTvTpOa = (TextView) itemView.findViewById(R.id.tv_tp_oa);
            mCbMarked = (CheckBox) itemView.findViewById(R.id.cb_check_for_report);
        }

        private void mark(boolean marked){
            mCbMarked.setChecked(marked);
            mMarked = marked;
        }

        private boolean isMarked() {
            return mMarked;
        }
    }
    public class SmsListAdapter extends RecyclerView.Adapter<SmsHolder> {

        private ArrayList<SmsBriefData> mSmsBriefDataList;
        private ArrayList<Integer> mCheckedList;

        SmsListAdapter() {
            this.mSmsBriefDataList = new ArrayList<>();
            this.mCheckedList = new ArrayList<>();
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
                    if (holder.isMarked()){
                        mCheckedList.add(holder.getAdapterPosition());
                    } else {
                        mCheckedList.remove((Integer) holder.getAdapterPosition());
                    }
                }
            });
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
    }
}
