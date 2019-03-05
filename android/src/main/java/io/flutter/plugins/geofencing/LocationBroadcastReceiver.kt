package io.flutter.plugins.geofencing

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationResult

class LocationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        val ACTION_PROCESS_UPDATE = "io.flutter.plugin.geofencing.action.UPDATE_LOCATION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!=null){
            val action = intent.action;
            if(action.equals(ACTION_PROCESS_UPDATE)){
                val result = LocationResult.extractResult(intent)
                if(result!=null){
                    val location = result.lastLocation;
                    val locationString = StringBuilder(location.latitude.toString())
                            .append("/")
                            .append(location.longitude.toString())
                    try {
                        Log.d("LocationBroadcast",locationString.toString())
                    }catch (ex:Exception){
                        //if app is in killed mode
                        Toast.makeText(context,locationString,Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
}
