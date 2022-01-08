# PoilabsNavigation React Native Integration

## iOS

### INSTALLATION

To integrate PoilabsNavigation into your Xcode project using CocoaPods, specify it in your Podfile:

```curl
pod 'PoilabsNavigation'
```

### PRE-REQUIREMENTS

To Integrate this framework you should add some features to your project info.plist file.

+MGLMapboxMetricsEnabledSettingShownInApp : YES

+Privacy - Location Usage Description

+Privacy - Location When In Use Usage Description

+Privacy - Bluetooth Peripheral Usage Description

+Privacy - Bluetooth Always Usage Description

### USAGE

Create a Swift file named **NavigationView** with content below. This will be your Map View. 

Do not forget change application id and application secret key with those which are given by Poilabs.

```Swift
import UIKit
import PoilabsNavigation

class NavigationView: UIView {
  
  var currentCarrier: PLNNavigationMapView?
  
  @objc var language: NSString = "tr" {
    didSet {
      PLNNavigationSettings.sharedInstance().applicationLanguage = language as String
      if self.currentCarrier != nil {
        self.currentCarrier?.removeFromSuperview()
        initMap()
      }
    }
  }
  
  @objc var showOnMap: NSString?
  @objc var getRouteTo: NSString?
  
  //initWithFrame to init view from code
  override init(frame: CGRect) {
      super.init(frame: frame)
      initMap()
  }
  
  //initWithCode to init view from xib or storyboard
  required init?(coder aDecoder: NSCoder) {
      super.init(coder: aDecoder)
      initMap()
  }

  func initMap() {
    NotificationCenter.default.addObserver(self, selector: #selector(showPointOnMap), name: Notification.Name("showPointOnMap"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(navigateTo), name: Notification.Name("getRouteTo"), object: nil)
    
    
    PLNNavigationSettings.sharedInstance().mallId = "PLACE_TITLE"
    PLNNavigationSettings.sharedInstance().applicationId = "APPLICATION_ID"
    PLNNavigationSettings.sharedInstance().applicationSecret = "APPLICATION_SECRET_KEY"
	PLNNavigationSettings.sharedInstance().navigationUniqueIdentifier = "UNIQUE_ID"

    PLNavigationManager.sharedInstance()?.getReadyForStoreMap(completionHandler: { (error) in
      if error == nil {
          let carrierView = PLNNavigationMapView(frame: CGRect(x: 0, y: 0, width: self.bounds.size.width, height: self.bounds.size.height))
          carrierView.awakeFromNib()
          carrierView.delegate = self
          carrierView.searchBarBaseView.backgroundColor = UIColor.black
          carrierView.searchCancelButton.setTitleColor(.white, for: .normal)
          self.currentCarrier = carrierView
          self.addSubview(carrierView)
        } else {
          //show error
        }
    })
  }
  
  override func removeFromSuperview() {
    self.removeFromSuperview()
    NotificationCenter.default.removeObserver(self)
  }
  
  @objc func showPointOnMap(_ notification: Notification) {
    if let storeId = notification.userInfo?["storeId"] as? String {
      currentCarrier?.getShowonMapPin(storeId)
    }
  }
  
  @objc func navigateTo(_ notification: Notification) {
    if let storeId = notification.userInfo?["storeId"] as? String {
      currentCarrier?.navigateWithStoreId(to: storeId)
    }
  }
}

extension NavigationView: PLNNavigationMapViewDelegate {
  func childsAreReady() {
    if let storeId = showOnMap {
      currentCarrier?.getShowonMapPin(storeId as String)
    } else if let storeId = getRouteTo {
      currentCarrier?.navigateWithStoreId(to: storeId as String)
    }
  }
}
```
Create a Objective C file named **PoilabsMapManager** with content below. This will create connection between Map view and React-Native. 

Do not forget to change **YOUR_PROJECT_NAME** with your project name.

```c
// RNTMapManager.m
#import <React/RCTViewManager.h>
#import "reactNativeNavigationIntegration-Swift.h"
@interface PoilabsMapManager : RCTViewManager
@end

@implementation PoilabsMapManager

RCT_EXPORT_MODULE(PoilabsNavigationMap)
RCT_EXPORT_VIEW_PROPERTY(language, NSString)
RCT_EXPORT_VIEW_PROPERTY(showOnMap, NSString)
RCT_EXPORT_VIEW_PROPERTY(getRouteTo, NSString)

- (UIView *)view
{
  return [[NavigationView alloc] init];
}

@end
```

You should create a header file called **PoilabsNavigationBridge.h** and a Objective-C file  called **PoilabsNavigationBridge.m** with content below. 

These methods are for getting route and showing pin after you create map if necessary. 
 
```c
#ifndef PoilabsNavigationBridge_h
#define PoilabsNavigationBridge_h

#import <React/RCTBridgeModule.h>

@interface PoilabsNavigationBridge : NSObject <RCTBridgeModule>

-(void) showPointOnMap:(NSString *)storeId;
-(void) getRouteTo:(NSString *)storeId;

@end
#endif /* PoilabsNavigationBridge_h */
```

```c
#import <Foundation/Foundation.h>
#import "PoilabsNavigationBridge.h"

@implementation PoilabsNavigationBridge: NSObject


RCT_EXPORT_MODULE(PoilabsNavigationBridge);

RCT_EXPORT_METHOD(showPointOnMap:(NSString *)storeId) {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSDictionary* userInfo = @{@"storeId": storeId};
    [[NSNotificationCenter defaultCenter] postNotificationName:@"showPointOnMap" object:self userInfo:userInfo];
  });
}

RCT_EXPORT_METHOD(getRouteTo:(NSString *)storeId) {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSDictionary* userInfo = @{@"storeId": storeId};
    [[NSNotificationCenter defaultCenter] postNotificationName:@"getRouteTo" object:self userInfo:userInfo];
  });
}

@end
```



## Android

### INSTALLATION

You can download our SDK via Gradle with following below steps


*  Add jitpack and mapbox dependency to your project level build.gradle file with their tokens.
   **JITPACK_TOKEN** is a token that PoiLabs will provide for you it will allow you to download our sdk.
   **MAPBOX_TOKEN** is a token that also PoiLabs will provide for you and it will allow you to download MapBox Sdk for your app. (Maps from PoiLabs will shown on MapBox maps)

~~~groovy  
allprojects {  
	  repositories {  
		  maven {  
			  url 'https://api.mapbox.com/downloads/v2/releases/maven'  
			  authentication {  
					  basic(BasicAuthentication)  
			  }  
			  credentials {  
					  username = 'mapbox'  
					  password = project.properties['MAPBOX_TOKEN'] ?: ""  
				}  
		 } 
		  maven {  
			   url "https://jitpack.io" 
			   credentials { username = 'JITPACK_TOKEN' }  
		  }  
	  }  
}
~~~  

* Add PoiLabs Navigation SDK dependency to your app level build.gradle file

~~~groovy  
  
dependencies {  
	 implementation 'org.bitbucket.poiteam:android-navigation-sdk:v2.8.28'  
 }  
~~~ 

### PRE-REQUIREMENTS

Android Manifest file:

~~~ groovy
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />  
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
~~~


### USAGE

Create a java file named **PoilabsPackage** with content below.

```Java
import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PoilabsPackage implements ReactPackage {


    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new PoiMapModule(reactContext));
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new PoiMapViewManager(reactContext));
    }
}
```

Create a java file called **PoiMapModule** 

```Java
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
```


Open up your MainApplication.java file, which can be found in the following path: android/app/src/main/java/com/your-app-name/MainApplication.java

Locate ReactNativeHost’s getPackages() method and add your package to the packages list getPackages() returns:

```Java
@Override
  protected List<ReactPackage> getPackages() {
    @SuppressWarnings("UnnecessaryLocalVariable")
    List<ReactPackage> packages = new PackageList(this).getPackages();
    packages.add(new PoilabsPackage());
    return packages;
  }
```

Create a java file called **PoiMapFragment**

Do not forget change application id and application secret key with those which are given by Poilabs.


```Java
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
```

Create a java file called **PoiMapViewManager**

```java
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

public class PoiMapViewManager extends ViewGroupManager<FrameLayout> {

    public static final String REACT_CLASS = "PoiMapViewManager";
    public final int COMMAND_CREATE = 1;
    private String language;
    private String showOnMapStoreId;
    private String getRouteStoreId;

    ReactApplicationContext reactContext;

    public PoiMapViewManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    /**
     * Return a FrameLayout which will later hold the Fragment
     */
    @Override
    public FrameLayout createViewInstance(ThemedReactContext reactContext) {
        return new FrameLayout(reactContext);
    }

    /**
     * Map the "create" command to an integer
     */
    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("create", COMMAND_CREATE);
    }

    /**
     * Handle "create" command (called from JS) and call createFragment method
     */
    @Override
    public void receiveCommand(
            @NonNull FrameLayout root,
            String commandId,
            @Nullable ReadableArray args
    ) {
        super.receiveCommand(root, commandId, args);
        int reactNativeViewId = args.getInt(0);
        int commandIdInt = Integer.parseInt(commandId);

        switch (commandIdInt) {
            case COMMAND_CREATE:
                createFragment(root, reactNativeViewId);
                break;
            default: {
            }
        }
    }

    @ReactProp(name = "language")
    public void setLanguage(FrameLayout view, String value) {
        language = value;
    }

    @ReactProp(name = "showPointOnMap")
    public void setShowPointOnMap(FrameLayout view, String value) {
        showOnMapStoreId = value;
    }

    @ReactProp(name = "getRouteTo")
    public void setGetRouteTo(FrameLayout view, String value) {
        getRouteStoreId = value;
    }

    /**
     * Replace your React Native view with a custom fragment
     */
    public void createFragment(FrameLayout root, int reactNativeViewId) {
        ViewGroup parentView = (ViewGroup) root.findViewById(reactNativeViewId);
        setupLayout(parentView);

        final PoiMapFragment poiMapFragment = PoiMapFragment.newInstance(language, showOnMapStoreId, getRouteStoreId);
        FragmentActivity activity = (FragmentActivity) reactContext.getCurrentActivity();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(reactNativeViewId, poiMapFragment, String.valueOf(reactNativeViewId))
                .commit();
    }

    public void setupLayout(ViewGroup view) {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                manuallyLayoutChildren(view);
                view.getViewTreeObserver().dispatchOnGlobalLayout();
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    /**
     * Layout all children properly
     */
    public void manuallyLayoutChildren(ViewGroup view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            child.measure(View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
    }
}

```

## React Native

You should import **NativeModules**

### iOS

Create a js file called **MapView.js**

```js
// MapView.js
import { requireNativeComponent } from 'react-native';
module.exports = requireNativeComponent('PoilabsNavigationMap');
```

Import MapView.js to file which is using Map.

```js
import MapView from './MapView.js';
```

Then you can add MapView like below.

```js
<MapView style={{ flex: 1}}/>
```

If you want to choose language of PoilabsNavigation. You can pass "tr" or "en" by MapView. 

```js
<MapView language={"en"} style={{ flex: 1}}/>
```

If you want to start map with showing a point on map. You can pass store_id to MapView.

```js
<MapView language={"en"} showOnMap={"store_id"} style={{ flex: 1}}/>
```
If you want to start map with a route. You can pass store_id to MapView.

```js
<MapView language={"en"} getRouteTo={"store_id"} style={{ flex: 1}}/>
```

#### Post operations

You can show a point or you can get route to a point after map's initialization. 

```js
NativeModules.PoilabsNavigationBridge.showPointOnMap("store_id");
...
NativeModules.PoilabsNavigationBridge.getRouteTo("store_id");
```

### Android

Create a js file called **PoiMapViewManager.js**

```js
//PoiMapViewManager.js
import { requireNativeComponent } from 'react-native';

export const PoiMapViewManager = requireNativeComponent(
  'PoiMapViewManager'
);
```

Create a js file called **PoiMapView.js**

```js
import React, { useEffect, useRef } from 'react';
import { UIManager, findNodeHandle, PixelRatio } from 'react-native';

import { PoiMapViewManager } from './PoiMapViewManager';

const createFragment = (viewId) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    UIManager.PoiMapViewManager.Commands.create.toString(),
    [viewId]
  );

export const PoiMapView = () => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return (
    <PoiMapViewManager
      style={{
        flex: 1
      }}
      ref={ref}
    />
  );
};
```

Import PoiMapView.js to file which is using Map.

```js
import { PoiMapView } from './PoiMapView.js';

const { PoiMapModule } = NativeModules;
```

Then you can add PoiMapView like below.

```js
<PoiMapView/>
```

If you want to choose language of PoilabsNavigation. You can pass "tr" or "en" by MapView. 

```js
<PoiMapView language={"en"}/>
```

If you want to start map with showing a point on map. You can pass store_id to PoiMapView.

```js
<PoiMapView language={"en"} showPointOnMap={"store_id"}/>
```
If you want to start map with a route. You can pass store_id to PoiMapView.

```js
<PoiMapView language={"en"} getRouteTo={"store_id"}/>
```

#### Post operations

You can show a point or you can get route to a point after map's initialization. 

```js
NativeModules.PoiMapModule.showPointOnMap(["store_id"]);
...
NativeModules.PoiMapModule.getRouteTo("store_id");
```