//
//  PoilabsNavigationBridge.h
//  reactNativeNavigationIntegration
//
//  Created by Emre Kuru on 21.10.2021.
//

#ifndef PoilabsNavigationBridge_h
#define PoilabsNavigationBridge_h

#import <React/RCTBridgeModule.h>

@interface PoilabsNavigationBridge : NSObject <RCTBridgeModule>

-(void) showPointOnMap:(NSString *)storeId;
-(void) getRouteTo:(NSString *)storeId;
-(void) reInitMap;

@end
#endif /* PoilabsNavigationBridge_h */
