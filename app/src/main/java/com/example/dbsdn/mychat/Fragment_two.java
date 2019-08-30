package com.example.dbsdn.mychat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dbsdn.mychat.Openchat.OpenChatItem;
import com.example.dbsdn.mychat.Openchat.OpenchatAdapter;
import com.example.dbsdn.mychat.Retrofit.RetroClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class Fragment_two extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public ArrayList<OpenChatItem> data = new ArrayList<>();
    OpenchatAdapter openchatAdapter; //어댑터
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RetroClient retroClient;
    Context context;
    OpenchatAdapter listAdapter; //어뎁터
    private static final String PRODUCT_URL = "http://내주소/friendadd.php";
    public Fragment_two() {

        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_two, container, false);

        //레트로핏 초기화 필수...
        retroClient = RetroClient.getInstance(context).createBaseApi();
        //당겨서 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //RedisDate();
        data.add(new OpenChatItem("일번방이야~") );
        data.add(new OpenChatItem("아무나 놀사람") );
        data.add(new OpenChatItem("3번 방이야") );
        Button makeroom = (Button) view.findViewById(R.id.makeroom);
        makeroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 작성법
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //다이얼로그 xml을 불러 그 xml에 넣어줄 에디터들을 넣어준다.
                View mView = getLayoutInflater ().inflate (R.layout.dialog_custom, null);
                final EditText roomtitle = (EditText) mView.findViewById (R.id.fn);
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
                            Log.e ("누를때","들어가나"+rt);

                            //배열이름. 리스트이름 순.
                            //add에 썸네일 이미지 가져온것 넣어야 할 듯.
                            data.add(new OpenChatItem(rt));
                            openchatAdapter.notifyDataSetChanged();

                            //인텐트로 브로드캐스트에 데이터 넣기.프래그먼트는 this대신 getActivity를 사용한다.
                            Intent Broadintent = new Intent (getActivity(), Chat.class);
                            Broadintent.putExtra("roomname",rt);
                           // Broadintent.putExtra("roomsummary",rs);
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
        openchatAdapter= new OpenchatAdapter(context,data);
        recyclerView.setAdapter(openchatAdapter);

        return view;
    }


//    private void RedisDate() {
//        //데이터요청
//
//        //데이터 삽입
//        data.add(1,"거");
//        //레트로핏으로 보낼 데이터 해쉬맵으로 작성.
//        HashMap<String, Object> parameters = new HashMap<>();
//        //서버에서 이값이 있어야만 데이터를 전송해줌.
//        parameters.put("roomdata", 1);
//
//        Log.e("데이터들", ""+parameters);
//
//        retroClient.postoutBroadcast (parameters, new RetroCallback() {
//            @Override
//            public void onError(Throwable t) {
//                Log.e("프레그먼트 원 onError도착", t.toString());
//
//            }
//            @Override
//            public void onSuccess(int code, Object receivedData) {
//                Log.e("프레그먼트 원 onSuccess", "연결됨");
//                //여기에 레트로핏에 받을 데이터
//                roomget data = (roomget) receivedData;
//                //브로드 캐스트 레트로핏 따로 구성.
//                Log.e("브로드 캐스트 레트로핏", "" + data.toString());
//                Log.e("브로드 캐스트 방이름 레트로핏", "" + data.name);
//                Log.e("브로드 캐스트 방설명 레트로핏", "" + data.title);
//                Log.e("브로드 캐스터", "" + data.broadcaster);
//                Log.e("브로드 캐스트 플레이어", "" + data.player);
//                //Log.e("썸네일",""+data.png);
//
//                data.add(new RoomItem(data.name, "https://i.ytimg.com/vi/JL51h56KdDg/hqdefault.jpg", data.title));
//                openchatAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(int code) {
//                Log.e("프레그먼트 원 onFailure", "아예 요청 자체를 실패했을때");
//
//
//            }
//        });
//    }

    //위로당겨서 새로고침.
    @Override
    public void onRefresh() {
        //리사이클러뷰 0.5초후 완료
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                data.clear();
                //RedisDate();
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e("어댑터 초기화됨","");
                Snackbar.make(recyclerView,"동기화 완료",Snackbar.LENGTH_SHORT).show();
            }
        },500);
    }

    private void setRefresh(){
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.google_colors));
    }
    //php 데이터 넣기
    public class ListDB extends AsyncTask<Void, Integer, Void> {

        String data = "";
        String dn;
        @Override
        public Void doInBackground(Void... unused) {

            /* 인풋 파라메터값 생성 */
            String param = "name=" + dn;
            Log.e ("들어가는값","있나"+dn);
            try {
                /* 서버연결 */
                URL url = new URL (
                        "http://주소/OpenchatList.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection ();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect ();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream ();
                outs.write (param.getBytes ("UTF-8"));
                outs.flush ();
                outs.close ();

                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;


                is = conn.getInputStream ();
                in = new BufferedReader (new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer ();
                while ((line = in.readLine ()) != null) {
                    buff.append (line + "\n");
                }
                data = buff.toString ().trim ();
                Log.e ("RECV DATA", data);//data는 php상 echo값임


            } catch (MalformedURLException e) {
                e.printStackTrace ();
            } catch (IOException e) {
                e.printStackTrace ();
            }

            return null;
        }


    }



    //mysql 저장된 내용이 표시된다.
    private void loadProducts(){

        StringRequest stringRequest = new StringRequest (Request.Method.GET, PRODUCT_URL,
                new Response.Listener<String> () {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //json 배열 객체로 문자열 변환
                            JSONArray products = new JSONArray (response);
                            // 모든 객체를 통과 함
                            for (int i =0; i<products.length (); i++){

                                // json 배열에서 제품 객체 가져 오기
                                JSONObject productObject = products.getJSONObject (i);
                                String key = productObject.getString ("name");
                                Log.e ("저장된거 뿌려지나","?"+key);
                                // key = productObject.getString ("kkk");

                                OpenChatItem openChatItem = new OpenChatItem(key);
                                data.add (openChatItem);
                                listAdapter.notifyDataSetChanged();

                            }
//                            //// 어댑터 객체를 생성하고 이것을 recyclerview로 설정한다.
//                            listAdapter = new friendaddAdapter (data);
//
//                            RecyclerView.setAdapter (listAdapter);

                        }catch (JSONException e) {
                            e.printStackTrace ();
                        }
                    }
                }, new Response.ErrorListener () {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText (context,error.getMessage (),Toast.LENGTH_SHORT).show ();
            }
        });

        Volley.newRequestQueue (context).add (stringRequest);
    } //mysql저장끝


}

