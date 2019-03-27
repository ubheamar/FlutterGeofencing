#include "AppDelegate.h"
#include "ManualPluginRegistrant.h"

#import <geofencing/GeofencingPlugin.h>

void registerPlugins(NSObject<FlutterPluginRegistry>* registry) {
    [ManualPluginRegistrant registerWithRegistry:registry];
}

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  [ManualPluginRegistrant registerWithRegistry:self];

[GeofencingPlugin setPluginRegistrantCallback:registerPlugins];

  // Override point for customization after application launch.
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

@end
