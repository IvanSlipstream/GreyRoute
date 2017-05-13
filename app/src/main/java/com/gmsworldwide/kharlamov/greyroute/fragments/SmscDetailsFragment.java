package com.gmsworldwide.kharlamov.greyroute.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmsworldwide.kharlamov.greyroute.models.KnownSmsc;

/**
 * Created by Slipstream-DESKTOP on 12.05.2017.
 */

public class SmscDetailsFragment extends Fragment {

    private static final String KEY_KNOWN_SMSC = "known_smsc";
    private KnownSmsc mKnownSmsc;
//    private OnFragmentInteractionListener mListener;
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    public static SmscDetailsFragment newInstance(KnownSmsc knownSmsc) {

        Bundle args = new Bundle();
        args.putParcelable(KEY_KNOWN_SMSC, knownSmsc);

        SmscDetailsFragment fragment = new SmscDetailsFragment();
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

//    public interface OnFragmentInteractionListener {
//
//    }
}
