package org.pytorch.demo.objectdetection.fragment;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pytorch.demo.objectdetection.R;

import java.util.ArrayList;


public class MapFragment extends AbstractMapFragment {
    @Override
    public String getSampleTitle() {
        return "Map";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_explore, container, false);
        final ITileSource tileSource = TileSourceFactory.MAPNIK;
        mMapView = new MapView(getActivity());
        mMapView.setTileSource(tileSource);
        mMapView.setMultiTouchControls(true);

        MyLocationNewOverlay mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireActivity()), mMapView);
        IMapController mapController = mMapView.getController();

        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        Runnable run = () -> requireActivity().runOnUiThread(
                () -> {
                    mapController.setCenter(mMyLocationOverlay.getMyLocation());
                    mapController.animateTo(mMyLocationOverlay.getMyLocation(), 17d, 900L);

                    // Example route
                    // needs these 2 lines to work
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    OSRMRoadManager roadManager = new OSRMRoadManager(requireContext(), "OBP_Tuto/1.0");
                    roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);
                    ArrayList<GeoPoint> waypoints = new ArrayList<>();
                    waypoints.add(mMyLocationOverlay.getMyLocation()); // start point
                    GeoPoint endPoint = new GeoPoint(47.142468, 8.430340);
                    waypoints.add(endPoint);
                    Road road = roadManager.getRoad(waypoints);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    mMapView.getOverlays().add(roadOverlay);
                }
        );
        mMyLocationOverlay.runOnFirstFix(run);
        mMapView.getOverlays().add(mMyLocationOverlay);


        // Example marker at rotkreuz bahnhof
        Marker startMarker = new Marker(mMapView);
        startMarker.setPosition(new GeoPoint(47.142468, 8.430340));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Recyling Point");
        // TODO: maybe add custom icons and info windows
        // startMarker.setIcon(getResources().getDrawable(R.drawable.marker_icon));
        // startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, mMapView));
        mMapView.getOverlays().add(startMarker);


        mMapView.invalidate();

        // add markers
        Bundle args = getArguments();
        if (args == null) {
            ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
            return root;
        }
        String type = args.getString("type");
        if (type == null) {
            ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
            return root;
        }

        // TODO: mock more markers depending on type
        switch (type) {
            case "cardboard":
            case "aluminum":
                Marker aluminiumMarker = new Marker(mMapView);
                aluminiumMarker.setPosition(new GeoPoint(47.143846681, 8.4352684021));
                aluminiumMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                aluminiumMarker.setTitle("Recyling Point");
                mMapView.getOverlays().add(aluminiumMarker);
                break;
        }

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }
}