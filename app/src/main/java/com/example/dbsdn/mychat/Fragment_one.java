package com.example.dbsdn.mychat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dbsdn.mychat.Retrofit.RetroCallback;
import com.example.dbsdn.mychat.Retrofit.RetroClient;
import com.example.dbsdn.mychat.Retrofit.roomget;
import com.example.dbsdn.mychat.liveVideoBroadcaster.LiveVideoBroadcasterActivity;

import java.util.ArrayList;
import java.util.HashMap;


public class Fragment_one extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public ArrayList<RoomItem> items = new ArrayList<>();
    RoomRecyclerViewAdapter recyclerViewAdapter; //어댑터
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RetroClient retroClient;
    Context context;

    public Fragment_one() {

        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_one, container, false);

        //레트로핏 초기화 필수...
        retroClient = RetroClient.getInstance(context).createBaseApi();
        //당겨서 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        RedisDate();
        Button makeroom = (Button) view.findViewById(R.id.makeroom);
        makeroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 작성법
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //다이얼로그 xml을 불러 그 xml에 넣어줄 에디터들을 넣어준다.
                View mView = getLayoutInflater ().inflate (R.layout.dialog_newroom, null);
                final EditText roomtitle = (EditText) mView.findViewById (R.id.roomtitle);
                final EditText roomsummary = (EditText) mView.findViewById (R.id.roomsummary);
                //방생성
                TextView fnbtn = (TextView) mView.findViewById (R.id.fnbtn);
                //방생성취소
                TextView fnbtnc = (TextView) mView.findViewById (R.id.fnc);
                builder.setView (mView);
                final AlertDialog alertDialog = builder.create ();
                alertDialog.show ();

                //방생성 다이얼로구 확인 버튼 누르면
                fnbtn.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        if (!roomtitle.getText ().toString ().isEmpty ()){
                            String rt = roomtitle.getText ().toString ();
                            String rs = roomsummary.getText ().toString ();
                            Log.e ("누를때","들어가나"+rs+rt);

                           //배열이름. 리스트이름 순.
                            //add에 썸네일 이미지 가져온것 넣어야 할 듯.
                           items.add(new RoomItem(rt,"http://썸네일 생성/th.png",rs));
                           recyclerViewAdapter.notifyDataSetChanged();

                           //인텐트로 브로드캐스트에 데이터 넣기.프래그먼트는 this대신 getActivity를 사용한다.
                            Intent Broadintent = new Intent (getActivity(), LiveVideoBroadcasterActivity.class);
                            Broadintent.putExtra("roomname",rt);
                            Broadintent.putExtra("roomsummary",rs);
                            startActivity (Broadintent);
                            alertDialog.dismiss();

                        }else {
                            alertDialog.dismiss();
                        }
                    }
                });


                fnbtnc.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

            }
        });

        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //리사이클려부 가져오기.
        recyclerViewAdapter= new RoomRecyclerViewAdapter(context,items);
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;
    }


    private void RedisDate() {
        //데이터요청

        //데이터 삽입
        items.add(new RoomItem("아이유", "https://i.ytimg.com/vi/PQq_rAtkzmU/maxresdefault.jpg", "아이유는 대한민국의 가수이다."));
        items.add(new RoomItem("수지", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Suzy_at_Asia_Artist_Awards_red_carpet%2C_16_November_2016_02.jpg/250px-Suzy_at_Asia_Artist_Awards_red_carpet%2C_16_November_2016_02.jpg", "수지 는 대한민국의 가수 겸 배우이다."));
        items.add(new RoomItem("박윤원", "data:image/jpeg;base64,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", "나야"));
        //레트로핏으로 보낼 데이터 해쉬맵으로 작성.
        HashMap<String, Object> parameters = new HashMap<>();
        //서버에서 이값이 있어야만 데이터를 전송해줌.
        parameters.put("roomdata", 1);

        Log.e("데이터들", ""+parameters);

        retroClient.postoutBroadcast (parameters, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e("프레그먼트 원 onError도착", t.toString());

            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e("프레그먼트 원 onSuccess", "연결됨");
                //여기에 레트로핏에 받을 데이터
                roomget data = (roomget) receivedData;
                    //브로드 캐스트 레트로핏 따로 구성.
                    Log.e("브로드 캐스트 레트로핏", "" + data.toString());
                    Log.e("브로드 캐스트 방이름 레트로핏", "" + data.name);
                    Log.e("브로드 캐스트 방설명 레트로핏", "" + data.title);
                    Log.e("브로드 캐스터", "" + data.broadcaster);
                    Log.e("브로드 캐스트 플레이어", "" + data.player);
                    //Log.e("썸네일",""+data.png);

                items.add(new RoomItem(data.name, "http://썸내일/th.png", data.title));
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int code) {
                Log.e("프레그먼트 원 onFailure", "아예 요청 자체를 실패했을때");


            }
        });
    }

    //위로당겨서 새로고침.
    @Override
        public void onRefresh() {
        //리사이클러뷰 0.5초후 완료
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                items.clear();
                RedisDate();
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e("어댑터 초기화됨",""+recyclerViewAdapter.RoomItem);
                Snackbar.make(recyclerView,"동기화 완료",Snackbar.LENGTH_SHORT).show();
            }
        },500);
    }

    private void setRefresh(){
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.google_colors));
    }

}

