package com.example.suvrat.helpdadministrator;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.internal.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//this class is my address lookup service
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "";

    public FetchAddressIntentService(){
        super("FetchAddressIntentService");
    }

    protected ResultReceiver mReceiver;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //Locale objects adjust the presentation of information,
        // such as numbers or dates, to suit the conventions in the region that is represented by the locale.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (intent == null) {
            return;
        }
        String errorMessage = "";

        // Get the location passed to this service(class) through an extra.
        Location location = intent.getParcelableExtra(com.example.suvrat.helpdadministrator.Constants.LOCATION_DATA_EXTRA);
        mReceiver= intent.getParcelableExtra(com.example.suvrat.helpdadministrator.Constants.RECEIVER);

        List<Address> addresses = null;
        try {
            //the getFromLocation() gets address from the lat and long
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    //maxResults means how many matching addresses we need. here we just get 1.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e(TAG, "IO or network Error", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid lati or longi values.
            Log.e(TAG, "Invalid lat and long " + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            Log.e(TAG, "No address found with these inputs");
        }
        //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
                }

                Log.i(TAG, "address_found");
            deliverResultToReceiver(com.example.suvrat.helpdadministrator.Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(com.example.suvrat.helpdadministrator.Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
