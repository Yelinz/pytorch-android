package org.pytorch.demo.objectdetection.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.pytorch.demo.objectdetection.R;
import org.pytorch.demo.objectdetection.Recyclables;

import java.util.Locale;

public class InfoFragment extends Fragment {
    private static InfoFragment INSTANCE;
    private Recyclables activeRecyclable = Recyclables.DEFAULT;

    MapFragment mapFragment = new MapFragment();

    private InfoFragment(){

    }

    public static InfoFragment get(){
        if (INSTANCE == null){
            INSTANCE = new InfoFragment();
        }
        return INSTANCE;
    }

    public void setClassIndex(int index){
        this.activeRecyclable = Recyclables.getRecyclableByIndex(index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (activeRecyclable.equals(Recyclables.DEFAULT)){
            return inflater.inflate(R.layout.no_recyclable_fragment, container, false);
        }
        View view = inflater.inflate(R.layout.information_fragment, container, false);

        ((LinearLayout) view.findViewById(R.id.info_header)).addView(this.getHeaderImage());
        ((LinearLayout) view.findViewById(R.id.info_header)).addView(this.getHeaderText());
        ((LinearLayout) view.findViewById(R.id.info_body)).addView(this.getBodyView());

        view.findViewById(R.id.take_to_station).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("type", activeRecyclable.name().toLowerCase(Locale.ROOT));
            mapFragment.setArguments(args);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, mapFragment)
                    .commit();
        });

        return view;
    }

    private ImageView getHeaderImage(){
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(activeRecyclable.getImage());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(-1, 400));
        imageView.setContentDescription(activeRecyclable.name());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return imageView;
    }

    private LinearLayout getHeaderText(){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(layoutParams);
        layout.setPadding(16, 16, 16, 16);
        TextView textView = new TextView(getActivity());
        textView.setText(activeRecyclable.getHeader());
        textView.setTextAppearance(R.style.TextAppearance_AppCompat_Title);
        layout.addView(textView, new LinearLayout.LayoutParams(-2, -2));
        return layout;
    }

    private LinearLayout getBodyView(){
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        layout.setPadding(16, 16, 16, 16);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);

        TextView whatHeader = new TextView(getActivity());
        whatHeader.setText(R.string.what_is_collected);
        whatHeader.setTextAppearance(R.style.TextAppearance_AppCompat_Title);
        TextView whatBody = new TextView(getActivity());
        whatBody.setText(activeRecyclable.getWhatRecycle());
        whatBody.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        whatBody.setPadding(24, 0, 0, 48);

        TextView whyHeader = new TextView(getActivity());
        whyHeader.setText(R.string.why_collect);
        whyHeader.setTextAppearance(R.style.TextAppearance_AppCompat_Title);
        TextView whyBody = new TextView(getActivity());
        whyBody.setText(activeRecyclable.getWhyRecycle());
        whyBody.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
        whyBody.setPadding(24, 0, 0, 48);

        layout.addView(whatHeader, layoutParams);
        layout.addView(whatBody, layoutParams);
        layout.addView(whyHeader, layoutParams);
        layout.addView(whyBody, layoutParams);

        return layout;
    }
}
