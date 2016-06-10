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
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by malabika on 6/8/16.
 */
public class FetchLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final LatLng INTREPID_LAB = new LatLng(MainActivity.INTREPID_LATI, MainActivity.INTREPID_LONG);

    Context context;
    GoogleApiClient inServiceApiClient;
    LocationRequest fetchLocationRequest;
    Location myLocation;
    Location IntrepidLabsLocation;

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

            IntrepidLabsLocation = new Location("");
            IntrepidLabsLocation.setLongitude(INTREPID_LAB.longitude);
            IntrepidLabsLocation.setLatitude(INTREPID_LAB.latitude);
            String distToIntrepid = getStraightDistance(myLocation, IntrepidLabsLocation);

            intent.putExtra(String.valueOf(R.string.NewLocation), myLocation);
            intent.putExtra(String.valueOf(R.string.DistanceToIntrepid),distToIntrepid);
            context.sendBroadcast(intent);
        }
    }

    //Finding the straight length distance between my location and the marker
    static final double M_PI = Math.PI;

    private String getStraightDistance(Location src, Location dest)
    {
        double _lat = src.getLatitude();
        double _long = src.getLongitude();
        double _lat2 = dest.getLatitude();
        double _long2 = dest.getLongitude();

        // Convert degrees to radians
        _lat = _lat * M_PI / 180.0;
        _long = _long * M_PI / 180.0;

        _lat2 = _lat2 * M_PI / 180.0;
        _long2= _long2 * M_PI / 180.0;

        // radius of earth in metres
        double r = 6378100;

        // P
        double rho1 = r * Math.cos(_lat);
        double z1 = r * Math.sin(_lat);
        double x1 = rho1 * Math.cos(_long);
        double y1 = rho1 * Math.sin(_long);

        // Q
        double rho2 = r * Math.cos(_lat2);
        double z2 = r * Math.sin(_lat2);
        double x2 = rho2 * Math.cos(_long2);
        double y2 = rho2 * Math.sin(_long2);

        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);

        double theta = Math.acos(cos_theta);

        Log.e("MadSlacker", "in get distance "+ String.valueOf(r*theta));
        // Distance in Metres
        return String.format("%.3f",((r * theta)* 0.000621371));

    }
}
