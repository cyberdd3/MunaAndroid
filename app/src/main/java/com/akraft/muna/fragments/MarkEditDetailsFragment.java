package com.akraft.muna.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.callbacks.MarkCreatingCallback;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarkEditDetailsFragment extends Fragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    @InjectView(R.id.mark_name)
    TextView markNameText;
    @InjectView(R.id.mark_note)
    TextView markNoteText;
    @InjectView(R.id.mark_photo)
    ImageView markPhoto;
    @InjectView(R.id.add_photo_label)
    TextView addPhotoLabel;
    @InjectView(R.id.codeword)
    EditText codewordText;

    MarkCreatingCallback mCallback;
    private File photoFile;
    private Mark mark;

    public MarkEditDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_mark_edit_details, container, false);
        ButterKnife.inject(this, rootView);

        mark = getArguments().getParcelable("mark");

        if (mark != null) {
            if (mark.getName() != null)
                markNameText.setText(mark.getName());
            if (mark.getNote() != null)
                markNoteText.setText(mark.getNote());
            if (mark.getCodeword() != null)
                codewordText.setText(mark.getCodeword());
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (MarkCreatingCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        mCallback.detailsGot(markNameText.getText().toString(), codewordText.getText().toString(), markNoteText.getText().toString(), photoFile);
        super.onDetach();
    }

    @OnClick(R.id.mark_photo)
    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = Utils.createImageFile(getActivity());
            } catch (IOException ex) {
                Toast.makeText(getActivity(), R.string.error_creating_file_for_photo, Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @OnClick(R.id.cancel)
    public void cancel() {mCallback.cancel();}

    @OnClick(R.id.finish)
    public void finish() {mCallback.next();}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == FragmentActivity.RESULT_OK) {
            markPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            markPhoto.setImageURI(Uri.fromFile(photoFile));
            addPhotoLabel.setVisibility(View.GONE);
        }

    }
}
