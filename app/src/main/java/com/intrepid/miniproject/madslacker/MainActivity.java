package com.intrepid.miniproject.madslacker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {

    public static final double INTREPID_LATI = 42.3670646;
    public static final double INTREPID_LONG = -71.0823675;
    public static final float RADIUS = 10;
    public static final int ZOOM_LEVEL = 15;
    public static final LatLng INTREPID_LAB = new LatLng(INTREPID_LATI, INTREPID_LONG);

    static GoogleMap myGoogleMap;
    static boolean mapUpdate = false;
    static boolean useGeoFence = false;

    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
    List<Geofence> geofenceList = new ArrayList<>();


    public GoogleApiClient googleApiClient;
    public FetchLocationService fetchLocationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //To use geoFencing, make useGeoFence = true
        if(useGeoFence)
            buildGeoFence();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.buildGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        supportMapFragment.getMapAsync(this);
    }


    /**Building Google API Client to access Location Services
     *
     */
    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    /**GeoFence implementation of 50 meters around Intrepid
     *
     */
    public void buildGeoFence() {
        Geofence geoFence = new Geofence.Builder()
                .setRequestId("intrepidlabs")
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(INTREPID_LATI, INTREPID_LONG, RADIUS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(1000)
                .build();

        geofenceList.add(geoFence);
    }

    //Create GeoFencing Request to monitor the entry of device in the circular region
    private GeofencingRequest createGeoFenceRequest() {
        return (new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofenceList)
                .build());
    }

    //Create GeoFence Pending Intent - calls
    private PendingIntent createGeoFencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent broadcastIntent = new Intent(this, GeoFenceTransitionService.class);
        return PendingIntent.getService(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //Start monitoring of  GeoFence
    public void startGeoFencing() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(googleApiClient, createGeoFenceRequest(), createGeoFencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d("MadSlacker", "geofence creation successful");
                }
            }
        });
    }

    /** Posts to the slack channel #whos-here by invoking SlackPostService
     *
     * @param view
     */

    @OnClick(R.id.postButton)
    public void postToSlack(View view) {
        Toast.makeText(this, "Posting to Slack", Toast.LENGTH_SHORT).show();

        //Call Service to connect to the slack webhook URL - HTTP Connection
        Intent postIntent = new Intent(getApplicationContext(), SlackPostService.class);
        startService(postIntent);
    }


    /** On successful connection of GoogleApiClient start the location fetch every 15 mins,
     * invoke IntentService to handle location updates in the background
     * @param bundle
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(useGeoFence)
            startGeoFencing();

        //start service for fetching user's location
        fetchLocationService = new FetchLocationService(this, googleApiClient);
        Intent fetchLocationIntent = new Intent();
        fetchLocationService.onHandleIntent(fetchLocationIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,createGeoFencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if(status.isSuccess()){
                    Log.d("MadSlacker", "Geofences removed");
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.reconnect();
    }

    /** when Map fragment ready to View, zoom in on to current location of User
     *
     * @param googleMap
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myGoogleMap.setMyLocationEnabled(true);
        myGoogleMap.addMarker(new MarkerOptions().position(INTREPID_LAB).title("Busch Campus Center"));

    }

    /** broadcast receiver to get present location from the intent service running in the background
     * registered in AndroidManifest
     */
    public static class locationReceiver extends BroadcastReceiver {

        Bundle extras;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(String.valueOf(R.string.UpdateLocation))){
                extras = intent.getExtras();
                Location myLastlocation = (Location) extras.get(String.valueOf(R.string.NewLocation));
                updateMarker(myLastlocation);

            }
        }
    }

    //Zooming to current location

    public static void updateMarker(Location location) {
        if (location != null) {

            LatLng myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            if (!mapUpdate) {
                myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc, ZOOM_LEVEL));
                mapUpdate = true;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

}
