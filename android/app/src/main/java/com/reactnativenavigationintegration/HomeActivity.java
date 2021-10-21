package com.reactnativenavigationintegration;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.poilabs.navigation.model.PoiNavigation;
import com.poilabs.navigation.view.fragments.MapFragment;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import getpoi.com.poibeaconsdk.PoiScanner;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String appId="appId";
    private String secretId="secret";
    private String uniqueId="emre-react-native-android";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startNavigation();

//        PoiScanner.Config config = new PoiScanner.Config(secretId,uniqueId,appId);
//        config.setEnabled(true);
//        config.setOpenSystemBluetooth(false);
//
//        PoiScanner.init(config,this);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            askLocalPermission();
//        } else {
//            PoiScanner.startScan(this);
//        }

    }

    private void startNavigation() {
        PoiNavigation poiNavigation = new PoiNavigation();
        poiNavigation.init(this, appId, secretId, uniqueId, "tr", false);
        poiNavigation.bind((PoiNavigation.OnNavigationReady)(new PoiNavigation.OnNavigationReady() {
            public void onReady(@Nullable MapFragment mapFragment) {
                if (mapFragment != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("storeId", "AVB1160");
                    mapFragment.setArguments(bundle);
                    HomeActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.mapLayout, (Fragment)mapFragment).commitAllowingStateLoss();
                }
            }
        }));
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askLocalPermission() {
        int hasLocalPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasLocalPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        } else {
            PoiScanner.startScan(this);
        }
    }

}