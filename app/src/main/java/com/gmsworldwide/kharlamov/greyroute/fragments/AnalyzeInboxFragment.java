package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;

import java.util.Calendar;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.INTERVAL_HOUR;

public class AnalyzeInboxFragment extends Fragment {

    private static final int SEEK_POSITION_LAST_HOUR = 0;
    private static final int SEEK_POSITION_TODAY = 1;
    private static final int SEEK_POSITION_LAST_WEEK = 2;
    private static final int SEEK_POSITION_LIFETIME = 3;
    private static final String RETAIN_INSTANCE_SB_POSITION = "scroll_bar_position";

    private OnFragmentInteractionListener mListener;
    private SeekBar mSbPeriod;
    private TextView mTvPeriodHint;
    private long mSelectionPeriod;
    private int mScrollBarPosition = 0;

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
        mSelectionPeriod = getDefaultSelectionPeriod();
        if (savedInstanceState != null) {
            mScrollBarPosition = savedInstanceState.getInt(RETAIN_INSTANCE_SB_POSITION, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze_inbox, container, false);
        mTvPeriodHint = (TextView) view.findViewById(R.id.tv_period_hint);
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
                        mSelectionPeriod = getDefaultSelectionPeriod();
                        mTvPeriodHint.setText(getText(R.string.period_last_hour));
                        break;
                    case SEEK_POSITION_TODAY:
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        mSelectionPeriod = calendar.getTimeInMillis();
                        mTvPeriodHint.setText(getText(R.string.period_today));
                        break;
                    case SEEK_POSITION_LAST_WEEK:
                        mSelectionPeriod = calendar.getTimeInMillis()-7*INTERVAL_DAY;
                        mTvPeriodHint.setText(getText(R.string.period_last_week));
                        break;
                    case SEEK_POSITION_LIFETIME:
                        mSelectionPeriod = 0;
                        mTvPeriodHint.setText(getText(R.string.period_lifetime));
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
        // scroll bar position retaining instance
        mSbPeriod.setVerticalScrollbarPosition(mScrollBarPosition);
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
    public void onResume() {
        super.onResume();
        mListener.onInboxAnalyzeFragmentResumed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && mSbPeriod != null) {
            outState.putInt(RETAIN_INSTANCE_SB_POSITION, mSbPeriod.getVerticalScrollbarPosition());
        }
    }

    public interface OnFragmentInteractionListener {
        void onInboxAnalyzeRequested(long seconds);
        void onInboxAnalyzeFragmentResumed();
    }

    private long getDefaultSelectionPeriod() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis()-INTERVAL_HOUR;
    }
}
