package com.example.dbsdn.mychat.liveVideoBroadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dbsdn.mychat.LiveChatAdapter.LiveChatAdapter;
import com.example.dbsdn.mychat.LiveChatAdapter.LiveChat_Item;
import com.example.dbsdn.mychat.Navigation;
import com.example.dbsdn.mychat.R;
import com.example.dbsdn.mychat.Retrofit.roomdel;
import com.example.dbsdn.mychat.Retrofit.roomget;
import com.example.dbsdn.mychat.Retrofit.RetroCallback;
import com.example.dbsdn.mychat.Retrofit.RetroClient;
import com.example.dbsdn.mychat.databinding.ActivityLiveVideoBroadcasterBinding;
import com.example.dbsdn.mychat.databinding.ChatrecyclerItemBinding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import io.antmedia.android.broadcaster.ILiveVideoBroadcaster;
import io.antmedia.android.broadcaster.LiveVideoBroadcaster;
import io.antmedia.android.broadcaster.utils.Resolution;

import static com.example.dbsdn.mychat.Navigation.RTMP_BASE_URL;


//다이얼로그로 데이터 적으면 여기로 넘어와서 바로 실행되게 만들어야함.
public class LiveVideoBroadcasterActivity extends AppCompatActivity {

        //라이브방송 관련
        private static final String TAG = LiveVideoBroadcasterActivity.class.getSimpleName();
        private ViewGroup mRootView;
        public boolean mIsRecording = false;
        private Timer mTimer;
        private long mElapsedTime;
        public TimerHandler mTimerHandler;
        private ImageButton mSettingsButton;
        private CameraResolutionsFragment mCameraResolutionsDialog;
        private Intent mLiveVideoBroadcasterServiceIntent;
        private TextView mStreamLiveStatus;
        private GLSurfaceView mGLView;
        private ILiveVideoBroadcaster mLiveVideoBroadcaster;
        private Button mBroadcastControlButton;

        //방정보
        String roomtitle;
        String roomsummary;

        //레트로핏
        RetroClient retroClient;

        //데이터 바인딩.
        ChatrecyclerItemBinding binding;

        //리사이클러
        android.support.v7.widget.RecyclerView RecyclerView; //리사이클러뷰

        //네티 핸들러.
        Handler handler;

        //아이디 텍스트
        TextView nick;

        //네티 채팅버튼
        Button sendMsgBtn;
        EditText sendMsgEditText;
        //받는채팅변수
        String receivedata;
        LinearLayoutManager layoutManager; //리사이클러뷰에서 필요한 레이아웃 매니저
        SocketChannel socketChannel;
        String HOST = Navigation.NETTY;
        private static final int PORT = 5001;
        String msg;
        ArrayList<LiveChat_Item> chatdata = new ArrayList<>();
        //라이브 채팅 리사이클러뷰
        LiveChatAdapter liveChatAdapter;
        String nickname = Navigation.kakaoname;

        private ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
                if (mLiveVideoBroadcaster == null) {
                    mLiveVideoBroadcaster = binder.getService();
                    mLiveVideoBroadcaster.init(LiveVideoBroadcasterActivity.this, mGLView);
                    mLiveVideoBroadcaster.setAdaptiveStreaming(true);
                }
                mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mLiveVideoBroadcaster = null;
            }
        };


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // 제목숨기기.  Hide title
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            binding = DataBindingUtil.setContentView(this, R.layout.chatrecycler_item);
            handler = new Handler();
            mTimerHandler = new TimerHandler();
            //binding on resume not to having leaked service connection
            mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
            //this makes service do its job until done
            startService(mLiveVideoBroadcasterServiceIntent);

            setContentView(R.layout.activity_live_video_broadcaster);

           //닉네임적용.
            nick = findViewById(R.id.nickname);
            nick.setText(nickname);

            mRootView = (ViewGroup) findViewById(R.id.root_layout);
            mSettingsButton = (ImageButton) findViewById(R.id.settings_button);
            mStreamLiveStatus = (TextView) findViewById(R.id.stream_live_status);
            mBroadcastControlButton = (Button) findViewById(R.id.toggle_broadcasting);

            // Configure the GLSurfaceView.  This will start the Renderer thread, with an
            // appropriate EGL activity.
            mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
            if (mGLView != null) {
                mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
            }

            //키보드 위로.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            //네티 채팅 리사이클러뷰 선언.
            RecyclerView = findViewById(R.id.Broadcaster_recycler);
            layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //아이템이 어떻게 나열될지 선택 vertical or horizental
            liveChatAdapter = new LiveChatAdapter(chatdata);
            RecyclerView.setLayoutManager(layoutManager); //레이아웃 매니저 연결
            RecyclerView.setAdapter(liveChatAdapter);

            //다이얼로그에서 방정보 받기.
            Intent Broadintent = getIntent();
            roomtitle = Broadintent.getStringExtra("roomname");
            roomsummary = Broadintent.getStringExtra("roomsummary");
            Log.e("다이얼로그에서 적은 정보", "" + roomtitle + roomsummary);

            //레트로핏 초기화
            retroClient = RetroClient.getInstance(this).createBaseApi();
        }

        public void changeCamera(View v) {
            if (mLiveVideoBroadcaster != null) {
                mLiveVideoBroadcaster.changeCamera();
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            //this lets activity bind
            bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);

        }

        //권한 체크
        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            switch (requestCode) {
                case LiveVideoBroadcaster.PERMISSIONS_REQUEST: {
                    if (mLiveVideoBroadcaster.isPermissionGranted()) {
                        mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.CAMERA) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        Manifest.permission.RECORD_AUDIO)) {
                            mLiveVideoBroadcaster.requestPermission();
                        } else {
                            new AlertDialog.Builder(LiveVideoBroadcasterActivity.this)
                                    .setTitle(R.string.permission)
                                    .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            try {
                                                //Open the specific App Info page:
                                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                                startActivity(intent);

                                            } catch (ActivityNotFoundException e) {
                                                //e.printStackTrace();

                                                //Open the generic Apps page:
                                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                                startActivity(intent);

                                            }
                                        }
                                    })
                                    .show();
                        }
                    }
                    return;
                }
            }
        }

        @Override
        protected void onPause() {
            super.onPause();
            Log.i(TAG, "onPause");

            //hide dialog if visible not to create leaked window exception
            if (mCameraResolutionsDialog != null && mCameraResolutionsDialog.isVisible()) {
                mCameraResolutionsDialog.dismiss();
            }
            mLiveVideoBroadcaster.pause();
        }

        //온 스탑
        @Override
        protected void onStop() {
            super.onStop();
            delredis();
            unbindService(mConnection);
        }

        //온디스트로이
        @Override
        protected void onDestroy() {
            super.onDestroy();
            try {
                socketChannel.close();
                delredis();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);

            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLiveVideoBroadcaster.setDisplayOrientation();
            }

        }

        //화질 변경
        public void showSetResolutionDialog(View v) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragmentDialog = getSupportFragmentManager().findFragmentByTag("dialog");
            if (fragmentDialog != null) {

                ft.remove(fragmentDialog);
            }

            ArrayList<Resolution> sizeList = mLiveVideoBroadcaster.getPreviewSizeList();


            if (sizeList != null && sizeList.size() > 0) {
                mCameraResolutionsDialog = new CameraResolutionsFragment();

                mCameraResolutionsDialog.setCameraResolutions(sizeList, mLiveVideoBroadcaster.getPreviewSize());
                mCameraResolutionsDialog.show(ft, "resolutiton_dialog");
            } else {
                Snackbar.make(mRootView, "No resolution available", Snackbar.LENGTH_LONG).show();
            }

        }

        @SuppressLint("StaticFieldLeak")
        public void toggleBroadcasting(View v) {
            if (!mIsRecording) {
                if (mLiveVideoBroadcaster != null) {
                    if (!mLiveVideoBroadcaster.isConnected()) {
                        Log.e("방송 bloon값",""+mIsRecording);
                        String streamName = roomtitle;
                        chatdata.add(new LiveChat_Item(nickname+"님이 방송을 시작하였습니다.",""));
                        liveChatAdapter.notifyDataSetChanged();
                        Log.e("chatdata",""+chatdata.get(0).chat);

                        //redis 방송정보 전송
                        inputredis();

                        //네티연결
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socketChannel = SocketChannel.open();
                                    socketChannel.configureBlocking(true);
                                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                                    Log.e("소켓연결성공",  "a"+HOST+PORT);
                                } catch (Exception ioe) {
                                    Log.e("연결안됨", ioe.getMessage());
                                    ioe.printStackTrace();
                                }
                                checkUpdate.start();
                            }
                        }).start();

                        new AsyncTask<String, String, Boolean>() {
                            ContentLoadingProgressBar
                                    progressBar;

                            @Override
                            protected void onPreExecute() {
                                progressBar = new ContentLoadingProgressBar(LiveVideoBroadcasterActivity.this);
                                progressBar.show();
                            }

                            @Override
                            protected Boolean doInBackground(String... url) {
                                return mLiveVideoBroadcaster.startBroadcasting(url[0]);

                            }


                            @Override
                            protected void onPostExecute(Boolean result) {
                                progressBar.hide();
                                mIsRecording = result;
                                if (result) {
                                    mStreamLiveStatus.setVisibility(View.VISIBLE);
                                    mBroadcastControlButton.setText(R.string.stop_broadcasting);
                                    mSettingsButton.setVisibility(View.GONE);
                                    startTimer();//start the recording duration
                                } else {
                                    Snackbar.make(mRootView, R.string.stream_not_started, Snackbar.LENGTH_LONG).show();

                                    triggerStopRecording();
                                }
                            }
                        }.execute(RTMP_BASE_URL + streamName);

                    } else {
                        Snackbar.make(mRootView, R.string.streaming_not_finished, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mRootView, R.string.oopps_shouldnt_happen, Snackbar.LENGTH_LONG).show();
                }
            } else {
                triggerStopRecording();
            }


            //채팅전송버튼
            //네티 버튼
            sendMsgBtn = findViewById(R.id.sendMsgBtn);
            sendMsgEditText = findViewById(R.id.sendMsgEditText);
            sendMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final String return_msg = sendMsgEditText.getText().toString();
                        Log.e("텍스트내용",  ""+return_msg);
                        //binding.receiveMsgTv.setText(return_msg);
                        chatdata.add(new LiveChat_Item(nickname,return_msg));
                        Log.e("채팅어레이 내용",  ""+chatdata);
                        //리사이클러뷰 최하단으로
                        RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
                        liveChatAdapter.notifyDataSetChanged();

                    // : 로 토크나저 형식으로 방이름과 유저이름을 나누어 보낸다.
                    //서버에서 받아 토크나이저 분해한 다음 방이름이 같으면 전송.
                        //
                        if (!TextUtils.isEmpty(return_msg)) {
                            new SendmsgTask().execute(roomtitle+":"+nickname+":"+return_msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        public void triggerStopRecording() {
            if (mIsRecording) {
                Log.e("방송 bloon값",""+mIsRecording);
                mBroadcastControlButton.setText(R.string.start_broadcasting);
                mStreamLiveStatus.setVisibility(View.GONE);
                mStreamLiveStatus.setText(R.string.live_indicator);
                mSettingsButton.setVisibility(View.VISIBLE);
                mLiveVideoBroadcaster.stopBroadcasting();
                //방송정보 삭제..
                delredis();
                stopTimer();
                try {
                    socketChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mIsRecording = false;
        }

        //This method starts a mTimer and updates the textview to show elapsed time for recording
        public void startTimer() {

            if (mTimer == null) {
                mTimer = new Timer();
            }

            mElapsedTime = 0;
            mTimer.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                    mElapsedTime += 1; //increase every sec
                    mTimerHandler.obtainMessage(TimerHandler.INCREASE_TIMER).sendToTarget();

                    if (mLiveVideoBroadcaster == null || !mLiveVideoBroadcaster.isConnected()) {
                        mTimerHandler.obtainMessage(TimerHandler.CONNECTION_LOST).sendToTarget();
                    }
                }
            }, 0, 1000);
        }


        public void stopTimer() {
            if (mTimer != null) {
                this.mTimer.cancel();
            }
            this.mTimer = null;
            this.mElapsedTime = 0;
        }

        public void setResolution(Resolution size) {
            mLiveVideoBroadcaster.setResolution(size);
        }

        private class TimerHandler extends Handler {
            static final int CONNECTION_LOST = 2;
            static final int INCREASE_TIMER = 1;

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case INCREASE_TIMER:
                        mStreamLiveStatus.setText(getString(R.string.live_indicator) + " - " + getDurationString((int) mElapsedTime));
                        break;
                    case CONNECTION_LOST:
                        triggerStopRecording();
                        new AlertDialog.Builder(LiveVideoBroadcasterActivity.this)
                                .setMessage(R.string.broadcast_connection_lost)
                                .setPositiveButton(android.R.string.yes, null)
                                .show();

                        break;
                }
            }
        }

        public static String getDurationString(int seconds) {

            if (seconds < 0 || seconds > 2000000)//there is an codec problem and duration is not set correctly,so display meaningfull string
                seconds = 0;
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            seconds = seconds % 60;

            if (hours == 0)
                return twoDigitString(minutes) + " : " + twoDigitString(seconds);
            else
                return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
        }

        public static String twoDigitString(int number) {

            if (number == 0) {
                return "00";
            }

            if (number / 10 == 0) {
                return "0" + number;
            }

            return String.valueOf(number);
        }

        //네티 Async

        //서버로보내는 스레드
        private class SendmsgTask extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    socketChannel
                            .socket()
                            .getOutputStream()
                            .write(strings[0].getBytes("EUC-KR")); // 서버로
                    Log.e("보내는 AsyncTask",  "되나");
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
                        sendMsgEditText.setText("");
                    }
                });
            }
        }


        //서버에서 받는
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
                Log.e("receive", "Bro_msg :" + receivedata);
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
                Log.e("브로드캐스터 채팅업데이트",""+receivedata);
                //리사이클러뷰 최하단으로
                RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
                liveChatAdapter.notifyDataSetChanged();
            }

        };


        //redis 데이터 전송.
        protected void inputredis() {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("name", roomtitle);
            parameters.put("title", roomsummary);
            parameters.put("broadcaster", nick.getText().toString());
            parameters.put("player", 1);
            //parameters.put("png", streamthumbnail);


            Log.e("데이터들", "" + parameters);

            retroClient.postinBroadcast(parameters, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e("방정정보onError도착", t.toString());
                    //activitySignBinding.textView.setText("Error");

                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.e("onSuccess", "연결됨");
                    //여기에 레트로핏에 받을 데이터
                    roomget data = (roomget) receivedData;
                    //브로드 캐스트 레트로핏 따로 구성.
                    Log.e("브로드 캐스트 레트로핏", "" + data);
                    Log.e("브로드 캐스트 방이름 레트로핏", "" + data.name);
                    Log.e("브로드 캐스트 방설명 레트로핏", "" + data.title);
                    Log.e("브로드 캐스터", "" + data.broadcaster);
                    Log.e("브로드 캐스트 플레이어", "" + data.player);
                    //Log.e("썸네일",""+data.png);

                }

                @Override
                public void onFailure(int code) {
                    Log.e("onFailure", "아예 요청 자체를 실패했을때");

                }
            });
        }
    //redis 데이터 삭제
    protected void delredis() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", roomtitle);
        Log.e("삭제데이터", "" + parameters);

        retroClient.delpostBroadcast(parameters, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e("삭제onError도착", t.toString());
                //activitySignBinding.textView.setText("Error");

            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e("삭제onSuccess", "연결됨");
                //여기에 레트로핏에 받을 데이터
                roomdel data = (roomdel) receivedData;
                //브로드 캐스트 레트로핏 따로 구성.
                Log.e("브로드 캐스트 레트로핏", "" + data.name);

            }

            @Override
            public void onFailure(int code) {
                Log.e("삭제onFailure", "아예 요청 자체를 실패했을때");


            }
        });

    }
    }
