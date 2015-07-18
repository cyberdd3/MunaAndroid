package com.akraft.muna.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.akraft.muna.R;
import com.akraft.muna.fragments.EditProfileFragment;

public class EditProfileActivity extends AppCompatActivity implements EditProfileFragment.OnSaveChangesListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setOnSaveChangesListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return false;
    }

    @Override
    public void saved() {
        Toast.makeText(this, R.string.profile_changes_saved, Toast.LENGTH_SHORT).show();
    }
}
