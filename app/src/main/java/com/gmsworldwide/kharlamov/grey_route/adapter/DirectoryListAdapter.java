package com.gmsworldwide.kharlamov.grey_route.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmsworldwide.kharlamov.grey_route.R;

import java.util.List;

public abstract class DirectoryListAdapter extends RecyclerView.Adapter<DirectoryListAdapter.DirectoryHolder> {

    List<String> mFileList;

    public DirectoryListAdapter(List<String> fileList) {
        this.mFileList = fileList;
    }

    @NonNull
    @Override
    public DirectoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_holder_directory, parent, false);
        return new DirectoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectoryHolder directoryHolder, int position) {
        final String directoryName = mFileList.get(position);
        if (directoryName != null) {
            directoryHolder.mDirectoryName.setText(directoryName);
        }
        directoryHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(directoryName);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    public abstract void onItemClick(String directoryName);


    public class DirectoryHolder extends RecyclerView.ViewHolder {

        TextView mDirectoryName;

        public DirectoryHolder(@NonNull View itemView) {
            super(itemView);
            mDirectoryName = itemView.findViewById(R.id.tv_directory_name);
        }
    }
}
