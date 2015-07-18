package com.akraft.muna.service;

import android.content.Context;

import com.akraft.muna.Config;
import com.akraft.muna.Utils;
import com.google.gson.Gson;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class ServiceManager {
    private static ServiceManager INSTANCE;

    public MainService service;

    private String END_POINT = Config.SERVER_URL + ":8000/api";
    private Interceptor client;

    public static ServiceManager getInstance() {

        return INSTANCE == null ? INSTANCE = new ServiceManager() : INSTANCE;
    }

    public ServiceManager() {
        client = new Interceptor();

        Gson gson = Utils.createGson();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(END_POINT)
                .setClient(client)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
        service = restAdapter.create(MainService.class);
    }

    public void setToken(String token) {
        client.setToken(token);
    }

    public void loadToken(Context context) {
        if (!client.hasToken())
            client.setToken(context.getSharedPreferences(Utils.AUTH_PREF, 0).getString("token", ""));
    }


}
