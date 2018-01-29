package com.gmsworldwide.kharlamov.grey_route.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gmsworldwide.kharlamov.grey_route.R;

import java.util.Calendar;
import java.util.Date;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.INTERVAL_HOUR;

public class AnalyzeInboxFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String RETAIN_INSTANCE_SELECTION_PERIOD = "selection_period";

    private OnFragmentInteractionListener mListener;
    private OnFragmentStateChangeListener mStateChangeListener;
    private long mSelectionPeriod;
    private TextView mTvStartTime;
    private TextView mTvStartDate;

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
            mSelectionPeriod = savedInstanceState.getLong(RETAIN_INSTANCE_SELECTION_PERIOD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze_inbox, container, false);
        mTvStartDate = (TextView) view.findViewById(R.id.tv_start_date);
        mTvStartTime = (TextView) view.findViewById(R.id.tv_start_time);
        Button btnAnalyzeInbox = (Button) view.findViewById(R.id.btn_start_analyze_inbox);
        btnAnalyzeInbox.setOnClickListener(this);
        mTvStartDate.setOnClickListener(this);
        mTvStartTime.setOnClickListener(this);
        for (int id: new int[]{R.id.tv_predefined_last_hour, R.id.tv_predefined_today,
                    R.id.tv_predefined_last_week, R.id.tv_predefined_lifetime}) {
            TextView tvPredefinedPeriod = (TextView) view.findViewById(id);
            tvPredefinedPeriod.setOnClickListener(this);
        }
        notifyStartDateTimeUpdate();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener &&
                context instanceof OnFragmentStateChangeListener) {
            mListener = (OnFragmentInteractionListener) context;
            mStateChangeListener = (OnFragmentStateChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener" +
                    " and OnFragmentStateChangeListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyStartDateTimeUpdate();
        mStateChangeListener.onFragmentResumed(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putLong(RETAIN_INSTANCE_SELECTION_PERIOD, mSelectionPeriod);
        }
    }

    @Override
    public void onClick(View view) {
        Calendar calendar = Calendar.getInstance();
        switch (view.getId()){
            case R.id.btn_start_analyze_inbox:
                if (mListener != null) {
                    mListener.onInboxAnalyzeRequested(mSelectionPeriod);
                }
                break;
            case R.id.tv_start_date:
                calendar.setTimeInMillis(mSelectionPeriod);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.tv_start_time:
                calendar.setTimeInMillis(mSelectionPeriod);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
                break;
            case R.id.tv_predefined_last_hour:
                mSelectionPeriod = getDefaultSelectionPeriod();
                break;
            case R.id.tv_predefined_today:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                mSelectionPeriod = calendar.getTimeInMillis();
                break;
            case R.id.tv_predefined_last_week:
                mSelectionPeriod = calendar.getTimeInMillis()-7*INTERVAL_DAY;
                break;
            case R.id.tv_predefined_lifetime:
                mSelectionPeriod = 0;
                break;
        }
        notifyStartDateTimeUpdate();
    }


    private void notifyStartDateTimeUpdate(){
        // based on mSelectionPeriod, restore values
        // of mTvStartDate and mTvStartTime
        Date date = new Date(mSelectionPeriod);
        mTvStartDate.setText(DateFormat.getDateFormat(getContext()).format(date));
        String format = DateFormat.getTimeFormat(getContext()).format(date);
        mTvStartTime.setText(format);
    }

    private long getDefaultSelectionPeriod() {
        Calendar calendar = Calendar.getInstance();
        long millis =  calendar.getTimeInMillis()-INTERVAL_HOUR;
        Log.d("test-"+getClass().getSimpleName(),
                "setting default start time: "+ (new Date(millis)).toString());
        return millis;
    }

    public void setSelectionPeriod(long millis){
        Log.d("test-"+getClass().getSimpleName(), "Requested to change selection period," +
                "new one is " + (new Date(millis)).toString());
        this.mSelectionPeriod = millis;
        notifyStartDateTimeUpdate();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Log.d("test-"+getClass().getSimpleName(), "Requested to change start date," +
                "new one is " + String.format("%d.%d.%d", year, month, dayOfMonth));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mSelectionPeriod);
        calendar.set(year, month, dayOfMonth);
        mSelectionPeriod = calendar.getTimeInMillis();
        notifyStartDateTimeUpdate();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Log.d("test-"+getClass().getSimpleName(), "Requested to change start time," +
                "new one is " + String.format("%d:%d", hour, minute));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mSelectionPeriod);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        mSelectionPeriod = calendar.getTimeInMillis();
        notifyStartDateTimeUpdate();
    }


    public interface OnFragmentInteractionListener {
        void onInboxAnalyzeRequested(long seconds);
    }
}
