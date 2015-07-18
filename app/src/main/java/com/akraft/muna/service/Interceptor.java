package com.akraft.muna.service;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;

public class Interceptor extends OkClient{
    private String token;
    private Context context;

    public Interceptor() {
    }

    @Override
    public Response execute(Request request) throws IOException {
        String method = request.getMethod();
        TypedOutput body = request.getBody();
        String url = request.getUrl();

        List<Header> headers = new ArrayList<>();
        headers.addAll(request.getHeaders());

        if (token == null) {
            Log.e("dbg", "no token provided: a request goes without jwt");
        } else {
            headers.add(new Header("Authorization", "JWT " + token));
        }

        Request req = new Request(method, url, headers, body);
        return super.execute(req);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean hasToken() {
        return token != null;
    }
}
