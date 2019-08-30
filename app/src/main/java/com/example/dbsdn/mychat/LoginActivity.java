package com.example.dbsdn.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dbsdn.mychat.Retrofit.ResponseGet;
import com.example.dbsdn.mychat.Retrofit.RetroCallback;
import com.example.dbsdn.mychat.Retrofit.RetroClient;
import com.example.dbsdn.mychat.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    RecyclerView recyclerview;
    public Context context;
    //데이터 바인딩
    //이 클래스가 로그인클래스라 로그인 바인딩이며 다른 클래스는 바인딩이름이 달라짐.
    ActivityLoginBinding activityLoginBinding;

    //구글 클라이어트 리절트 값. 동일하면 출력
    GoogleApiClient mGoogleApiClient;
    int RC_SIGN_IN = 1000;

    //카카오 로그인 오브젝트 생성
    SessionCallback callback;
//    RecyclerView recyclerview;
    private LoginButton btn_kakao_login;

    //아이디 패스워드 담을 변수
    String ID;
    String PASSWORD;

    //레트로핏
    RetroClient retroClient;

    //여기에 셰어드 들어있나 확인하고 넘어가기
    @Override
    protected void onStart() {
        super.onStart();
        //추후 로그아웃하면 셰어드로 삭제하게 해야함. 2018 11 09 주석
//        pref = context.getSharedPreferences ("pref",Context.MODE_PRIVATE);
//        String cookie = pref.getString ("Cookie","쿠키 없어");
//
//        if (cookie!=null&&cookie!=""){
//            Intent intent = new Intent (this, MainActivity.class);
//            intent.putExtra("name", pref.getString ("user_id","아이디 없어"));
//            startActivity (intent);
//        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);


        //카카오톡 콜백클래스 초기화.
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        //이렇게 소환해주면 사용할 때 모든 버스턴을 그냥 setOnClickLisner처럼 바로 불러와 사용 할 수 있다.
        activityLoginBinding = DataBindingUtil.setContentView (this,R.layout.activity_login);
        activityLoginBinding.setActivity (this);


        //레트로핏 초기화
        retroClient = RetroClient.getInstance(this).createBaseApi();
//        //액션바 감추기
//        ActionBar actionBar = getSupportActionBar ();
//        actionBar.hide ();


        // 구글 사용자의 ID, 전자 메일 주소 및 기본을 요청하도록 로그인을 구성.
        // 프로필. ID 및 기본 프로필은 DEFAULT_SIGN_IN에 포함됩니다.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId ()
                .requestProfile ()
                .requestEmail()
                .build();


        // Google 로그인 API에 액세스 할 수있는 GoogleApiClient를 빌드하고
        // gso에 의해 지정된 옵션.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,  this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = findViewById(R.id.gg_login_buttun);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        //이부분을 리사이클러뷰에 넣어보자.
        findViewById(R.id.gg_login_buttun).setOnClickListener(this);


        //키보드 위로
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //카카오 로그인 버튼
        btn_kakao_login = findViewById(R.id.btn_kakao_login);
        btn_kakao_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session session = Session.getCurrentSession();
                session.addCallback(new SessionCallback ());
                session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
                Log.e("클릭눌렀을때","");
            }
        });




        //익스펜더블 리사이클러뷰
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<Login_ExpandableListAdapter.Item> data = new ArrayList<> ();

        Login_ExpandableListAdapter.Item places = new Login_ExpandableListAdapter.Item(Login_ExpandableListAdapter.HEADER, "SNS로그인");
        places.invisibleChildren = new ArrayList<>();
        places.invisibleChildren.add(new Login_ExpandableListAdapter.Item(Login_ExpandableListAdapter.CHILD, "구글로그인"));
        places.invisibleChildren.add(new Login_ExpandableListAdapter.Item(Login_ExpandableListAdapter.CHILD, "카카오로그인"));
        data.add(places);
        recyclerview.setAdapter(new Login_ExpandableListAdapter(data));



        //일반 로그인 버튼 인텐트
        activityLoginBinding.btnLogin.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //데이터 삽입
                ID = activityLoginBinding.inputEmail.getText().toString();
                PASSWORD = activityLoginBinding.inputPassword.getText().toString();

                if (ID.equals (null)&&PASSWORD.equals (null)){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                }
                else if (ID.equals ("")&&PASSWORD.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                }
                else if (PASSWORD.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                }
                else if (ID.equals ("")){
                    Toast.makeText(getApplicationContext(),"회원정보가 비었습니다.",Toast.LENGTH_LONG).show();
                }
                //데이터 전송.
                HashMap<String, Object> parameters = new HashMap<>();
                parameters.put("id", ID);
                parameters.put("pw", PASSWORD);
                Log.e("로그인 파라메터값", String.valueOf(parameters));
                retroClient.postThird (parameters, new RetroCallback() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e("onError도착, 전송은 했지만 회신안댐.", t.toString());
                        //activitySignBinding.textView.setText("Error");

                    }
                    @Override
                    public void onSuccess(int code, Object receivedData) {
                        Log.e("onSuccess", "모든연결이순조로움");
                        ResponseGet data = (ResponseGet) receivedData;
                        //회원이 아니면 1
                        if(data.pw.equals ("1")){
                            Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();
                        }
                        //회원이면 2
                        else if (data.pw.equals ("2")){
                            //유저가 맞으면 유저아이디를 쉐어드에 넣어준다. 여기서 넣어줌과 동시에
                            //레트로핏으로 쿠키값도 가져와서 바로 저장됨
//                            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = pref.edit();
//                            editor.putString ("user_id", ID);
//                            editor.apply();
                            Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();
                            Intent intent= new Intent(LoginActivity.this,Navigation.class);
                            intent.putExtra("name",ID);
                            startActivity(intent);

                        }

                        Toast.makeText(getApplicationContext(),data.email,Toast.LENGTH_LONG).show();


                        Log.e ("받아오는값",""+data.id+" "+data.pw+""+data.email);
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.e("onFailure", "전송도 회신도 못받은상태");


                    }
                });
                //자동로그인 설정 체크

//                //자동로그인이 체크 되었을 때(셰어드 저장해주고)
//                if (activityLoginBinding.AutoLogin.isChecked ()==true){
//                    new ReceivedCookiesInterceptor ();
//                }
//                //자동로그인이 체크 되있지 않을때(저장은 하지 않는다.)
//                else if (activityLoginBinding.AutoLogin.isChecked ()==false){
//
//                }

            }
        });



        //회원가입 텍스트뷰 인텐트
        TextView link_signup = findViewById (R.id.link_signup);
        link_signup.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(LoginActivity.this,SignActivity.class);
                startActivity(intent);
            }
        });

        //화면상에 구글버튼 지우기 여기서 값만있고 지원다음 익스펜더블에 넣어주는것.
        //여기넣으면 코드가 지저분해져서 xml에 넣었음. 나중에 다른게 필요할때 사용하자.
//        signInButton.setVisibility(View.GONE);
//        authLoginButton.setVisibility (View.GONE);
//        btn_kakao_login.setVisibility (View.GONE);

    }

    //구글 로그인 부분.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), ""+connectionResult, Toast.LENGTH_SHORT).show();

    }
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.gg_login_buttun:
                signIn();
                break;
        }

    }

    //구글 액티비티 리졀트 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null){
                if (result.isSuccess()) {

                // 로그인 성공 했을때 값 가져오는 부분.
                    GoogleSignInAccount acct = result.getSignInAccount();

                    String personName = acct.getDisplayName();
                    String personEmail = acct.getEmail();
                    String personId = acct.getId();
                    String tokenKey = acct.getServerAuthCode();

                    mGoogleApiClient.disconnect();

                // 로그나온다.
                    Log.e("GoogleLogin", "personName=" + personName);
                    Log.e("GoogleLogin", "personEmail=" + personEmail);
                    Log.e("GoogleLogin", "personId=" + personId);
                    Log.e("GoogleLogin", "tokenKey=" + tokenKey);

            }else {
                GoogleSignInAccount account = result.getSignInAccount ();
            }
        }
    }
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public class SessionCallback implements ISessionCallback {

        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            requestMe();
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void requestMe() {
            // 사용자정보 요청 결과에 대한 Callback
            UserManagement.getInstance().requestMe(new MeResponseCallback() {
                // 세션 오픈 실패. 세션이 삭제된 경우,
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.e("SessionCallback :: ", "onSessionClosed : " + errorResult.getErrorMessage());
                }

                // 회원이 아닌 경우,
                @Override
                public void onNotSignedUp() {
                    Log.e("SessionCallback :: ", "onNotSignedUp");
                }

                // 사용자정보 요청에 성공한 경우,
                @Override
                public void onSuccess(UserProfile userProfile) {

                    Log.e("SessionCallback :: ", "onSuccess");

                    String nickname = userProfile.getNickname();
                    String email = userProfile.getEmail();
                    String profileImagePath = userProfile.getProfileImagePath();
                    String thumnailPath = userProfile.getThumbnailImagePath();
                    String UUID = userProfile.getUUID();

                    long id = userProfile.getId();

                    Intent kakaointent = new Intent (LoginActivity.this, Navigation.class);
                    kakaointent.putExtra("name",nickname);
                    kakaointent.putExtra("email",email);
                    kakaointent.putExtra("profileImagePath",profileImagePath);
                    startActivity (kakaointent);


                    Log.e("Profile : ", nickname + "");
                    Log.e("Profile : ", email + "");
                    Log.e("Profile : ", profileImagePath  + "");
                    Log.e("Profile : ", thumnailPath + "");
                    Log.e("Profile : ", UUID + "");
                    Log.e("Profile : ", id + "");
                }



                // 사용자 정보 요청 실패

                @Override

                public void onFailure(ErrorResult errorResult) {

                    Log.e("SessionCallback :: ", "onFailure : " + errorResult.getErrorMessage());

                }

            });

        }

    }

    //세션 연결 성공 시 SignupActivity로 넘김
    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, Navigation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    //카카오 앱꺼지면 세션도 종료되게 만듬.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }







}
