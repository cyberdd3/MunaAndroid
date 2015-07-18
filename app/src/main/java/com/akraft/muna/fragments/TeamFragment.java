package com.akraft.muna.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.TeamRequestsActivity;
import com.akraft.muna.models.Profile;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeamFragment extends Fragment {

    @InjectView(R.id.incoming_requests)
    Button incomingRequestsButton;
    private long id;
    private UsersListFragment listFragment;

    public TeamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        ButterKnife.inject(this, rootView);

        id = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);

        listFragment = UsersListFragment.newInstance(UsersListFragment.TEAM);
        getChildFragmentManager().beginTransaction().replace(R.id.container, listFragment).commit();
        incomingRequestsButton.setVisibility(View.GONE);

        checkIncoming();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIncoming();
    }

    private void checkIncoming() {
        ServiceManager.getInstance().service.incomingRequests(id, new Callback<Profile>() {
            @Override
            public void success(Profile profile, Response response) {
                if (profile.getIncomingRequests().size() > 0) {
                    incomingRequestsButton.setVisibility(View.VISIBLE);
                } else {
                    incomingRequestsButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @OnClick(R.id.incoming_requests)
    public void openIncomingRequests() {
        startActivity(new Intent(getActivity(), TeamRequestsActivity.class));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.users_fragment, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setQueryHint(getResources().getString(R.string.search_players));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String constraint = searchView.getQuery().toString();
                if (constraint.length() > 0) {
                    makeSearch(constraint);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                listFragment.restoreData();
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSearch("");
            }
        });

    }

    private void makeSearch(String constraint) {
        listFragment.setLoading();
        ServiceManager.getInstance().service.searchUsers(constraint, searchCallback);
    }

    private Callback<ArrayList<User>> searchCallback = new Callback<ArrayList<User>>() {
        @Override
        public void success(ArrayList<User> users, Response response) {
            listFragment.updateUsersList(users);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };

}
