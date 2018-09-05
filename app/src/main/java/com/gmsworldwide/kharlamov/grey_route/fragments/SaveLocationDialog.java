package com.gmsworldwide.kharlamov.grey_route.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.grey_route.R;
import com.gmsworldwide.kharlamov.grey_route.adapter.DirectoryListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SaveLocationDialog extends DialogFragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private File mCurrentPath;
    private File mRootFile;
    private ImageView mBtnUp;
    private Button mBtnSaveFile;
    private TextView mTvPath;
    private List<String> mFileList = new ArrayList<>();
    private RecyclerView mRvFileList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString() +
                    "must implement OnFragmentInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_path_choice, container, false);
        mBtnUp = view.findViewById(R.id.ib_up);
        mBtnUp.setOnClickListener(this);
        mBtnSaveFile = view.findViewById(R.id.btn_save_file);
        mBtnSaveFile.setOnClickListener(this);
        mRootFile = new File(Environment
                .getExternalStorageDirectory()
                .getAbsolutePath());
        mTvPath = view.findViewById(R.id.tv_path);
        mRvFileList = view.findViewById(R.id.lv_file_list);
        mRvFileList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        listDir(mRootFile);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ib_up:
                listDir(mCurrentPath.getParentFile());
                break;
            case R.id.btn_save_file:
                if (mListener != null) {
                    mListener.onPathDefined(mCurrentPath.getAbsolutePath());
                }
                dismiss();
        }
    }

    public interface OnFragmentInteractionListener {
        void onPathDefined(String path);
    }

    void listDir(File f) {

        mBtnUp.setEnabled(!f.equals(mRootFile));
        mCurrentPath = f;
        mTvPath.setText(f.getAbsolutePath().replace(mRootFile.getAbsolutePath(), ""));

        File[] files = f.listFiles();
        mFileList.clear();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) mFileList.add(file.getName());
            }
        }

        mRvFileList.setAdapter(new DirectoryListAdapter(mFileList) {
            @Override
            public void onItemClick(String directoryName) {
                listDir(new File(mCurrentPath, directoryName));
            }
        });
    }

}
