package com.roman1792.maptestapi9;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TXT_FILE_URL = "https://dl.dropboxusercontent.com/u/5842089/route.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void onButtonClicked(View view) {
        new RouteTask().execute();
    }


    private class RouteTask extends AsyncTask<Void, Void, Void> {

        PolylineOptions polylineOptions;
        CameraUpdate cu;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(TXT_FILE_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) buffer.append(line);
                String fileJsonStr = buffer.toString();
                JSONObject dataJsonObj = new JSONObject(fileJsonStr);
                JSONArray coords = dataJsonObj.getJSONArray("coords");

                polylineOptions = new PolylineOptions();
                LatLngBounds.Builder b = new LatLngBounds.Builder();
                for (int i = 0; i < coords.length(); i++) {
                    JSONObject waypointJson = coords.getJSONObject(i);
                    LatLng waypoint = new LatLng(waypointJson.getDouble("la"), waypointJson.getDouble("lo"));
                    polylineOptions.add(waypoint);
                    b.include(waypoint);
                }

                LatLngBounds bounds = b.build();
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 25);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                mMap.addPolyline(polylineOptions);
                mMap.animateCamera(cu);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
