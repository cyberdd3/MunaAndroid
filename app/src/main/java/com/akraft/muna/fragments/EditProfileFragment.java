package com.akraft.muna.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.models.Profile;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class EditProfileFragment extends Fragment {


    private static final int CHOOSE_PICTURE = 1;
    private long id;
    private Profile profile;

    @InjectView(R.id.first_name)
    EditText firstNameText;
    @InjectView(R.id.last_name)
    EditText lastNameText;
    @InjectView(R.id.country)
    EditText countryText;


    @InjectView(R.id.username)
    TextView usernameText;
    @InjectView(R.id.email)
    TextView emailText;

    @InjectView(R.id.password)
    EditText passwordText;
    @InjectView(R.id.password_again)
    EditText passwordAgainText;

    @InjectView(R.id.password_layout)
    LinearLayout passwordLayout;

    @InjectView(R.id.avatar)
    ImageButton avatarImage;

    private String facebookId;

    private File avatarFile;
    private OnSaveChangesListener onSaveChangesListener;
    private MenuItem submitButton;
    private boolean avatarFromDocuments = false;

    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.avatar_placeholder).cacheInMemory(false).cacheOnDisk(false).build();
    private boolean avatarFromCamera = false;

    public void setOnSaveChangesListener(OnSaveChangesListener onSaveChangesListener) {
        this.onSaveChangesListener = onSaveChangesListener;
    }

    public interface OnSaveChangesListener {
        void saved();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.inject(this, rootView);

        id = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
        if (id == 0) {
            //TODO error
        }
        loadProfile();
        setHasOptionsMenu(true);
        passwordLayout.setVisibility(View.GONE);

        return rootView;
    }


    private void loadProfile() {
        ServiceManager.getInstance().service.loadProfile(id, new Callback<Profile>() {

            @Override
            public void success(Profile result, Response response) {
                profile = result;
                firstNameText.setText(profile.getUser().getFirst_name());
                lastNameText.setText(profile.getUser().getLast_name());
                countryText.setText(profile.getCountry());

                usernameText.setText(profile.getUser().getUsername());
                emailText.setText(profile.getUser().getEmail());

                if (profile.getFacebook() == null || profile.getFacebook().equals("")) {
                    passwordLayout.setVisibility(View.VISIBLE);
                }

                submitButton.setVisible(true);

                ImageLoader.getInstance().displayImage(profile.getAvatar(), avatarImage, options);
                avatarImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent pickIntent = new Intent();
                        pickIntent.setType("image/*");
                        pickIntent.setAction(Intent.ACTION_PICK);

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Intent chooserIntent = Intent.createChooser(pickIntent, getResources().getString(R.string.pick_image));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
                        avatarFile = null;
                        try {
                            avatarFile = Utils.createImageFile(getActivity());
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Toast.makeText(getActivity(), R.string.error_creating_file_for_photo, Toast.LENGTH_SHORT).show();
                        }
                        // Continue only if the File was successfully created
                        if (avatarFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(avatarFile));
                            startActivityForResult(chooserIntent, CHOOSE_PICTURE);
                        }
                    }
                });

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_PICTURE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                if (data.getData() != null) {
                    //From documents
                    try {
                        InputStream imageStream = getActivity().getContentResolver().openInputStream(data.getData());
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                        Utils.bitmapToFile(avatarFile, selectedImage);
                        avatarImage.setImageBitmap(selectedImage);
                        avatarFromDocuments = true;
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), R.string.error_creating_file_for_photo, Toast.LENGTH_SHORT);
                    }

                } else {
                    //From camera
                    try {
                        Utils.compressImage(avatarFile);
                        avatarImage.setImageURI(Uri.fromFile(avatarFile));
                        avatarFromCamera = true;
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), R.string.error_creating_file_for_photo, Toast.LENGTH_SHORT);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_edit_profile, menu);
        submitButton = menu.findItem(R.id.edit_profile);
        submitButton.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_profile) {

            String password = passwordText.getText().toString();
            String passwordAgain = passwordAgainText.getText().toString();
            if (!password.equals(passwordAgain)) {
                passwordAgainText.setError(getResources().getString(R.string.passwords_dont_match));
                return false;
            }

            String myUsername = profile.getUser().getUsername();
            final User user = new User();

            String firstName = firstNameText.getText().toString();
            if (firstName.length() > 0)
                user.setFirst_name(firstName);
            String lastName = lastNameText.getText().toString();
            if (lastName.length() > 0)
                user.setLast_name(lastName);
            if (password.length() > 0)
                user.setPassword(password);
            profile.setUser(user);
            String country = countryText.getText().toString();
            if (country.length() > 0)
                profile.setCountry(country);

            profile.setFacebook(facebookId);

            final String username = usernameText.getText().toString();
            if (username.length() > 3 && !username.equals(myUsername)) {
                ServiceManager.getInstance().service.usernameExists(username, new Callback<Boolean>() {
                    @Override
                    public void success(Boolean notExists, Response response) {
                        if (notExists) {
                            user.setUsername(username);
                            submitProfile();
                        } else {
                            usernameText.setError(getResources().getString(R.string.username_exists));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            } else {
                submitProfile();
            }
        }
        return false;
    }

    private void submitProfile() {
        submitButton.setEnabled(false);

        ServiceManager.getInstance().service.editProfile(id, profile, new Callback<Profile>() {
            @Override
            public void success(Profile result, Response response) {
                if (avatarFromCamera || avatarFromDocuments) {
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
                    ServiceManager.getInstance().service.uploadAvatar(new TypedFile(mimeType, avatarFile), new Callback<Object>() {
                        @Override
                        public void success(Object o, Response response) {

                            successSubmiting();
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });

                } else {
                    successSubmiting();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void successSubmiting() {
        if (avatarFile != null)
            avatarFile.delete();
        onSaveChangesListener.saved();
        submitButton.setEnabled(true);
    }
}
