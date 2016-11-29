package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.greyroute.R;

/**
 * Created by Slipstream on 29.11.2016 in GreyRoute.
 */
public class PermissionExplanationDialog extends DialogFragment {

    private static final String KEY_EXPLANATION = "explanation";

    private String explanation;

    public static PermissionExplanationDialog newInstance(String explanation) {

        Bundle args = new Bundle();
        args.putString(KEY_EXPLANATION, explanation);

        PermissionExplanationDialog fragment = new PermissionExplanationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            explanation = getArguments().getString(KEY_EXPLANATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_text_button, container, false);
        TextView textView = (TextView) view.findViewById(R.id.tv_explanation);
        textView.setText(explanation);
        return view;
    }
}
