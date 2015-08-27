package com.hiqes.android.demopermissionsm.loader;

import com.hiqes.android.demopermissionsm.model.Message;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface EchoMessageEndpoint {
    @POST("/echo")
    Message echo(@Body Message outMessage);

    @POST("/echo")
    void echo(@Body Message outMessage, Callback<Message> cb);
}
