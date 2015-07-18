package com.akraft.muna.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akraft.muna.DividerItemDecoration;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.adapters.MarksAdapter;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.MainService;
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
public class MarksListFragment extends Fragment implements MarksAdapter.BookmarkListener {


    public static final int ALL = 0;
    public static final int MY = 1;
    public static final int NEARBY = 2;
    public static final int BOOKMARKED = 3;
    public static final int SEARCH = 4;

    private static final int MARKS_OFFSET_TO_LOAD_NEXT_PAGE = 3;

    private View rootView;
    @InjectView(R.id.marks_list)
    RecyclerView marksList;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.layout)
    View layout;

    private List<Mark> marks = new ArrayList<>();

    private int type;
    private MainService service;
    private MarksAdapter adapter;
    private MarksAdapter.OnItemClickListener onClickListener;
    private OnMarksLoadedListener onMarksLoadedListener;
    private ArrayList<Mark> initialData;
    private MarksAdapter.BookmarkListener bookmarkListener;

    private int currentPage = 1;
    private boolean connected;
    private LinearLayoutManager mLayoutManager;
    private boolean justCreated;
    private int initialType;
    private List<Mark> bookmarked;
    private List<Mark> hidden;

    public MarksListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justCreated = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_marks_list, container, false);
        ButterKnife.inject(this, rootView);

        service = ServiceManager.getInstance().service;
        connected = Utils.isNetworkConnected(getActivity());
        type = getArguments().getInt("type", ALL);
        initialType = type;

        bookmarked = Mark.find(Mark.class, "bookmarked = 1");
        hidden = Mark.find(Mark.class, "hidden = 1");

        if (justCreated) {
            getMarks();
            justCreated = false;
        }
        createList();
        return rootView;
    }

    private void getMarks() {
        progressBar.setVisibility(View.VISIBLE);
        switch (type) {
            case ALL:
                if (connected)
                    service.getActiveMarks(currentPage, marksListCallback);
                else {
                    progressBar.setVisibility(View.GONE);
                    marks = Mark.listAll(Mark.class);
                }
                marksList.addOnScrollListener(onScrollListener);
                currentPage++;
                break;
            case NEARBY:
                if (connected) {
                    double lat = getArguments().getDouble("lat", -1000);
                    double lon = getArguments().getDouble("lon", -1000);
                    if (lat == -1000 || lon == -1000)
                        throw new IllegalArgumentException("Provide lat and lon as arguments for this fragment");
                    service.getMarksNearby(lat, lon, null, marksListCallback);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
                }
                break;
            case BOOKMARKED:
                progressBar.setVisibility(View.GONE);
                marks.addAll(bookmarked);
                bookmarkListener = this;
                break;
        }
    }


    private Callback<ArrayList<Mark>> marksListCallback = new Callback<ArrayList<Mark>>() {
        @Override
        public void success(ArrayList<Mark> data, Response response) {
            updateMarksList(data);
            progressBar.setVisibility(View.GONE);
            loadingMarks = false;
            if (onMarksLoadedListener != null)
                onMarksLoadedListener.onFinishLoading(marks.size() == 0);
        }

        @Override
        public void failure(RetrofitError error) {
            progressBar.setVisibility(View.GONE);
            //TODO error
        }
    };

    public void setOnItemClickListener(MarksAdapter.OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void createList() {
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.scrollToPosition(0);
        marksList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        marksList.setLayoutManager(mLayoutManager);
        adapter = new MarksAdapter(getActivity(), marks, bookmarked, hidden, onClickListener);
        adapter.setBookmarkListener(bookmarkListener);
        marksList.setAdapter(adapter);
    }

    private boolean loadingMarks = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            visibleItemCount = mLayoutManager.getChildCount();
            totalItemCount = mLayoutManager.getItemCount();
            pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

            if (!loadingMarks) {
                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    loadingMarks = true;
                    getMarks();
                }
            }
        }
    };

    public MarksAdapter getAdapter() {
        return adapter;
    }

    public List<Mark> getMarksList() {
        return marks;
    }

    public void setOnMarksLoadedListener(OnMarksLoadedListener onMarksLoadedListener) {
        this.onMarksLoadedListener = onMarksLoadedListener;
    }


    public static MarksListFragment newInstance(int type) {
        MarksListFragment fragment = new MarksListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void removed(final Mark mark, final int position) {
        marks.remove(mark);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, marks.size());

        mark.setBookmarked(false);
        mark.save();
        Snackbar.make(layout, R.string.removed_from_bookmarks, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mark.setBookmarked(true);
                mark.save();
                marks.add(position, mark);
                adapter.notifyItemInserted(position);
                adapter.notifyItemRangeChanged(position, marks.size());

            }
        }).show();
    }


    public interface OnMarksLoadedListener {
        void onFinishLoading(boolean empty);
    }

    public void setLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void updateMarksList(List<Mark> data) {;
        if (marks != null) {
            if (type != ALL) {
                marks.clear();
            }

            marks.addAll(data);
            adapter.notifyDataSetChanged();

        }
        if (initialData == null) {
            initialData = new ArrayList<>(marks);
        }
        progressBar.setVisibility(View.GONE);
    }

    public void restoreData() {
        type = initialType;
        updateMarksList(initialData);
    }

    public void setType(int type) {
        this.type = type;
    }
}
