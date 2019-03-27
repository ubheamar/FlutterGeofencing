

#ifndef ManualPluginRegistrant_h
#define ManualPluginRegistrant_h

#import <Flutter/Flutter.h>

@interface ManualPluginRegistrant : NSObject
+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry;
@end

#endif 
