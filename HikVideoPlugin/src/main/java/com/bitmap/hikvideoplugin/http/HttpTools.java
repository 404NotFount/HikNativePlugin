package com.bitmap.hikvideoplugin.http;


import android.util.Log;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Create By axd On 2020/10/29.
 * Describe：只限购买者使用，未经授权未经授权私自传播作者有权追究其责任
 * @author axd
 */
public class HttpTools {
    /**
     * get 同步Get同求
     *
     * @param url url
     * @return
     */
    public String syncGet(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * get 异步Get同求
     *
     * @param url url
     * @return
     */
    public void nonSyncGet(String url, Callback responseCallback) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(responseCallback);
    }

    /**
     * 原始的get请求
     *
     * @author axd
     *
     */
    public static void okHttpGet(String Url) {
        //1.okhttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient.Builder().
        //在这里，还可以设置数据缓存等
         //设置超时时间
                connectTimeout(15, TimeUnit.SECONDS).
                readTimeout(20, TimeUnit.SECONDS).
                writeTimeout(20,  TimeUnit.SECONDS).
                //错误重连
                retryOnConnectionFailure(true).
                build();

            //2构造Request,
            //builder.get()代表的是get请求，url方法里面放的参数是一个网络地址
            Request.Builder builder = new Request.Builder();

            Request request = builder.get().url(Url).build();

            //3将Request封装成call
            Call call = okHttpClient.newCall(request);

            //4，执行call，这个方法是异步请求数据
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call arg0, IOException e) {
                    //失败调用
                    Log.d("TAG", "onFailure: " + e.getMessage());
                }

                @Override
                //由于OkHttp在解析response的时候依靠的是response头信息当中的Content-Type字段来判断解码方式
                //OkHttp会使用默认的UTF-8编码方式来解码
                //这里使用的是异步加载，如果需要使用控件，则在主线程中调用
                public void onResponse(Call arg0, Response response) throws IOException {

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //如果有更新UI的操作，需要自己写runOnUiThread这一类的方法去执行
//                        }
//                    });
                    //成功调用
                    Log.d("TAG", response.protocol() + " " +response.code() + " " + response.message());
                    Headers headers = response.headers();
                    for (int i = 0; i < headers.size(); i++) {
                        Log.d("TAG", headers.name(i) + ":" + headers.value(i));
                    }
                    Log.e("TAG", "onResponse: " + response.body().string());

                }
            });

        }


    public static void okHttpPost(String path, String cameraIndexCode, Integer action, String command){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(RxUtils.createSSLSocketFactory())
                .hostnameVerifier(new RxUtils.TrustAllHostnameVerifier())
                .build();

        RequestBody requestBody = new FormBody.Builder()
                .add("cameraIndexCode", cameraIndexCode)
                .add("action", String.valueOf(action))
                .add("command", command)
                .build();
        Request request = new Request.Builder()
                .url(path)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG", response.protocol() + " " +response.code() + " " + response.message());
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d("TAG", headers.name(i) + ":" + headers.value(i));
                }
                Log.e("TAG", "onResponse: " + response.body().string());
            }
        });
    }



    /**
     * 原始的get请求
     *
     * @author axd
     *
     */
    public static void okHttpGet(String Url, HttpBackListener httpBackListener) {
        //1.okhttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient.Builder().
                //在这里，还可以设置数据缓存等
                //设置超时时间
                        connectTimeout(15, TimeUnit.SECONDS).
                        readTimeout(20, TimeUnit.SECONDS).
                        writeTimeout(20,  TimeUnit.SECONDS).
                //错误重连
                        retryOnConnectionFailure(true).
                        build();

        //2构造Request,
        //builder.get()代表的是get请求，url方法里面放的参数是一个网络地址
        Request.Builder builder = new Request.Builder();

        Request request = builder.get().url(Url).build();

        //3将Request封装成call
        Call call = okHttpClient.newCall(request);

        //4，执行call，这个方法是异步请求数据
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call arg0, IOException e) {
                //这里的失败指的是没有网络请求发送不出去，或者请求地址有误找不到服务器这类情况
                //如果服务器返回的是404错误也说明请求到服务器了，属于请求成功的情况，要在下面的方法中处理
                Log.e("TAG", "onFailure: " + e.getMessage());
                httpBackListener.onError("网络异常:"+e.getMessage(),10000);
            }

            @Override
            //由于OkHttp在解析response的时候依靠的是response头信息当中的Content-Type字段来判断解码方式
            //OkHttp会使用默认的UTF-8编码方式来解码
            //这里使用的是异步加载，如果需要使用控件，则在主线程中调用
            public void onResponse(Call arg0, Response response) throws IOException {
                //请求成功以后的操作在这个方法里执行，并且这是个子线程，不能做更新界面的操作

                Log.d("TAG", response.protocol() + " " +response.code() + " " + response.message());
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d("TAG", headers.name(i) + ":" + headers.value(i));
                }
                String responseJson =  response.body().string();
                Log.e("TAG", "onResponse: " + responseJson);
                httpBackListener.onSuccess(responseJson,response.code());
            }
        });

    }

    public interface  HttpBackListener{
        void onSuccess(String json, int code);
        void onError(String error, int code);
    }
}
