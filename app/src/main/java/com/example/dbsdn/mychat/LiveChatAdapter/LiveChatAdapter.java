package com.example.dbsdn.mychat.LiveChatAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dbsdn.mychat.R;
import com.example.dbsdn.mychat.databinding.ChatrecyclerItemBinding;

import java.util.ArrayList;

//어댑터 시작
//어댑터는 ui와 입력한 데이터를 연결시켜주는 부분.
public class LiveChatAdapter extends android.support.v7.widget.RecyclerView.Adapter{

    protected final ArrayList<LiveChat_Item> chatdata;

    public LiveChatAdapter(ArrayList<LiveChat_Item> chatdata){
        this.chatdata = chatdata;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // 아이템 xml과 연결
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(ChatrecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }


    //아래부턴 리사이클러뷰 관련.
    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder{
        ChatrecyclerItemBinding binding;

        public ViewHolder(  ChatrecyclerItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }//뷰홀더 끝
    //바인드 뷰홀더= 뷰홀더를 데이터와 바인딩 시킬때 어떻게 할 것이냐고 묻는다.
    //여기서 데이터를 추가하고 추가한 데이터들을 컨트롤 하기위해서 어레이리스트만든다.
    //아이템을 추가하는 메소드를 만드는 부분 여기선 add로 하겠다.
    //데이터가 거쳐가는곳이라고 생각하면 좋다. 이는 어댑터에 쌓여있어야 이것들을 리스트에 뿌려 줄 수있기때문
    @Override
    public void onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {

        LiveChat_Item ChatingItem = chatdata.get(position);
        Log.e ("채팅 데이터",""+chatdata);
        ((ViewHolder) holder).binding.itemName.setText(ChatingItem.getname());
        ((ViewHolder) holder).binding.itemChat.setText(ChatingItem.getchat());

    }


    //데이터셋 리셋
    @Override
    public int getItemCount() {
        return chatdata.size();
    }
} //어댑터끝