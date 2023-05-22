package org.pytorch.demo.objectdetection.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.pytorch.demo.objectdetection.R;

public class HomeFragment extends Fragment {
    MapFragment mapFragment = new MapFragment();
    InfoFragment infoFragment = InfoFragment.get();

    public HomeFragment(){
        // require a empty public constructor
        // TODO: add more tiles, maybe programatically
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);


        view.findViewById(R.id.card_aluminum).setOnClickListener(v -> {
            infoFragment.setClassIndex(0);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, infoFragment)
                    .commit();
        });

        view.findViewById(R.id.card_cardboard).setOnClickListener(v -> {
            infoFragment.setClassIndex(1);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, infoFragment)
                    .commit();
        });

        return view;
    }
}
