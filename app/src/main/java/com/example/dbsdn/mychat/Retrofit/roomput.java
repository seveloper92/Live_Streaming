package com.example.dbsdn.mychat.Retrofit;
import java.util.HashMap;

public class roomput {

    public final String name;
    public final String broadcaster;
    public final String title;
    public final int player;
    public final String png;

    //데이터 보내는 메소드
    public roomput(HashMap<String, Object> parameters) {

        this.name = (String) parameters.get("name");
        this.title = (String) parameters.get("title");
        this.broadcaster = (String) parameters.get("broadcaster");
        this.player = (int) parameters.get("player");
        this.png = (String) parameters.get("png");
    }


}
