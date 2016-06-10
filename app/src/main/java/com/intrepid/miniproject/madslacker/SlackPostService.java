package com.intrepid.miniproject.madslacker;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by malabika on 6/8/16.
 */
public class SlackPostService extends IntentService {

    String slack_URL = "https://hooks.slack.com/services/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";

    public SlackPostService(){
        super("SlackPostService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SlackPostService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Connect to Square - Post Message and Stop service after
        SlackPostModel slackPostModel = new SlackPostModel();
        String helloMessage = "{\"username\":\"mdas\",\"text\":\"I'm Here\"}";
        String jsonMessage = slackPostModel.setPostMessage(helloMessage);

        try {
            String responseMessage = slackPostModel.post(slack_URL,jsonMessage);
            Log.e("SlackPS", responseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


