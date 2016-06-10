package com.intrepid.miniproject.madslacker;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeoFenceTransitionService extends IntentService implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {


    public GeoFenceTransitionService(){
        super("GeoFenceTransitionService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeoFenceTransitionService(String name) {
        super(name);
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

//        Log.e("MadSlacker","inside on handle intent of geofencing transition");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()){
            return;
        }
        else{
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            if((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)||(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)){
                Log.d("MadSlacker", "Entered geofence near Intrepid Labs");
                List TriggeringGeofences = geofencingEvent.getTriggeringGeofences();
                Log.d("MadSlacker"," Geofences triggered: "+ TriggeringGeofences.toString());
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
