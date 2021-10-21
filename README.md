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

Create a java file named **NavigationModule** with content below.

```Java
public class NavigationModule extends ReactContextBaseJavaModule {
    NavigationModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "NavigationModule";
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

        modules.add(new NavigationModule(reactContext));

        return modules;
    }
}
```

Create a java file named **HomeActivity**. For implementation, **follow all steps** of native Android documentation.

## React Native

You should import **NativeModules** and define NavigationModule

```js
const { NavigationModule } = NativeModules;
```
You can call PoilabsNavigation with codes below.

**iOS**

```js
NativeModules.PoilabsNavigationBridge.startPoilabsNavigation();
```
**Android**

```js
NavigationModule.startNavigation();
```