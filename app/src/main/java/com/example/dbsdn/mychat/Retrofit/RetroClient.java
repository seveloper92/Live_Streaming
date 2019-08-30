package com.example.dbsdn.mychat.Retrofit;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//레트로핏 인스턴스 생성.
public class RetroClient {

    public RetroBaseApiService apiService;
    public static String baseUrl = RetroBaseApiService.Base_URL;
    public static Context mContext;
    public static Retrofit retrofit;
    Gson gson;

    //싱글톤으로.
    public static class SingletonHolder {
        public static RetroClient INSTANCE = new RetroClient(mContext);
    }

    public static RetroClient getInstance(Context context) {
        if (context != null) {
            mContext = context;
        }
        return SingletonHolder.INSTANCE;
    }

    public RetroClient(Context context) {


        gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                //클라이언트 http통신 로그를 찍어주는것이고 echo가 깔끔해야함. 디비 연결에 에코가 깔끔하지 못한 데이터를 전송해서 문제가 발생.
                .client (createOkHttpClient())
                .baseUrl(baseUrl)
                .build();
    }

    public RetroClient createBaseApi() {
        apiService = create(RetroBaseApiService.class);
        return this;
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public  <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

//
//    public void getFirst(String id, final RetroCallback callback) {
//        apiService.getFirst(id).enqueue(new Callback<ResponseGet>() {
//            @Override
//            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(response.code(), response.body());
//                } else {
//                    callback.onFailure(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseGet> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }
//
//    public void getSecond(String id, final RetroCallback callback) {
//        apiService.getSecond(id).enqueue(new Callback<List<ResponseGet>>() {
//            @Override
//            public void onResponse(Call<List<ResponseGet>> call, Response<List<ResponseGet>> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(response.code(), response.body());
//                } else {
//                    callback.onFailure(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<ResponseGet>> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }


    //회원데이터 보내는 포스트
    public void postFirst(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.postFirst(parameters).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                callback.onError(t);
            }
        });
    }


    //중복확인하는 포스트
    public void postSecond(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.postSecond (parameters).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    //로그인
    public void postThird(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.postThird (parameters).enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    //레트로핏 방송정보 전송 postBroadcast
    public void postinBroadcast(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.inputpostBroadcast (parameters).enqueue(new Callback<roomget>() {
            @Override
            public void onResponse(Call<roomget> call, Response<roomget> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<roomget> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    //레트로핏 방송정보 받아오기 postBroadcast
    public void postoutBroadcast(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.outputpostBroadcast (parameters).enqueue(new Callback<roomget>() {
            @Override
            public void onResponse(Call<roomget> call, Response<roomget> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<roomget> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    //레트로핏 방송정보 받아오기 postBroadcast
    public void delpostBroadcast(HashMap<String, Object> parameters, final RetroCallback callback) {
        apiService.delpostBroadcast (parameters).enqueue(new Callback<roomdel>() {
            @Override
            public void onResponse(Call<roomdel> call, Response<roomdel> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<roomdel> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
//    public void putFirst(HashMap<String, Object> parameters, final RetroCallback callback) {
//        apiService.putFirst(new RequestPut(parameters)).enqueue(new Callback<ResponseGet>() {
//            @Override
//            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(response.code(), response.body());
//                } else {
//                    callback.onFailure(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseGet> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }
//
//    public void patchFirst(String title, final RetroCallback callback) {
//        apiService.patchFirst(title).enqueue(new Callback<ResponseGet>() {
//            @Override
//            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(response.code(), response.body());
//                } else {
//                    callback.onFailure(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseGet> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }
//
//    public void deleteFirst(final RetroCallback callback) {
//        apiService.deleteFirst().enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    callback.onSuccess(response.code(), response.body());
//                } else {
//                    callback.onFailure(response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                callback.onError(t);
//            }
//        });
//    }

    /**  okhttp 로그를 남기기 위한 메소드 중요......  **/
    private static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
               OkHttpClient client = new OkHttpClient();


//        // 쿠키를 Prefreence에 저장하고 가져옴
//        client.interceptors().add(new AddCookiesInterceptor ());
//        client.interceptors().add(new ReceivedCookiesInterceptor ());



        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        return builder.build();
    }


}
