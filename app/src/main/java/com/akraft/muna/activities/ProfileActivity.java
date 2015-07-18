package com.akraft.muna.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.codes.UsersRelationsCodes;
import com.akraft.muna.map.MapboxTileProvider;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.Profile;
import com.akraft.muna.models.User;
import com.akraft.muna.models.wrappers.UserId;
import com.akraft.muna.service.MainService;
import com.akraft.muna.service.ServiceManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int SNACKBAR_DURATION = 2750;
    private final MainService service = ServiceManager.getInstance().service;
    @InjectView(R.id.avatar)
    ImageView avatarView;
    @InjectView(R.id.username)
    TextView usernameText;
    @InjectView(R.id.name)
    TextView nameText;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.send_message)
    FloatingActionButton sendMessageButton;
    @InjectView(R.id.team_button_flipper)
    ViewFlipper teamButtonFlipper;
    @InjectView(R.id.scrollView)
    NestedScrollView scrollView;
    @InjectView(R.id.edit_profile)
    Button editProfile;
    @InjectView(R.id.level)
    TextView levelText;
    @InjectView(R.id.level_progress)
    ProgressBar levelProgress;
    @InjectView(R.id.experience)
    TextView expText;
    @InjectView(R.id.level_layout)
    LinearLayout levelLayout;

    private User user;
    private int userRelation;
    private Button teamButton;
    private long userId;
    private Profile profile;

    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);
        user = (User) getIntent().getParcelableExtra("user");
        editProfile.setVisibility(View.GONE);
        levelLayout.setVisibility(View.GONE);
        if (user == null) {
            userId = getIntent().getLongExtra("id", 0);
        } else {
            userId = user.getId();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        createNavigation();
    }

    private void loadProfile() {
        ServiceManager.getInstance().service.loadProfile(userId, new Callback<Profile>() {
            @Override
            public void success(Profile result, Response response) {
                profile = result;
                mapFragment.getMapAsync(onMapReadyCallback);
                user = profile.getUser();
                updateProfileInfo();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void defineRelations() {
        service.defineRelations(user.getId(), new Callback<Integer>() {
            @Override
            public void success(Integer result, Response response) {
                if (teamButton != null)
                    teamButton.setEnabled(true);
                userRelation = result;

                //a result code from server is correlated with views order in the view flipper
                teamButtonFlipper.setVisibility(View.VISIBLE);
                teamButtonFlipper.setDisplayedChild(userRelation);
            }

            @Override
            public void failure(RetrofitError error) {
                if (teamButton != null)
                    teamButton.setEnabled(true);

            }
        });
    }

    private void createNavigation() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
    }

    private void finishWithError() {
        Toast.makeText(this, "Sorry, no such user", Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnClick({R.id.send_request, R.id.accept_request, R.id.decline_request, R.id.remove_request, R.id.remove_from_team})
    public void teamButtonClick(View view) {
        view.setEnabled(false);
        teamButton = (Button) view;
        final UserId userId = new UserId(user.getId());
        switch (view.getId()) {
            case R.id.send_request:
                service.sendRequest(userId, teamCallback);
                break;
            case R.id.accept_request:
                service.acceptRequest(userId, teamCallback);
                break;
            case R.id.decline_request:
                service.declineRequest(userId, teamCallback);
                break;
            case R.id.remove_request:
                service.removeRequest(userId, teamCallback);
                break;
            case R.id.remove_from_team:
                teamButtonFlipper.setDisplayedChild(UsersRelationsCodes.USER_RELATION_NONE);
                final CountDownTimer timer = new CountDownTimer(SNACKBAR_DURATION, SNACKBAR_DURATION) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        service.removeTeammate(userId, teamCallback);
                    }
                };
                timer.start();
                Snackbar.make(scrollView, R.string.removed_from_team, Snackbar.LENGTH_LONG).setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timer.cancel();
                        defineRelations();
                    }
                }).show();

                break;
        }
    }

    private Callback<Object> teamCallback = new Callback<Object>() {
        @Override
        public void success(Object object, Response response) {
            defineRelations();
        }

        @Override
        public void failure(RetrofitError error) {
            teamButton.setEnabled(true);
            Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    @OnClick(R.id.send_message)
    public void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    private void updateProfileInfo() {
        if (user.getId() == getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0)) {
            sendMessageButton.setVisibility(View.GONE);
            editProfile.setVisibility(View.VISIBLE);
        } else {
            defineRelations();
        }

        teamButtonFlipper.setVisibility(View.GONE);
        usernameText.setText("@" + user.getUsername());
        ImageLoader.getInstance().displayImage(profile.getAvatar(), avatarView, Utils.NO_CACHE_OPTION);
        nameText.setText(user.getFirst_name() + " " + user.getLast_name());

        levelText.setText(getResources().getString(R.string.level) + " " + profile.getLevel());
        expText.setText(profile.getExp() + "/" + profile.getExpNext());

        levelLayout.setVisibility(View.VISIBLE);
        levelProgress.setMax(profile.getExpNext() - profile.getExpCurr());
        animateLevelBar();
    }

    private void animateLevelBar() {
        ObjectAnimator animation = ObjectAnimator.ofInt(levelProgress, "progress", profile.getExp() - profile.getExpCurr());
        animation.setDuration(1250); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.edit_profile)
    public void editProfile() {
        startActivity(new Intent(this, EditProfileActivity.class));
    }


    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (mapFragment.getView() != null)
                mapFragment.getView().setClickable(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(new MapboxTileProvider(MapboxTileProvider.MAP_TILE_DIMENSION, MapboxTileProvider.MAP_TILE_DIMENSION)));


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Mark mark : profile.getMarks()) {
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mark.getLat(), mark.getLon()))
                        .title(mark.getName())
                        .icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.ic_map_marker_collected)));
                builder.include(marker.getPosition());
            }
            if (profile.getMarks().size() > 0) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 50);
                googleMap.moveCamera(cu);
            }
        }
    };
}
