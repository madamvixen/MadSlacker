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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {

    public static final double INTREPID_LATI = 42.3670646;
    public static final double INTREPID_LONG = -71.0823675;
    public static final float RADIUS = 10;

    static GoogleMap googleMap;
    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
    List<Geofence> geofenceList = new ArrayList<>();

//    @BindView(R.id.statusTextView)
//    TextView Tv_PostStatus;

    public GoogleApiClient googleApiClient;
    public FetchLocationService fetchLocationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //uncomment the line below to use geoFencing
        //buildGeoFence();
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


    //Building Google API Client to access Location Services
    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    //Building GEOFENCE of 50 meters around Intrepid Labs
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


    @OnClick(R.id.postButton)
    public void postToSlack(View view) {
        Toast.makeText(this, "Posting to Slack", Toast.LENGTH_SHORT).show();

        //Call Service to connect to the slack webhook URL - HTTP Connection
        Intent postIntent = new Intent(getApplicationContext(), SlackPostService.class);
        startService(postIntent);
    }

    /**

     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //startGeoFencing();

        //start service for fetching users location
        fetchLocationService = new FetchLocationService(this, googleApiClient);
        Intent fetchLocationIntent = new Intent();
        fetchLocationService.onHandleIntent(fetchLocationIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
//        LocationServices.GeofencingApi.removeGeofences(googleApiClient,createGeoFencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
//            @Override
//            public void onResult(Status status) {
//                if(status.isSuccess()){
//                    Log.d("MadSlacker", "Geofences removed");
//                }
//            }
//        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.reconnect();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
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
        googleMap.setMyLocationEnabled(true);
    }

    public static class locationReceiver extends BroadcastReceiver {

        Bundle extras;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "UPDATELOCATION") {
                extras = intent.getExtras();
                //Distances = intent.getStringArrayListExtra("distances");
                Location myLastlocation = (Location) extras.get("newlocation");
                updateMarker(myLastlocation);

                //update the Database to generate heatmap
//                Timer _timer = new Timer("check in");
//                final HMDatabaseHandler dbh = new HMDatabaseHandler(context, null, null, 1);
//
//                _timer.scheduleAtFixedRate(new TimerTask() {
//                    @Override
//                    public void run() {
//                        checkedInLocation _checkedInLocation = new checkedInLocation(dbh.getLocationsCount(), "LocatioNinja-autocheckin",
//                                String.valueOf(myLastlocation.getLatitude()), String.valueOf(myLastlocation.getLongitude()));
//                        dbh.createHMLocation(_checkedInLocation);
//                    }
//                }, 10000, 300000);//schedule task of updating the locations every 5 mins
//            }

            }
        }
    }

    //Zooming to current location
    static boolean mapupd = false;
    public static void updateMarker(Location location) {
        Log.e("MadSlacker","updating marker...");
        if (location != null) {
            Log.e("LocatioNinja", "my Location not null");

            LatLng myPresentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            if (!mapupd) {
//                mMap.addMarker(new MarkerOptions().position(myPresentLoc).title("I am Here"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPresentLoc, 13));
                mapupd = true;
            }
//            longitudeTV.setText(String.valueOf(location.getLongitude()));
//            latitudeTV.setText(String.valueOf(location.getLatitude()));
//            updateAddress(location);
        }
        else
            Log.e("MadSlacker","Location is NULL");
    }
}
