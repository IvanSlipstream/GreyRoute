package com.gmsworldwide.kharlamov.grey_route.fragments;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.models.KnownSmsc;

/**
 * Created by Slipstream-DESKTOP on 21.12.2017.
 */

public class SmscAnalyzeDialog extends DialogFragment implements View.OnClickListener {

    public static final String KEY_KNOWN_SMSC = "known_smsc";
    private static final String KEY_EXPANDED = "expanded";
    private static final String PLACEHOLDER = "*";
    private static final String KEY_EXPLAIN_TEXT = "explain_text";

    private KnownSmsc mKnownSmsc;
    private boolean mExpanded = false;
    private OnFragmentInteractionListener mListener;
    private EditText mEtUserReason;
    private TextView mTvUserReason;
    private Button mBtnSubmit;

    public static SmscAnalyzeDialog newInstance(KnownSmsc knownSmsc) {

        Bundle args = new Bundle();
        args.putParcelable(KEY_KNOWN_SMSC, knownSmsc);

        SmscAnalyzeDialog fragment = new SmscAnalyzeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            mListener = (OnFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()+
                    "must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            mKnownSmsc = args.getParcelable(KEY_KNOWN_SMSC);
        }
        if (savedInstanceState != null) {
            mExpanded = savedInstanceState.getBoolean(KEY_EXPANDED);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EXPANDED, mExpanded);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_smsc_details, container, false);
        TextView tvSmscDescription = (TextView) view.findViewById(R.id.tv_smsc_details);
        TextView tvSmscPrefix = (TextView) view.findViewById(R.id.tv_smsc_prefix);
        mBtnSubmit = (Button) view.findViewById(R.id.btn_submit);
        Resources resources = view.getResources();
        mTvUserReason = (TextView) view.findViewById(R.id.tv_user_reason);
        mEtUserReason = (EditText) view.findViewById(R.id.et_user_reason);
        if (savedInstanceState != null) {
            mEtUserReason.setText(savedInstanceState.getString(KEY_EXPLAIN_TEXT));
        }
        setupHiddenViews();
        String smscPrefix = mKnownSmsc.getSmscPrefix() + PLACEHOLDER;
        tvSmscPrefix.setText(smscPrefix);
        switch (mKnownSmsc.getLegality()) {
            case KnownSmsc.LEGALITY_AGGREGATOR:
                tvSmscDescription.setText(resources.getString(R.string.smsc_legality_aggregator, mKnownSmsc.getCarrierName()));
                tvSmscPrefix.setCompoundDrawables(resources.getDrawable(R.mipmap.ic_aggregator_smsc), null, null, null);
                break;
            case KnownSmsc.LEGALITY_GREY:
                tvSmscDescription.setText(resources.getString(R.string.smsc_is_grey));
                tvSmscPrefix.setCompoundDrawables(resources.getDrawable(R.mipmap.ic_launcher), null, null, null);
                mTvUserReason.setText(R.string.hint_explain_not_gray);
                break;
        }
        mBtnSubmit.setOnClickListener(this);
        return view;
    }

    private void setupHiddenViews() {
        if (mKnownSmsc.getLegality() == KnownSmsc.LEGALITY_GREY) {
            mBtnSubmit.setText(mExpanded ? R.string.submit : R.string.not_gray);
        } else {
            mBtnSubmit.setVisibility(View.GONE);
        }
        mTvUserReason.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
        mEtUserReason.setVisibility(mExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_submit:
                if (mExpanded) {
                    mListener.onMessageSubmit(mKnownSmsc, String.valueOf(mEtUserReason.getText()));
                }
                mExpanded = !mExpanded;
                setupHiddenViews();
                break;
            case R.id.btn_dialog_dismiss:
                dismiss();
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void onMessageSubmit(KnownSmsc knownSmsc, String reason);
    }
}
