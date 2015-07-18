package com.akraft.muna.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.MainActivity;
import com.akraft.muna.activities.TeamRequestsActivity;
import com.akraft.muna.codes.NotificationsCodes;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class NotificationsService extends Service {

    private Socket mSocket;
    private long id;

    {
        try {
            mSocket = IO.socket(Config.SERVER_URL + ":3000");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id = getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
        if (!mSocket.connected()) {
            mSocket.connect();
            mSocket.on("notifications" + id, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length < 2)
                        return;
                    switch ((int) args[0]) {
                        case NotificationsCodes.NOTIFICATION_NEW_REQUEST:
                            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("team_requests_notifications", true))
                                showNotificationNewRequest((int) args[1]);

                            break;
                    }

                }
            });
        }
        return START_STICKY;
    }

    private void showNotificationNewRequest(int count) {

        NotificationCompat.Builder mBuilder = createNotification(R.drawable.ic_notif_new_request, getString(R.string.incoming_request_notification), "You have " + count + " team requests");

        PendingIntent pendingIntent = TaskStackBuilder.create(this).addNextIntent(new Intent(this, MainActivity.class)).addNextIntent(new Intent(this, TeamRequestsActivity.class)).getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify("incoming_requests", 1, mBuilder.build());
    }

    private NotificationCompat.Builder createNotification(int drawable, String title, String text) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
