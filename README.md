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

If your app uses a corporate proxy or PAC file, add the required ATS exceptions for those proxy hostnames under `NSAppTransportSecurity > NSExceptionDomains` in your app `Info.plist`.

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
          carrierView.searchBarBaseView?.backgroundColor = UIColor.black
          carrierView.searchCancelButton?.setTitleColor(.white, for: .normal)
          self.currentCarrier = carrierView
          self.addSubview(carrierView)
        } else {
          //show error
        }
    })
  }
  
  override func removeFromSuperview() {
    super.removeFromSuperview()
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



### Version-Specific Notes

#### PoilabsNavigation 7.1.2+

- Replace `use_frameworks!` with `use_frameworks! :linkage => :static` in your Podfile.
- Do **not** add `pod 'Mapbox-iOS-SDK', '~> 5.9'`. PoilabsNavigation 7.1.2 already includes the required Mapbox frameworks. Adding it separately will cause conflicts.
- Remove `PLNNavigationSettings.sharedInstance().mallId` — this field no longer exists in SDK 7.1.2.

---

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
    implementation 'com.github.poiteam:Android-Navigation-SDK:7.0.1'
}  
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

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(showOnMapReceiver,
                new IntentFilter("show-on-map"));

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(navigateToStoreReceiver,
                new IntentFilter("navigate-to-store"));
        
        // Call below method if all permissions granted
        // startNavigation()
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
            
           @Override
           public void onError(Throwable throwable) {

           }

           @Override
           public void onStatusChanged(PLPStatus plpStatus) {}
        });

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

### Version-Specific Notes

#### React Native 0.73+

React Native 0.73 migrated the Android template from Java to Kotlin and changed how Gradle repositories are declared. If you are on **RN 0.73 or above**, apply the following changes on top of the base setup.

**MainApplication**

Replace `MainApplication.java` with `MainApplication.kt`:

```kotlin
package com.yourappname

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader

class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    add(PoilabsPackage())
                }
            override fun getJSMainModuleName(): String = "index"
            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("c++_shared")
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) { load() }
    }
}
```

**MainActivity**

Replace `MainActivity.java` with `MainActivity.kt`:

```kotlin
package com.yourappname

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {
    override fun getMainComponentName(): String = "YourAppName"
    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
```

**app/build.gradle**

Add the Kotlin and React Native plugins, a `namespace`, and the Mapbox NDK packaging options:

~~~groovy
apply plugin: "com.android.application"
apply plugin: "org.jetbrains.kotlin.android"
apply plugin: "com.facebook.react"

react {
    debuggableVariants = []
}

android {
    namespace "com.yourappname"
    // ...
    buildFeatures {
        buildConfig true
    }
    packagingOptions {
        jniLibs {
            useLegacyPackaging true
        }
    }
}

afterEvaluate {
    tasks.matching { it.name.startsWith("merge") && it.name.endsWith("JniLibFolders") }.configureEach {
        doFirst {
            def buildDir = it.project.buildDir.absolutePath
            def abi = it.name.contains("Arm64") ? "arm64-v8a" :
                       it.name.contains("X86_64") ? "x86_64" :
                       it.name.contains("X86") ? "x86" : "armeabi-v7a"
            def src = new File("${buildDir}/intermediates/merged_native_libs/${abi}/out/lib/${abi}/libc++_shared.so")
            def dst = new File("${buildDir}/intermediates/stripped_native_libs/${abi}/out/lib/${abi}/libc++_shared.so")
            if (src.exists() && !dst.exists()) {
                dst.parentFile.mkdirs()
                src.copyTo(dst)
            }
        }
    }
}

dependencies {
    // ...
    implementation 'com.github.poiteam:Android-Navigation-SDK:7.0.1'
    implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.1.0'
    implementation 'androidx.activity:activity-ktx:1.7.0'
}
~~~

**root build.gradle**

Add the Kotlin classpath and apply the React root project plugin at the bottom of the file:

~~~groovy
buildscript {
    ext {
        kotlinVersion = "1.8.0"
        // ...
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin")
        // ...
    }
}

// add at the end of the file
apply plugin: "com.facebook.react.rootproject"
~~~

**settings.gradle**

Add this line:

~~~groovy
includeBuild("../node_modules/@react-native/gradle-plugin")
~~~

**gradle.properties**

Add the following entries. Instead of hardcoding credentials in `PoiMapFragment`, read them via `BuildConfig`:

~~~
newArchEnabled=false
hermesEnabled=true
POILABS_APP_ID=APPLICATION_ID
POILABS_SECRET_KEY=APPLICATION_SECRET_KEY
POILABS_UNIQUE_ID=UNIQUE_IDENTIFIER
~~~

In `PoiMapFragment`, use:

```Java
private String appId = BuildConfig.POILABS_APP_ID;
private String secretId = BuildConfig.POILABS_SECRET_KEY;
private String uniqueId = BuildConfig.POILABS_UNIQUE_ID;
```

**PoiMapViewManager**

The original `PoiMapViewManager` can produce blank views on RN 0.73+ due to fragment lifecycle timing. Replace `createViewInstance` and `receiveCommand` as follows:

```Java
private static final Set<Integer> initializedViewIds = ConcurrentHashMap.newKeySet();
private static final Set<Integer> createRequestedViewIds = ConcurrentHashMap.newKeySet();

@Override
public FrameLayout createViewInstance(ThemedReactContext reactContext) {
    FrameLayout frameLayout = new FrameLayout(reactContext);
    frameLayout.setId(View.generateViewId());
    frameLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(@NonNull View v) {
            tryAttachFragment(frameLayout);
        }
        @Override
        public void onViewDetachedFromWindow(@NonNull View v) {}
    });
    frameLayout.post(() -> tryAttachFragment(frameLayout));
    return frameLayout;
}

@Override
public void receiveCommand(@NonNull FrameLayout root, String commandId, @Nullable ReadableArray args) {
    super.receiveCommand(root, commandId, args);
    if (args == null || args.size() == 0) return;
    if (Integer.parseInt(commandId) == COMMAND_CREATE) {
        createRequestedViewIds.add(root.getId());
        tryAttachFragment(root);
    }
}

@Override
public void onDropViewInstance(@NonNull FrameLayout view) {
    super.onDropViewInstance(view);
    int id = view.getId();
    initializedViewIds.remove(id);
    createRequestedViewIds.remove(id);
    if (reactContext.getCurrentActivity() instanceof FragmentActivity) {
        FragmentActivity activity = (FragmentActivity) reactContext.getCurrentActivity();
        if (!activity.isFinishing() && !activity.isDestroyed()) {
            try {
                FragmentManager fm = activity.getSupportFragmentManager();
                Fragment f = fm.findFragmentByTag("poilabs_map_" + id);
                if (f != null && !fm.isDestroyed()) {
                    fm.beginTransaction().remove(f).commitNowAllowingStateLoss();
                }
            } catch (Exception ignored) {}
        }
    }
    try { PoiNavigation.getInstance().clearResources(); } catch (Exception ignored) {}
}

private void tryAttachFragment(@NonNull FrameLayout root) {
    int containerId = root.getId();
    if (containerId == View.NO_ID) return;
    if (!createRequestedViewIds.contains(containerId)) return;
    if (!(reactContext.getCurrentActivity() instanceof FragmentActivity)) return;
    FragmentActivity activity = (FragmentActivity) reactContext.getCurrentActivity();
    if (activity.isFinishing() || activity.isDestroyed()) return;
    if (!root.isAttachedToWindow()) { root.post(() -> tryAttachFragment(root)); return; }
    FragmentManager fm = activity.getSupportFragmentManager();
    if (fm.isDestroyed()) return;
    String tag = "poilabs_map_" + containerId;
    if (fm.findFragmentByTag(tag) != null || initializedViewIds.contains(containerId)) return;
    setupLayout(root);
    initializedViewIds.add(containerId);
    try {
        PoiMapFragment fragment = PoiMapFragment.newInstance(language, showOnMapStoreId, getRouteStoreId);
        fm.beginTransaction().replace(containerId, fragment, tag).commitNowAllowingStateLoss();
    } catch (Exception e) {
        initializedViewIds.remove(containerId);
    }
}
```

**PoiMapFragment**

On RN 0.73+, starting navigation immediately in `onViewCreated` can cause a crash because the fragment is not yet fully attached. Use a short delay and queue any commands that arrive before `onStoresReady`:

```Java
private boolean isStoresReady = false;
private String pendingRouteStoreId;
private ArrayList<String> pendingShowOnMapStoreIds;
private final Handler mainHandler = new Handler(Looper.getMainLooper());

@Override
public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    language = getArguments().getString("language");
    getRouteStoreId = getArguments().getString("getRouteStoreId");
    showOnMapStoreId = getArguments().getString("showOnMapStoreId");
    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(showOnMapReceiver, new IntentFilter("show-on-map"));
    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(navigateToStoreReceiver, new IntentFilter("navigate-to-store"));
    mainHandler.postDelayed(() -> { if (isAdded()) startNavigation(language); }, 300);
}

// Update your BroadcastReceivers to queue commands when map is not ready yet:
private final BroadcastReceiver showOnMapReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<String> store_ids = intent.getStringArrayListExtra("store_ids");
        if (!isStoresReady) { pendingShowOnMapStoreIds = store_ids; return; }
        requireActivity().runOnUiThread(() -> PoiNavigation.getInstance().showPointsOnMap(store_ids));
    }
};

private final BroadcastReceiver navigateToStoreReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String storeId = intent.getStringExtra("store_id");
        if (!isStoresReady) { pendingRouteStoreId = storeId; return; }
        requireActivity().runOnUiThread(() -> PoiNavigation.getInstance().navigateToStore(storeId));
    }
};

// In onStoresReady, drain the queue after handling initial props:
@Override
public void onStoresReady() {
    isStoresReady = true;
    requireActivity().runOnUiThread(() -> {
        if (getRouteStoreId != null) {
            PoiNavigation.getInstance().navigateToStore(getRouteStoreId);
        } else if (showOnMapStoreId != null) {
            PoiNavigation.getInstance().showPointsOnMap(Arrays.asList(showOnMapStoreId));
        }
        if (pendingRouteStoreId != null) {
            String id = pendingRouteStoreId; pendingRouteStoreId = null;
            PoiNavigation.getInstance().navigateToStore(id);
        } else if (pendingShowOnMapStoreIds != null) {
            ArrayList<String> ids = pendingShowOnMapStoreIds; pendingShowOnMapStoreIds = null;
            PoiNavigation.getInstance().showPointsOnMap(ids);
        }
    });
}
```

#### React Native 0.76+

These changes apply **on top of** the RN 0.73+ setup above.

**MainApplication.kt**

Replace the `SoLoader` import and init call:

```kotlin
// remove
import com.facebook.soloader.SoLoader
SoLoader.init(this, false)

// add
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
SoLoader.init(this, OpenSourceMergedSoMapping)
```

**settings.gradle**

Replace the file contents with:

~~~groovy
pluginManagement { includeBuild("../node_modules/@react-native/gradle-plugin") }
plugins { id("com.facebook.react.settings") }
extensions.configure(com.facebook.react.ReactSettingsExtension) { ex -> ex.autolinkLibrariesFromCommand() }
rootProject.name = "YourAppName"
include ":app"
includeBuild("../node_modules/@react-native/gradle-plugin")
~~~

**app/build.gradle**

Add `autolinkLibrariesWithApp()` to the `react` block:

~~~groovy
react {
    autolinkLibrariesWithApp()
    debuggableVariants = []
}
~~~

Also replace the `afterEvaluate` block with this more robust version that pulls `libc++_shared.so` directly from the Mapbox AAR:

~~~groovy
afterEvaluate {
    android.applicationVariants.all { variant ->
        def variantName = variant.name
        def capitalizedVariantName = variantName.capitalize()

        tasks.matching { it.name == "merge${capitalizedVariantName}NativeLibs" }.configureEach { task ->
            task.doLast {
                def mapboxCommonConfig = configurations.detachedConfiguration(
                    dependencies.create("com.mapbox.common:common-ndk27:24.19.0@aar")
                )
                mapboxCommonConfig.transitive = false
                def mapboxCommonArtifact = mapboxCommonConfig.singleFile

                def mergedLibRoots = [
                    file("$buildDir/intermediates/merged_native_libs/${variantName}/merge${capitalizedVariantName}NativeLibs/out/lib"),
                    file("$buildDir/intermediates/merged_native_libs/${variantName}/out/lib")
                ]

                copy {
                    from(zipTree(mapboxCommonArtifact)) {
                        include "jni/*/libc++_shared.so"
                        eachFile { fcd -> fcd.path = fcd.path.replaceFirst("^jni/", "") }
                    }
                    includeEmptyDirs = false
                    into file("$buildDir/tmp/mapboxCommonLibcxx/${variantName}")
                }

                def mapboxLibRoot = file("$buildDir/tmp/mapboxCommonLibcxx/${variantName}")
                mergedLibRoots.findAll { it.exists() }.each { mergedLibRoot ->
                    copy { from(mapboxLibRoot); into(mergedLibRoot) }
                }
            }
        }

        tasks.matching { it.name == "strip${capitalizedVariantName}DebugSymbols" }.configureEach { task ->
            task.doLast {
                def mapboxLibRoot = file("$buildDir/tmp/mapboxCommonLibcxx/${variantName}")
                if (!mapboxLibRoot.exists()) return
                def strippedLibRoots = [
                    file("$buildDir/intermediates/stripped_native_libs/${variantName}/strip${capitalizedVariantName}DebugSymbols/out/lib"),
                    file("$buildDir/intermediates/stripped_native_libs/${variantName}/out/lib")
                ]
                strippedLibRoots.findAll { it.exists() }.each { strippedLibRoot ->
                    copy { from(mapboxLibRoot); into(strippedLibRoot) }
                }
            }
        }
    }
}
~~~

**PoiMapViewManager**

Add a `removeStaleFragments` helper and call it just before committing a new fragment in `tryAttachFragment`. This prevents ghost fragments when the component remounts:

```java
private void removeStaleFragments(@NonNull FragmentManager fragmentManager, @NonNull String activeTag) {
    for (Fragment fragment : fragmentManager.getFragments()) {
        if (fragment == null || fragment.getTag() == null) continue;
        String tag = fragment.getTag();
        if (!tag.startsWith("poilabs_map_") || tag.equals(activeTag)) continue;
        try {
            fragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss();
        } catch (IllegalStateException ignored) {}
    }
}
```

Call it inside `tryAttachFragment`, just before the `fragmentManager.beginTransaction()` line:

```java
removeStaleFragments(fragmentManager, fragmentTag);
```

**PoiMapFragment**

Two additions to prevent double-registration of broadcast receivers:

```java
private boolean receiversRegistered = false;

private void registerReceiversIfNeeded() {
    if (receiversRegistered) return;
    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(showOnMapReceiver, new IntentFilter("show-on-map"));
    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(navigateToStoreReceiver, new IntentFilter("navigate-to-store"));
    receiversRegistered = true;
}

private void unregisterReceiversIfNeeded() {
    if (!receiversRegistered) return;
    LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(showOnMapReceiver);
    LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(navigateToStoreReceiver);
    receiversRegistered = false;
}
```

In `onViewCreated`, replace the direct `registerReceiver` calls with `registerReceiversIfNeeded()`.  
In `onDestroyView`, replace the direct `unregisterReceiver` calls with `unregisterReceiversIfNeeded()`.

#### New Architecture (newArchEnabled=true)

PoilabsNavigation uses a fragment-based `ViewManager` which is not Fabric-compatible. You can keep `newArchEnabled=true` (for TurboModules support) but must disable Fabric and Bridgeless explicitly.

**MainApplication.kt**

```kotlin
if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
    load(turboModulesEnabled = true, fabricEnabled = false, bridgelessEnabled = false)
}
```

**MainActivity.kt**

Pass `false` instead of `fabricEnabled`:

```kotlin
override fun createReactActivityDelegate(): ReactActivityDelegate =
    DefaultReactActivityDelegate(this, mainComponentName, false)
```

Also remove the `fabricEnabled` import:

```kotlin
// remove this line
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
```

**PoiMapViewManager.java**

Move `language`, `showOnMapStoreId`, `getRouteStoreId` out of instance fields into a per-view `MapState`. With New Architecture, props and the `create` command can arrive in a different order — per-view state ensures each view always uses its own props:

```java
private static final Map<Integer, MapState> viewStates = new ConcurrentHashMap<>();

private static class MapState {
    String language = "en";
    String showOnMapStoreId;
    String getRouteStoreId;
}

@NonNull
private MapState getState(@NonNull FrameLayout view) {
    return viewStates.computeIfAbsent(view.getId(), id -> new MapState());
}
```

Update `createViewInstance` to initialise the state for the new view:

```java
viewStates.put(frameLayout.getId(), new MapState());
```

Update `onDropViewInstance` to clean it up:

```java
viewStates.remove(droppedViewId);
```

Update each `@ReactProp` setter to write into the per-view state and retry attachment:

```java
@ReactProp(name = "language")
public void setLanguage(FrameLayout view, String value) {
    getState(view).language = value == null ? "en" : value;
    tryAttachFragment(view, "setLanguage");
}

@ReactProp(name = "showPointOnMap")
public void setShowPointOnMap(FrameLayout view, String value) {
    getState(view).showOnMapStoreId = value;
    tryAttachFragment(view, "setShowPointOnMap");
}

@ReactProp(name = "getRouteTo")
public void setGetRouteTo(FrameLayout view, String value) {
    getState(view).getRouteStoreId = value;
    tryAttachFragment(view, "setGetRouteTo");
}
```

Finally, update `tryAttachFragment` to read from the state map instead of instance fields when creating the fragment:

```java
MapState state = getState(root);
final PoiMapFragment poiMapFragment =
    PoiMapFragment.newInstance(state.language, state.showOnMapStoreId, state.getRouteStoreId);
```

---

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

export const PoiMapView = (props) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return (
    <PoiMapViewManager
    language = {props.language}
    showPointOnMap = {props.showPointOnMap}
    getRouteTo = {props.getRouteTo}
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

