package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gmsworldwide.kharlamov.greyroute.R;
import com.gmsworldwide.kharlamov.greyroute.models.SmsBriefData;

import java.util.ArrayList;

/**
 * Created by Slipstream on 27.12.2016 in GreyRoute.
 */

public class ReportChooseDialog extends DialogFragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private ArrayList<SmsBriefData> mSmsList;
    private static final String KEY_SMS_LIST_TO_SAVE = "sms_list";

    public static ReportChooseDialog newInstance() {

        Bundle args = new Bundle();

        ReportChooseDialog fragment = new ReportChooseDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_report_choice, container, false);
        Button btnCSV = (Button) view.findViewById(R.id.btn_report_choice_csv);
        Button btnPush = (Button) view.findViewById(R.id.btn_report_choice_push);
        Button btnDismiss = (Button) view.findViewById(R.id.btn_report_choice_cancel);
        btnDismiss.setOnClickListener(this);
        btnCSV.setOnClickListener(this);
        btnPush.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new UnsupportedOperationException("Must implement "
                    + OnFragmentInteractionListener.class.getName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_report_choice_cancel:
                dismiss();
                break;
            case R.id.btn_report_choice_csv:
                if (mListener != null) {
                    mListener.onCSVReportRequested();
                }
                break;
            case R.id.btn_report_choice_push:
                if (mListener != null) {
                    mListener.onPushReportRequested();
                }
                dismiss();
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void onPushReportRequested();
        void onCSVReportRequested();
    }
}
