# PoilabsNavigation React Native Integration

## iOS

You should create a header file called **PoilabsNavigationBridge.h** and a Objective-C file  called **PoilabsNavigationBridge.m** with content below.

```c
#ifndef PoilabsNavigationBridge_h
#define PoilabsNavigationBridge_h

#import <React/RCTBridgeModule.h>

@interface PoilabsNavigationBridge : NSObject <RCTBridgeModule>

-(void) startPoilabsNavigation;

@end
#endif /* PoilabsNavigationBridge_h */
```

```c
#import <Foundation/Foundation.h>
#import "PoilabsNavigationBridge.h"
#import "AppDelegate.h"

@implementation PoilabsNavigationBridge: NSObject


RCT_EXPORT_MODULE(PoilabsNavigationBridge);

RCT_EXPORT_METHOD(startPoilabsNavigation) {
  dispatch_async(dispatch_get_main_queue(), ^{
    AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [appDelegate showNavigationViewController];
  });

}

@end
```

In AppDelegate.h file, you should define a method called **showNavigationViewController** 

```c
...
-(void)showNavigationViewController;
...
```

In AppDelegate.m file, implement showNavigationViewController method

```c
...
-(void)showNavigationViewController {
  UIViewController *vc = [UIStoryboard storyboardWithName:@"Main" bundle:nil].instantiateInitialViewController;
  vc.modalPresentationStyle = UIModalPresentationFullScreen;
  [self.window.rootViewController presentViewController:vc animated:true completion:nil];
}
...
```
Create a Cocoa Touch Class file named **NavigationViewController**. For implementation, **follow all steps** of native iOS documentation.

Create a **Storyboard** file named **Main**. New storyboard will start with a uiviewcontroller. Change its class to **NavigationViewController**. Add a UIView and connect it to **navigationView** of NavigationViewContoller. 

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

You should import **NativeModules** and define NavigationModule

```js
const { PoilabsNavigationModule } = NativeModules;
```
You can call PoilabsNavigation with codes below.

**iOS**

```js
NativeModules.PoilabsNavigationBridge.startPoilabsNavigation();
```
**Android**

```js
PoilabsNavigationModule.startNavigation();
```