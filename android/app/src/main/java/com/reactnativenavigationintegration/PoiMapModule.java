package com.reactnativenavigationintegration;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import java.util.ArrayList;

public class PoiMapModule  extends ReactContextBaseJavaModule {
    PoiMapModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "PoiMapModule";
    }

    @ReactMethod
    public void restartMap(String language) {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent("restart-map");
        intent.putExtra("language", language);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @ReactMethod
    public void getRouteTo(String storeId) {
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent("navigate-to-store");

        intent.putExtra("store_id", storeId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    @ReactMethod
    public void showPointOnMap(ReadableArray storeIds) {
        ArrayList<String> storeIdList = new ArrayList<String>();
        for (int i = 0; i < storeIds.size(); i++ ) {
            storeIdList.add(storeIds.getString(i));
        }
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent("show-on-map");
        intent.putStringArrayListExtra("store_ids", storeIdList);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}