// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.geofencing

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.JobIntentService
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.util.concurrent.atomic.AtomicBoolean

import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationResult
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.GEO_LOCATION_LAT_KEY
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.GEO_LOCATION_RAD_KEY
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.LAST_ATTENDANCE_KEY
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.LAST_LOCATION_LAT_KEY
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.LAST_LOCATION_LONG_KEY
import io.flutter.plugins.geofencing.GeofencingPlugin.Companion.SHARED_PREFERENCES_KEY
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LocationService : MethodCallHandler, JobIntentService() {
    private val queue = ArrayDeque<List<Any>>()
    private lateinit var mBackgroundChannel: MethodChannel
    private lateinit var mContext: Context

    companion object {
        @JvmStatic
        private val TAG = "LocationService"
        @JvmStatic
        private val JOB_ID = UUID.randomUUID().mostSignificantBits.toInt()
        @JvmStatic
        private var sBackgroundFlutterView: FlutterNativeView? = null
        @JvmStatic
        private val sServiceStarted = AtomicBoolean(false)

        @JvmStatic
        private lateinit var sPluginRegistrantCallback: PluginRegistrantCallback

        @JvmStatic
        public val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, LocationService::class.java, JOB_ID, work)
        }

        @JvmStatic
        fun setPluginRegistrant(callback: PluginRegistrantCallback) {
            sPluginRegistrantCallback = callback
        }
    }

    private fun startLocationService(context: Context) {
        synchronized(sServiceStarted) {
            mContext = context
            if (sBackgroundFlutterView == null) {
                val callbackHandle = context.getSharedPreferences(
                        GeofencingPlugin.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(GeofencingPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)

                val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find callback")
                    return
                }
                Log.i(TAG, "Starting LocationService...")
                sBackgroundFlutterView = FlutterNativeView(context, true)

                val registry = sBackgroundFlutterView!!.pluginRegistry
                sPluginRegistrantCallback.registerWith(registry)
                val args = FlutterRunArguments()
                args.bundlePath = FlutterMain.findAppBundlePath(context)
                args.entrypoint = callbackInfo.callbackName
                args.libraryPath = callbackInfo.callbackLibraryPath

                sBackgroundFlutterView!!.runFromBundle(args)
                IsolateHolderService.setBackgroundFlutterView(sBackgroundFlutterView)
            }
        }
        mBackgroundChannel = MethodChannel(sBackgroundFlutterView,
                "plugins.flutter.io/geofencing_plugin_background")
        mBackgroundChannel.setMethodCallHandler(this)
    }

   override fun onMethodCall(call: MethodCall, result: Result) {
       when(call.method) {
            "GeofencingService.initialized" -> {
                synchronized(sServiceStarted) {
                    while (!queue.isEmpty()) {
                        mBackgroundChannel.invokeMethod("", queue.remove())
                    }
                    sServiceStarted.set(true)
                }
            }
            "GeofencingService.promoteToForeground" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(Intent(mContext, IsolateHolderService::class.java))
                }
            }
            "GeofencingService.demoteToBackground" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(mContext, IsolateHolderService::class.java)
                intent.setAction(IsolateHolderService.ACTION_SHUTDOWN)
                    mContext.startForegroundService(intent)
                }
            }
            else -> result.notImplemented()
        }
        result.success(null)
    }

    override fun onCreate() {
        super.onCreate()
        startLocationService(this)
    }

    override fun onHandleWork(intent: Intent) {
        val p = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        val lastAttendanceDate = p.getString(LAST_ATTENDANCE_KEY,"")
        Log.i(TAG, "Last Attendance Date : $lastAttendanceDate")
        if(lastAttendanceDate.isNullOrEmpty() && lastAttendanceDate!= dateFormatter.format(Date())) {
        val callbackHandle = p.getLong(GeofencingPlugin.CALLBACK_HANDLE_KEY, 0)
        val result = LocationResult.extractResult(intent)
        Log.i(TAG,"Location result fetching for Location Service")
        if(result!=null){
            val location = result.lastLocation;
            Log.i(TAG,"Android location trigger : location :[ "+ location.latitude +","+location.longitude+"] ")
            p.edit().putString(LAST_LOCATION_LAT_KEY,location.latitude.toString()).apply()
            p.edit().putString(LAST_LOCATION_LONG_KEY,location.longitude.toString()).apply()
            val geoLat =  p.getString(GEO_LOCATION_LAT_KEY,"0").toDouble()
            val geoLong =  p.getString(GEO_LOCATION_LAT_KEY,"0").toDouble()
            val locationResults = FloatArray(1)
            val geoRad =  p.getString(GEO_LOCATION_RAD_KEY,"0").toDouble()
            Location.distanceBetween(geoLat,geoLong,location.latitude,location.longitude,locationResults)
                val locationList = listOf(location.latitude,
                        location.longitude)
                val geofenceUpdateList = listOf(callbackHandle,
                        listOf("mtv"),
                        locationList,
                        1)
                synchronized(sServiceStarted) {
                    if (!sServiceStarted.get()) {
                        // Queue up geofencing events while background isolate is starting
                        queue.add(geofenceUpdateList)
                    } else {
                        // Callback method name is intentionally left blank.
                        mBackgroundChannel.invokeMethod("", geofenceUpdateList)
                    }
                }
            }
        }


    }
}
