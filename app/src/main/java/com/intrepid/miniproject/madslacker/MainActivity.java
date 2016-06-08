package com.intrepid.miniproject.madslacker;

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
import com.google.android.gms.location.LocationServices;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {

    @BindView(R.id.statusTextView) TextView Tv_PostStatus;

    public GoogleApiClient googleApiClient;
    public FetchLocationService fetchLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @OnClick(R.id.postButton)
    public void postToSlack(View view){
        Toast.makeText(this, "Posting to Slack", Toast.LENGTH_SHORT).show();

        //Call Service to connect to the slack webhook URL - HTTP Connection
        Intent postIntent = new Intent(this,SlackPostService.class);
        startService(postIntent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //start service for fetching users location
        fetchLocationService = new FetchLocationService(this, googleApiClient);
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
}
