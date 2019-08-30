package com.example.dbsdn.mychat.Openchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dbsdn.mychat.Openchat.OpenChat;
import com.example.dbsdn.mychat.R;

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

public class OpenChatList extends AppCompatActivity  {
    ArrayList<OpenChatItem> data = new ArrayList<>();
    android.support.v7.widget.RecyclerView RecyclerView; //리사이클러뷰
    LinearLayoutManager layoutManager; //리사이클러뷰에서 필요한 레이아웃 매니저
    OpenchatAdapter listAdapter; //어뎁터
    Button addbtn;
    String dn;
    Context context;

    private static final String PRODUCT_URL = "http://내주소/friendadd.php";

    public void onClick(final View view, int position){
        int itemPosition = RecyclerView.getChildLayoutPosition(view);
        OpenChatItem openChatItem = data.get(itemPosition);
        Intent chatintent= new android.content.Intent (OpenChatList.this, OpenChat.class);
        chatintent.putExtra("key", String.valueOf(openChatItem));
        Log.e ("보내는쪽","채팅방이름"+position);
        Toast.makeText (OpenChatList.this, (CharSequence) openChatItem,Toast.LENGTH_SHORT).show ();
        startActivity(chatintent);

    }
    //php 데이터 넣기
    public class ListDB extends AsyncTask<Void, Integer, Void> {

        String data = "";

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
                in = new BufferedReader (new InputStreamReader (is), 8 * 1024);
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

                                OpenChatItem openChatItem = new OpenChatItem (key);
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
                Toast.makeText (OpenChatList.this,error.getMessage (),Toast.LENGTH_SHORT).show ();
            }
        });

        Volley.newRequestQueue (this).add (stringRequest);
    } //mysql저장끝





    static class ListItemViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder{

        TextView date, cal;

        public ListItemViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.friendname);
            cal = (TextView) itemView.findViewById(R.id.friendn);

        }
    }

    //항목제거
    private void removeItemView(int position) {
//        OpenchatList.remove(position);
//        notifyItemRemoved(position);
//        notifyItemRangeChanged(position, OpenchatList.size()); // 지워진 만큼 다시 채워넣기.
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_openchatadd);



        addbtn = (Button)findViewById (R.id.addbtn);
        RecyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.friendrecycler);
        layoutManager = new LinearLayoutManager (getApplicationContext ());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //아이템이 어떻게 나열될지 선택 vertical or horizental
        RecyclerView.setLayoutManager(layoutManager); //레이아웃 매니저 연결
        listAdapter = new OpenchatAdapter(context,data); //어뎁터 만들고
        RecyclerView.setAdapter(listAdapter); //어뎁터 연결


        loadProducts();
        addbtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //다이얼로그 작성
                AlertDialog.Builder builder = new AlertDialog.Builder (OpenChatList.this);
                //다이얼로그 xml을 불러 그 xml에 넣어줄 에디터들을 넣어준다.
                View mView = getLayoutInflater ().inflate (R.layout.dialog_custom, null);
                final EditText fn = (EditText) mView.findViewById (R.id.fn);
                TextView fnbtn = (TextView) mView.findViewById (R.id.fnbtn);
                TextView fnbtnc = (TextView) mView.findViewById (R.id.fnc);
                builder.setView (mView);
                final AlertDialog alertDialog = builder.create ();
                alertDialog.show ();
                //다이얼로그 확인 버튼 누르면
                fnbtn.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        if (!fn.getText ().toString ().isEmpty ()){
                            Toast.makeText (OpenChatList.this,"추가되었습니다.",Toast.LENGTH_SHORT).show ();
                            dn=fn.getText ().toString ();
                            data.add(new OpenChatItem (dn));
                            listAdapter.notifyDataSetChanged();
                            Log.e ("누를때","들어가나"+dn);
                            ListDB rdb = new ListDB();
                            rdb.execute();
                            Log.e ("디비에 전송","ㅇ");
                            alertDialog.dismiss();

                        }else {
                            Toast.makeText (OpenChatList.this,"에러났어",Toast.LENGTH_SHORT).show ();
                            alertDialog.dismiss();
                        }
                    }
                });


                fnbtnc.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();                    }
                });


            }

        });

    }
}
