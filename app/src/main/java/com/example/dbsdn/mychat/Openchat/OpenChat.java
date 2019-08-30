package com.example.dbsdn.mychat.Openchat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dbsdn.mychat.Navigation;
import com.example.dbsdn.mychat.R;
import com.example.dbsdn.mychat.rtc.ConnectActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class OpenChat extends AppCompatActivity {


    TextView txtMessage, name_text;
    Button  btnSend;
    EditText editMessage;
    Handler msgHandler;
    SocketClient client;
    ReceiveThread receive;
    SendThread send;
    Socket socket;
    String name;
    //LinkedList<SocketClient> threadList;
    Context context;
    //리싸이클러뷰 변수
    String myname;
    ArrayList<chatingList> data = new ArrayList<>();
    android.support.v7.widget.RecyclerView RecyclerView; //리사이클러뷰
    LinearLayoutManager layoutManager; //리사이클러뷰에서 필요한 레이아웃 매니저
    ListAdapter listAdapter; //어뎁터
    String message;
    int key;
    //채팅내용 mysql에 저장하는 부분

    private static final String PRODUCT_URL = "http://내주소/chatadd.php";
    @SuppressLint({"HandlerLeak", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_openchat);
        //저장한거 불러오기
        loadProducts();
        //키보드 위로
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //닉네임 가져오기
        Intent chatintent = getIntent();
        key = chatintent.getIntExtra ("key",0);
        Log.e ("들어올때 채팅방이름.",""+key);
        context = this; //현재 액티비티정보.
        editMessage = (EditText) findViewById (R.id.addtext);
        btnSend = (Button)findViewById (R.id.addbyn);
//        txtMessage = (TextView)findViewById (R.id.textView);
        name_text = (TextView)findViewById (R.id.name_text);
        myname = Navigation.kakaoname;
        name_text.setText(" ID :   "+myname);
        //서버접속
        client = new SocketClient();
        client.start();
        RecyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.recycler);
        layoutManager = new LinearLayoutManager (getApplicationContext ());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //아이템이 어떻게 나열될지 선택 vertical or horizental
        RecyclerView.setLayoutManager(layoutManager); //레이아웃 매니저 연결
        listAdapter = new ListAdapter(data);
        RecyclerView.setAdapter (listAdapter);






        //핸들러
        msgHandler = new Handler (){
            @Override
            public void handleMessage(Message msg) {
                //네트워크 통신은 항상 백그라운드 스레드를 사용해야 한다. 이제부터 스레드 직접구현가능 어씽크가능.지금은 화면을 고치는일 전담.
                //백그라운드 스레드에서 받은 메시지 처리.
                super.handleMessage (msg);
                if (msg.what == 1111){
                    //채팅서버로부터 수신한 메시지를 텍스트뷰에 추가
                    String chat=msg.obj.toString ();
                    Log.e ("채팅내용 전부",""+chat);
                    StringTokenizer stk = new StringTokenizer(chat, ":");
                    //서버에서 보내준 상대방 아이디
                    name = stk.nextToken();
                    //서버에서 보내준 상대방 채팅내용
                    String c = stk.nextToken();
                    //서버에서 보내준 키. 이 값으로 채팅을 나누어 준다.
                    int tokenkey = Integer.parseInt (stk.nextToken());
                    Log.e ("받을때 key 맞나",""+key+"띄고"+tokenkey);
                    if (key==tokenkey) {

                        Log.e ("키값이 같으면", "문안" + key + tokenkey);
                        if (myname.equals (name)) {//서버에서 받은 이름과 내이름이 같으면
                            Log.e ("이프문안", "문안" + myname + name);

                        } else {
                            data.add (new chatingList (name, c)); //상대방 클랴이언트한테 받는다.
                            listAdapter.notifyDataSetChanged ();
                            RecyclerView.scrollToPosition (data.size ()-1);
                        }
                    }else if(key!=tokenkey) {

                        Log.e ("키 값이 같지 않으면", "" + myname + c);
                    }
                }

            }
        };


        //메세지 전송 버튼
        btnSend.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //메세지의 값이 아무것도 없거나 비어있지 않으면 데이터를 보낸다.
                if ( editMessage.getText ().toString ()!= null && ! editMessage.getText ().toString ().equals ("")){ //사용자가 입력한 값이 널이 아니고 빈값도 아니면
                    //리사이클러뷰 데이터 전송
                    Log.e ("메세지전송",""+editMessage.getText ().toString ());
                    data.add(new chatingList(myname,editMessage.getText ().toString ()));
                    listAdapter.notifyDataSetChanged ();
                    //입력 대화상자에는 빈값으로 전환한다.
                    send = new SendThread(socket, myname, editMessage.getText ().toString ());//전송용 스레드를 만들어서
                    send.start();//전송하고
                    // Log.e ("전송 메세지"+""+)
                    editMessage.setText ("");
                }
            }
        });

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
                                String id = productObject.getString ("user_id");
                                String chat = productObject.getString ("user_content");
                                int chatkey =  productObject.getInt ("chatkey");
                                chatingList chatingList = new chatingList (id,chat);
                                if (chatkey==key) {
                                    data.add (chatingList);
                                }
                            }
                            //// 어댑터 객체를 생성하고 이것을 recyclerview로 설정한다.
                            listAdapter = new ListAdapter(data);

                            RecyclerView.setAdapter (listAdapter);

                        }catch (JSONException e) {
                            e.printStackTrace ();
                        }
                    }
                }, new Response.ErrorListener () {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText (OpenChat.this,error.getMessage (),Toast.LENGTH_SHORT).show ();
            }
        });

        Volley.newRequestQueue (this).add (stringRequest);
    } //mysql저장끝


    //리사이클러뷰에 뿌릴 정보 완성
    public class chatingList {
        public  String date;
        public String cal;


        //메소드로 만들어서 가져온다.
        public String getDate() {
            return date;
        }

        public String getCal() {
            return cal;
        }

        public chatingList(String date, String cal){
            this.date = date;
            this.cal = cal;
        }
    }//뷰에 뿌릴 내용 작성완료


    //뷰홀더 뷰안의 속성이 계속 변경되어 해당 뷰위치를 계속 갱신하면 리소스를 많이 먹으니 이를 상속해서 고정함.
    class ListItemViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder{

        TextView date, cal;
        ImageView imageView;
        LinearLayout linearLayout;
        LinearLayout linearLayoutmain;
        public ListItemViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.item_name);
            cal = (TextView) itemView.findViewById(R.id.item_chat);
            linearLayout = (LinearLayout) itemView.findViewById (R.id.chatlayout);
            linearLayoutmain = (LinearLayout)itemView.findViewById (R.id.chatlinear);
        }
    }//뷰홀더 끝

    //어댑터 시작
    //어댑터는 ui와 입력한 데이터를 연결시켜주는 부분.
    class ListAdapter extends android.support.v7.widget.RecyclerView.Adapter{

        ArrayList<chatingList> data;

        ListAdapter(ArrayList<chatingList> data){
            this.data = data;
        }

        //뷰홀더를 어떻게 생성할 것인가 불어보는 부분 객체만들어주고 리턴해주면됨
        @Override
        public android.support.v7.widget.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.chatrecycler_item, parent, false);
            return new ListItemViewHolder(item);
        }//뷰홀더 끝

        //바인드 뷰홀더= 뷰홀더를 데이터와 바인딩 시킬때 어떻게 할 것이냐고 묻는다.
        //여기서 데이터를 추가하고 추가한 데이터들을 컨트롤 하기위해서 어레이리스트만든다.
        //아이템을 추가하는 메소드를 만드는 부분 여기선 add로 하겠다.
        //데이터가 거쳐가는곳이라고 생각하면 좋다. 이는 어댑터에 쌓여있어야 이것들을 리스트에 뿌려 줄 수있기때문
        @Override
        public void onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {
            chatingList chatingList = data.get(position);
            ListItemViewHolder item = (ListItemViewHolder) holder;
            //아까 만들어준 메소드를 넣어줌.
            //나인패치 적용하는 부분 내가 받은 메세지는 왼쪽에 내 메세지는 오른쪽에.
            Log.e ("바이드뷰홀더 조건",""+data);

            //내가보는메세지
            if (myname.equals (data.get (position).date)){
                item.cal.setText(chatingList.getCal());
                item.cal.setBackgroundResource (R.drawable.ms2);
                item.linearLayout.setVisibility (View.INVISIBLE);
                //item.cal.setTextSize (25);
                item.linearLayoutmain.setGravity (Gravity.RIGHT);

                //상대방이 보는 메세지
            }else {
                item.date.setText(chatingList.getDate());
                item.linearLayout.setVisibility (View.VISIBLE);
                item.cal.setBackgroundResource (R.drawable.ms1);
                //item.cal.setTextSize (25);
                item.cal.setText (chatingList.getCal ());
                item.linearLayoutmain.setGravity (Gravity.LEFT);
            }

            ((ListItemViewHolder) holder).linearLayout.setOnLongClickListener (new View.OnLongClickListener () {
                @Override
                public boolean onLongClick(View v) {

                    Toast.makeText(getApplicationContext(), "클릭", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // 제목셋팅
                    alertDialogBuilder.setTitle("영상통화 연결");

                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage("영상통화로 연결 하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("연결",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            // 여기서 인텐트로 영상 넘어가면 된다.
                                            M_DB mdb = new M_DB ();
                                            mdb.execute();
                                            Toast.makeText(getApplicationContext(), "영상통화", Toast.LENGTH_SHORT).show();
                                            Intent callintent= new Intent (OpenChat.this,ConnectActivity.class);
                                            startActivity(callintent);

                                        }
                                    })
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            // 다이얼로그를 취소한다
                                            dialog.cancel();
                                        }
                                    });

                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // 다이얼로그 보여주기
                    alertDialog.show();

                    return false;
                }
            });




        }


        //데이터셋 리셋
        @Override
        public int getItemCount() {
            return data.size();
        }
    } //어댑터끝




    private class SocketClient extends Thread {
        boolean threadAlive; //앱은 종료됬는데 스레드는 종료되지 않는 경우를 막기위해 블룬값으로 따로 온 오프 해준다.
        String ip;
        String port;

        OutputStream outputStream=null;
        BufferedReader br=null;
        DataOutputStream output=null;

        public SocketClient() {
            threadAlive=true;
            this.ip="54.180.8.252";
            this.port="8888";
        }


        //자바 서버와 접속하는 부분
        public void run(){
            try{
                //채팅서버접속
                socket = new Socket (ip, Integer.parseInt (port));
                //서버에 맥어드레스 메세지를 전달하기 위한 스트림 만듬
                output = new DataOutputStream (socket.getOutputStream ());
                //메세지 수신용 스레드 생성
                receive = new ReceiveThread(socket);
                receive.start();
                //와이파이 정보 관리자 객체로부터 폰의 맥 어드레스를 가져와서 채팅서버에 전달.
//                WifiManager mng=(WifiManager)context.getApplicationContext().getSystemService (
//                        WIFI_SERVICE);
//                WifiInfo info = mng.getConnectionInfo ();

                output.writeUTF (myname);
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
    }// 소켓클라이언트가 끝나는 부분
    //내부클래스

    //서버에서 도착한 메세지를 받아서 핸들러가 화면에 표시하는 즉 수신용 스레드.
    class ReceiveThread extends Thread{
        Socket socket=null;
        DataInputStream input=null;
        public ReceiveThread(Socket socket){ //생성자에서 소켓을 받고있다.
            this.socket=socket;
            try{
                //채팅서버로부터 메세지를 받기 위한 스트림 생성
                input = new DataInputStream (socket.getInputStream ());
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
        public  void run(){
            try{
                while (input != null){ //객체가 널이 아니면 즉 존재하면
                    //채팅 서버로 부터 내가 받은 메세지
                    String msg = input.readUTF();//여기서 읽는다
                    if (msg != null){//채팅서버에서 보낸 메시지가 널이 아니라면
                        //핸들러에게 전달할 메시지 객체
                        Message hdmsg = msgHandler.obtainMessage ();
                        hdmsg.what=1111; //메세지 식별자 (값 확인)
                        hdmsg.obj=msg;//메세지 본문
                        Log.e("메세지",""+hdmsg.obj);
                        //핸들러에게 메시지 전달 함.(화면 변경 요청)
                        msgHandler.sendMessage(hdmsg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace ();
            }
        }

    } //리시브 스레드 끝
    //내부클래스 샌드 스레드
    class SendThread extends Thread{ // 폰에서 작성한 메세지를 채팅서버로 보내주는 역할을 함
        Socket socket;
        String myname1;
        String sendmsg1;

      DataOutputStream output;
        public SendThread(Socket socket, String myname, String sendmsg){
            this.socket = socket;
            this.myname1 = myname;
            this.sendmsg1 = sendmsg;
            //마이네임 + 상대방 닉네임 c
            // this.integer =
            try{
                //채팅서버로 메시지를 보내기 위한 스트림 생성
                output = new DataOutputStream (socket.getOutputStream ());

            }catch (Exception e){
                e.printStackTrace ();
            }

        }
        public void run(){
            try{
                if (output != null){
                    if (sendmsg1 != null){
                        Log.e("서버보내는스레드",""+this.myname1+""+this.sendmsg1);
                        //채팅서버에 메시지 전달
                        //integer ();
                        //맥이 사용자 이름 그리고 샌드메세지가 내가 보낸 내용 그러니 내가 받을때 이 맥이 같으면 받지 않는것으로 한다.
                        output.writeUTF (this.myname1 + ":"+this.sendmsg1+":"+key); 
                        Log.e ("서버에 보낼때 포지션값",""+key);
                    }
                }
            }catch (Exception e){
                e.printStackTrace ();
            }

        }


    }
    //php 데이터 넣기
    public class M_DB extends AsyncTask<Void, Integer, Void> {

        String data = "";

        @Override
        public Void doInBackground(Void... unused) {

            /* 인풋 파라메터값 생성 */
            String param = "name=" + myname;
            Log.e ("들어가는값","있나"+myname);
            try {
                /* 서버연결 */
                URL url = new URL (
                        "http://내주소/push_notification.php");
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

}
