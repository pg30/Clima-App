package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:

    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;//provides live location of the device


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;//starts or stop location updates
    LocationListener mLocationListener;//listens to location updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        //navigation between activities is done using Intent in android
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this,ChangeCityController.class);
                startActivity(myIntent);
            }
        });
    }


    // TODO: Add onResume() here:
    // this method runs just after the app has started and before interacting with user
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "OnResume() Called");
        Log.d("Clima", "Getting Weather for current location");
        //check if we have a new city name from the intent we created in the other class and make the corresponding API call
        Intent myIntent = getIntent();
        String City  = myIntent.getStringExtra("City");
        if(City==null)
            getWeatherForCurrentLocation();
        else
            getWeatherForNewCity(City);
    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city)
    {
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);

    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//location manager gets hold of the current location manager
        mLocationListener = new LocationListener() { //this will listen for location updates
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback recieved");
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                Log.d("Clima", "latitude : "+latitude);
                Log.d("Clima", "longitude : "+longitude);
                //passing parameter to the api using imported dependency
                RequestParams params = new RequestParams();
                params.put("lat",latitude);
                params.put("lon",longitude);
                params.put("appid",APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() callback recieved");
            }
        };
        //used to take permission from user to access location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this , new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Log.d("Clima","onRequestPermissionsResult():permission granted");
                getWeatherForCurrentLocation();
            }
            else
            {
                Log.d("Clima","onRequestPermissionsResult():permission denied");
            }
        }
    }
    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int StatusCode, Header[] headers, JSONObject response)
            {
                Log.d("Clima","Success! JSON : "+response.toString());
                //paste the JSON response on jsonmate.com to understand the fields and hierarchies
                WeatherDataModel weatherData = WeatherDataModel.fromJSON(response);
                updateUI(weatherData);
            }
            @Override
            public void onFailure(int StatusCode, Header[] headers,Throwable e,JSONObject response)
            {
                Log.e("Clima","Fail : "+e.toString());
                Log.d("Clima","StatusCode : "+StatusCode);
                Toast.makeText(WeatherController.this,"Request Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weatherData)
    {
        mTemperatureLabel.setText(weatherData.getTemperature());
        mCityLabel.setText(weatherData.getCity());
        int resourceId = getResources().getIdentifier(weatherData.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceId);
    }


    // TODO: Add onPause() here:
//Note : It is important to free up resources at the time app is being exited so that it does not consumes battery. This should be done in the onPause() method.

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null) mLocationManager.removeUpdates(mLocationListener);
    }
}
