package org.pytorch.demo.objectdetection;

import androidx.annotation.NonNull;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class RecyclingMarker extends Marker {
    private final ArrayList<Integer> recyclingOptions = new ArrayList<>();

    public RecyclingMarker(@NonNull MapView mapView){
        super(mapView);
    }

    public void setRecyclingOptions(int option){
        recyclingOptions.add(option);
    }

    public void setRecyclingOptions(ArrayList<Integer> options){
        recyclingOptions.addAll(options);
    }

    public ArrayList<Integer> getRecyclingOptions(){
        return new ArrayList<>(recyclingOptions);
    }
}
