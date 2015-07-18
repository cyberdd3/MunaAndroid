package com.akraft.muna.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.akraft.muna.R;
import com.akraft.muna.models.Mark;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MarkInfoFragment extends Fragment implements MarkDetailFragment.MarkDetailInteraction {

    private Mark mark;

    @InjectView(R.id.bookmark)
    ImageButton bookmarkButton;
    @InjectView(R.id.mark_toolbar)
    Toolbar markToolbar;
    @InjectView(R.id.close_mark_info)
    ImageButton closeButton;
    @InjectView(R.id.hide_mark)
    ImageButton hideButton;

    private MarkDetailFragment markDetailFragment;
    private CountMarkFragment countMarkFragment;
    private CountMarkFragment.SuccessListener successListener;
    private MarkInfoInteraction markInfoInteraction;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mark_info, container, false);
        ButterKnife.inject(this, rootView);

        mark = getArguments().getParcelable("mark");

        bookmarkButton.setSelected(mark.isBookmarked());
        hideButton.setVisibility(mark.isActive() ? View.VISIBLE : View.GONE);

        Bundle bundle = new Bundle();
        bundle.putParcelable("mark", mark);

        markDetailFragment = new MarkDetailFragment();
        markDetailFragment.setArguments(bundle);

        getChildFragmentManager().beginTransaction().replace(R.id.content, markDetailFragment).commit();

        return rootView;
    }

    public void setMark(Mark mark) {
        markDetailFragment.setMark(mark);
    }

    @Override
    public void markLoaded(Mark mark) {
        this.mark = mark;
    }

    @Override
    public void showCountMarkLayout(Mark mark) {
        countMarkFragment = new CountMarkFragment();
        countMarkFragment.setSuccessListener(successListener);
        Bundle bundle = new Bundle();
        bundle.putParcelable("mark", mark);
        countMarkFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.content, countMarkFragment).addToBackStack(null).commit();
    }

    public void setSuccessListener(CountMarkFragment.SuccessListener successListener) {
        this.successListener = successListener;
    }

    @OnClick(R.id.close_mark_info)
    public void close() {
        markInfoInteraction.closeInfo();
    }

    @OnClick(R.id.hide_mark)
    public void hideMark() {
        mark.setHidden(true);
        mark.setBookmarked(false);
        mark.save();
        Snackbar.make(rootView, R.string.mark_hidden, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mark.setHidden(false);
                mark.save();
                markInfoInteraction.undoHideMark(mark);
            }
        }).show();
        markInfoInteraction.closeInfo();
        markInfoInteraction.hideMark(mark);
    }

    @OnClick(R.id.bookmark)
    public void bookmark() {
        bookmarkButton.setSelected(!bookmarkButton.isSelected());
        if (bookmarkButton.isSelected())
            mark.setBookmarked(true);
        else
            mark.setBookmarked(false);
        mark.save();
    }

    public void setMarkInfoInteraction(MarkInfoInteraction markInfoInteraction) {
        this.markInfoInteraction = markInfoInteraction;
    }


    public interface MarkInfoInteraction {
        void closeInfo();

        void hideMark(Mark mark);

        void undoHideMark(Mark mark);
    }
}
