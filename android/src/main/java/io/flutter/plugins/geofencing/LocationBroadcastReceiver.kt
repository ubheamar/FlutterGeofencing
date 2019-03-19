package io.flutter.plugins.geofencing

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationResult
import io.flutter.view.FlutterMain

class LocationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        val ACTION_PROCESS_UPDATE = "io.flutter.plugin.geofencing.action.UPDATE_LOCATION"
        @JvmStatic
        private val TAG = "LocationBroadcast"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG,"Called LocationBroadcastReceiver")

        if(intent!=null && intent.action!=null) {
            if(intent.action.equals(ACTION_PROCESS_UPDATE)) {
                Log.i(TAG,"Called Location Broadcast with action ACTION_PROCESS_UPDATE")
                FlutterMain.ensureInitializationComplete(context, null)
                LocationService.enqueueWork(context, intent)
            }
        }
        /*if(intent!=null){
            val action = intent.action;
            if(action.equals(ACTION_PROCESS_UPDATE)){
                val result = LocationResult.extractResult(intent)
                if(result!=null){
                    val location = result.lastLocation;
                    val locationString = StringBuilder(location.latitude.toString())
                            .append("/")
                            .append(location.longitude.toString())
                    try {
                        Log.i("LocationBroadcast",locationString.toString())
                        FlutterMain.ensureInitializationComplete(context, null)

                    }catch (ex:Exception){
                        //if app is in killed mode
                        Toast.makeText(context,locationString,Toast.LENGTH_LONG).show()
                    }
                }

            }
        }*/
    }
}
