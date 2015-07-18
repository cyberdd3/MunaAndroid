package com.akraft.muna.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.ProfileActivity;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MarkDetailFragment extends Fragment {


    @InjectView(R.id.mark_name)
    TextView markNameText;
    @InjectView(R.id.mark_date)
    TextView markDateText;
    @InjectView(R.id.mark_author)
    TextView markAuthorText;
    @InjectView(R.id.count_mark)
    Button countMarkButton;
    @InjectView(R.id.mark_info)
    LinearLayout markInfoLayout;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;

    MarkDetailInteraction markDetailInteraction;

    private Mark mark;
    private long myId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mark_detail, container, false);
        ButterKnife.inject(this, rootView);

        countMarkButton.setVisibility(View.INVISIBLE);
        markInfoLayout.setVisibility(View.INVISIBLE);
        myId = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);

        Mark emptyMark = (Mark) getArguments().getParcelable("mark");
        if (emptyMark != null) {
            ServiceManager.getInstance().service.loadMark(emptyMark.getId(), new Callback<Mark>() {
                @Override
                public void success(Mark data, Response response) {
                    markInfoLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    mark = data;
                    mark.save();
                    setMark(mark);
                    markDetailInteraction.markLoaded(mark);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
        return rootView;
    }


    public void setMark(final Mark mark) {
        this.mark = mark;
        markNameText.setText(mark.getName());
        markAuthorText.setText(mark.getUsername());
        markDateText.setText(Mark.dateFormat.format(mark.getAdded()));

        countMarkButton.setVisibility(((!mark.isActive()) || (mark.getAuthor() == myId)) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            markDetailInteraction = (MarkDetailInteraction) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getParentFragment().toString()
                    + " must implement OnCountMarkClick");
        }
    }

    @OnClick(R.id.count_mark)
    public void countMarkClick() {
        markDetailInteraction.showCountMarkLayout(mark);
    }

    @OnClick(R.id.author_row)
    public void openAuthorProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("id", mark.getAuthor());
        startActivity(intent);
    }

    public interface MarkDetailInteraction {
        void markLoaded(Mark mark);
        void showCountMarkLayout(Mark selectedMark);
    }
}
