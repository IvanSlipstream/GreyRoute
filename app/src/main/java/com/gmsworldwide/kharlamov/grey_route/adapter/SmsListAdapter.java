package com.gmsworldwide.kharlamov.grey_route.adapter;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.fragments.SmsListFragment;
import com.gmsworldwide.kharlamov.grey_route.matcher.SmscMatcher;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;
import com.gmsworldwide.kharlamov.grey_route.models.SmsBriefData;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Slipstream-DESKTOP on 24.05.2017.
 */

public class SmsListAdapter extends RecyclerView.Adapter<SmsListAdapter.SmsHolder> {

    private ArrayList<Long> mCheckedList;
    private SmscMatcher mMatcher;
    private Cursor mCursor;
    private SmsListFragment.OnFragmentInteractionListener mListener;
    private long mStartTime;

    public SmsListAdapter(Cursor c, SmsListFragment.OnFragmentInteractionListener listener) {
        this.mCursor = c;
        this.mListener = listener;
        this.mCheckedList = new ArrayList<>();
        this.mStartTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public SmsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_holder_sms, parent, false);
        return new SmsHolder(view);
    }

    @Override
    public void onBindViewHolder(SmsHolder holder, int position) {
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToPosition(position);
            SmsBriefData smsBriefData = new SmsBriefData(mCursor);
            for (int i=0;i<mCursor.getColumnCount();i++){
                Log.d("dbg-"+SmsListFragment.class.getSimpleName(),
                        mCursor.getColumnName(i)+"="+mCursor.getString(i));
            }
            final Long id = smsBriefData.getId();
            holder.mTvSmscAddress.setText(smsBriefData.getSmsc());
            holder.mTvTpOaTime.setText(holder.mTvTpOaTime.getResources().getString(R.string.template_oa_time,
                    smsBriefData.getTpOa(), smsBriefData.getFormattedTime()));
            holder.mTvText.setText(smsBriefData.getText());
            holder.mCbMarked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!mCheckedList.contains(id)) {
                            mCheckedList.add(id);
                        }
                    } else {
                        mCheckedList.remove(id);
                    }
                }
            });
            holder.mCbMarked.setChecked(mCheckedList.contains(id));
            if (smsBriefData.getTime() > mStartTime){
                holder.mCbMarked.setChecked(true);
                mStartTime = smsBriefData.getTime();
            }
            if (mMatcher != null){
                final KnownSmsc knownSmsc = mMatcher.matchSmscAddress(smsBriefData.getSmsc());
                if (knownSmsc != null) {
                    Resources resources = holder.mIvLegality.getResources();
                    switch (knownSmsc.getLegality()) {
                        case KnownSmsc.LEGALITY_AGGREGATOR:
                            holder.mIvLegality.setVisibility(View.VISIBLE);
                            holder.mIvLegality.setImageDrawable(resources.getDrawable(R.mipmap.ic_aggregator_smsc));
                            break;
                        case KnownSmsc.LEGALITY_GREY:
                            holder.mIvLegality.setVisibility(View.VISIBLE);
                            holder.mIvLegality.setImageDrawable(resources.getDrawable(R.mipmap.ic_launcher));
                            break;
                        case KnownSmsc.LEGALITY_UNKNOWN:
                            holder.mIvLegality.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    holder.mIvLegality.setVisibility(View.GONE);
                    holder.mIvLegality.setOnClickListener(null);
                }
                holder.mIvLegality.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onLegalityButtonClicked(knownSmsc);
                    }
                });
            }
        }
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
    }

    @Override
    public int getItemCount() {
        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }


    public ArrayList<SmsBriefData> getSmsList(boolean checkedOnly) {
        ArrayList<SmsBriefData> result = new ArrayList<>();
        if (mCursor != null && mCursor.getCount()>0 && mCheckedList != null) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                SmsBriefData smsBriefData = new SmsBriefData(mCursor);
                if (!checkedOnly || mCheckedList.contains(smsBriefData.getId())) {
                    result.add(smsBriefData);
                }
                mCursor.moveToNext();
            }
        }
        return result;
    }

    public ArrayList<Long> getCheckedList() {
        return mCheckedList;
    }

    public void setCheckedList(ArrayList<Long> checkedList) {
        this.mCheckedList = checkedList;
    }

    public void setKnownSmscList(ArrayList<KnownSmsc> knownSmscList) {
        this.mMatcher = new SmscMatcher(knownSmscList);
    }

    class SmsHolder extends RecyclerView.ViewHolder {

        private TextView mTvSmscAddress;
        private TextView mTvText;
        private TextView mTvTpOaTime;
        private CheckBox mCbMarked;
        private ImageView mIvLegality;

        SmsHolder(View itemView) {
            super(itemView);
            mTvSmscAddress = (TextView) itemView.findViewById(R.id.tv_smsc_address);
            mTvText = (TextView) itemView.findViewById(R.id.tv_sms_text);
            mTvTpOaTime = (TextView) itemView.findViewById(R.id.tv_tp_oa_time);
            mCbMarked = (CheckBox) itemView.findViewById(R.id.cb_check_for_report);
            mIvLegality = (ImageView) itemView.findViewById(R.id.iv_smsc_legality);
        }

    }

}

