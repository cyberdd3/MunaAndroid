package com.akraft.muna.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.ChatActivity;
import com.akraft.muna.activities.ProfileActivity;
import com.akraft.muna.fragments.UsersListFragment;
import com.akraft.muna.models.User;
import com.akraft.muna.models.wrappers.UserId;
import com.akraft.muna.service.ServiceManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final int type;
    private Context context;
    private List<User> users;
    private final long id;

    public UsersAdapter(Context context, List<User> users, int type) {
        this.context = context;
        this.users = users;
        id = context.getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_users, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText("@" + user.getUsername());

        if (!user.getAvatar().equals(""))
            ImageLoader.getInstance().displayImage(Config.SERVER_URL_PORT + user.getAvatar(), holder.avatarView, Utils.NO_CACHE_OPTION);
        else
            holder.avatarView.setImageResource(R.drawable.avatar_placeholder);
        if (user.getId() == id) {
            holder.chatButton.setVisibility(View.GONE);
        } else {
            holder.chatButton.setVisibility(View.VISIBLE);
        }

        if (type == UsersListFragment.INCOMING_REQUESTS) {
            holder.chatButton.setVisibility(View.GONE);
            holder.acceptRequestButton.setVisibility(View.VISIBLE);
            holder.declineRequestButton.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.avatar)
        ImageView avatarView;
        @InjectView(R.id.username)
        TextView usernameText;
        @InjectView(R.id.chat)
        ImageButton chatButton;
        @InjectView(R.id.accept_request)
        ImageButton acceptRequestButton;
        @InjectView(R.id.decline_request)
        ImageButton declineRequestButton;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
            chatButton.setOnClickListener(this);
            acceptRequestButton.setVisibility(View.GONE);
            declineRequestButton.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            Class activityClass = ProfileActivity.class;
            if (v.getId() == R.id.chat) {
                activityClass = ChatActivity.class;
            }
            Intent intent = new Intent(context, activityClass);
            intent.putExtra("user", users.get(getAdapterPosition()));
            context.startActivity(intent);
        }

        @OnClick(R.id.accept_request)
        public void acceptRequest() {
            final User user = users.get(getAdapterPosition());
            ServiceManager.getInstance().service.acceptRequest(new UserId(user.getId()), new Callback<Object>() {
                @Override
                public void success(Object o, Response response) {
                    removeUser(user);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

        @OnClick(R.id.decline_request)
        public void declineRequest() {
            final User user = users.get(getAdapterPosition());
            ServiceManager.getInstance().service.declineRequest(new UserId(user.getId()), new Callback<Object>() {
                @Override
                public void success(Object o, Response response) {
                    removeUser(user);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    private void removeUser(User user) {
        users.remove(user);
        notifyDataSetChanged();
    }
}
