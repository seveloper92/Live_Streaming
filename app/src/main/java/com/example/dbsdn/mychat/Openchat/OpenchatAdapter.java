package com.example.dbsdn.mychat.Openchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dbsdn.mychat.Chat;
import com.example.dbsdn.mychat.R;

import java.util.ArrayList;

public class OpenchatAdapter extends RecyclerView.Adapter<OpenchatAdapter.MyViewHolder>{

    ArrayList<OpenChatItem> data;
    private LayoutInflater mInflate;
    Context context;


    public OpenchatAdapter(Context context,ArrayList<OpenChatItem> data){
        this.data = data;
        this.mInflate = LayoutInflater.from(context);
        this.context = context;
    }


    @NonNull
    @Override
    public OpenchatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.openchatitem, parent, false);
        OpenchatAdapter.MyViewHolder viewHolder = new OpenchatAdapter.MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OpenchatAdapter.MyViewHolder myViewHolder, final int position) {
        OpenChatItem openChatItem = data.get(position);
        myViewHolder.name.setText(openChatItem.date);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent ChatClickintent = new Intent(context, Chat.class);
                    Log.e("방이름",""+ data.get(position).date+position);
                    context.startActivity(ChatClickintent);

            }
        });
    }


    //데이터 순서값
    @Override
    public int getItemCount() {
        return data.size();
    }


    //ViewHolder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
       TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.friendname);
        }
    }
}
