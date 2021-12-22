//
//  PoilabsNavigationBridge.m
//  reactNativeNavigationIntegration
//
//  Created by Emre Kuru on 21.10.2021.
//

#import <Foundation/Foundation.h>
#import "PoilabsNavigationBridge.h"
#import <PoilabsNavigation/PoilabsNavigation.h>
#import "AppDelegate.h"

@implementation PoilabsNavigationBridge: NSObject


RCT_EXPORT_MODULE(PoilabsNavigationBridge);

RCT_EXPORT_METHOD(startPoilabsNavigation: (NSString *) language) {
  dispatch_async(dispatch_get_main_queue(), ^{
    AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [[PLNNavigationSettings sharedInstance] setApplicationLanguage:language];
    [appDelegate showNavigationViewController];
  });

}


RCT_EXPORT_METHOD(showStoreWith: (NSString *) storeId) {
  dispatch_async(dispatch_get_main_queue(), ^{

  });

}

@end
