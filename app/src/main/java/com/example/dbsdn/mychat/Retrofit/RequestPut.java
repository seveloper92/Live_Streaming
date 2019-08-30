package com.example.dbsdn.mychat.Retrofit;

import java.util.HashMap;

/**
 * Created by dbsdnjs on 2018. 10. 6..
 */


//레트로핏 데이터 넣기.
public class RequestPut {

    public final String id;
    public final String email;
    public final String pw;

    //레트로픽 받아오는 값.
    public RequestPut(HashMap<String, Object> parameters) {

        this.id = (String) parameters.get("id");
        this.email = (String) parameters.get("email");
        this.pw = (String) parameters.get("pw");
    }
}
