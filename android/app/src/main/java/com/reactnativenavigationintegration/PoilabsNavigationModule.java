package com.reactnativenavigationintegration;


import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoilabsNavigationModule extends ReactContextBaseJavaModule {
    PoilabsNavigationModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "PoilabsNavigationModule";
    }

    @ReactMethod
    public void startNavigation(String language) {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("language", language);
        context.startActivity(intent);
    }

     @ReactMethod
    public void navigateToStore(String storeId, String language) {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("navigateToStore", storeId);
         intent.putExtra("language", language);
        context.startActivity(intent);
    
    }

    
     @ReactMethod
    public void showStoresOnMap(ReadableArray storeIds, String language) {
         ArrayList<String> storeIdList = new ArrayList<String>();
        for (int i = 0; i < storeIds.size(); i++ ) {
            storeIdList.add(storeIds.getString(i));
        }
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putStringArrayListExtra("showStores",storeIdList);
         intent.putExtra("language", language);
        context.startActivity(intent);
    }

}

