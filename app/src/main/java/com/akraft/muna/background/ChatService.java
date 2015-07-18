package com.akraft.muna.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.ChatActivity;
import com.akraft.muna.codes.MessagesCodes;
import com.akraft.muna.models.Message;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.net.URISyntaxException;

public class ChatService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private Socket mSocket;
    private Message lastMessage;
    private Gson gson;
    private long id;

    private Emitter.Listener messagesListener;

    {
        try {
            mSocket = IO.socket(Config.SERVER_URL + ":3000");
            gson = Utils.createGson();
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id = getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
        if (!mSocket.connected()) {
            mSocket.connect();
            messagesListener = createListener(null);
            mSocket.on("messages" + id, messagesListener);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Emitter.Listener createListener(final IMessagesReceiver receiver) {
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                switch (Integer.parseInt(args[0].toString())) {
                    case MessagesCodes.NEW_MESSAGE:
                        if (args.length < 2)
                            return;

                        Message message = gson.fromJson(args[1].toString(), Message.class);

                        if (receiver != null)
                            receiver.newMessage(message);
                        else if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("chat_notifications", true))
                            sendNotification(message);
                        break;
                    case MessagesCodes.MESSAGE_SENT:
                        if (receiver != null) {
                            receiver.messageSent(lastMessage);
                        }
                        break;
                    case MessagesCodes.ERROR_SENDING:

                }
            }
        };
    }

    public class LocalBinder extends Binder implements IChatService {

        ChatService getService() {
            return ChatService.this;
        }

        @Override
        public void setMessagesReceiver(IMessagesReceiver receiver) {
            if (messagesListener != null)
                mSocket.off("messages" + id, messagesListener);
            messagesListener = createListener(receiver);
            mSocket.on("messages" + id, messagesListener);
        }

        @Override
        public void sendMessage(Message message) {
            lastMessage = message;
            message.setAuthor(id);
            mSocket.emit("messages", gson.toJson(message));
        }
    }

    private void sendNotification(Message message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif_message)
                        .setContentTitle("New message")
                        .setContentText(message.getText())
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("id", message.getAuthor());
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, chatIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify("message", 1, mBuilder.build());
    }

    public interface IChatService {
        void setMessagesReceiver(IMessagesReceiver receiver);

        void sendMessage(Message message);
    }


    public interface IMessagesReceiver {
        void newMessage(Message message);

        void messageSent(Message message);
    }
}
