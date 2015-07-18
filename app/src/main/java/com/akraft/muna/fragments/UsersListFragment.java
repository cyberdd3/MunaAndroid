package com.akraft.muna.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.akraft.muna.DividerItemDecoration;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.adapters.UsersAdapter;
import com.akraft.muna.models.Profile;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersListFragment extends Fragment {

    public static final int ALL = 0;
    public static final int TEAM = 1;
    public static final int INCOMING_REQUESTS = 2;

    private View rootView;
    @InjectView(R.id.users_list)
    RecyclerView usersList;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    private ArrayList<User> users = new ArrayList<>();
    private UsersAdapter adapter;
    private long id;
    private int type;
    private ArrayList<User> initialData;

    public UsersListFragment() {
        // Required empty public constructor
    }

    public static UsersListFragment newInstance(int type) {
        UsersListFragment fragment = new UsersListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView  = inflater.inflate(R.layout.fragment_users_list, container, false);
        ButterKnife.inject(this, rootView);

        id = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);

        type = getArguments().getInt("type", ALL);
        createList();
        loadUsers(type);
        return rootView;
    }

    private void loadUsers(int type) {
        switch (type){
            case ALL:
                ServiceManager.getInstance().service.users(new Callback<ArrayList<User>>() {
                    @Override
                    public void success(ArrayList<User> data, Response response) {
                        updateUsersList(data);
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                break;
            case TEAM:
                ServiceManager.getInstance().service.team(id, new Callback<Profile>() {
                    @Override
                    public void success(Profile profile, Response response) {
                        updateUsersList(profile.getTeam());
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                break;
            case INCOMING_REQUESTS:
                ServiceManager.getInstance().service.incomingRequests(id, new Callback<Profile>() {
                    @Override
                    public void success(Profile profile, Response response) {
                        updateUsersList(profile.getIncomingRequests());
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
                break;
        }
    }

    public void updateUsersList(List<User> users) {
        if (users != null) {
            this.users.clear();
            this.users.addAll(users);
            adapter.notifyDataSetChanged();

        }
        if (initialData == null) {
            initialData = new ArrayList<>(users);
        }
        progressBar.setVisibility(View.GONE);
    }

    public void restoreData() {
        updateUsersList(initialData);
    }

    public void setLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void createList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        usersList.setHasFixedSize(true);
        usersList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        usersList.setLayoutManager(layoutManager);
        usersList.setItemAnimator(new DefaultItemAnimator());
        adapter = new UsersAdapter(getActivity(),users, type);
        usersList.setAdapter(adapter);

    }
    @Override
    public void onResume() {
        super.onResume();
        if (users.equals(initialData))
            loadUsers(type);
    }
}
