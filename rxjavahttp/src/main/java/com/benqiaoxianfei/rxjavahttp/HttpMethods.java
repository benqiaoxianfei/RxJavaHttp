package com.benqiaoxianfei.rxjavahttp;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HttpMethods {

    private static final String TAG = HttpMethods.class.getSimpleName();
    private Retrofit retrofit;
    private String mUrl = null;
    private long httpTimeOut = 10L;
    private static volatile HttpMethods httpMethods;

    /**
     * url 不能为空否则会抛空指针异常
     *
     * @param url
     * @param timeOut
     */
    private HttpMethods(String url, long timeOut) {
        if (url == null && url.equals("")) {
            throw new NullPointerException("url is not null");
        }
        if (timeOut != 0) {
            this.httpTimeOut = timeOut;
        }
        this.mUrl = url;
        retrofit = new Retrofit.Builder()
                .baseUrl(mUrl)
                .client(getOkHttpClient(httpTimeOut))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    /**
     * 双重验证单利模式
     *
     * @param url     请求地址
     * @param timeOut 请求超时时间
     * @return
     */
    public static HttpMethods getInstance(String url, long timeOut) {
        if (httpMethods == null) {
            synchronized (HttpMethods.class) {
                if (httpMethods == null) {
                    httpMethods = new HttpMethods(url, timeOut);
                }
            }
        }
        return httpMethods;
    }

    /**
     * 不同的请求 不同的超时时间
     *
     * @param url     请求地址
     * @param timeOut 请求超时时间
     * @return
     */
    public static HttpMethods getHttpMethods(String url, long timeOut) {
        httpMethods = new HttpMethods(url, timeOut);
        return httpMethods;
    }

    /**
     * 拦截器
     *
     * @param timeOut
     * @return
     */
    private OkHttpClient getOkHttpClient(long timeOut) {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e(TAG, "OkHttp====Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        //定制OkHttp
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient
                .Builder()
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .writeTimeout(timeOut, TimeUnit.SECONDS);
        //OkHttp进行添加拦截器loggingInterceptor
        httpClientBuilder.addInterceptor(loggingInterceptor);
        return httpClientBuilder.build();
    }

    public <T> T createService(Class<T> clazz) {
        return retrofit.create(clazz);
    }

}
