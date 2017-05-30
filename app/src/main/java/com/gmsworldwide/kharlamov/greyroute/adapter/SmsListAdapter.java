package com.gmsworldwide.kharlamov.greyroute.adapter;

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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by Slipstream-DESKTOP on 24.05.2017.
 */

public class SmsListAdapter extends RecyclerView.Adapter<SmsListAdapter.SmsHolder> {

    private ArrayList<SmsBriefData> mSmsBriefDataList;
    private ArrayList<Integer> mCheckedList;
    private ArrayList<KnownSmsc> mKnownSmscList;
    private SmscMatcher mMatcher;

    public SmsListAdapter() {
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
        holder.mTvTpOaTime.setText(smsBriefData.getTpOa());
        holder.mTvText.setText(smsBriefData.getText());
        holder.mTvText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.mExpanded){
                    holder.collapse();
                } else {
                    holder.expand(null);
                }
            }
        });
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
                String smscDetails = "";
                switch (knownSmsc.getLegality()) {
                    case KnownSmsc.LEGALITY_AGGREGATOR:
                        holder.mIvLegality.setVisibility(View.VISIBLE);
                        holder.mIvLegality.setImageDrawable(holder.mIvLegality.getResources().getDrawable(R.mipmap.ic_aggregator_smsc));
                        smscDetails = holder.mIvLegality.getResources().getString(R.string.placeholder_match_smsc);
                        break;
                }
                final String finalSmscDetails = smscDetails;
                holder.mIvLegality.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mExpanded) {
                            holder.collapse();
                        } else {
                            holder.expand(finalSmscDetails);
                        }
                    }
                });
            }
        }
        holder.mark(mCheckedList.contains(position));
    }

    @Override
    public int getItemCount() {
        return mSmsBriefDataList.size();
    }

    public void addSmsBriefData(SmsBriefData smsBriefData) {
        mSmsBriefDataList.add(0, smsBriefData);
        for (int i=0;i<mCheckedList.size();i++){
            Integer itemIndex = mCheckedList.get(i);
            itemIndex++;
            mCheckedList.set(i, itemIndex);
        }
        mCheckedList.add(0);
    }

    public ArrayList<SmsBriefData> getSmsBriefDataList() {
        return mSmsBriefDataList;
    }

    public void setSmsBriefDataList(ArrayList<SmsBriefData> smsBriefDataList) {
        this.mSmsBriefDataList = smsBriefDataList;
    }

    public ArrayList<Integer> getCheckedList() {
        return mCheckedList;
    }

    public void setCheckedList(ArrayList<Integer> checkedList) {
        this.mCheckedList = checkedList;
    }

    public void setKnownSmscList(ArrayList<KnownSmsc> knownSmscList) {
        this.mKnownSmscList = knownSmscList;
        this.mMatcher = new SmscMatcher(knownSmscList);
    }

    class SmsHolder extends RecyclerView.ViewHolder {

        private TextView mTvSmscAddress;
        private TextView mTvText;
        private TextView mTvTpOaTime;
        private TextView mTvSmscDetails;
        private CheckBox mCbMarked;
        private ImageView mIvLegality;
        private boolean mMarked;
        private boolean mExpanded;

        SmsHolder(View itemView) {
            super(itemView);
            mTvSmscAddress = (TextView) itemView.findViewById(R.id.tv_smsc_address);
            mTvText = (TextView) itemView.findViewById(R.id.tv_sms_text);
            mTvTpOaTime = (TextView) itemView.findViewById(R.id.tv_tp_oa_time);
            mCbMarked = (CheckBox) itemView.findViewById(R.id.cb_check_for_report);
            mIvLegality = (ImageView) itemView.findViewById(R.id.iv_smsc_legality);
            mTvSmscDetails = (TextView) itemView.findViewById(R.id.tv_smsc_details);
        }

        private void mark(boolean marked){
            mCbMarked.setChecked(marked);
            mMarked = marked;
        }

        private void expand(String smscDetails){
            if (smscDetails != null) {
                mTvSmscDetails.setVisibility(View.VISIBLE);
                mTvSmscDetails.setText(smscDetails);
            }
            mTvText.setMaxLines(100);
            mExpanded = true;
        }

        private void collapse(){
            mTvText.setMaxLines(1);
            mTvSmscDetails.setVisibility(View.GONE);
            mExpanded = false;
        }

        private boolean isMarked() {
            return mMarked;
        }
    }

}

