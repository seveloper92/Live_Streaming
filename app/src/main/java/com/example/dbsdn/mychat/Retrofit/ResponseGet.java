package com.example.dbsdn.mychat.Retrofit;


//레트로핏 데이터 얻기
public class ResponseGet {

    public final String id;
    public final String email;
    public final String pw;


    //데이터 가져오는 메소드
        public ResponseGet(String id, String email, String pw) {
        this.id = id;
        this.email = email;
        this.pw = pw;
    }


}
