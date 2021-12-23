//
//  PoilabsMapManager.m
//  reactNativeNavigationIntegration
//
//  Created by Emre Kuru on 22.12.2021.
//

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
