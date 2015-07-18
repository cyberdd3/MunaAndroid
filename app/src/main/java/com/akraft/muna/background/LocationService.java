package com.akraft.muna.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;

public class LocationService extends Service {

    private static Timer timer = new Timer();

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
