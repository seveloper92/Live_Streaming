/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.dbsdn.mychat.liveVideoPlayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dbsdn.mychat.LiveChatAdapter.LiveChatAdapter;
import com.example.dbsdn.mychat.LiveChatAdapter.LiveChat_Item;
import com.example.dbsdn.mychat.Navigation;
import com.example.dbsdn.mychat.R;
import com.example.dbsdn.mychat.databinding.ActivityLiveVideoPlayerBinding;
import com.example.dbsdn.mychat.databinding.ChatrecyclerItemBinding;
import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LiveVideoPlayerActivity extends AppCompatActivity implements OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

  public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;
  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private Handler mainHandler;
  private EventLogger eventLogger;
  private SimpleExoPlayerView simpleExoPlayerView;


  private DataSource.Factory mediaDataSourceFactory;
  private SimpleExoPlayer player;
  private DefaultTrackSelector trackSelector;
  private boolean needRetrySource;

  private boolean shouldAutoPlay;
  private int resumeWindow;
  private long resumePosition;
  private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
  protected String userAgent;

  String Playerroomname;
  String nickname = Navigation.kakaoname;

  //아이디 텍스트
  TextView nick;
  //네티 채팅
  Button sendMsgBtn;
  EditText sendMsgEditText;
  Handler handler;
  String receivedata;
  SocketChannel socketChannel;
  //네티채팅 서버주소
  String HOST = Navigation.NETTY;
  LinearLayoutManager layoutManager;
  private static final int PORT = 5001;
  ArrayList<LiveChat_Item> chatdata = new ArrayList<>();
  LiveChatAdapter liveChatAdapter;
  ChatrecyclerItemBinding binding;
  android.support.v7.widget.RecyclerView RecyclerView; //리사이클러


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //닉네임
//    myname = Navigation.nickname;
//    Log.e("닉네임",""+myname);

    //메세지
    final String return_msg;
    //방이름 받기.
    Intent roomClickintent = getIntent();
    Playerroomname = (String) roomClickintent.getStringExtra("roomname");
    Log.e("사용자방이름",""+ Playerroomname);
    userAgent = Util.getUserAgent(this, "ExoPlayerDemo");

    //바인딩
    binding = DataBindingUtil.setContentView(this, R.layout.chatrecycler_item);


    handler = new Handler();
    shouldAutoPlay = true;
    clearResumePosition();
    mediaDataSourceFactory = buildDataSourceFactory(true);
    rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();
    mainHandler = new Handler();
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    setContentView(R.layout.activity_live_video_player);
    View rootView = findViewById(R.id.root);
    rootView.setOnClickListener(this);

    simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
    simpleExoPlayerView.setControllerVisibilityListener(this);
    simpleExoPlayerView.requestFocus();

    //리사이클러뷰 선언.
    RecyclerView = findViewById(R.id.player_recycler);
    layoutManager = new LinearLayoutManager(getApplicationContext ());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //아이템이 어떻게 나열될지 선택 vertical or horizental
    RecyclerView.setLayoutManager(layoutManager); //레이아웃 매니저 연결
    liveChatAdapter = new LiveChatAdapter(chatdata);
    RecyclerView.setAdapter (liveChatAdapter);
    //키보드 위로.
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    play(null);

    //네티소켓연결 스레드.
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          socketChannel = SocketChannel.open();
          socketChannel.configureBlocking(true);
          socketChannel.connect(new InetSocketAddress(HOST, PORT));
          Log.e("소켓연결성공",  "a"+HOST+PORT);
        } catch (Exception ioe) {
          Log.e("연결안됨", ioe.getMessage() + "a");
          ioe.printStackTrace();

        }
        checkUpdate.start();
      }
    }).start();

//    //닉네임적용.
    nick = findViewById(R.id.nickname);
    nick.setText(nickname);
    chatdata.add(new LiveChat_Item(nickname+"님 환영합니다.",""));
    //채팅전송버튼
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
          Log.e("채팅어레이 내용",  ""+liveChatAdapter);
          //리사이클러뷰 최하단으로
          RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
          liveChatAdapter.notifyDataSetChanged();


          //여기에 : 로 토크나저 형식으로 방이름과 유저이름을 나누어 보낸다.
          //서버에서 토크나이저 분해한다음 방이름이 같아야만 보내자.
          if (!TextUtils.isEmpty(return_msg)) {
            new SendmsgTask().execute(Playerroomname+":"+nickname+":"+return_msg);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }


  @Override
  public void onNewIntent(Intent intent) {
    releasePlayer();
    shouldAutoPlay = true;
    clearResumePosition();
    setIntent(intent);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      play(null);
    } else {
      showToast(R.string.storage_permission_denied);
      finish();
    }
  }

  // Activity input

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Show the controls on any key event.
    simpleExoPlayerView.showController();
    // If the event was not handled then see if the player view can handle it as a media key event.
    return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchMediaKeyEvent(event);
  }

  // OnClickListener methods

  @Override
  public void onClick(View view) {

      final String return_msg = sendMsgEditText.getText().toString();
      Log.e("텍스트내용",  ""+return_msg);
      chatdata.add(new LiveChat_Item(nick.getText().toString(),return_msg));
      Log.e("온클릭 채팅어레이 내용",  ""+chatdata);
      //리사이클러뷰 최하단으로
      RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
      liveChatAdapter.notifyDataSetChanged();
      if (!TextUtils.isEmpty(return_msg)) {
        new SendmsgTask().execute(return_msg);
      }

  }

  // PlaybackControlView.VisibilityListener implementation

  @Override
  public void onVisibilityChange(int visibility) {
    Log.e("뷰가 바뀔때",  "표시됨.");

  }

  // Internal methods

  private void initializePlayer(String rtmpUrl) {
    Intent intent = getIntent();
    boolean needNewPlayer = player == null;
    if (needNewPlayer) {

      boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
      @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
              useExtensionRenderers()
                      ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                      : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
      TrackSelection.Factory videoTrackSelectionFactory =
              new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
      player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
              null, extensionRendererMode);
      player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
              new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
              null, extensionRendererMode);
      player.addListener(this);

      eventLogger = new EventLogger(trackSelector);
      player.addListener(eventLogger);
      player.setAudioDebugListener(eventLogger);
      player.setVideoDebugListener(eventLogger);
      player.setMetadataOutput(eventLogger);
      Log.e("eventLogger값",""+eventLogger);
      simpleExoPlayerView.setPlayer(player);
      player.setPlayWhenReady(shouldAutoPlay);

    }

    if (needNewPlayer || needRetrySource) {
      //  String action = intent.getAction();
      Uri[] uris;
      String[] extensions;

      uris = new Uri[1];
      uris[0] = Uri.parse(rtmpUrl);
      extensions = new String[1];
      extensions[0] = "";
      if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
        // The player will be reinitialized if the permission is granted.
        return;
      }
      MediaSource[] mediaSources = new MediaSource[uris.length];
      for (int i = 0; i < uris.length; i++) {
        mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
      }
      MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
              : new ConcatenatingMediaSource(mediaSources);
      boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(resumeWindow, resumePosition);
      }
      player.prepare(mediaSource, !haveResumePosition, false);
      needRetrySource = false;
    }
  }

  private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
    int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
            : Util.inferContentType("." + overrideExtension);
    switch (type) {
      case C.TYPE_SS:
        return new SsMediaSource(uri, buildDataSourceFactory(false),
                new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_DASH:
        return new DashMediaSource(uri, buildDataSourceFactory(false),
                new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
      case C.TYPE_HLS:
        return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
      case C.TYPE_OTHER:
        if (uri.getScheme().equals("rtmp")) {
          return new ExtractorMediaSource(uri, rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV(),
                  mainHandler, eventLogger);
        }
        else {
          return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                  mainHandler, eventLogger);
        }
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }


  private void releasePlayer() {
    if (player != null) {
     // debugViewHelper.stop();
      shouldAutoPlay = player.getPlayWhenReady();
      updateResumePosition();
      player.release();
      player = null;
      trackSelector = null;
      //trackSelectionHelper = null;
      eventLogger = null;
    }

  }

  private void updateResumePosition() {
    resumeWindow = player.getCurrentWindowIndex();
    resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
            : C.TIME_UNSET;
  }

  private void clearResumePosition() {
    resumeWindow = C.INDEX_UNSET;
    resumePosition = C.TIME_UNSET;
  }

  /**
   * Returns a new DataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new DataSource factory.
   */
  private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
    return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  /**
   * Returns a new HttpDataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new HttpDataSource factory.
   */
  private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
    return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  // ExoPlayer.EventListener implementation

  @Override
  public void onLoadingChanged(boolean isLoading) {
    // Do nothing.
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {

    }
  }

  @Override
  public void onPositionDiscontinuity() {
    if (needRetrySource) {
      // This will only occur if the user has performed a seek whilst in the error state. Update the
      // resume position so that if the user then retries, playback will resume from the position to
      // which they seeked.
      updateResumePosition();
    }
  }

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest) {
    // Do nothing.
  }

  @Override
  public void onPlayerError(ExoPlaybackException e) {
    //videoStartControlLayout.setVisibility(View.VISIBLE);
    String errorString = null;
    if (e.type == ExoPlaybackException.TYPE_RENDERER) {
      Exception cause = e.getRendererException();
      if (cause instanceof DecoderInitializationException) {
        // Special case for decoder initialization failures.
        DecoderInitializationException decoderInitializationException =
                (DecoderInitializationException) cause;
        if (decoderInitializationException.decoderName == null) {
          if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
            errorString = getString(R.string.error_querying_decoders);
          } else if (decoderInitializationException.secureDecoderRequired) {
            errorString = getString(R.string.error_no_secure_decoder,
                    decoderInitializationException.mimeType);
          } else {
            errorString = getString(R.string.error_no_decoder,
                    decoderInitializationException.mimeType);
          }

        } else {
          errorString = getString(R.string.error_instantiating_decoder,
                  decoderInitializationException.decoderName);
        }
      }
    }
    if (errorString != null) {
      showToast(errorString);
    }
    needRetrySource = true;
    if (isBehindLiveWindow(e)) {
      clearResumePosition();
      play(null);
    } else {
      updateResumePosition();
    }
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
    if (mappedTrackInfo != null) {
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_video);
      }
      if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
        showToast(R.string.error_unsupported_audio);
      }
    }
  }


  private void showToast(int messageId) {
    showToast(getString(messageId));
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }

  private static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }


  public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultDataSourceFactory(this, bandwidthMeter,
            buildHttpDataSourceFactory(bandwidthMeter));
  }

  public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
    return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
  }

  public boolean useExtensionRenderers() {
    return BuildConfig.FLAVOR.equals("withExtensions");
  }


  public void play(View view) {

    //비동기식 데이터 받아오는 부분 이부분에서 mpeg-dash로 데이터를 받아오면 된다.
/*    String URL = RTMP_BASE_URL + videoNameEditText.getText().toString();
    //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";*/
   // String URL = "http://54.180.119.92/LiveApp/streams/"+videoNameEditText.getText().toString()+".m3u8";
   // String URL = "http://54.180.119.92:8080/hls/"+videoNameEditText.getText().toString()+".m3u8";

    //dash방식으로 가져온다, 이게 표준이기때문에
    String URL = "http://내주소/str/"+Playerroomname+".mpd";
    Log.e("방이랑",Playerroomname);
    initializePlayer(URL);

    //플레이 버튼 곤.
    //videoStartControlLayout.setVisibility(View.GONE);

  }





  //네티 관련 클래스
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
        Log.e("receive", "player_msg :" + receivedata);
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


  //채팅업데이트 받는부분.
  private Runnable showUpdate = new Runnable() {

    public void run() {
      String receive = "Coming word : " + receivedata+"\n";
      StringTokenizer token = new StringTokenizer(receivedata,":");
      String username = token.nextToken();
      String chat = token.nextToken();
      chatdata.add(new LiveChat_Item(username, chat));
      Log.e("플레어이채팅업데이트",""+receivedata);
      //리사이클러뷰 최하단으로
      RecyclerView.scrollToPosition(liveChatAdapter.getItemCount()-1);
      liveChatAdapter.notifyDataSetChanged();
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
