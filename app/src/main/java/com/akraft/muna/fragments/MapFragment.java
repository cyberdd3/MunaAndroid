package com.akraft.muna.fragments;


import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.MarkCreateActivity;
import com.akraft.muna.adapters.MarksAdapter;
import com.akraft.muna.map.MapboxTileProvider;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.Profile;
import com.akraft.muna.service.ServiceManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapFragment extends Fragment implements OnMapReadyCallback, CountMarkFragment.SuccessListener, MarkInfoFragment.MarkInfoInteraction {


    private static final float ANCHOR_POINT = 0.5f;
    private final float ZOOM_NEARBY = 11f;
    private final float ZOOM_MARK = 17;
    private List<Mark> marks = Collections.synchronizedList(new ArrayList<Mark>());

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    @InjectView(R.id.sliding_panel)
    SlidingUpPanelLayout slidingPanel;
    @InjectView(R.id.panel_container)
    ViewGroup panelContainer;
    @InjectView(R.id.count_mark_quick)
    Button countMarkQuickButton;
    @InjectView(R.id.panel_collapsed)
    ViewSwitcher panelCollapsed;
    @InjectView(R.id.close_panel)
    ImageButton closePanel;
    @InjectView(R.id.list_header)
    TextView listHeader;


    private Map<Marker, Mark> marksData = new ConcurrentHashMap<>();
    private MarkInfoFragment markInfoFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private boolean isWaitingForLocation = false;
    private Marker currentMarker;

    private long myId;
    private Mark mark;
    private float lastZoom;
    private List<Mark> hidden;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        ButterKnife.inject(this, rootView);

        hidden = Mark.find(Mark.class, "hidden = 1");

        if (getArguments() != null)
            mark = getArguments().getParcelable("mark");

        setupSlidingPanel();

        return rootView;
    }


    private void setupSlidingPanel() {
        listHeader.setVisibility(View.GONE);
        panelContainer.setVisibility(View.GONE);
        slidingPanel.setTouchEnabled(false);
        slidingPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                if (v > 0) {
                    if (panelContainer.getVisibility() != View.VISIBLE) {
                        panelContainer.setVisibility(View.VISIBLE);
                        countMarkQuickButton.setVisibility(View.GONE);
                        panelCollapsed.setDisplayedChild(0);
                        panelCollapsed.setVisibility(View.GONE);
                    }
                } else {
                    panelContainer.setVisibility(View.GONE);
                    countMarkQuickButton.setVisibility(View.VISIBLE);
                    panelCollapsed.setDisplayedChild(0);
                    panelCollapsed.setVisibility(View.VISIBLE);
                    closePanel.setVisibility(View.GONE);

                }
                updateMapPadding(v);

            }

            @Override
            public void onPanelCollapsed(View view) {
                listHeader.setVisibility(View.GONE);
                map.animateCamera(CameraUpdateFactory.zoomTo(lastZoom));
            }

            @Override
            public void onPanelExpanded(View view) {
            }

            @Override
            public void onPanelAnchored(View view) {
                updateMapPadding(slidingPanel.getAnchorPoint());
            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
    }

    private void updateMapPadding(float v) {
        if (mapFragment.getView() != null)
            map.setPadding(0, 0, 0, (int) (mapFragment.getView().getHeight() * v));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mapFragment).commit();
        }
        myId = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).getLong("id", 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            mapFragment.getMapAsync(this);
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .build();
        }
        getActivity().sendBroadcast(new Intent("com.akraft.muna.action.DISABLE_MARKS_DETECTOR"));
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().sendBroadcast(new Intent("com.akraft.muna.action.START_MARKS_DETECTOR"));
        mGoogleApiClient.disconnect();
    }


    private void openMarkInfo(Mark mark, boolean setBackStack) {
        isWaitingForLocation = false;
        lastZoom = map.getCameraPosition().zoom;
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        closePanel.setVisibility(View.GONE);
        currentMarker = getMarkMarker(mark);
        if (currentMarker != null) {
            updateMapPadding(ANCHOR_POINT);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), ZOOM_MARK));
        }
        markInfoFragment = new MarkInfoFragment();
        markInfoFragment.setSuccessListener(this);
        Bundle bundle = new Bundle();
        bundle.putParcelable("mark", mark);
        markInfoFragment.setArguments(bundle);
        markInfoFragment.setMarkInfoInteraction(this);

        getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.panel_container, markInfoFragment);
        if (setBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    @OnClick(R.id.count_mark_quick)
    public void countMarkQuickClick() {
        if (map.getMyLocation() == null) {
            isWaitingForLocation = true;
            panelCollapsed.setDisplayedChild(1);
        } else {
            showMarksNearby(map.getMyLocation());
        }

    }

    private void showMarksNearby(Location location) {
        updateMapPadding(ANCHOR_POINT);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_NEARBY));
        listHeader.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putInt("type", MarksListFragment.NEARBY);

        bundle.putDouble("lat", location.getLatitude());
        bundle.putDouble("lon", location.getLongitude());
        final MarksListFragment marksListFragment = new MarksListFragment();
        marksListFragment.setArguments(bundle);
        marksListFragment.setOnItemClickListener(new MarksAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                openMarkInfo(marksListFragment.getMarksList().get(position), true);
            }
        });
        marksListFragment.setOnMarksLoadedListener(new MarksListFragment.OnMarksLoadedListener() {

            @Override
            public void onFinishLoading(boolean empty) {
                if (!empty) {
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    closePanel.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), R.string.no_marks_nearby, Toast.LENGTH_SHORT).show();
                    panelCollapsed.setDisplayedChild(0);
                }
            }
        });

        getChildFragmentManager().beginTransaction().replace(R.id.panel_container, marksListFragment).commit();
    }


    private void loadMarks() {
        ServiceManager.getInstance().service.getActiveMarksList(new Callback<ArrayList<Mark>>() {
            @Override
            public void success(ArrayList<Mark> objects, Response response) {
                marks.addAll(objects);
                // saveMarks(objects);
                createMarkers(objects);

                if (mark != null)
                    openMarkInfo(mark, false);
            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_collected_marks", true)) {
            ServiceManager.getInstance().service.getUserMarksList(myId, new Callback<Profile>() {
                @Override
                public void success(Profile profile, Response response) {
                    ArrayList<Mark> objects = profile.getMarks();
                    marks.addAll(objects);
                    //saveMarks(objects);
                    createMarkers(objects);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    @OnClick(R.id.add_mark)
    public void addMarkClick() {
        startActivity(new Intent(getActivity(), MarkCreateActivity.class));
    }

    private void saveMarks(ArrayList<Mark> marks) {
        Mark.saveInTx(marks);
    }

    private void createMarkers(ArrayList<Mark> marks) {
        Iterator<Mark> markIterator = marks.iterator();

        while (markIterator.hasNext()) {
            Mark mark = markIterator.next();
            if (hidden.contains(mark)) {
                markIterator.remove();
                continue;
            }
            int markerIcon;
            if (mark.isActive()) {
                if (mark.getAuthor() == myId) {
                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_my_marks", true))
                        markerIcon = R.drawable.ic_map_marker_my;
                    else {
                        markIterator.remove();
                        continue;
                    }
                } else {
                    markerIcon = R.drawable.ic_map_marker;
                }
            } else {
                markerIcon = R.drawable.ic_map_marker_collected;
            }
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(mark.getLat(), mark.getLon()))
                    .icon(BitmapDescriptorFactory.fromResource(markerIcon)));
            marksData.put(marker, mark);
        }
    }


    public boolean isPanelOpened() {
        return slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED;
    }

    public void closePanel() {
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        loadMarks();
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapboxTileProvider(MapboxTileProvider.MAP_TILE_DIMENSION, MapboxTileProvider.MAP_TILE_DIMENSION)));
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentMarker = marker;
                openMarkInfo(marksData.get(marker), false);
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                checkGPS();
                return false;
            }
        });
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (isWaitingForLocation) {
                    isWaitingForLocation = false;
                    showMarksNearby(location);
                }
            }
        });
    }

    private void checkGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), 2);
                        } catch (IntentSender.SendIntentException e) {
                            return;
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getActivity(), R.string.gps_unavailable, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void markCompleted() {
        //TODO animate this!
        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_collected));
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @OnClick(R.id.close_panel)
    public void closePanelButtonClick() {
        closePanel();
    }


    private Marker getMarkMarker(Mark mark) {
        for (Map.Entry<Marker, Mark> entry : marksData.entrySet()) {
            if (entry.getValue().getId() == mark.getId())
                return entry.getKey();
        }
        return null;
    }

    public void onBackPressed() {
        if (markInfoFragment != null && markInfoFragment.getChildFragmentManager().getBackStackEntryCount() != 0) {
            markInfoFragment.getChildFragmentManager().popBackStack();
        } else {
            getChildFragmentManager().popBackStack();
        }
    }

    public boolean hasFragmentBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() != 0 || (markInfoFragment != null && markInfoFragment.getChildFragmentManager().getBackStackEntryCount() != 0);
    }

    @Override
    public void closeInfo() {
        closePanel();
    }

    @Override
    public void hideMark(Mark mark) {
        marks.remove(mark);
        Marker marker = getMarkMarker(mark);
        if (marker != null) {
            marker.remove();
            marksData.remove(marker);
        }
    }

    @Override
    public void undoHideMark(Mark mark) {
        marks.add(mark);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(mark.getLat(), mark.getLon()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
        marksData.put(marker, mark);
    }
}
