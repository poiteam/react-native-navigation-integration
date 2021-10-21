//
//  PoilabsNavigationBridge.m
//  reactNativeNavigationIntegration
//
//  Created by Emre Kuru on 21.10.2021.
//

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
