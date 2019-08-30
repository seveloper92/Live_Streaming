package com.example.dbsdn.mychat.Retrofit;
import java.util.HashMap;

public class roomdel {

    public final String name;

    //데이터 보내는 메소드
    public roomdel(HashMap<String, Object> parameters) {
        this.name = (String) parameters.get("name");
    }


}
