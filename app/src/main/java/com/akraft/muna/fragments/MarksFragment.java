package com.akraft.muna.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.akraft.muna.R;
import com.akraft.muna.adapters.MarksAdapter;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MarksFragment extends Fragment {

    private MarksListFragment listFragment;
    private MarksAdapter.OnItemClickListener onItemClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_marks, container, false);
        ButterKnife.inject(this, rootView);

        listFragment = MarksListFragment.newInstance(MarksListFragment.ALL);
        getChildFragmentManager().beginTransaction().replace(R.id.container, listFragment).commit();
        listFragment.setOnItemClickListener(onItemClickListener);
        setHasOptionsMenu(true);
        return rootView;
    }

    public MarksListFragment getListFragment() {
        return listFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_marks, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setQueryHint(getResources().getString(R.string.search_marks));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String constraint = searchView.getQuery().toString();
                if (constraint.length() > 0) {
                    listFragment.setLoading();
                    ServiceManager.getInstance().service.searchMarks(constraint, searchCallback);
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

            }
        });
    }

    private Callback<ArrayList<Mark>> searchCallback = new Callback<ArrayList<Mark>>() {
        @Override
        public void success(ArrayList<Mark> marks, Response response) {
            listFragment.setType(MarksListFragment.SEARCH);
            listFragment.updateMarksList(marks);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };

    public void setOnItemClickListener(MarksAdapter.OnItemClickListener onItemClickListener) {

        this.onItemClickListener = onItemClickListener;
    }

}
