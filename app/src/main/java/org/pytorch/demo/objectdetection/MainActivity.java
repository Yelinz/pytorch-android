package org.pytorch.demo.objectdetection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.osmdroid.config.Configuration;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.demo.objectdetection.fragment.*;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    final HomeFragment homeFragment = new HomeFragment();
    final MapFragment mapFragment = new MapFragment();
    final InfoFragment infoFragment = InfoFragment.get();
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        Context ctx = getApplicationContext();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("your_preferences_name", Context.MODE_PRIVATE);
        Configuration.getInstance().load(ctx, sharedPreferences);
        /*
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
         */
        requestPermissionsIfNecessary(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle the result
                        assert result.getData() != null;
                        Bundle extras = result.getData().getExtras();
                        Bitmap capturedBitmap = (Bitmap) extras.get("data");

                        mImgScaleX = (float)capturedBitmap.getWidth() / PrePostProcessor.mInputWidth;
                        mImgScaleY = (float)capturedBitmap.getHeight() / PrePostProcessor.mInputHeight;

                        // TODO: check if 640 works
                        mIvScaleX = (capturedBitmap.getWidth() > capturedBitmap.getHeight() ? (float)640 / capturedBitmap.getWidth() : (float)640 / capturedBitmap.getHeight());
                        mIvScaleY  = (capturedBitmap.getHeight() > capturedBitmap.getWidth() ? (float)640 / capturedBitmap.getHeight() : (float)640 / capturedBitmap.getWidth());

                        mStartX = (640 - mIvScaleX * capturedBitmap.getWidth())/2;
                        mStartY = (640 -  mIvScaleY * capturedBitmap.getHeight())/2;

                        // Process the captured image
                        processCapturedImage(capturedBitmap);
                    }
                });

        try {
            // TODO: change to custom model and classes
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, homeFragment)
                    .commit();
            return true;
        }
        else if (id == R.id.nav_scan) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, homeFragment)
                    .commit();
            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            }

            return true;
        }
        else if (id == R.id.nav_map) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentFrame, mapFragment)
                    .commit();
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void processCapturedImage(Bitmap capturedBitmap) {
        // Resize the captured image
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(capturedBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);

        // Convert the resized image to a tensor
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);

        // Perform forward pass on the input tensor
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();

        // Convert the output tensor to an array of floats
        final float[] outputs = outputTensor.getDataAsFloatArray();

        // Process the output to get the results
        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        // TODO: parse results and call information fragment
        int resultClassIndex = results.stream()
                        .max(Comparator.comparingDouble(Result::getScore))
                        .orElse(new Result(-1, 0f, new Rect())).classIndex;

        infoFragment.setClassIndex(resultClassIndex);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentFrame, infoFragment)
                .commit();
    }
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = Files.newOutputStream(file.toPath())) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}