package com.example.dbsdn.mychat;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.dbsdn.mychat.LiveChatAdapter.LiveChatAdapter;
import com.example.dbsdn.mychat.LiveChatAdapter.LiveChat_Item;
import com.example.dbsdn.mychat.databinding.ActivityMain2Binding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Chat extends AppCompatActivity {

    Handler handler;
    String receivedata;
    android.support.v7.widget.RecyclerView RecyclerView; //리사이클러뷰
    LinearLayoutManager layoutManager; //리사이클러뷰에서 필요한 레이아웃 매니저
    SocketChannel socketChannel;
    private static final String HOST = "192.168.219.111";
    private static final int PORT = 5001;
    public ArrayList<LiveChat_Item> chatdata = new ArrayList<>();
    LiveChatAdapter liveChatAdapter;
    ActivityMain2Binding binding;
    String nickname = Navigation.kakaoname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2);
        handler = new Handler();
        //리사이클러뷰 선언.
        RecyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.openrecycler);
        layoutManager = new LinearLayoutManager(getApplicationContext ());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //아이템이 어떻게 나열될지 선택 vertical or horizental
        liveChatAdapter = new LiveChatAdapter(chatdata);
        RecyclerView.setLayoutManager(layoutManager); //레이아웃 매니저 연결
        RecyclerView.setAdapter (liveChatAdapter);
        binding.nickname.setText(" ID :   "+nickname);

        //키보드 위로.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent ChatClickintent = getIntent();
        chatdata.add(new LiveChat_Item(ChatClickintent.getStringExtra("name1"),ChatClickintent.getStringExtra("chat1")));
        chatdata.add(new LiveChat_Item(ChatClickintent.getStringExtra("name2"),ChatClickintent.getStringExtra("chat2")));
        liveChatAdapter.notifyDataSetChanged();


        //소켓연결 스레드.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    Log.e("소켓연결성공",  "a"+HOST+PORT);
                    binding.sendMsgBtn.setEnabled(true);
                } catch (Exception ioe) {
                    Log.e("연결안됨", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();

        //서버로 보냄
        binding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String return_msg = binding.sendMsgEditText.getText().toString();
                    Log.e("텍스트내용",  ""+return_msg);
                    chatdata.add(new LiveChat_Item(nickname,return_msg));
                    Log.e("채팅어레이 내용",  ""+chatdata);
                    //리사이클러뷰 최하단
                    RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
                    liveChatAdapter.notifyDataSetChanged();
                    //RecyclerView.scrollToPosition (chatdata.size ()-1);
                    if (!TextUtils.isEmpty(return_msg)) {
                        new Chat.SendmsgTask().execute("방이름"+":"+nickname+":"+return_msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //서버로보내는 스레드
    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("EUC-KR")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.sendMsgEditText.setText("");
                }
            });
        }
    }

    //서버에서 받는 스레드
    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.e("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("EUC-KR");
                receivedata = charset.decode(byteBuffer).toString();
                Log.e("receive", "msg :" + receivedata);
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.e("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //채팅업데이트
    private Runnable showUpdate = new Runnable() {

        public void run() {
            StringTokenizer token = new StringTokenizer(receivedata,":");
            String username = token.nextToken();
            String chat = token.nextToken();
            chatdata.add(new LiveChat_Item(username, chat));
            //리사이클러뷰 최하단으로
            RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
            liveChatAdapter.notifyDataSetChanged();
//            chatdata.add(new LiveChat_Item(nickname,receivedata));
//            liveChatAdapter.notifyDataSetChanged();
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}

