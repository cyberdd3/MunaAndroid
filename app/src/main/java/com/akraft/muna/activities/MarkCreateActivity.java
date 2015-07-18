package com.akraft.muna.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.fragments.MarkEditDetailsFragment;
import com.akraft.muna.fragments.MarkLocationFragment;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

public class MarkCreateActivity extends AppCompatActivity
        implements MarkLocationFragment.OnMarkLocationChosen, MarkEditDetailsFragment.OnMarkDetailsProvided {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private FragmentManager fragmentManager;

    private Class[] steps = new Class[]{MarkLocationFragment.class, MarkEditDetailsFragment.class};
    private int[] titles = new int[]{R.string.mark_location, R.string.mark_details};
    private int currentStep = 0;

    private Mark mark;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_create);
        ButterKnife.inject(this);
        createNavigation();

        mark = new Mark();
        mark.setAuthor(getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0));
        fragmentManager = getSupportFragmentManager();
        updateStepFragment();
    }


    private void createNavigation() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mark_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.next:
                if (currentStep < steps.length - 1) {
                    currentStep++;
                    updateStepFragment();
                    if (currentStep == steps.length - 2)
                        item.setTitle(R.string.add_mark);
                } else {
                    Fragment fragment = fragmentManager.findFragmentById(R.id.flContent);
                    if (fragment != null)
                        fragmentManager.beginTransaction().remove(fragment).commit();
                    addMark();
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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
            fragmentManager.beginTransaction().replace(R.id.flContent, (Fragment) steps[currentStep].newInstance()).commit();
            getSupportActionBar().setTitle(titles[currentStep]);
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
    public void detailsGot(String name, File imageFile) {
        mark.setName(name);
        this.imageFile = imageFile;
    }
}
