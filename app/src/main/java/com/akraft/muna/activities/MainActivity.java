package com.akraft.muna.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.adapters.MarksAdapter;
import com.akraft.muna.background.ChatService;
import com.akraft.muna.background.NotificationsService;
import com.akraft.muna.fragments.MapFragment;
import com.akraft.muna.fragments.MarksFragment;
import com.akraft.muna.fragments.MarksListFragment;
import com.akraft.muna.fragments.SettingsFragment;
import com.akraft.muna.fragments.TeamFragment;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.User;
import com.akraft.muna.service.ServiceManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    public static final int MARKS_NEARBY = 1;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.nvView)
    NavigationView nvDrawer;
    @InjectView(R.id.avatar)
    ImageView avatarView;
    @InjectView(R.id.username)
    TextView usernameText;
    @InjectView(R.id.name)
    TextView nameText;


    ChatService.IChatService chatService;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            chatService = (ChatService.IChatService) service;
            //chatService.startReceivingMessages(getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private ActionBarDrawerToggle drawerToggle;
    private FragmentManager fragmentManager;
    private long id;
    private User user;


    private MenuItem clickedDrawerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initializeImageLoader();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new com.akraft.muna.fragments.MapFragment()).commit();

        createNavigation();

        user = (User) getIntent().getParcelableExtra("user");
        if (user == null) {
            id = getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
            loadProfile();
        } else {
            id = user.getId();
            updateDrawerHeader();
        }

        startService(new Intent(this, ChatService.class));
        startService(new Intent(this, NotificationsService.class));
        sendBroadcast(new Intent("com.akraft.muna.action.START_MARKS_DETECTOR"));

    }

    private void loadProfile() {
        ServiceManager.getInstance().service.user(null, null, id, new Callback<User>() {

            @Override
            public void success(User data, Response response) {
                user = data;
                updateDrawerHeader();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void initializeImageLoader() {
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                //.cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(imageOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void createNavigation() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        setupDrawerContent(nvDrawer);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);

        clickedDrawerItem = nvDrawer.getMenu().findItem(R.id.nav_map_fragment);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });

    }

    private void updateDrawerHeader() {
        ImageLoader.getInstance().displayImage(Config.SERVER_URL_PORT + user.getAvatar(), avatarView, Utils.NO_CACHE_OPTION);

        usernameText.setText("@" + user.getUsername());
        nameText.setVisibility(View.GONE);
    }

    @OnClick(R.id.nav_header)
    public void openMyProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    private void selectDrawerItem(final MenuItem menuItem) {
        clickedDrawerItem = menuItem;
        changeDrawerState(menuItem);

        Fragment fragment = null;
        Class fragmentClass = null;
        switch (menuItem.getItemId()) {
            case R.id.nav_map_fragment:
                fragmentClass = MapFragment.class;
                break;
            case R.id.nav_marks_fragment:
                final MarksFragment marksFragment = new MarksFragment();
                marksFragment.setOnItemClickListener(new MarksAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (marksFragment.getListFragment() != null) {
                            selectMark(marksFragment.getListFragment().getAdapter().getItem(position));
                            changeDrawerState(nvDrawer.getMenu().findItem(R.id.nav_map_fragment));
                        }
                    }
                });
                fragment = marksFragment;
                break;
            case R.id.nav_team_fragment:
                fragmentClass = TeamFragment.class;
                break;
            case R.id.nav_bookmarked_fragment:
                final MarksListFragment marksListFragment = MarksListFragment.newInstance(MarksListFragment.BOOKMARKED);
                marksListFragment.setOnItemClickListener(new MarksAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        selectMark(marksListFragment.getAdapter().getItem(position));
                        changeDrawerState(nvDrawer.getMenu().findItem(R.id.nav_map_fragment));
                    }
                });
                fragment = marksListFragment;
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = MapFragment.class;
        }

        if (fragment == null && fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fragmentManager.popBackStack(R.id.flContent, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        nvDrawer.bringToFront();
    }

    public void selectMark(Mark mark) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("mark", mark);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flContent, mapFragment).addToBackStack(null).commit();
    }

    private void changeDrawerState(MenuItem menuItem) {
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.flContent);
        if (f instanceof MapFragment) {
            MapFragment mapFragment = (MapFragment) f;
            if (mapFragment.hasFragmentBackStack()) {
                mapFragment.onBackPressed();
            } else if (mapFragment.isPanelOpened()) {
                mapFragment.closePanel();
            } else {
                changeDrawerState(clickedDrawerItem);
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }


}
