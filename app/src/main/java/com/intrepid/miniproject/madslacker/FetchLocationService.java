package com.intrepid.miniproject.madslacker;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malabika on 6/8/16.
 */
public class FetchLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    public static final double INTREPID_LATI = 0.0;
    public static final double INTREPID_LONG = 0.0;
    public static final float RADIUS = 50;

    Context context;
    GoogleApiClient inServiceApiClient;
    LocationRequest fetchLocationRequest;
    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
//    List<Geofence> geofenceList = new ArrayList<>();
    Geofence geoFence;

    public FetchLocationService(){
        super("FetchLocationService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchLocationService(String name) {
        super(name);
    }

    public FetchLocationService(Context context, GoogleApiClient googleApiClient){
        super(String.valueOf(context));
        this.context = context;
        inServiceApiClient = googleApiClient;
        createLocationRequest();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Redirects here when called startService from MainActivity
        Log.e("MadSlacker","In OnHandleINtent");

        LocationServices.FusedLocationApi.requestLocationUpdates(inServiceApiClient,fetchLocationRequest,this);

//        geoFence = new Geofence.Builder()
//                .setCircularRegion(INTREPID_LATI,INTREPID_LONG,RADIUS)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//                .build();

        //geofenceList.add(geoFence);
    }

    //Create Location Request to fetch location updates every 15 minutes
    private void createLocationRequest(){
        long interval = 15*60*1000;
        fetchLocationRequest = LocationRequest.create()
                                .setInterval(interval)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    //Create GeoFencing Request to monitor the entry of device in the circular region
    private void createGeoFenceRequest(){
//        geofencingRequest = new GeofencingRequest.Builder()
//                .addGeofence(geoFence)
//                .build();
    }

    //Create GeoFence Pending Intent
    private void createGeoFencePendingIntent(){

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

    @Override
    public void onLocationChanged(Location location) {
        Log.d("MadSlacker","Longitude: "+location.getLongitude());
        Log.d("MadSlacker","Latitude: "+location.getLatitude());

        //Monitor geofence entrance and exit- w.r.t Intrepid location

    }
}
