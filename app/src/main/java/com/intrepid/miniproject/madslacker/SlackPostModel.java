package com.intrepid.miniproject.madslacker;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by malabika on 6/8/16.
 */

public class SlackPostModel{

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    String postMessage;

    OkHttpClient okHttpClient = new OkHttpClient();

    String post(String url, String jsonStr) throws IOException {
        RequestBody requestBody = RequestBody.create(JSON,jsonStr);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            return response.body().string();
        }
    }

    String setPostMessage(String message){
        postMessage = message;
        return postMessage;
    }
}

