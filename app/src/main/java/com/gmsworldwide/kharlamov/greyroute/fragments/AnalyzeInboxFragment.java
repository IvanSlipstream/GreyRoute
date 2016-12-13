package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gmsworldwide.kharlamov.greyroute.R;

public class AnalyzeInboxFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AnalyzeInboxFragment() {}

    public static AnalyzeInboxFragment newInstance() {
        AnalyzeInboxFragment fragment = new AnalyzeInboxFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO pass arguments if any
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze_inbox, container, false);
        Button mBtnAnalyzeInbox = (Button) view.findViewById(R.id.btn_start_analyze_inbox);
        mBtnAnalyzeInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert mListener != null;
                mListener.onInboxAnalyzeRequested();
            }
        });
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onInboxAnalyzeRequested();
    }
}
