package com.intrepid.miniproject.madslacker;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,ResultCallback {

    public static final double INTREPID_LATI = 46.367513;
    public static final double INTREPID_LONG = -71.080152;
    public static final float RADIUS = 50;



    @BindView(R.id.statusTextView) TextView Tv_PostStatus;

    public GoogleApiClient googleApiClient;
    public FetchLocationService fetchLocationService;
    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
    List<Geofence> geofenceList = new ArrayList<>();
    Geofence geoFence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.buildGoogleApiClient();
        this.buildGeoFence();
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
    private void buildGeoFence(){
        geoFence = new Geofence.Builder()
                .setRequestId("intrepidlabs")
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(INTREPID_LATI,INTREPID_LONG,RADIUS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(1000)
                .build();

        geofenceList.add(geoFence);
    }

    @Override
    public void onResume(){
        super.onResume();
        geofencePendingIntent = createGeoFencePendingIntent();
        createGeoFenceRequest();

    }

    //Create GeoFencing Request to monitor the entry of device in the circular region
    private void createGeoFenceRequest(){
        geofencingRequest = new GeofencingRequest.Builder()
                .addGeofences(geofenceList)
                .build();
    }

    //Create GeoFence Pending Intent
    private PendingIntent createGeoFencePendingIntent(){

        if(geofencePendingIntent == null) {
            Intent intent = new Intent(getApplicationContext(), FetchLocationService.class);
            geofencePendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return geofencePendingIntent;
    }

    @OnClick(R.id.postButton)
    public void postToSlack(View view){
        Toast.makeText(this, "Posting to Slack", Toast.LENGTH_SHORT).show();

        //Call Service to connect to the slack webhook URL - HTTP Connection
        Intent postIntent = new Intent(getApplicationContext(),SlackPostService.class);
        startService(postIntent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationServices.GeofencingApi.addGeofences(googleApiClient,geofencingRequest,geofencePendingIntent).setResultCallback(this);

        //start service for fetching users location
        fetchLocationService = new FetchLocationService(getApplicationContext(), googleApiClient);
        Intent fetchLocationIntent = new Intent();
        fetchLocationService.onHandleIntent(fetchLocationIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.reconnect();
    }

    @Override
    public void onResult(Result result) {
        Toast.makeText(getApplicationContext(),"Added GeoFence successfully", Toast.LENGTH_SHORT).show();
    }
}
