package com.example.dbsdn.mychat.Retrofit;

public class roomget {

    public final String name;
    public final String broadcaster;
    public final String title;
    public final int player;
    public final String png;


    //데이터 가져오는 메소드
    public roomget(String name, String title, String broadcaster, String png, int player) {
        this.name = name;
        this.title = title;
        this.broadcaster = broadcaster;
        this.player = player;
        this.png = png;
    }
}
