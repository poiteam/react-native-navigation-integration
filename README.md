# PoilabsNavigation React Native Integration

This guide shows how to add the PoiLabs Navigation map to a React Native app. It was tested with **React Native 0.87** (New Architecture, the default) on iOS and Android.

If your application has more than one venue, you can open each of them with the same credentials; see [Opening a specific venue (placeId)](#opening-a-specific-venue-placeid).

> The sample app in this repository still uses an older React Native version; for new integrations follow this README.
>
> Looking for the old guide (React Native 0.69 – 0.76, SDK 7.0.2 / CocoaPods)? It is kept in the [previous version of this README](https://github.com/poiteam/react-native-navigation-integration/blob/7f05ea2/README.md).

## Requirements

| | iOS | Android |
|---|---|---|
| PoilabsNavigation | 7.3.2 | 8.1.2 |
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
4. Choose **Exact Version** `7.3.2` and add the **PoilabsNavigation** product to your **app target**.

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

Create a Swift file named **NavigationView.swift** with the content below. This is your map view. Replace `APPLICATION_ID`, `APPLICATION_SECRET_KEY` and `UNIQUE_ID` with your values.

The map opens when the view is added to the screen, with the props it has at that moment. To change the language or the venue later, render the map with a new React `key` (see [Opening a specific venue (placeId)](#opening-a-specific-venue-placeid)).

```swift
import UIKit
import PoilabsNavigation

class NavigationView: UIView {

  var currentCarrier: PLNNavigationMapView?
  private var opened = false

  @objc var language: NSString = "tr"
  /// Optional. The venue to open. Empty or nil → the venue defined for your application.
  @objc var placeId: NSString?
  @objc var showOnMap: NSString?
  @objc var getRouteTo: NSString?

  override init(frame: CGRect) {
    super.init(frame: frame)
    NotificationCenter.default.addObserver(self, selector: #selector(showPointOnMap), name: Notification.Name("showPointOnMap"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(navigateTo), name: Notification.Name("getRouteTo"), object: nil)
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  override func didMoveToWindow() {
    super.didMoveToWindow()
    guard window != nil, !opened else { return }
    opened = true
    // React Native sets the props right after creating the view; open the map on the next
    // run loop so that every prop is in place.
    DispatchQueue.main.async { [weak self] in self?.initMap() }
  }

  func initMap() {
    let settings = PLNNavigationSettings.sharedInstance()
    settings?.applicationId = "APPLICATION_ID"
    settings?.applicationSecret = "APPLICATION_SECRET_KEY"
    settings?.navigationUniqueIdentifier = "UNIQUE_ID"
    settings?.applicationLanguage = language as String
    // Set placeId every time, also to nil: the settings object is shared and keeps the last value.
    let place = (placeId as String?)?.trimmingCharacters(in: .whitespaces) ?? ""
    settings?.placeId = place.isEmpty ? nil : place

    let manager = PLNavigationManager.sharedInstance()
    // The manager is shared and still holds the previous venue's stores. Clear them so the new map
    // waits for this venue's stores instead of listing the previous venue's stores in search.
    manager?.pois = nil

    manager?.getReadyForStoreMap(completionHandler: { [weak self] (error) in
      guard let self = self, self.currentCarrier == nil else { return }
      if let error = error {
        print("PoiLabs SDK init failed: \(error)")
        return
      }
      // The view was removed before the map data loaded.
      guard self.window != nil else { return }
      let carrierView = PLNNavigationMapView(frame: self.bounds)
      carrierView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
      // Required by the SDK: PLNNavigationMapView builds its interface in awakeFromNib.
      carrierView.awakeFromNib()
      carrierView.delegate = self
      self.currentCarrier = carrierView
      self.addSubview(carrierView)
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

When React Native removes the view, the SDK's `PLNNavigationMapView` is removed with it and the SDK stops the map, positioning and Bluetooth scanning. No extra call is needed.

Create an Objective-C file named **PoilabsMapManager.m** with the content below. It connects the map view to React Native. Replace **YOUR_PROJECT_NAME** with your Xcode target name.

```objc
#import <React/RCTViewManager.h>
#import "YOUR_PROJECT_NAME-Swift.h"

@interface PoilabsMapManager : RCTViewManager
@end

@implementation PoilabsMapManager

RCT_EXPORT_MODULE(PoilabsNavigationMap)
RCT_EXPORT_VIEW_PROPERTY(language, NSString)
RCT_EXPORT_VIEW_PROPERTY(placeId, NSString)
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
            content { includeGroupByRegex "com\\.mapbox\\..*" }
        }
        maven {
            url "https://jitpack.io"
            credentials { username = project.properties["JITPACK_TOKEN"] ?: "" }
            content { includeGroupByRegex "com\\.github\\..*" }
        }
    }
}

apply plugin: "com.facebook.react.rootproject"
```

The `content` filters make Gradle ask these repositories only for the PoiLabs and Mapbox libraries. Without them Gradle looks up every library (including React Native's) there first, which can make dependency resolution very slow.

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
    implementation "com.github.poiteam:Android-Navigation-SDK:8.1.2"
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

Create **PoiMapView.kt**. It starts the SDK and hosts the map. The map opens when the view is added to the screen, with the props it has at that moment. To change the language or the venue later, render the map with a new React `key` (see [Opening a specific venue (placeId)](#opening-a-specific-venue-placeid)).

```kotlin
package com.yourappname

import android.util.Log
import android.view.Choreographer
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.uimanager.ThemedReactContext
import com.poilabs.navigation.model.PoiNavigation
import com.poilabs.navigation.model.PoiSdkConfig
import com.poilabs.navigation.view.fragments.MapFragment
import com.poilabs.poilabspositioning.model.PLPStatus
import java.lang.ref.WeakReference

/**
 * Hosts the PoiLabs map. React Native sets this view's id to its React tag; that id is the
 * container id of the map.
 */
class PoiMapView(private val reactContext: ThemedReactContext) : FrameLayout(reactContext) {

    var language: String = "en"

    /** Optional. The venue to open. Empty or null → the venue defined for your application. */
    var placeId: String? = null

    private var pendingRouteStoreId: String? = null
    private var pendingShowOnMapStoreIds: List<String>? = null
    private var storesReady = false
    private var opened = false
    private var layoutLoopRunning = false

    private val fragmentTag get() = "poilabs_map_$id"

    private val fragmentManager
        get() = (reactContext.currentActivity as? FragmentActivity)?.supportFragmentManager?.takeUnless { it.isDestroyed }

    /** Store to route to when the map is ready. */
    fun setInitialRoute(storeId: String?) {
        pendingRouteStoreId = storeId
    }

    /** Store to show when the map is ready. */
    fun setInitialPoint(storeId: String?) {
        pendingShowOnMapStoreIds = storeId?.let { listOf(it) }
    }

    fun navigateTo(storeId: String) {
        if (storesReady) PoiNavigation.getInstance().navigateToStore(storeId)
        else pendingRouteStoreId = storeId
    }

    fun showPoints(storeIds: List<String>) {
        if (storesReady) PoiNavigation.getInstance().showPointsOnMap(storeIds)
        else pendingShowOnMapStoreIds = storeIds
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startLayoutLoop()
        if (!opened) {
            opened = true
            // Open on the next loop so that every prop is in place.
            post { openMap() }
        }
    }

    /** Removes the map and stops the SDK. Called when React Native removes the view. */
    fun close() {
        if (activeMap?.get() === this) {
            activeMap = null
            PoiNavigation.getInstance().clearResources()
        }
        val fm = fragmentManager ?: return
        fm.findFragmentByTag(fragmentTag)?.let { fm.beginTransaction().remove(it).commitNowAllowingStateLoss() }
    }

    private fun openMap() {
        val activity = reactContext.currentActivity
        if (activity == null || !isAttachedToWindow) {
            Log.w(TAG, "Map not opened: the view is not on screen")
            return
        }
        activeMap = WeakReference(this)

        // The SDK is a singleton: release the previous map before opening a new one.
        PoiNavigation.getInstance().clearResources()

        val config = PoiSdkConfig(
            BuildConfig.POILABS_APP_ID,
            BuildConfig.POILABS_SECRET_KEY,
            BuildConfig.POILABS_UNIQUE_ID
        )
        // null → the venue defined for your application.
        val overridePlaceId = placeId?.trim()?.takeIf { it.isNotEmpty() }

        PoiNavigation.getInstance(activity, language, config, overridePlaceId)
            .bind(object : PoiNavigation.OnNavigationReady {
                override fun onReady(mapFragment: MapFragment) {
                    // The SDK can call this while a fragment transaction is running. Add the map
                    // with post, after the current transaction.
                    post { showMap(mapFragment) }
                }

                override fun onStoresReady() {
                    if (activeMap?.get() !== this@PoiMapView) return
                    storesReady = true
                    pendingRouteStoreId?.let {
                        pendingRouteStoreId = null
                        PoiNavigation.getInstance().navigateToStore(it)
                    } ?: pendingShowOnMapStoreIds?.let {
                        pendingShowOnMapStoreIds = null
                        PoiNavigation.getInstance().showPointsOnMap(it)
                    }
                }

                override fun onError(exception: Throwable?) {
                    Log.e(TAG, "PoiLabs SDK error", exception)
                }

                override fun onStatusChanged(plpStatus: PLPStatus) {}
            })
    }

    private fun showMap(mapFragment: MapFragment) {
        // Skip if this view was removed or another map was opened in the meantime.
        if (activeMap?.get() !== this || !isAttachedToWindow) return
        val fm = fragmentManager ?: return
        // The map must be in the activity's FragmentManager (not a child FragmentManager):
        // the SDK's search and category screens look it up there.
        fm.beginTransaction().replace(id, mapFragment, fragmentTag).commitNowAllowingStateLoss()
    }

    // React Native does not measure or lay out native child views. The map's own views change
    // size at any time, so the map is laid out on every frame while this view is on screen.
    private fun startLayoutLoop() {
        if (layoutLoopRunning) return
        layoutLoopRunning = true
        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isAttachedToWindow) {
                    layoutLoopRunning = false
                    return
                }
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    child.measure(
                        MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                    )
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        })
    }

    companion object {
        private const val TAG = "PoiMapView"

        /** The map that is using the SDK. The SDK is a singleton, so only one map is active. */
        private var activeMap: WeakReference<PoiMapView>? = null

        val current: PoiMapView? get() = activeMap?.get()
    }
}
```

Create **PoiMapViewManager.kt**. It exposes the view and its props to React Native:

```kotlin
package com.yourappname

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class PoiMapViewManager : SimpleViewManager<PoiMapView>() {

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(context: ThemedReactContext): PoiMapView = PoiMapView(context)

    @ReactProp(name = "language")
    fun setLanguage(view: PoiMapView, value: String?) {
        view.language = value ?: "en"
    }

    @ReactProp(name = "placeId")
    fun setPlaceId(view: PoiMapView, value: String?) {
        view.placeId = value
    }

    @ReactProp(name = "showPointOnMap")
    fun setShowPointOnMap(view: PoiMapView, value: String?) {
        view.setInitialPoint(value)
    }

    @ReactProp(name = "getRouteTo")
    fun setGetRouteTo(view: PoiMapView, value: String?) {
        view.setInitialRoute(value)
    }

    override fun onDropViewInstance(view: PoiMapView) {
        view.close()
        super.onDropViewInstance(view)
    }

    // Kept for apps that still dispatch the old "create" command; the map opens by itself.
    override fun getCommandsMap(): Map<String, Int> = mapOf("create" to COMMAND_CREATE)

    override fun receiveCommand(view: PoiMapView, commandId: String, args: ReadableArray?) {}

    companion object {
        const val REACT_CLASS = "PoiMapViewManager"
        private const val COMMAND_CREATE = 1
    }
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
        UiThreadUtil.runOnUiThread { PoiMapView.current?.navigateTo(storeId) }
    }

    @ReactMethod
    fun showPointOnMap(storeIds: ReadableArray) {
        val ids = (0 until storeIds.size()).mapNotNull { storeIds.getString(it) }
        UiThreadUtil.runOnUiThread { PoiMapView.current?.showPoints(ids) }
    }
}
```

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
        listOf(PoiMapViewManager())
}
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

In **MainActivity.kt**, do not restore the saved state:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(null)
}
```

The map is a fragment inside a React Native view. If Android restored it after your app was killed in the background, it would look for that view before React Native creates it, and the app would crash. The map opens again when the screen renders.

### Upgrading from the old guide

- Replace the old files with the Kotlin files above. Delete `PoiMapFragment` (Java or Kotlin), `res/layout/fragment_poi_map.xml` and `res/values/ids.xml`; they are no longer used. `androidx.localbroadcastmanager` is no longer needed.
- Add the `MainActivity.onCreate` override above and the `content` filters to the repositories.
- You can remove the `libc++_shared.so` `afterEvaluate` workaround and the `packagingOptions { jniLibs { useLegacyPackaging true } }` block that the old guide added for Mapbox.
- You do not need to disable Fabric or Bridgeless; the map works with the New Architecture.
- Since SDK 8.0.0 `PoiNavigation.OnNavigationReady` is a Kotlin interface: `onReady(mapFragment: MapFragment)` and `onStatusChanged(plpStatus: PLPStatus)` take non-null parameters. Kotlin code that overrides them with `MapFragment?` / `PLPStatus?` reports *"overrides nothing"*.
- Props are read when the map opens. If your app changes `language` while the map is on screen, also change the map's `key` (see [Opening a specific venue (placeId)](#opening-a-specific-venue-placeid)).

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
import React from 'react';

import { PoiMapViewManager } from './PoiMapViewManager';

export const PoiMapView = (props) => (
  <PoiMapViewManager
    language={props.language}
    placeId={props.placeId}
    showPointOnMap={props.showPointOnMap}
    getRouteTo={props.getRouteTo}
    style={{ flex: 1 }}
  />
);
```

The map opens by itself when it is rendered. Apps written for the old guide that still dispatch the `create` command keep working; the command does nothing.

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

---

## Opening a specific venue (placeId)

Normally the SDK opens the venue defined for your application: the configuration it loads with your **APPLICATION_ID** and **APPLICATION_SECRET_KEY** contains a **placeId**. If your application has more than one venue, you can open a different one with the same credentials by passing its `placeId`. The `placeId` **overrides** the one from the configuration. If you pass nothing, `null` or an empty string, the venue defined for your application opens, as before.

Every `placeId` you use must be enabled for your application by PoiLabs. Ask PoiLabs for the `placeId` values of your venues.

On Android, switching venues needs **Android-Navigation-SDK 8.1.1 or later**. Earlier versions cached the venue map without its `placeId`, so the next venue opened with the previous venue's map.

Pass the venue with the `placeId` prop, on both platforms:

```js
// iOS
<MapView key={placeId} language={"en"} placeId={placeId} style={{ flex: 1 }} />

// Android
<PoiMapView key={placeId} language={"en"} placeId={placeId} />
```

The native map is created once, with the props it has when it is rendered. **To switch to another venue, change the `key` together with the `placeId`.** React then removes the old map and creates a new one with the new venue. Changing only `placeId` does not reopen the map.

Example: a button for each venue, with the map below.

```js
import React, { useState } from 'react';
import { Button, Platform, View } from 'react-native';

import MapView from './MapView.js';            // iOS
import { PoiMapView } from './PoiMapView.js';  // Android

const VENUES = [
  { title: 'Default venue', placeId: '' },     // '' → the venue defined for your application
  { title: 'Venue A', placeId: 'PLACE_ID_A' },
  { title: 'Venue B', placeId: 'PLACE_ID_B' },
];

export default function VenueMap() {
  const [placeId, setPlaceId] = useState(VENUES[0].placeId);
  const key = placeId || 'default';

  return (
    <View style={{ flex: 1 }}>
      <View style={{ flexDirection: 'row' }}>
        {VENUES.map(venue => (
          <Button key={venue.title} title={venue.title} onPress={() => setPlaceId(venue.placeId)} />
        ))}
      </View>
      {Platform.OS === 'ios' ? (
        <MapView key={key} language={"en"} placeId={placeId} style={{ flex: 1 }} />
      ) : (
        <PoiMapView key={key} language={"en"} placeId={placeId} />
      )}
    </View>
  );
}
```

What the native code does with `placeId` (already included in the files above):

| | Android (`PoiMapView.kt`) | iOS (`NavigationView.swift`) |
|---|---|---|
| Release the previous map | `PoiNavigation.getInstance().clearResources()` | The old `PLNNavigationMapView` is removed together with the React view |
| Clear the previous venue's stores | Done by `clearResources()` | `PLNavigationManager.sharedInstance().pois = nil` |
| Pass the venue | `PoiNavigation.getInstance(context, language, config, placeId)` | `PLNNavigationSettings.sharedInstance().placeId = placeId` (or `nil`), then `getReadyForStoreMap` |

On iOS, `placeId` must be set before every `getReadyForStoreMap` call, also to `nil` for the default venue: the settings object is shared and otherwise keeps the venue opened last. For the same reason, set `PLNavigationManager.sharedInstance().pois` to `nil` before the call; otherwise search and categories can list the previous venue's stores.
