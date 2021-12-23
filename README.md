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

Create a java file named ** PoilabsNavigationModule** with content below.

```Java
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
```

Create a java file named **NavigationPackage** with content below.

```Java
public class NavigationPackage implements ReactPackage {
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new PoilabsNavigationModule(reactContext));

        return modules;
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
    // below MyAppPackage is added to the list of packages returned
    packages.add(new NavigationPackage());
    return packages;
  }
```

Create a java file named **HomeActivity**. For implementation, **follow all steps** of native Android documentation.

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

```js
PoilabsNavigationModule.startNavigation();
```