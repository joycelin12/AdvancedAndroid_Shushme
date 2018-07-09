package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joycelin12 on 7/8/18.
 */

public class Geofencing implements ResultCallback {

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencingPendingIntent;

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours


    public Geofencing(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
        mGeofencingPendingIntent = null;
        mGeofenceList = new ArrayList<>();

    }

    public void registerAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try{
            LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencingPendingIntent()
            ).setResultCallback(this);

        } catch (SecurityException securityException) {
            Log.e(TAG, securityException.getMessage());

        }
    }

    public void unRegisterAllGeofences() {
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencingPendingIntent()
            ).setResultCallback(this);

        } catch(SecurityException securityException){
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void updateGeofencesList(PlaceBuffer places){
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0) return;
        for (Place place: places) {

            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);

        }

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencingPendingIntent() {
        if(mGeofencingPendingIntent != null) {
            return mGeofencingPendingIntent;

        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencingPendingIntent = PendingIntent.getBroadcast(mContext,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencingPendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing geofence : %s",
                result.getStatus().toString()));

    }
}
