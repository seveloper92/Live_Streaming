package com.example.dbsdn.mychat.Retrofit;



import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetroBaseApiService {

     String Base_URL = "http://내주소";

//    @GET("/posts/{userId}")
//    Call<ResponseGet> getFirst(@Path("userId") String id);
//
//    @GET("/posts")
//    Call<List<ResponseGet>> getSecond(@Query("userId") String id);

    //회원가입 포스트
    @FormUrlEncoded
    @POST("/member.php")
    Call<ResponseGet> postFirst(@FieldMap HashMap<String, Object> parameters);


    //아이디 체크 포스트
    @FormUrlEncoded
    @POST("/sign_ck.php")
    Call<ResponseGet> postSecond(@FieldMap HashMap<String, Object> parameters);


    //로그인 포스트
    @FormUrlEncoded
    @POST("/sign_in.php")
    Call<ResponseGet> postThird(@FieldMap HashMap<String, Object> parameters);

    //방송정보 받는 포스트
    @FormUrlEncoded
    @POST("/outputredis.php")
    Call<roomget> outputpostBroadcast(@FieldMap HashMap<String, Object> parameters);

    //방송정보 보내는 포스트
    @FormUrlEncoded
    @POST("inputredis.php")
    Call<roomget> inputpostBroadcast(@FieldMap HashMap<String, Object> parameters);

    //방송정보 삭제하는 포스트
    @FormUrlEncoded
    @POST("delredis.php")
    Call<roomdel> delpostBroadcast(@FieldMap HashMap<String, Object> parameters);


//    @PUT("/posts/1")
//    Call<ResponseGet> putFirst(@Body RequestPut parameters);
//
//    @FormUrlEncoded
//    @PATCH("/posts/1")
//    Call<ResponseGet> patchFirst(@Field("title") String title);
//
//    @DELETE("/posts/1")
//    Call<ResponseBody> deleteFirst();
}
