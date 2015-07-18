package com.akraft.muna.activities;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.akraft.muna.R;
import com.akraft.muna.adapters.MessagesAdapter;
import com.akraft.muna.background.ChatService;
import com.akraft.muna.models.Message;
import com.akraft.muna.models.MessagesPage;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChatActivity extends AppCompatActivity {
    private static final int MESSAGES_OFFSET_TO_LOAD_NEXT_PAGE = 2;
    private User user;
    private ChatService.IMessagesReceiver receiver;
    @InjectView(R.id.messages_list)
    RecyclerView messagesList;
    @InjectView(R.id.message_text)
    EditText messageText;
    @InjectView(R.id.send_message)
    Button sendMessageButton;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;


    private ArrayList<Message> messages = new ArrayList<>();
    private MessagesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private long userId;
    private int currentPage = 1;
    private boolean loadingMessages = true;
    private Integer nextPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        createNavigation();
        ServiceManager.getInstance().loadToken(this);
        user = (User) getIntent().getParcelableExtra("user");
        if (user == null) {
            userId = getIntent().getLongExtra("id", 0);
            ServiceManager.getInstance().service.user(null, null, userId, new Callback<User>() {
                @Override
                public void success(User result, Response response) {
                    user = result;
                    initializeChat();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            initializeChat();
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel("message", 1);
    }

    private void initializeChat() {
        createList();
        loadMessages(1);

        getSupportActionBar().setTitle("@" + user.getUsername());
    }

    private void loadMessages(int page) {
        loadingMessages = true;
        ServiceManager.getInstance().service.messages(user.getId(), page, new Callback<MessagesPage>() {

            @Override
            public void success(MessagesPage messagesPage, Response response) {
                nextPage = messagesPage.getNext();
                if (nextPage == null)
                    messagesList.removeOnScrollListener(onScrollListener);

                Collections.reverse(messagesPage.getResults());
                messages.addAll(0, messagesPage.getResults());
                adapter.notifyDataSetChanged();
                loadingMessages = false;
                //layoutManager.scrollToPosition(MESSAGES_OFFSET_TO_LOAD_NEXT_PAGE + currentPage * (int) messagesPage.getCount());
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void createNavigation() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void createList() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        messagesList.setVerticalScrollBarEnabled(true);
        messagesList.addOnScrollListener(onScrollListener);


        adapter = new MessagesAdapter(this, messages);
        messagesList.setAdapter(adapter);
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            if (!loadingMessages && layoutManager.findFirstCompletelyVisibleItemPosition() <= MESSAGES_OFFSET_TO_LOAD_NEXT_PAGE) {
                loadingMessages = true;
                loadMessages(nextPage);
            }
        }
    };

    @OnClick(R.id.send_message)
    public void sendMessage() {
        String text = messageText.getText().toString();
        if (text.length() == 0)
            return;

        sendMessageButton.setEnabled(false);
        Message message = new Message();
        message.setText(text);
        message.setRecipient(user.getId());
        service.sendMessage(message);


    }


    ChatService.IChatService service;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ChatService.IChatService) binder;
            service.setMessagesReceiver(messagesReceiver);
            enableChat();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    private void enableChat() {
        progressBar.setVisibility(View.GONE);
        sendMessageButton.setEnabled(true);
    }

    private void finishWithError() {
        Toast.makeText(this, "Sorry, no such user", Toast.LENGTH_SHORT).show();
        finish();
    }

    ChatService.IMessagesReceiver messagesReceiver = new ChatService.IMessagesReceiver() {
        @Override
        public void newMessage(final Message message) {
            message.save();
            //ORMManager.getInstance().save(getApplicationContext(), message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addMessage(message);
                    layoutManager.scrollToPosition(messages.size() - 1);
                }
            });
        }

        @Override
        public void messageSent(final Message message) {
            message.save();
            //ORMManager.getInstance().save(getApplicationContext(), message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageText.setText("");
                    adapter.addMessage(message);
                    sendMessageButton.setEnabled(true);
                    layoutManager.scrollToPosition(messages.size() - 1);
                }
            });
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (service != null)
            service.setMessagesReceiver(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(getApplicationContext(), ChatService.class), mConnection, Service.BIND_AUTO_CREATE);
        if (service != null)
            service.setMessagesReceiver(messagesReceiver);
    }
}
