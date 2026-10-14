# PoilabsNavigation React Native Integration

This guide shows how to add the PoiLabs Navigation map to a React Native app. It was tested with **React Native 0.87** (New Architecture, the default) on iOS and Android.

> The sample app in this repository still uses an older React Native version; for new integrations follow this README.
>
> Looking for the old guide (React Native 0.69 – 0.76, SDK 7.0.2 / CocoaPods)? It is kept in the [previous version of this README](https://github.com/poiteam/react-native-navigation-integration/blob/7f05ea2/README.md).

## Requirements

| | iOS | Android |
|---|---|---|
| PoilabsNavigation | 7.3.1 | 8.1.0 |
| Minimum OS | iOS 14.0 | Android 7.0 (API 24) |
| Installation | Swift Package Manager | Gradle (JitPack) |
| Other | | Compile SDK 35+, JDK 17 |

PoiLabs provides your **APPLICATION_ID** and **APPLICATION_SECRET_KEY**, and for Android also the **JITPACK_TOKEN** and **MAPBOX_TOKEN**. **UNIQUE_ID** must be unique per app user.

---

## iOS

### INSTALLATION

PoilabsNavigation is distributed with **Swift Package Manager (SPM)**. CocoaPods is no longer supported for the SDK; new versions are not published there. Your React Native app keeps using CocoaPods for React Native itself, and you add the SDK to the app target with SPM:

1. Open `ios/YourAppName.xcworkspace` in Xcode.
2. Select **File > Add Package Dependencies...**
3. Enter the repository URL: `https://github.com/poiteam/ios-navigation-pod.git`
4. Choose **Exact Version** `7.3.1` and add the **PoilabsNavigation** product to your **app target**.

The package contains everything the SDK needs: PoilabsNavigation, PoilabsMapView, PoilabsCommon and Mapbox Maps 11.18.0. PoilabsPositioning, PoilabsSdkAnalytics and PoilabsCore are resolved automatically. You do not need to add Mapbox or any other dependency yourself.

> The first package resolution can take a few minutes because the package contains the Mapbox frameworks.

**Note:** The SDK ships its own Mapbox Maps 11.18.0. If your app (or another React Native library) also adds Mapbox Maps, the two copies conflict.

### Migrating from CocoaPods

1. Remove `pod 'PoilabsNavigation'` from `ios/Podfile`, together with any `PoilabsCore`, `PoilabsPositioning`, `PoilabsSdkAnalytics`, `Mapbox-iOS-SDK` or `MapboxMobileEvents` lines. Also remove the `use_frameworks! :linkage => :static` line if you added it only for PoilabsNavigation.
2. Run `pod install` in the `ios` folder.
3. Add the package with Swift Package Manager as described above.

Do not keep the SDK in your `Podfile` while it is added with SPM; the same frameworks would be embedded twice.

### PRE-REQUIREMENTS

Add the following keys to your `Info.plist` file:

- **Privacy - Location Usage Description**
- **Privacy - Location When In Use Usage Description**
- **Privacy - Bluetooth Peripheral Usage Description**
- **Privacy - Bluetooth Always Usage Description**

`MGLMapboxMetricsEnabledSettingShownInApp` was needed only by the old Mapbox SDK; you can remove it.

If your app uses a corporate proxy or PAC file, add the required ATS exceptions for those proxy hostnames under `NSAppTransportSecurity > NSExceptionDomains` in your app `Info.plist`.

### USAGE

Create a Swift file named **NavigationView.swift** with the content below. This is your map view. Replace `APPLICATION_ID`, `APPLICATION_SECRET_KEY` and `UNIQUE_ID` with your values. If Xcode asks to create a bridging header, accept it.

```swift
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

  override init(frame: CGRect) {
    super.init(frame: frame)
    NotificationCenter.default.addObserver(self, selector: #selector(showPointOnMap), name: Notification.Name("showPointOnMap"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(navigateTo), name: Notification.Name("getRouteTo"), object: nil)
    initMap()
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    initMap()
  }

  func initMap() {
    let settings = PLNNavigationSettings.sharedInstance()
    settings?.applicationId = "APPLICATION_ID"
    settings?.applicationSecret = "APPLICATION_SECRET_KEY"
    settings?.navigationUniqueIdentifier = "UNIQUE_ID"
    settings?.applicationLanguage = language as String

    PLNavigationManager.sharedInstance()?.getReadyForStoreMap(completionHandler: { (error) in
      if error == nil {
        let carrierView = PLNNavigationMapView(frame: self.bounds)
        carrierView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        carrierView.awakeFromNib()
        carrierView.delegate = self
        self.currentCarrier = carrierView
        self.addSubview(carrierView)
      } else {
        print("PoiLabs SDK init failed: \(String(describing: error))")
      }
    })
  }

  deinit {
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

Create an Objective-C file named **PoilabsMapManager.m** with the content below. It connects the map view to React Native. Replace **YOUR_PROJECT_NAME** with your Xcode target name.

```objc
#import <React/RCTViewManager.h>
#import "YOUR_PROJECT_NAME-Swift.h"

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

Create a header file named **PoilabsNavigationBridge.h** and an Objective-C file named **PoilabsNavigationBridge.m** with the content below. These methods show a pin or start a route after the map is created.

```objc
#ifndef PoilabsNavigationBridge_h
#define PoilabsNavigationBridge_h

#import <React/RCTBridgeModule.h>

@interface PoilabsNavigationBridge : NSObject <RCTBridgeModule>

-(void) showPointOnMap:(NSString *)storeId;
-(void) getRouteTo:(NSString *)storeId;

@end
#endif /* PoilabsNavigationBridge_h */
```

```objc
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

---

## Android

### INSTALLATION

Add the PoiLabs and Mapbox repositories to your project level `android/build.gradle`:

```groovy
allprojects {
    repositories {
        maven {
            url "https://api.mapbox.com/downloads/v2/releases/maven"
            authentication {
                basic(BasicAuthentication)
            }
            credentials {
                username = "mapbox"
                password = project.properties["MAPBOX_TOKEN"] ?: ""
            }
        }
        maven {
            url "https://jitpack.io"
            credentials { username = project.properties["JITPACK_TOKEN"] ?: "" }
        }
    }
}

apply plugin: "com.facebook.react.rootproject"
```

Add the SDK and your PoiLabs credentials to `android/app/build.gradle`:

```groovy
android {
    // ...
    buildFeatures {
        buildConfig true
    }
    defaultConfig {
        // ...
        minSdkVersion 24
        buildConfigField "String", "POILABS_APP_ID", "\"${project.findProperty("POILABS_APP_ID") ?: ""}\""
        buildConfigField "String", "POILABS_SECRET_KEY", "\"${project.findProperty("POILABS_SECRET_KEY") ?: ""}\""
        buildConfigField "String", "POILABS_UNIQUE_ID", "\"${project.findProperty("POILABS_UNIQUE_ID") ?: ""}\""
    }
}

dependencies {
    // ...
    implementation "com.github.poiteam:Android-Navigation-SDK:8.1.0"
}
```

Add the values to `android/gradle.properties` (or to `~/.gradle/gradle.properties` to keep them out of version control):

```properties
JITPACK_TOKEN=YOUR_JITPACK_TOKEN
MAPBOX_TOKEN=YOUR_MAPBOX_TOKEN
POILABS_APP_ID=APPLICATION_ID
POILABS_SECRET_KEY=APPLICATION_SECRET_KEY
POILABS_UNIQUE_ID=UNIQUE_ID
```

The SDK adds its location and Bluetooth permissions to your manifest and ships its own ProGuard rules. Ask for the location and Bluetooth runtime permissions before opening the map.

### USAGE

All files below go into `android/app/src/main/java/com/yourappname/`. Replace `com.yourappname` with your package name.

Create **PoilabsPackage.kt**:

```kotlin
package com.yourappname

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class PoilabsPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
        listOf(PoiMapModule(reactContext))

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
        listOf(PoiMapViewManager(reactContext))
}
```

Create **PoiMapModule.kt**. It forwards the JavaScript calls to the map:

```kotlin
package com.yourappname

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.UiThreadUtil

class PoiMapModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String = "PoiMapModule"

    @ReactMethod
    fun getRouteTo(storeId: String) {
        UiThreadUtil.runOnUiThread { PoiMapFragment.current?.navigateTo(storeId) }
    }

    @ReactMethod
    fun showPointOnMap(storeIds: ReadableArray) {
        val ids = (0 until storeIds.size()).mapNotNull { storeIds.getString(it) }
        UiThreadUtil.runOnUiThread { PoiMapFragment.current?.showPoints(ids) }
    }
}
```

Create **PoiMapFragment.kt**. It starts the SDK and hosts the map:

```kotlin
package com.yourappname

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.poilabs.navigation.model.PoiNavigation
import com.poilabs.navigation.model.PoiSdkConfig
import com.poilabs.navigation.view.fragments.MapFragment
import com.poilabs.poilabspositioning.model.PLPStatus
import java.lang.ref.WeakReference

class PoiMapFragment : Fragment() {

    private var isStoresReady = false
    private var mapFragment: MapFragment? = null
    private var pendingRouteStoreId: String? = null
    private var pendingShowOnMapStoreIds: List<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_poi_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentRef = WeakReference(this)
        pendingRouteStoreId = arguments?.getString(ARG_ROUTE_STORE_ID)
        pendingShowOnMapStoreIds = arguments?.getString(ARG_SHOW_ON_MAP_STORE_ID)?.let { listOf(it) }
        startNavigation(arguments?.getString(ARG_LANGUAGE) ?: "en")
    }

    override fun onDestroyView() {
        if (currentRef?.get() === this) currentRef = null
        val fm = activity?.supportFragmentManager
        mapFragment?.let { if (fm != null && !fm.isDestroyed) fm.beginTransaction().remove(it).commitNowAllowingStateLoss() }
        mapFragment = null
        super.onDestroyView()
    }

    fun navigateTo(storeId: String) {
        if (isStoresReady) PoiNavigation.getInstance().navigateToStore(storeId)
        else pendingRouteStoreId = storeId
    }

    fun showPoints(storeIds: List<String>) {
        if (isStoresReady) PoiNavigation.getInstance().showPointsOnMap(storeIds)
        else pendingShowOnMapStoreIds = storeIds
    }

    private fun startNavigation(language: String) {
        val config = PoiSdkConfig(
            BuildConfig.POILABS_APP_ID,
            BuildConfig.POILABS_SECRET_KEY,
            BuildConfig.POILABS_UNIQUE_ID
        )
        PoiNavigation.getInstance(requireContext(), language, config)
            .bind(object : PoiNavigation.OnNavigationReady {
                override fun onReady(mapFragment: MapFragment) {
                    if (!isAdded) return
                    // Add MapFragment to the activity's FragmentManager (not childFragmentManager):
                    // the SDK's category and search screens look it up there.
                    this@PoiMapFragment.mapFragment = mapFragment
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mapLayout, mapFragment)
                        .commitAllowingStateLoss()
                }

                override fun onStoresReady() {
                    activity?.runOnUiThread {
                        isStoresReady = true
                        pendingRouteStoreId?.let {
                            pendingRouteStoreId = null
                            PoiNavigation.getInstance().navigateToStore(it)
                        } ?: pendingShowOnMapStoreIds?.let {
                            pendingShowOnMapStoreIds = null
                            PoiNavigation.getInstance().showPointsOnMap(it)
                        }
                    }
                }

                override fun onError(exception: Throwable?) {}

                override fun onStatusChanged(plpStatus: PLPStatus) {}
            })
    }

    companion object {
        private const val ARG_LANGUAGE = "language"
        private const val ARG_SHOW_ON_MAP_STORE_ID = "showOnMapStoreId"
        private const val ARG_ROUTE_STORE_ID = "getRouteStoreId"

        private var currentRef: WeakReference<PoiMapFragment>? = null
        val current: PoiMapFragment? get() = currentRef?.get()

        fun newInstance(language: String?, showOnMapStoreId: String?, getRouteStoreId: String?) =
            PoiMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LANGUAGE, language)
                    putString(ARG_SHOW_ON_MAP_STORE_ID, showOnMapStoreId)
                    putString(ARG_ROUTE_STORE_ID, getRouteStoreId)
                }
            }
    }
}
```

> `MapFragment` must be added to the **activity's** `supportFragmentManager`, not to `childFragmentManager`. The SDK's category and search screens look it up there; otherwise selecting a store from a category list does nothing.

Create **PoiMapViewManager.kt**. It creates the native view and attaches `PoiMapFragment` to it:

```kotlin
package com.yourappname

import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class PoiMapViewManager(private val reactContext: ReactApplicationContext) : ViewGroupManager<FrameLayout>() {

    private class MapProps {
        var language: String = "en"
        var showOnMapStoreId: String? = null
        var getRouteStoreId: String? = null
    }

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(context: ThemedReactContext): FrameLayout {
        val root = FrameLayout(context)
        root.id = View.generateViewId()
        root.setTag(R.id.poilabs_map_props, MapProps())
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = attachFragment(root)
            override fun onViewDetachedFromWindow(v: View) {}
        })
        return root
    }

    // Kept for apps that still dispatch the old "create" command; the map is attached automatically.
    override fun getCommandsMap(): Map<String, Int> = mapOf("create" to COMMAND_CREATE)

    override fun receiveCommand(root: FrameLayout, commandId: String, args: ReadableArray?) {
        if (commandId == "create" || commandId == COMMAND_CREATE.toString()) attachFragment(root)
    }

    @ReactProp(name = "language")
    fun setLanguage(view: FrameLayout, value: String?) {
        props(view).language = value ?: "en"
    }

    @ReactProp(name = "showPointOnMap")
    fun setShowPointOnMap(view: FrameLayout, value: String?) {
        props(view).showOnMapStoreId = value
    }

    @ReactProp(name = "getRouteTo")
    fun setGetRouteTo(view: FrameLayout, value: String?) {
        props(view).getRouteStoreId = value
    }

    override fun onDropViewInstance(view: FrameLayout) {
        super.onDropViewInstance(view)
        val activity = reactContext.currentActivity as? FragmentActivity ?: return
        val fm = activity.supportFragmentManager
        if (fm.isDestroyed) return
        fm.findFragmentByTag(tag(view))?.let { fm.beginTransaction().remove(it).commitNowAllowingStateLoss() }
    }

    private fun props(view: FrameLayout) = view.getTag(R.id.poilabs_map_props) as MapProps

    private fun tag(view: FrameLayout) = "poilabs_map_${view.id}"

    private fun attachFragment(root: FrameLayout) {
        val activity = reactContext.currentActivity as? FragmentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed || !root.isAttachedToWindow) return
        val fm = activity.supportFragmentManager
        if (fm.isDestroyed || fm.findFragmentByTag(tag(root)) != null) return
        // Props are applied right after the view is created; wait one frame so they are set.
        root.post {
            if (!root.isAttachedToWindow || fm.isDestroyed || fm.findFragmentByTag(tag(root)) != null) return@post
            val p = props(root)
            layoutChildrenEveryFrame(root)
            fm.beginTransaction()
                .replace(root.id, PoiMapFragment.newInstance(p.language, p.showOnMapStoreId, p.getRouteStoreId), tag(root))
                .commitNowAllowingStateLoss()
        }
    }

    // React Native does not lay out native child views, so the fragment's views are measured manually.
    private fun layoutChildrenEveryFrame(view: ViewGroup) {
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!view.isAttachedToWindow) return
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    child.measure(
                        View.MeasureSpec.makeMeasureSpec(view.measuredWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(view.measuredHeight, View.MeasureSpec.EXACTLY)
                    )
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
                view.viewTreeObserver.dispatchOnGlobalLayout()
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    companion object {
        const val REACT_CLASS = "PoiMapViewManager"
        private const val COMMAND_CREATE = 1
    }
}
```

Create `android/app/src/main/res/layout/fragment_poi_map.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/mapLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Create `android/app/src/main/res/values/ids.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="poilabs_map_props" type="id" />
</resources>
```

Register the package in **MainApplication.kt**:

```kotlin
override val reactHost: ReactHost by lazy {
  getDefaultReactHost(
    context = applicationContext,
    packageList =
      PackageList(this).packages.apply {
        add(PoilabsPackage())
      },
  )
}
```

### Upgrading from the old guide

- Replace the old Java files (`PoilabsPackage`, `PoiMapModule`, `PoiMapFragment`, `PoiMapViewManager`) with the Kotlin files above. `androidx.localbroadcastmanager` is no longer needed.
- You can remove the `libc++_shared.so` `afterEvaluate` workaround and the `packagingOptions { jniLibs { useLegacyPackaging true } }` block that the old guide added for Mapbox.
- You do not need to disable Fabric or Bridgeless; the map works with the New Architecture.
- Since SDK 8.0.0 `PoiNavigation.OnNavigationReady` is a Kotlin interface: `onReady(mapFragment: MapFragment)` and `onStatusChanged(plpStatus: PLPStatus)` take non-null parameters. Kotlin code that overrides them with `MapFragment?` / `PLPStatus?` reports *"overrides nothing"*.

---

## React Native

### iOS

Create a js file called **MapView.js**:

```js
// MapView.js
import { requireNativeComponent } from 'react-native';
module.exports = requireNativeComponent('PoilabsNavigationMap');
```

Then add the map:

```js
import MapView from './MapView.js';

<MapView language={"en"} style={{ flex: 1 }} />
```

To start the map by showing a point, pass a store id with `showOnMap`. To start with a route, pass it with `getRouteTo`:

```js
<MapView language={"en"} showOnMap={"store_id"} style={{ flex: 1 }} />
<MapView language={"en"} getRouteTo={"store_id"} style={{ flex: 1 }} />
```

After the map is open, you can show a point or start a route:

```js
import { NativeModules } from 'react-native';

NativeModules.PoilabsNavigationBridge.showPointOnMap("store_id");
NativeModules.PoilabsNavigationBridge.getRouteTo("store_id");
```

### Android

Create a js file called **PoiMapViewManager.js**:

```js
//PoiMapViewManager.js
import { requireNativeComponent } from 'react-native';

export const PoiMapViewManager = requireNativeComponent(
  'PoiMapViewManager'
);
```

Create a js file called **PoiMapView.js**:

```js
import React, { useEffect, useRef } from 'react';
import { UIManager, findNodeHandle } from 'react-native';

import { PoiMapViewManager } from './PoiMapViewManager';

const createFragment = (viewId) => {
  const createCommand =
    UIManager.getViewManagerConfig('PoiMapViewManager')?.Commands?.create ??
    UIManager.PoiMapViewManager?.Commands?.create;

  if (viewId == null || createCommand == null) return;

  UIManager.dispatchViewManagerCommand(viewId, createCommand.toString(), [viewId]);
};

export const PoiMapView = (props) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return (
    <PoiMapViewManager
      language={props.language}
      showPointOnMap={props.showPointOnMap}
      getRouteTo={props.getRouteTo}
      style={{ flex: 1 }}
      ref={ref}
    />
  );
};
```

> The `create` command is optional with the Kotlin `PoiMapViewManager` above (the map attaches automatically); it is kept so existing apps keep working.

Then add the map:

```js
import { PoiMapView } from './PoiMapView.js';

<PoiMapView language={"en"} />
```

To start the map by showing a point, pass a store id with `showPointOnMap`. To start with a route, pass it with `getRouteTo`:

```js
<PoiMapView language={"en"} showPointOnMap={"store_id"} />
<PoiMapView language={"en"} getRouteTo={"store_id"} />
```

After the map is open, you can show points or start a route:

```js
import { NativeModules } from 'react-native';

NativeModules.PoiMapModule.showPointOnMap(["store_id"]);
NativeModules.PoiMapModule.getRouteTo("store_id");
```

Available languages: `en`, `tr`, `hr`, `ar`, `de`, `ru`, `pl`.
