package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;

/**
 * Created by Slipstream on 29.11.2016 in GreyRoute.
 */
public class PermissionExplanationDialog extends DialogFragment {

    private static final String KEY_EXPLANATION = "explanation";
    private static final String KEY_REQUEST_CODE = "request_code";

    private String mExplanation;
    private int mRequestCode;
    private OnFragmentInteractionListener mListener;

    public static PermissionExplanationDialog newInstance(String explanation, int requestCode) {

        Bundle args = new Bundle();
        args.putString(KEY_EXPLANATION, explanation);
        args.putInt(KEY_REQUEST_CODE, requestCode);
        PermissionExplanationDialog fragment = new PermissionExplanationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExplanation = getArguments().getString(KEY_EXPLANATION);
            mRequestCode = getArguments().getInt(KEY_REQUEST_CODE, -1);
        }
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_text_button, container, false);
        TextView textView = (TextView) view.findViewById(R.id.tv_explanation);
        Button dismissButton = (Button) view.findViewById(R.id.btn_dialog_dismiss);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mListener.onPermissionExplanationDismiss(mRequestCode);
            }
        });
        textView.setText(mExplanation);
        return view;
    }

    public interface OnFragmentInteractionListener {
        void onPermissionExplanationDismiss(int requestCode);
    }
}
