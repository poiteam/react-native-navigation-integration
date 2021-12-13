package com.reactnativenavigationintegration;


import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class PoilabsNavigationModule extends ReactContextBaseJavaModule {
    PoilabsNavigationModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "PoilabsNavigationModule";
    }

    @ReactMethod
    public void startNavigation() {
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}

