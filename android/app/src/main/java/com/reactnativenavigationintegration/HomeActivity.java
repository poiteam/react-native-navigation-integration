package com.reactnativenavigationintegration;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.poilabs.navigation.model.PoiNavigation;
import com.poilabs.navigation.model.PoiSdkConfig;
import com.poilabs.navigation.view.fragments.MapFragment;
import com.poilabs.poilabspositioning.model.PLPStatus;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String appId="appId";
    private String secretId="secret";
    private String uniqueId="emre-react-native-android";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        askLocalPermission();

    }

    private void startNavigation(String language) {


        PoiSdkConfig poiSdkConfig = new PoiSdkConfig(
                appId,
                secretId,
                uniqueId
        );
        PoiNavigation.getInstance(
                this,
                language,
                poiSdkConfig
        ).bind(new PoiNavigation.OnNavigationReady() {
            @Override
            public void onReady(MapFragment mapFragment) {
                getSupportFragmentManager().beginTransaction().replace(R.id.mapLayout, mapFragment).commitAllowingStateLoss();
            }

            @Override
            public void onStoresReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getIntent().getStringExtra("navigateToStore")!=null) {
                            PoiNavigation.getInstance().navigateToStore(getIntent().getStringExtra("navigateToStore"));
                        }
                        if (getIntent().getStringArrayListExtra("showStores") != null) {
                            PoiNavigation.getInstance().showPointsOnMap(getIntent().getStringArrayListExtra("showStores"));
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onStatusChanged(PLPStatus plpStatus) {

            }
        });

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askLocalPermission() {
        int hasLocalPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasLocalPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        } else {
            String lan = getIntent().getStringExtra("language");
            if (lan == null) {
                lan = "en";
            }
            startNavigation(lan);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            askLocalPermission();
        }
    }
}