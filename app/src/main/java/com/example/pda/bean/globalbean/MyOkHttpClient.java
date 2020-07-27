package com.example.pda.bean.globalbean;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyOkHttpClient {
    private static Object object = new Object();
    private static OkHttpClient okHttpClient = null;

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (object) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                            .readTimeout(10, TimeUnit.SECONDS) //设置读取超时时间
                            .retryOnConnectionFailure(false)
                            .build();
                }
            }
        }
        return okHttpClient;
    }

}
