package com.example.dbsdn.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IntroActivity extends AppCompatActivity {
    Context context;
    //ImageView img1, img2, img3, img4, img5, img6, img7, img8, img9, img10, img11, img12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_intro);

//        //액션바 감추기

//        ActionBar actionBar =getSupportActionBar ();
//        actionBar.hide ();

        //상태바 없애기.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        //핸들러로 2초뒤 액티비티 꺼지기
        Handler intro_handler = new Handler ();
        intro_handler.postDelayed (new Runnable () {
            @Override
            public void run() {
                Intent intro_intent = new Intent (IntroActivity.this, LoginActivity.class);
                startActivity (intro_intent);
                //액티비티 전환 부드럽게 하기
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        },1000);
//
//        //애니메이션
//        img1 = (ImageView) findViewById (R.id.imageView1);
//        img2 = (ImageView) findViewById (R.id.imageView2);
//        img3 = (ImageView) findViewById (R.id.imageView3);
//        img4 = (ImageView) findViewById (R.id.imageView4);
//        img5 = (ImageView) findViewById (R.id.imageView5);
//        img6 = (ImageView) findViewById (R.id.imageView6);
//        img7 = (ImageView) findViewById (R.id.imageView7);
//        img8 = (ImageView) findViewById (R.id.imageView8);
//        img9 = (ImageView) findViewById (R.id.imageView9);
//        img10 = (ImageView) findViewById (R.id.imageView10);
//        img11 = (ImageView) findViewById (R.id.imageView11);
//        img12 = (ImageView) findViewById (R.id.imageView12);
//
//
//       // 간단하게 사용할거니 제거..
//        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.stardrop);
//        img1.startAnimation(animation);
//        img2.startAnimation(animation);
//        img3.startAnimation(animation);
//        img4.startAnimation(animation);
//        img5.startAnimation(animation);
//        img6.startAnimation(animation);
//        img7.startAnimation(animation);
//        img8.startAnimation(animation);
//        img9.startAnimation(animation);
//        img10.startAnimation(animation);
//        img11.startAnimation(animation);
//        img12.startAnimation(animation);


        //카카오 해쉬키 가져오기
        try {
            PackageInfo info = getPackageManager ().getPackageInfo ("com.example.dbsdn.mychat", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance ("SHA");
                md.update (signature.toByteArray ());
                Log.e ("KeyHash:", Base64.encodeToString (md.digest (), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }

    }

}
