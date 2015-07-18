package com.akraft.muna.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private long myId;
    private ArrayList<Message> messages;
    private Context context;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private int timezoneOffset;


    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        this.messages = messages;
        this.context = context;

        myId = context.getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
        offsetTimezones();
    }

    private void offsetTimezones() {
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        timezoneOffset = tz.getOffset(now.getTime());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_messages, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Message message = messages.get(i);
        viewHolder.messageMe.setVisibility(View.GONE);
        viewHolder.messagePerson.setVisibility(View.GONE);
        TextView messageText;
        TextView timeText;
        if (message.getAuthor() == myId) {
            messageText = viewHolder.messageTextMe;
            timeText = viewHolder.messageTimeMe;
            viewHolder.messageMe.setVisibility(View.VISIBLE);
        } else {
            messageText = viewHolder.messageTextPerson;
            timeText = viewHolder.messageTimePerson;
            viewHolder.messagePerson.setVisibility(View.VISIBLE);
        }
        messageText.setText(message.getText());
        timeText.setText(dateFormat.format(new Date(message.getTimestamp().getTime() + timezoneOffset)));

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.message_text_person)
        TextView messageTextPerson;
        @InjectView(R.id.message_text_me)
        TextView messageTextMe;
        @InjectView(R.id.message_time_me)
        TextView messageTimeMe;
        @InjectView(R.id.message_time_person)
        TextView messageTimePerson;

        @InjectView(R.id.message_me)
        LinearLayout messageMe;
        @InjectView(R.id.message_person)
        LinearLayout messagePerson;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setTimestamp(new Date(System.currentTimeMillis() - timezoneOffset));

        notifyDataSetChanged();
    }

}
