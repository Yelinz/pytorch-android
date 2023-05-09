package org.pytorch.demo.objectdetection.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pytorch.demo.objectdetection.R;


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
                }
        );
        mMyLocationOverlay.runOnFirstFix(run);
        mMapView.getOverlays().add(mMyLocationOverlay);

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

        switch (type) {
            case "aluminum":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "plastic":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "glass":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "paper":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "battery":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "electronics":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "clothes":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "furniture":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "metal":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
            case "cardboard":
                new Marker(mMapView)
                        .setPosition(mMyLocationOverlay.getMyLocation());
                break;
        }

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }
}