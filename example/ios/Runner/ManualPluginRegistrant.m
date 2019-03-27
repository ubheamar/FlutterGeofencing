//
//  Generated file. Do not edit.
//

#import "ManualPluginRegistrant.h"
#import <flutter_local_notifications/FlutterLocalNotificationsPlugin.h>
#import <geofencing/GeofencingPlugin.h>

@implementation ManualPluginRegistrant

+ (void)registerWithRegistry:(NSObject<FlutterPluginRegistry>*)registry {
    if([registry hasPlugin:@"FlutterLocalNotificationsPlugin"]){
        
    }else{
            [FlutterLocalNotificationsPlugin registerWithRegistrar:[registry registrarForPlugin:@"FlutterLocalNotificationsPlugin"]];
    }
     if([registry hasPlugin:@"GeofencingPlugin"]){
     }else{
  [GeofencingPlugin registerWithRegistrar:[registry registrarForPlugin:@"GeofencingPlugin"]];
     }
}

@end
