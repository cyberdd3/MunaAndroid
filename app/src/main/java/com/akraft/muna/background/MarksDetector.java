package com.akraft.muna.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.akraft.muna.R;
import com.akraft.muna.activities.MainActivity;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MarksDetector extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            ServiceManager.getInstance().service.getMarksNearby(mLastLocation.getLatitude(), mLastLocation.getLongitude(), null, new Callback<ArrayList<Mark>>() {
                @Override
                public void success(ArrayList<Mark> marks, Response response) {
                    if (marks.size() > 0)
                        createNotification(marks.size());
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
        mGoogleApiClient.disconnect();
    }

    private void createNotification(int count) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_marks_nearby)
                        .setContentTitle("There are " + count + " uncollected marks nearby")
                        .setContentText("Go check them out!")
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                        .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mNotificationManager.notify(1, mBuilder.build());

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
