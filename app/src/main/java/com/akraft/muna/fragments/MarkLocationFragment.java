package com.akraft.muna.fragments;


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.map.MapboxTileProvider;
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
public class MarkLocationFragment extends Fragment {

    private static final double ALLOWABLE_RADIUS_SINGLE = 75;
    private static final double ALLOWABLE_RADIUS_AREA = 200;
    private static final double AREA_CIRCLE_INITIAL_RADIUS = 150;
    private View rootView;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    OnMarkLocationChosen mCallback;
    private CameraPosition markPosition;

    private double currentAllowedRadius = ALLOWABLE_RADIUS_SINGLE;

    @InjectView(R.id.info_panel)
    RelativeLayout infoPanel;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.set_single_mark)
    RelativeLayout setSingleMarkButton;
    @InjectView(R.id.set_area_mark)
    RelativeLayout setAreaMarkButton;
    private Circle boundCircle;
    private Circle areaCircle;

    final LatLng[] myLocation = new LatLng[1];

    public interface OnMarkLocationChosen {
        public void locationGot(LatLng latLng);
    }

    public MarkLocationFragment() {
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
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    myLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation[0], 17), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            map.getUiSettings().setScrollGesturesEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            infoPanel.setVisibility(View.VISIBLE);


                            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                @Override
                                public void onCameraChange(CameraPosition cameraPosition) {
                                    double dist = Utils.calculateDistance(cameraPosition.target, myLocation[0]);
                                    if (dist > currentAllowedRadius) {
                                        map.animateCamera(CameraUpdateFactory.newLatLng(myLocation[0]));
                                    } else {
                                        markPosition = cameraPosition;
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    map.setOnMyLocationChangeListener(null);
                    drawBoundCircle(ALLOWABLE_RADIUS_SINGLE);
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mark_location, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnMarkLocationChosen) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMarkLocationChosen");
        }
    }

    @Override
    public void onDetach() {
        if (markPosition != null) {
            mCallback.locationGot(markPosition.target);
        }
        super.onDetach();
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
                        .center(myLocation[0])
                        .radius(radius)
                        .strokeWidth(2)
                        .strokeColor(getActivity().getResources().getColor(R.color.circle_stroke))
                        .fillColor(getActivity().getResources().getColor(R.color.circle_fill))
                        .zIndex(10)
        );
    }
}
