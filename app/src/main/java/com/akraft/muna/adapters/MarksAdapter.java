package com.akraft.muna.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.models.Mark;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.ViewHolder> {

    private final OnItemClickListener clickListener;
    private List<Mark> marks = new ArrayList<>();
    private List<Mark> bookmarked = new ArrayList<>();
    private List<Mark> hidden = new ArrayList<>();
    private Context context;
    private BookmarkListener bookmarkListener;
    private boolean binding = false;

    public interface BookmarkListener {
        void removed(Mark mark, int position);
    }


    public MarksAdapter(Context context, List<Mark> marks, List<Mark> bookmarked, List<Mark> hidden, OnItemClickListener clickListener) {
        this.marks = marks;
        this.context = context;
        this.bookmarked = bookmarked;
        this.hidden = hidden;
        this.clickListener = clickListener;

    }

    public void setBookmarkListener(BookmarkListener bookmarkListener) {
        this.bookmarkListener = bookmarkListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_marks, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        binding = true;
        Mark mark = marks.get(i);
        viewHolder.name.setText(mark.getName());
        if (mark.getThumbnail() != null)
            ImageLoader.getInstance().displayImage(mark.getThumbnail(), viewHolder.photo);
        else
            viewHolder.photo.setImageResource(R.drawable.ic_photo_camera_black_24dp);

        viewHolder.bookmarkCheckBox.setChecked(bookmarked.contains(mark));
        binding = false;
    }

    @Override
    public int getItemCount() {
        return marks.size();
    }

    public Mark getItem(int position) {
        return marks.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.mark_name)
        TextView name;
        @InjectView(R.id.mark_photo)
        ImageView photo;
        @InjectView(R.id.mark_location)
        TextView markLocation;
        @InjectView(R.id.bookmark)
        CheckBox bookmarkCheckBox;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });

            bookmarkCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (binding)
                        return;
                    Mark mark = getItem(getLayoutPosition());
                    if (isChecked)
                        mark.setBookmarked(true);
                    else {
                        if (bookmarkListener != null)
                            bookmarkListener.removed(mark, getAdapterPosition());
                        else
                            mark.setBookmarked(false);
                    }
                    mark.save();
                }
            });

        }

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public List<Mark> getMarks() {
        return marks;
    }
}
