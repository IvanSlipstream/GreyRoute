package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Slipstream-DESKTOP on 23.06.2017.
 */

public class CalendarViewDialog extends DialogFragment implements CalendarView.OnDateChangeListener {

    private static final String KEY_TIME_MILLIS = "millis";
    private long mTime;
    private CalendarView mCvDateChoice;
    private OnFragmentInteractionListener mListener;

    public static CalendarViewDialog newInstance(long millis) {
        
        Bundle args = new Bundle();
        args.putLong(KEY_TIME_MILLIS, millis);

        CalendarViewDialog fragment = new CalendarViewDialog();
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTime = getArguments().getLong(KEY_TIME_MILLIS);
        }
        if (savedInstanceState != null) {
            mTime = savedInstanceState.getLong(KEY_TIME_MILLIS, mTime);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_date_choice, container, false);
        mCvDateChoice = (CalendarView) view.findViewById(R.id.cv_start_date_choice);
        mCvDateChoice.setDate(mTime);
        mCvDateChoice.setMaxDate((new Date()).getTime());
        mCvDateChoice.setOnDateChangeListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_TIME_MILLIS, mTime);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        mTime = calendar.getTimeInMillis();
        mListener.onStartDateSelectedCalendar(mTime);
        dismiss();
    }

    public interface OnFragmentInteractionListener {
        void onStartDateSelectedCalendar(long millis);
    }
}
