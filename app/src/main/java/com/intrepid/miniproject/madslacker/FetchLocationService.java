package com.intrepid.miniproject.madslacker;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by malabika on 6/8/16.
 */
public class FetchLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    Context context;
    GoogleApiClient inServiceApiClient;
    LocationRequest fetchLocationRequest;
    Location myLocation;

    public FetchLocationService() {
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

    public FetchLocationService(Context context, GoogleApiClient googleApiClient) {
        super(String.valueOf(context));
        this.context = context;
        inServiceApiClient = googleApiClient;
        createLocationRequest();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Redirects here when called from MainActivity
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(inServiceApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(inServiceApiClient, fetchLocationRequest, this);
        }
    }


    //Create Location Request to fetch location updates every 15 minutes
    private void createLocationRequest() {
        long interval = 5 * 1000;
        fetchLocationRequest = LocationRequest.create()
                .setInterval(interval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(inServiceApiClient);

        if (myLocation != null) {
            final Intent intent = new Intent(context, MainActivity.locationReceiver.class);
            intent.addCategory("LOCATION CHANGED");
            intent.setAction(String.valueOf(R.string.UpdateLocation));
            intent.putExtra(String.valueOf(R.string.NewLocation), myLocation);
            context.sendBroadcast(intent);
        }
    }
}
