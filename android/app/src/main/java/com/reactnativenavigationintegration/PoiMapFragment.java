package com.reactnativenavigationintegration;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// replace with your view's import
import com.poilabs.navigation.model.PoiNavigation;
import com.poilabs.navigation.model.PoiSdkConfig;
import com.poilabs.navigation.view.fragments.MapFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class PoiMapFragment extends Fragment {
    private String appId="APPLICATION_ID";
    private String secretId="APPLICATION_SECRET_KEY";
    private String uniqueId="UNIQUE_IDENTIFIER";
    private String language;
    private String showOnMapStoreId;
    private String getRouteStoreId;
    private boolean isStoresReady = false;

    private final BroadcastReceiver showOnMapReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<String> store_ids = intent.getStringArrayListExtra("store_ids");
            if (isStoresReady) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PoiNavigation.getInstance().showPointsOnMap(store_ids);
                    }
                });
            }
        }
    };

    private final BroadcastReceiver navigateToStoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String storeId = intent.getStringExtra("store_id");
            if (isStoresReady) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PoiNavigation.getInstance().navigateToStore(storeId);
                    }
                });
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);

        return inflater.inflate(R.layout.fragment_poi_map, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRouteStoreId = getArguments().getString("getRouteStoreId");
        showOnMapStoreId = getArguments().getString("showOnMapStoreId");
        askLocalPermission();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(showOnMapReceiver,
                new IntentFilter("show-on-map"));

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(navigateToStoreReceiver,
                new IntentFilter("navigate-to-store"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // do any logic that should happen in an `onPause` method
        // e.g.: customView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // do any logic that should happen in an `onResume` method
        // e.g.: customView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // do any logic that should happen in an `onDestroy` method
        // e.g.: customView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(showOnMapReceiver);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(navigateToStoreReceiver);
        super.onDestroyView();
    }

    public static PoiMapFragment newInstance(String language, String showOnMapStoreId, String getRouteStoreId) {
        PoiMapFragment poiMapFragment = new PoiMapFragment();

        Bundle args = new Bundle();
        args.putString("language", language);
        args.putString("showOnMapStoreId", showOnMapStoreId);
        args.putString("getRouteStoreId", getRouteStoreId);
        poiMapFragment.setArguments(args);

        return poiMapFragment;
    }

    private void startNavigation(String language) {
        PoiSdkConfig poiSdkConfig = new PoiSdkConfig(
                appId,
                secretId,
                uniqueId
        );
        PoiNavigation.getInstance(
                this.getContext(),
                language,
                poiSdkConfig
        ).bind(new PoiNavigation.OnNavigationReady() {
            @Override
            public void onReady(MapFragment mapFragment) {
                getChildFragmentManager().beginTransaction().replace(R.id.mapLayout, mapFragment).commitAllowingStateLoss();
            }

            @Override
            public void onStoresReady() {
                isStoresReady = true;
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getRouteStoreId!=null) {
                            PoiNavigation.getInstance().navigateToStore(getRouteStoreId);
                        }else if (showOnMapStoreId != null) {
                            PoiNavigation.getInstance().showPointsOnMap(Arrays.asList(showOnMapStoreId));
                        }
                    }
                });

            }
        });

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askLocalPermission() {
        int hasLocalPermission = requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasLocalPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            language = getArguments().getString("language");
            if (language == null) {
                language = "tr";
            }
            startNavigation(language);
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
