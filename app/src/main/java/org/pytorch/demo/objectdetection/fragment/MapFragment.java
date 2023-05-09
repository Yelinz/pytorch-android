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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pytorch.demo.objectdetection.R;


public class MapFragment extends AbstractMapFragment {
    @Override
    public String getSampleTitle() {
        return "Map Fragment in a view pager";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_explore, container, false);
        final ITileSource tileSource = TileSourceFactory.MAPNIK;
        mMapView = new MapView(getActivity());
        mMapView.setTileSource(tileSource);

        MyLocationNewOverlay mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()), mMapView);
        IMapController mapController = mMapView.getController();

        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        Runnable run = () -> getActivity().runOnUiThread(
                () -> {
                    mapController.setCenter(mMyLocationOverlay.getMyLocation());
                    mapController.animateTo(mMyLocationOverlay.getMyLocation(), 17d, 900L);
                }
        );
        mMyLocationOverlay.runOnFirstFix(run);
        mMapView.getOverlays().add(mMyLocationOverlay);

        // add markers

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }
}