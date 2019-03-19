package io.flutter.plugins.geofencingexample

import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.plugins.geofencing.GeofencingService
import io.flutter.plugins.geofencing.LocationService

class Application : FlutterApplication(), PluginRegistrantCallback {
  override fun onCreate() {
    super.onCreate();
    GeofencingService.setPluginRegistrant(this);
    LocationService.setPluginRegistrant(this);
  }

  override fun registerWith(registry: PluginRegistry) {
    GeneratedPluginRegistrant.registerWith(registry);
  }
}