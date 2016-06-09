package com.intrepid.miniproject.madslacker;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final double INTREPID_LATI = 42.3670646;
    public static final double INTREPID_LONG = -71.0823675;
    public static final float RADIUS = 10;

    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
    List<Geofence> geofenceList = new ArrayList<>();

    @BindView(R.id.statusTextView)
    TextView Tv_PostStatus;

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
    public void postToSlack(View view){
        Toast.makeText(this, "Posting to Slack", Toast.LENGTH_SHORT).show();

        //Call Service to connect to the slack webhook URL - HTTP Connection
        Intent postIntent = new Intent(getApplicationContext(),SlackPostService.class);
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


}
