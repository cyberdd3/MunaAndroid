package com.akraft.muna.fragments;


import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.callbacks.MarkCreatingCallback;
import com.akraft.muna.map.MapboxTileProvider;
import com.akraft.muna.models.Mark;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarkChooseLocationFragment extends Fragment {

    private static final double ALLOWABLE_RADIUS_SINGLE = 75;
    private static final double ALLOWABLE_RADIUS_AREA = 200;
    private static final double AREA_CIRCLE_INITIAL_RADIUS = 150;
    private View rootView;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    MarkCreatingCallback mCallback;
    private CameraPosition markPosition;

    private double currentAllowedRadius = ALLOWABLE_RADIUS_SINGLE;

    @InjectView(R.id.info_panel)
    View infoPanel;
    @InjectView(R.id.loading)
    View loadingView;
    @InjectView(R.id.set_single_mark)
    RelativeLayout setSingleMarkButton;
    @InjectView(R.id.set_area_mark)
    RelativeLayout setAreaMarkButton;


    private Circle boundCircle;
    private Circle areaCircle;

    private LatLng myLocation;
    private Mark mark;

    public interface OnMarkLocationChosen {
        void locationGot(LatLng latLng);
    }

    public MarkChooseLocationFragment() {
        // Required empty public constructor
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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = mapFragment.getMap();
            map.setMapType(GoogleMap.MAP_TYPE_NONE);
            map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapboxTileProvider(MapboxTileProvider.MAP_TILE_DIMENSION, MapboxTileProvider.MAP_TILE_DIMENSION)));
            map.setMyLocationEnabled(true);
            map.getUiSettings().setScrollGesturesEnabled(false);
            if (mark != null && mark.getLat() != null && mark.getLon() != null) {
                setLocation(new LatLng(mark.getLat(), mark.getLon()), false);
            } else {
                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        map.setOnMyLocationChangeListener(null);
                        setLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);
                    }
                });
            }

        }
    }

    private void setLocation(final LatLng myLocation, boolean animate) {
        this.myLocation = myLocation;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 17);
        if (animate) {
            map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    finishSettingLocation();
                }

                @Override
                public void onCancel() {

                }
            });
        } else {
            map.moveCamera(cameraUpdate);
            finishSettingLocation();
        }
    }

    private void finishSettingLocation() {
        drawBoundCircle(ALLOWABLE_RADIUS_SINGLE);
        map.getUiSettings().setScrollGesturesEnabled(true);
        loadingView.setVisibility(View.GONE);
        infoPanel.setVisibility(View.VISIBLE);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                double dist = Utils.calculateDistance(cameraPosition.target, myLocation);
                if (dist > currentAllowedRadius) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(myLocation));
                } else {
                    markPosition = cameraPosition;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mark_choose_location, container, false);
        ButterKnife.inject(this, rootView);

        loadingView.setVisibility(View.VISIBLE);
        infoPanel.setVisibility(View.GONE);

        mark = getArguments().getParcelable("mark");
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (MarkCreatingCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMarkLocationChosen");
        }
    }


    @OnClick(R.id.set_single_mark)
    public void setSingleMark(View view) {
        selectButton(view);
        currentAllowedRadius = ALLOWABLE_RADIUS_SINGLE;
        redrawCircle();
        map.animateCamera(CameraUpdateFactory.zoomTo(17));
        if (areaCircle != null)
            areaCircle.setVisible(false);
    }

    private void redrawCircle() {
        drawBoundCircle(currentAllowedRadius);
    }

    @OnClick(R.id.set_area_mark)
    public void setAreaMark(View view) {
        selectButton(view);
        currentAllowedRadius = ALLOWABLE_RADIUS_AREA;
        redrawCircle();
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        drawAreaCircle();
    }

    private void selectButton(View view) {
        if (view.isSelected())
            return;
        setSingleMarkButton.setSelected(false);
        setAreaMarkButton.setSelected(false);
        view.setSelected(true);
    }

    private void drawAreaCircle() {
        if (areaCircle == null) {
            areaCircle = map.addCircle(new CircleOptions()
                            .center(map.getCameraPosition().target)
                            .radius(AREA_CIRCLE_INITIAL_RADIUS)
                            .strokeWidth(2)
                            .strokeColor(getActivity().getResources().getColor(R.color.area_circle_stroke))
                            .fillColor(getActivity().getResources().getColor(R.color.area_circle_fill))
                            .zIndex(11)
            );
        } else {
            areaCircle.setVisible(true);
            areaCircle.setCenter(map.getCameraPosition().target);
        }
    }

    private void drawBoundCircle(double radius) {
        if (boundCircle != null)
            boundCircle.remove();
        boundCircle = map.addCircle(new CircleOptions()
                        .center(myLocation)
                        .radius(radius)
                        .strokeWidth(2)
                        .strokeColor(getActivity().getResources().getColor(R.color.circle_stroke))
                        .fillColor(getActivity().getResources().getColor(R.color.circle_fill))
                        .zIndex(10)
        );
    }

    @OnClick(R.id.next)
    public void nextStep() {
        mCallback.next();
    }

    @OnClick(R.id.cancel)
    public void cancel() {mCallback.cancel();}

    @Override
    public void onDetach() {
        super.onDetach();
        if (markPosition != null)
            mCallback.locationGot(markPosition.target);
    }


}
