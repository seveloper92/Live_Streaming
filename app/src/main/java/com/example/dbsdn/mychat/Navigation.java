package com.example.dbsdn.mychat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dbsdn.mychat.Openchat.OpenChatList;

public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Rtmp wnth
    public static final String RTMP_BASE_URL = "rtmp://내주소/show/";
    //네티 채팅 주소.
    public static final String NETTY = "네티서버주소";
    public static String nickname;
    public static String kakaoname;
    Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //컨택스트
        mContext = getApplicationContext();
        //카카오 정보 받기
        Intent kakaointent = getIntent();
        String kakaoprofile = kakaointent.getStringExtra("profileImagePath");
        String kakaoemail = kakaointent.getStringExtra("email");
        kakaoname = kakaointent.getStringExtra("name");

        Log.e("카카오 정보들",""+kakaoprofile+kakaoprofile+kakaoemail+kakaoname);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);
        //생성
        ImageView headerimageView  = (ImageView) nav_header_view.findViewById(R.id.headerimageView);
        TextView headeremail = (TextView) nav_header_view.findViewById(R.id.headeremail);
        TextView headername = (TextView) nav_header_view.findViewById(R.id.headername);
        //카카오톡 이미지뷰 동그랗게
        headerimageView.setBackground(new ShapeDrawable(new OvalShape()));
        headerimageView.setClipToOutline(true);
        Glide.with(mContext)
                .load(kakaoprofile)
                .override(250,250)
                .centerCrop()
                .into(headerimageView);
        headeremail.setText(kakaoemail);
        headername.setText(kakaoname);
        Log.e("컨택스트",""+mContext);
        //idtext.setText(name);

        //플로팅 버튼 머에쓸지 고민해보자.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "방송을 시작해 보세요!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //프레그먼트
        //TabLayout
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("LIVE"));
        tabs.addTab(tabs.newTab().setText("OPEN_CHATING"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);

        //어답터설정
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), 2);
        viewPager.setAdapter(myPagerAdapter);

        //탭메뉴를 클릭하면 해당 프래그먼트로 변경-싱크화
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_openchat) {
            //오픈채팅넘어가기
            Intent intent= new Intent (Navigation.this, Fragment_two.class);
            startActivity(intent);

        } else if (id == R.id.nav_moviechat) {
            //LIVE방송
            Intent intent= new Intent (Navigation.this, Fragment_one.class);
            startActivity(intent);
        } else if (id == R.id.nav_game) {
            //게임
            Intent intent= new Intent (Navigation.this,UnityPlayerActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
