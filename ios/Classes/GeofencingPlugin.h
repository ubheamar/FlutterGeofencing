#ifndef GeofencingPlugin_h
#define GeofencingPlugin_h

#import <Flutter/Flutter.h>

#import <CoreLocation/CoreLocation.h>

/**
 * A plugin registration callback.
 *
 * Used for registering plugins with additional instances of
 * `FlutterPluginRegistry`.
 *
 * @param registry The registry to register plugins with.
 */
typedef void (*FlutterPluginRegistrantCallback)(NSObject<FlutterPluginRegistry>* registry);
#endif


@interface GeofencingPlugin : NSObject<FlutterPlugin, CLLocationManagerDelegate>


+ (void)setPluginRegistrantCallback:(FlutterPluginRegistrantCallback)callback;


@end
