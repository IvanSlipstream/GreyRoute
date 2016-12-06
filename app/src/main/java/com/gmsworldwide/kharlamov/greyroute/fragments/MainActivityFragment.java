package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
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
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;
import com.gmsworldwide.kharlamov.greyroute.service.SmsIntentService;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<SmsBriefData>> {

    public static final Uri URI_SMS_INBOX = Uri.parse("content://sms/inbox");
    public static final String INBOX_SORT_ORDER = "date";

    private static final int LOADER_ID_INBOX = 1;
    private static final String RETAIN_INSTANCE_KEY_SMS_LIST = "sms_list";
    private RecyclerView mRecyclerView;
    private SmsListAdapter adapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SmsListAdapter();
        if (savedInstanceState != null) {
            adapter.setSmsBriefDataList(savedInstanceState.<SmsBriefData>getParcelableArrayList(RETAIN_INSTANCE_KEY_SMS_LIST));
        }
        ResultReceiver mReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                adapter.addSmsBriefData(new SmsBriefData(resultData));
            }
        };
        SmsIntentService.startActionSetListener(getActivity(), mReceiver);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID_INBOX, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rvSmsList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SmsListAdapter adapter = (SmsListAdapter) mRecyclerView.getAdapter();
        outState.putParcelableArrayList(RETAIN_INSTANCE_KEY_SMS_LIST, adapter.getSmsBriefDataList());
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
                ArrayList<SmsBriefData> result = new ArrayList<>();
                Cursor c = getContext().getContentResolver().query(URI_SMS_INBOX, null, null, null, INBOX_SORT_ORDER);
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
        for (SmsBriefData smsBriefData: data) {
            adapter.addSmsBriefData(smsBriefData);
            mRecyclerView.swapAdapter(adapter, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<SmsBriefData>> loader) {

    }

    private class SmsHolder extends RecyclerView.ViewHolder {

        private TextView tvSmscOaAddress;
        private TextView tvText;
        private int mUnmarkedColor;
        private int mMarkedColor;
        private boolean mMarked;

        public SmsHolder(View itemView) {
            super(itemView);
            tvSmscOaAddress = (TextView) itemView.findViewById(android.R.id.text1);
            tvText = (TextView) itemView.findViewById(android.R.id.text2);
            Drawable background = itemView.getBackground();
//            Resources.Theme theme = itemView.getContext().getTheme();
//            mMarkedColor = theme.getResources().getColor(android.R.color.background_dark, theme);
            if (background instanceof ColorDrawable){
                mUnmarkedColor = ((ColorDrawable) background).getColor();
            } else {
                mUnmarkedColor = Color.TRANSPARENT;
            }
        }

        public void mark (boolean marked){
            if (marked) {
                itemView.setBackgroundColor(Color.GREEN);
                this.mMarked = true;
                // TODO bind color to the theme and fix hardcoded color
            } else {
                itemView.setBackgroundColor(mUnmarkedColor);
                this.mMarked = false;
            }
        }

        public boolean isMarked() {
            return mMarked;
        }

        public TextView getTvSmscOaAddress() {
            return tvSmscOaAddress;
        }

        public TextView getTvText() {
            return tvText;
        }
    }
    public class SmsListAdapter extends RecyclerView.Adapter<SmsHolder> {

        private ArrayList<SmsBriefData> mSmsBriefDataList;

        public SmsListAdapter() {
            this.mSmsBriefDataList = new ArrayList<>();
        }

        public ArrayList<SmsBriefData> getSmsBriefDataList() {
            return mSmsBriefDataList;
        }

        public void setSmsBriefDataList(ArrayList<SmsBriefData> smsBriefDataList) {
            this.mSmsBriefDataList = smsBriefDataList;
        }

        @Override
        public SmsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            // TODO: make custom layout
            return new SmsHolder(view);
        }

        @Override
        public void onBindViewHolder(final SmsHolder holder, int position) {
            SmsBriefData mSmsBriefData = mSmsBriefDataList.get(position);
            String smscOa = String.format("SMSC: %s, TP-OA: %s",
                    mSmsBriefData.getSmsc(), mSmsBriefData.getTpOa());
            holder.getTvSmscOaAddress().setText(smscOa);
            holder.getTvText().setText(mSmsBriefData.getText());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mark(!holder.isMarked());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSmsBriefDataList.size();
        }

        public void addSmsBriefData (SmsBriefData smsBriefData) {
            mSmsBriefDataList.add(0, smsBriefData);
        }
    }
}
