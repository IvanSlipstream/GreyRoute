package com.gmsworldwide.kharlamov.grey_route.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;

/**
 * Created by Slipstream-DESKTOP on 12.05.2017.
 */

public class SmscDetailsFragment extends Fragment {

    private static final String KEY_KNOWN_SMSC = "known_smsc";
    private static final String KEY_INVOKER_X = "invoker_x";
    private static final String KEY_INVOKER_Y = "invoker_y";
    private KnownSmsc mKnownSmsc;
    private TextView mTvSmscDetails;
    private float mInvokerX;
    private float mInvokerY;

    public static SmscDetailsFragment newInstance(KnownSmsc knownSmsc, float invokerX, float invokerY) {

        Bundle args = new Bundle();
        args.putParcelable(KEY_KNOWN_SMSC, knownSmsc);
        args.putFloat(KEY_INVOKER_X, invokerX);
        args.putFloat(KEY_INVOKER_Y, invokerY);
        SmscDetailsFragment fragment = new SmscDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mKnownSmsc = bundle.getParcelable(KEY_KNOWN_SMSC);
        mInvokerY = bundle.getFloat(KEY_INVOKER_Y, 0);
        mInvokerX = bundle.getFloat(KEY_INVOKER_X, 0);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_smsc_details, container, false);
        mTvSmscDetails = (TextView) view.findViewById(R.id.tv_smsc_details);
        if (container != null) {
            if (mInvokerY < container.getHeight() / 2) {
                view.setPadding(0, view.getPaddingTop() + 5 * (int) mInvokerY, (int) (container.getWidth() - mInvokerX), 0);
//                view.setTranslationY(view.getTranslationY()+mInvokerY);
            } else {
//                view.setTranslationY(-mInvokerY);
                view.setPadding(0, 0, (int) (container.getWidth() - mInvokerX), (int) (container.getHeight() - mInvokerY));
            }
        }
        mTvSmscDetails.setText(R.string.placeholder_sms_text);
        return view;
    }

}
