package org.pytorch.demo.objectdetection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.pytorch.demo.objectdetection.R;
import org.pytorch.demo.objectdetection.Recyclables;
import org.pytorch.demo.objectdetection.RecyclingMarker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MapFragment extends AbstractMapFragment {
    private ArrayList<RecyclingMarker> mapMarkers = new ArrayList<>();

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
        Runnable run = getRunnable(mMyLocationOverlay, mapController);
        mMyLocationOverlay.runOnFirstFix(run);
        mMapView.getOverlays().add(mMyLocationOverlay);

        extractMarkersFromCsvSource();

        mapMarkers.forEach(i -> mMapView.getOverlays().add(i));

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                View popupView = inflater.inflate(R.layout.add_marker_popup, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupView.findViewById(R.id.button_confirm).setOnClickListener(v -> {
                    String name = Stream.of((TextInputEditText) popupView.findViewById(R.id.marker_name))
                            .map(editText -> editText != null ? editText.getText() : null)
                            .collect(Collectors.joining());
                    String description = Stream.of((TextInputEditText) popupView.findViewById(R.id.marker_description))
                            .map(editText -> editText != null ? editText.getText() : null)
                            .collect(Collectors.joining());
                    ArrayList<Integer> recyclables = new ArrayList<>();
                    if (Stream.of((CheckBox) popupView.findViewById(R.id.aluminum_checkbox))
                            .map(CheckBox::isChecked)
                            .findFirst()
                            .orElse(false)){
                        recyclables.add(Recyclables.ALUMINUM.getIndex());
                    }
                    if (Stream.of((CheckBox) popupView.findViewById(R.id.cardboard_checkbox))
                            .map(CheckBox::isChecked)
                            .findFirst()
                            .orElse(false)){
                        recyclables.add(Recyclables.CARDBOARD.getIndex());
                    }

                    if (!name.isEmpty() && !recyclables.isEmpty()){
                        addMarkerToCsvSource(name, description, recyclables, p);
                        RecyclingMarker marker = new RecyclingMarker(mMapView);
                        marker.setPosition(p);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setTitle(name);
                        marker.setRecyclingOptions(recyclables);
                        if (!description.isEmpty()){
                            marker.setSubDescription(description);
                        }
                        mMapView.getOverlays().add(marker);
                        mMapView.invalidate();
                    }
                    popupWindow.dismiss();
                });
                popupView.findViewById(R.id.button_cancel).setOnClickListener(v -> popupWindow.dismiss());
                popupWindow.showAtLocation(mMapView, Gravity.CENTER, 0, 0);
                return false;
            }
        };
        MapEventsOverlay mMapEventsOverlay = new MapEventsOverlay(receiver);
        mMapView.getOverlays().add(mMapEventsOverlay);
        mMapView.invalidate();

        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        return root;
    }

    @NonNull
    private Runnable getRunnable(MyLocationNewOverlay mMyLocationOverlay, IMapController mapController) {
        Runnable run = () -> requireActivity().runOnUiThread(
                () -> {
                    mapController.setCenter(mMyLocationOverlay.getMyLocation());
                    mapController.animateTo(mMyLocationOverlay.getMyLocation(), 17d, 900L);

                    // add markers
                    Bundle args = getArguments();
                    if (args != null) {
                        String type = args.getString("type");
                        if (type != null) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            GeoPoint myLocation = mMyLocationOverlay.getMyLocation();
                            switch (type) {
                                case "cardboard":
                                    Road cardboard_road = getShortestRoad(myLocation, Recyclables.CARDBOARD.getIndex());
                                    if (cardboard_road != null) {
                                        Polyline roadOverlay = RoadManager.buildRoadOverlay(cardboard_road, 0x800000FF, 12.5f);
                                        mMapView.getOverlays().add(roadOverlay);
                                    }
                                    break;
                                case "aluminum":
                                    Road alu_road = getShortestRoad(myLocation, Recyclables.ALUMINUM.getIndex());
                                    if (alu_road != null) {
                                        Polyline roadOverlay = RoadManager.buildRoadOverlay(alu_road, 0x800000FF, 12.5f);
                                        mMapView.getOverlays().add(roadOverlay);
                                    }
                                    break;
                            }
                        }
                    }
                }
        );
        return run;
    }

    private void addMarkerToCsvSource(String name, String description, @NonNull ArrayList<Integer> options, GeoPoint coordinates){
        try(FileOutputStream outputStream = requireContext().openFileOutput("locations.csv", Context.MODE_APPEND)){
            String output = name +
                    ";" +
                    description +
                    ";" +
                    coordinates.getLatitude() +
                    ";" +
                    coordinates.getLongitude() +
                    ";" +
                    options +
                    "\n";
            outputStream.write(output.getBytes());
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void extractMarkersFromCsvSource(){
        try (FileInputStream inputStream = requireContext().openFileInput("locations.csv")){
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);
            String DELIMITER = ";";
            String line;
            while ((line = br.readLine()) != null){
                String[] columns = line.split(DELIMITER);
                RecyclingMarker marker = new RecyclingMarker(mMapView);
                marker.setPosition(new GeoPoint(Double.parseDouble(columns[2]), Double.parseDouble(columns[3])));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(columns[0]);
                marker.setSubDescription(columns[1]);
                ArrayList<Integer> recycle_options = new ArrayList<>();
                String listString = columns[4].substring(1, columns[4].length() - 1);
                for (String token : listString.split(",")) {
                    recycle_options.add(Integer.valueOf(token.trim()));
                }
                marker.setRecyclingOptions(recycle_options);
                mapMarkers.add(marker);
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private Road getShortestRoad(GeoPoint currentLocation, int recyclable_index){
        OSRMRoadManager roadManager = new OSRMRoadManager(requireContext(), "OBP_Tuto/1.0");
        roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT);
        return mapMarkers.stream()
                .filter(i -> i.getRecyclingOptions().contains(recyclable_index))
                .map(RecyclingMarker::getPosition)
                .map(i -> roadManager.getRoad(new ArrayList<>(Arrays.asList(currentLocation, i))))
                .min(Comparator.comparingDouble(i -> i.mDuration))
                .orElse(null);
    }
}