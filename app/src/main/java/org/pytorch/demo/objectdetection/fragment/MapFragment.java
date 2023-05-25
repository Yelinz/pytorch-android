package org.pytorch.demo.objectdetection.fragment;

import android.content.ClipData;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.ThemeUtils;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pytorch.demo.objectdetection.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.prefs.Preferences;
import java.util.stream.Stream;


public class MapFragment extends AbstractMapFragment {
    private ArrayList<OverlayItem> mapMarkers = new ArrayList<>();

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

                    // add markers
                    Bundle args = getArguments();
                    if (args != null) {
                        String type = args.getString("type");
                        if (type != null) {
                            switch (type) {
                                case "cardboard":
                                case "aluminum":
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                    StrictMode.setThreadPolicy(policy);
                                    GeoPoint myLocation = mMyLocationOverlay.getMyLocation();
                                    Road road = getShortestRoad(myLocation);
                                    if (road != null) {
                                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF, 12.5f);
                                        mMapView.getOverlays().add(roadOverlay);
                                    }
                                    break;
                            }
                        }
                    }
                }
        );
        mMyLocationOverlay.runOnFirstFix(run);
        mMapView.getOverlays().add(mMyLocationOverlay);


        this.extractMarkersFromCsvSource();
        ItemizedIconOverlay<OverlayItem> mOverlay = new ItemizedIconOverlay<>(mapMarkers,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, getActivity());

        mMapView.getOverlays().add(mOverlay);


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

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }

    private void extractMarkersFromCsvSource(){
        try (InputStreamReader inputStream = new InputStreamReader(getResources().getAssets().open("recycling_locations.csv"))){
            BufferedReader br = new BufferedReader(inputStream);
            String DELIMITER = ",";
            String line;
            while ((line = br.readLine()) != null){
                String[] columns = line.split(DELIMITER);
                mapMarkers.add(new OverlayItem(columns[0], columns[1], new GeoPoint(Double.parseDouble(columns[2]), Double.parseDouble(columns[3]))));
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private Road getShortestRoad(GeoPoint currentLocation){
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        OSRMRoadManager roadManager = new OSRMRoadManager(requireContext(), "OBP_Tuto/1.0");
        roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);
        return mapMarkers.stream()
                .map(i -> (GeoPoint) i.getPoint())
                .map(i -> roadManager.getRoad(new ArrayList<GeoPoint>(Arrays.asList(currentLocation, i))))
                .min(Comparator.comparingDouble(i -> i.mDuration))
                .orElse(null);
    }
}