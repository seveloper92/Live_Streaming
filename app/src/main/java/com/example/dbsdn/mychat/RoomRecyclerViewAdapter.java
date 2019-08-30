package com.example.dbsdn.mychat;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dbsdn.mychat.liveVideoPlayer.LiveVideoPlayerActivity;

import java.util.ArrayList;

public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.MyViewHolder> {

    protected final ArrayList<RoomItem> RoomItem;
    private LayoutInflater mInflate;
    private Context mContext;

    public RoomRecyclerViewAdapter(Context context, ArrayList<RoomItem> persons) {
        this.mContext = context;
        this.mInflate = LayoutInflater.from(context);
        this.RoomItem = persons;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.live_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        //데이터오 뷰를 바인딩
        String url = RoomItem.get(position).photo;
        holder.name.setText(RoomItem.get(position).roomname);
        Glide.with(mContext)
                .load(url)
                .centerCrop()
                .into(holder.imageView);
        holder.summary.setText(RoomItem.get(position).summary);

        //아이콘 클릭하면 방에 들어간다.
        holder.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //클릭시 웹검색하게 하자.
//                String term = RoomItem.get(position).roomname;
//                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//                intent.putExtra(SearchManager.QUERY, term);
//                mContext.startActivity(intent);
                Intent roomClickintent = new Intent(mContext, LiveVideoPlayerActivity.class);
                Log.e("방이름",""+ RoomItem.get(position).roomname +RoomItem.get(position));
                roomClickintent.putExtra("roomname",RoomItem.get(position).roomname);
                mContext.startActivity(roomClickintent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return RoomItem.size();
    }


    //ViewHolder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageView;
        public TextView summary;
        ImageView search;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.tv_name);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            summary = (TextView) itemView.findViewById(R.id.tv_summary);
            search = (ImageView) itemView.findViewById(R.id.bt_search);
            //search.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        }
    }
}
