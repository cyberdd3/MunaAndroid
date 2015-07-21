package com.akraft.muna.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.callbacks.MarkCreatingCallback;
import com.akraft.muna.fragments.MarkEditDetailsFragment;
import com.akraft.muna.fragments.MarkChooseLocationFragment;
import com.akraft.muna.fragments.MarkStartCreatingFragment;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public class MarkCreateActivity extends AppCompatActivity implements MarkCreatingCallback {

    private FragmentManager fragmentManager;

    private Class[] steps = new Class[]{MarkStartCreatingFragment.class, MarkChooseLocationFragment.class, MarkEditDetailsFragment.class};
    private int currentStep = 0;

    private Mark mark;
    private File imageFile;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_create);
        ButterKnife.inject(this);

        fragmentManager = getSupportFragmentManager();
        updateStepFragment();

        //if (!getSharedPreferences(Utils.SHOWCASE_PREF, 0).getBoolean("mark_creation", false)) {
         //   getSharedPreferences(Utils.SHOWCASE_PREF, 0).edit().putBoolean("mark_creation", true).commit();
            Intent intent = new Intent(this, ShowcaseActivity.class);
            intent.putExtra("type", ShowcaseActivity.MARK_CREATION);
            startActivity(intent);
        //}
    }

    private void nextStep() {
        if (currentStep < steps.length - 1) {
            currentStep++;
            updateStepFragment();
        } else {
            Fragment fragment = fragmentManager.findFragmentById(R.id.flContent);
            if (fragment != null)
                fragmentManager.beginTransaction().remove(fragment).commit();
            addMark();
        }
    }

    private void addMark() {
        ServiceManager.getInstance().service.addMark(mark, new Callback<Mark>() {
            @Override
            public void success(Mark o, Response response) {
                if (imageFile != null) {
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
                    try {
                        Utils.compressImage(imageFile);
                        ServiceManager.getInstance().service.uploadMarkPhoto(new TypedFile(mimeType, imageFile), new TypedString(o.getId() + ""), new Callback<Object>() {
                            @Override
                            public void success(Object o, Response response) {
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void updateStepFragment() {
        try {
            if (currentFragment != null)
                fragmentManager.beginTransaction().remove(currentFragment).commit();

            Bundle bundle = new Bundle();
            bundle.putParcelable("mark", mark);
            currentFragment = (Fragment) steps[currentStep].newInstance();
            currentFragment.setArguments(bundle);

            fragmentManager.beginTransaction().add(R.id.flContent, currentFragment).commit();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void locationGot(LatLng latLng) {
        mark.setLat(latLng.latitude);
        mark.setLon(latLng.longitude);
    }

    @Override
    public void detailsGot(String name, String codeword, String note, File imageFile) {
        mark.setName(name);
        mark.setCodeword(codeword);
        mark.setNote(note);
        this.imageFile = imageFile;
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            currentStep--;
            updateStepFragment();
        } else {
            finish();
        }
    }

    @Override
    public void next() {
        nextStep();
    }

    @Override
    public void started(String codeword) {
        if (mark == null) {
            mark = new Mark();
            mark.setAuthor(getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0));
        }
        mark.setCodeword(codeword);
    }

    @Override
    public void cancel() {
        finish();
    }

}
