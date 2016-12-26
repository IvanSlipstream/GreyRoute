package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.gmsworldwide.kharlamov.greyroute.R;

import java.util.Calendar;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.INTERVAL_HOUR;

public class AnalyzeInboxFragment extends Fragment {

    public static final int SEEK_POSITION_LAST_HOUR = 0;
    public static final int SEEK_POSITION_TODAY = 1;
    public static final int SEEK_POSITION_LAST_WEEK = 2;
    public static final int SEEK_POSITION_LIFETIME = 3;

    private OnFragmentInteractionListener mListener;
    private SeekBar mSbPeriod;
    private long mSelectionPeriod = 0;

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
        // TODO retain seek bar position
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze_inbox, container, false);
        Button btnAnalyzeInbox = (Button) view.findViewById(R.id.btn_start_analyze_inbox);
        btnAnalyzeInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onInboxAnalyzeRequested(mSelectionPeriod);
                }
            }
        });
        mSbPeriod = (SeekBar) view.findViewById(R.id.sb_inbox_count);
        mSbPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Calendar calendar = Calendar.getInstance();
                switch (progress) {
                    case SEEK_POSITION_LAST_HOUR:
                        mSelectionPeriod = calendar.getTimeInMillis()-INTERVAL_HOUR;
                        break;
                    case SEEK_POSITION_TODAY:
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        mSelectionPeriod = calendar.getTimeInMillis();
                        break;
                    case SEEK_POSITION_LAST_WEEK:
                        mSelectionPeriod = calendar.getTimeInMillis()-7*INTERVAL_DAY;
                        break;
                    case SEEK_POSITION_LIFETIME:
                        mSelectionPeriod = 0;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        void onInboxAnalyzeRequested(long seconds);
    }
}
