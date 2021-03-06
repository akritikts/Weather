package silive.in.weather.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import silive.in.weather.Models.GetLocation;
import silive.in.weather.Models.WeatherData;
import silive.in.weather.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static double latitude, longitude;
    LinearLayout weather_info;
    TextView city_text, temp, temp_unit, sky_desc, current_time, date_day, current_time_min, current_time_sec, hourly;
    ImageView icon;
    ImageButton ref;
    TextView humidity, dew, cloud, precip, max_temp, min_temp;
    String APIKey = "5b29d34aeee88dc47264e71ed058a592";
    //String GeoAPIKey = "AIzaSyAX52peWddi3gJQfuB-5teYPoo5haPb5Iw";
    WeatherData weatherData;
    GetLocation getLocation;
    Context context;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates = true;
    LocationListener mLocationListener;
    Location mLastLocation;
    String mLastUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        //Initializing the layout contents
        weather_info = (LinearLayout) findViewById(R.id.weather_info);
        current_time = (TextView) findViewById(R.id.current_time);
        current_time_min = (TextView) findViewById(R.id.current_time_min);
        current_time_sec = (TextView) findViewById(R.id.current_time_sec);
        hourly = (TextView) findViewById(R.id.hourly);
        date_day = (TextView) findViewById(R.id.date_day);
        city_text = (TextView) findViewById(R.id.city_text);
        temp = (TextView) findViewById(R.id.temp);
        temp_unit = (TextView) findViewById(R.id.temp_unit);
        humidity = (TextView) findViewById(R.id.humidity);
        dew = (TextView) findViewById(R.id.dew);
        precip = (TextView) findViewById(R.id.precip);
        cloud = (TextView) findViewById(R.id.cloud);
        max_temp = (TextView) findViewById(R.id.max_temp);
        min_temp = (TextView) findViewById(R.id.min_temp);
        sky_desc = (TextView) findViewById(R.id.sky_desc);
        icon = (ImageView) findViewById(R.id.icon);
        ref = (ImageButton) findViewById(R.id.ref);
        ref.setOnClickListener(this);
        //check for connection
        checkConnection();
        //Google API client
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        /*if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }*/
        Log.d("TAG", latitude + " " + longitude + "inside onCreate");
        if (latitude ==0||longitude ==0){
            updateValuesFromBundle(savedInstanceState);
        }

        weatherData = new WeatherData();
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (Build.VERSION.SDK_INT >= 24 &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        latitude = mLastLocation.getLatitude();
                        longitude = mLastLocation.getLongitude();
                        Log.d("TAG", latitude + " cal lat");
                        Log.d("TAG", longitude + " cal lng");
                        //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                        //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                    }
                }


            }
        };
            /*getLocation = new GetLocation(this);
            latitude = getLocation.getLatitude();
            longitude = getLocation.getLongitude();*/
        /*final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            Log.d("TAG", latitude + " " + longitude + "inside onCreate");
        }*/
        ForecastApi.create(APIKey);

        RequestBuilder weather = new RequestBuilder();
        Request request = new Request();
        request.setLat(latitude + "");
        request.setLng(longitude + "");
        request.setUnits(Request.Units.UK);
        request.setLanguage(Request.Language.ENGLISH);
        request.addExcludeBlock(Request.Block.CURRENTLY);
        request.removeExcludeBlock(Request.Block.CURRENTLY);
        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                Log.d("TAG", "Temp: " + weatherResponse.getCurrently().getTemperature());
                Log.d("TAG", "Summary: " + weatherResponse.getCurrently().getSummary());
                Log.d("TAG", "Hourly Sum: " + weatherResponse.getHourly().getSummary());
                weatherData.setTemperature(weatherResponse.getCurrently().getTemperature());
                weatherData.setDesc(weatherResponse.getCurrently().getSummary());
                String img = weatherResponse.getCurrently().getIcon();
                //icon.setImageResource(Integer.parseInt(img));
                //setIcon(img);
                //city_text.setText(GetCity(latitude, longitude));
                weatherData.setHrs(weatherResponse.getHourly().getSummary());
                weatherData.setMydate(java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                weatherData.setPres(weatherResponse.getCurrently().getPressure());
                weatherData.setPrec(weatherResponse.getCurrently().getPrecipIntensity());
                weatherData.setHumid(weatherResponse.getCurrently().getHumidity());
                weatherData.setDewp(weatherResponse.getCurrently().getDewPoint());
                weatherData.setMax(weatherResponse.getCurrently().getTemperatureMax());
                weatherData.setMin(weatherResponse.getCurrently().getTemperatureMin());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("TAG", "Error while calling: " + retrofitError.getUrl());
                Log.d("TAG", retrofitError.toString());
            }
        });
        temp.setText(" " + weatherData.getTemperature());
        temp_unit.setText("C");
        sky_desc.setText(weatherData.getDesc());
        city_text.setText(GetCity(latitude, longitude));
        hourly.setText(weatherData.getHrs());
        date_day.setText(weatherData.getMydate());
        cloud.setText("Pressure : " + weatherData.getPres());
        Log.d("TAG", weatherData.getMax() + " " + weatherData.getMin() + "max min");
        Log.d("TAG", weatherData.getPres() + " " + "pressure");
        precip.setText("Precipitation : " + weatherData.getPrec());
        humidity.setText("Humidity : " + weatherData.getHumid());
        dew.setText("Dew Point : " + weatherData.getDewp());
        max_temp.setText("Max.T : " + weatherData.getMax());
        min_temp.setText("Min.T : " + weatherData.getMin());
        updateTimeOnEachSecond();
        //new GetData(this).execute();

    }

    public void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            Snackbar snackbar = Snackbar
                    .make(weather_info, "No Internet Connection", Snackbar.LENGTH_LONG)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            checkConnection();
                        }
                    });

// Changing message text color
            snackbar.setActionTextColor(Color.RED);

// Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        } else {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /*cornr.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Splash.this, MainActivity.class);
                    startActivity(intent);
                    finish();*/
                }
            }, 2000);


        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ref:
                displayLocation();
                new GetData(this).execute();
                //WeatherUpdate(latitude,longitude);
                break;
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode)) {
                api.showErrorDialogFragment(this,resultCode,GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE);
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("TAG", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    private void displayLocation() {
        if (Build.VERSION.SDK_INT >= 24 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
             latitude = mLastLocation.getLatitude();
             longitude = mLastLocation.getLongitude();

            //lblLocation.setText(latitude + ", " + longitude);

        } else {

            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }


    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void updateTimeOnEachSecond() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Calendar c;
                c = Calendar.getInstance();
                Log.d("TAG", "time changed");
                final int hrs = c.get(Calendar.HOUR_OF_DAY);
                final int min = c.get(Calendar.MINUTE);
                final int sec = c.get(Calendar.SECOND);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        current_time.setText(String.valueOf(hrs + " :"));
                        current_time_min.setText(String.valueOf(min + " :"));
                        current_time_sec.setText(String.valueOf(sec));
                        mLastUpdateTime = String.valueOf(hrs+":");
                        mLastUpdateTime.concat(String.valueOf(min + " :"));
                        mLastUpdateTime.concat(String.valueOf(sec));
                    }
                });

            }
        }, 0, 1000);

    }

    /*public void setIcon(String str) {
        //icon = (ImageView) findViewById(R.id.icon);

        *//*if (str.contains("sun") && str.contains("clear")) {
            icon.setImageResource(R.mipmap.sunny);
        } else if (str.contains("night") && str.contains("clear")) {
            icon.setImageResource(R.mipmap.clrnt);
        } else if (str.contains("night") && str.contains("cloud")) {
            icon.setImageResource(R.mipmap.cldnt);
        } else if (str.contains("night") && str.contains("rain")) {
            icon.setImageResource(R.mipmap.ntrain);
        } else if (str.contains("sun") && str.contains("rain")) {
            icon.setImageResource(R.mipmap.sunrain);
        } else if (str.contains("night") && str.contains("thunder")) {
            icon.setImageResource(R.mipmap.thndr);
        } else if (str.contains("rain") && str.contains("thunder")) {
            icon.setImageResource(R.mipmap.thndr);
        } else if (str.contains("day") && str.contains("rain")) {
            icon.setImageResource(R.mipmap.rain);
        } else if (str.contains("day") && str.contains("cloudy")) {
            icon.setImageResource(R.mipmap.prtcld);
        }*//*
    }*/

    public String GetCity(double latitude, double longitude) {
        Log.d("TAG", latitude + " " + longitude + "one");
        /*if (latitude == 0 || longitude == 0) {
            latitude = getLocation.getLatitude();
            longitude = getLocation.getLongitude();
        }*/
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
            Log.d("TAG", latitude + " " + longitude);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            //int maxLines = 2;

            for (int i = 0; i < maxLines; i++) {
                Log.d("TAG", maxLines + " ");
                String addressStr = address.get(0).getAddressLine(i);
                Log.d("TAG", maxLines + " ");
                if ((i >= 1) && (i <= (maxLines - 1))) {
                    builder.append(addressStr);
                    builder.append(" ");
                }
            }

            String finalAddress = builder.toString(); //This is the complete address.
            return finalAddress;

        } catch (IOException e) {
            // Handle IOException
        } catch (NullPointerException e) {
            // Handle NullPointerException
        }
        return null;

    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("REQUESTING_LOCATION_UPDATES_KEY",
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable("LOCATION_KEY", mLastLocation);
        savedInstanceState.putString("LAST_UPDATED_TIME_STRING_KEY", mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("REQUESTING_LOCATION_UPDATES_KEY")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        "REQUESTING_LOCATION_UPDATES_KEY");
                //setButtonsEnabledState();
            }
            if (savedInstanceState.keySet().contains("LOCATION_KEY")) {
                mLastLocation = savedInstanceState.getParcelable("LOCATION_KEY");
            }
            if (savedInstanceState.keySet().contains("LAST_UPDATED_TIME_STRING_KEY")) {
                mLastUpdateTime = savedInstanceState.getString(
                        "LAST_UPDATED_TIME_STRING_KEY");
                updateTimeOnEachSecond();
            }

        }
    }


    //Class to get API data
    public class GetData extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;
        private double lat, lng;


        public GetData(Context c) {
            this.progressDialog = new ProgressDialog(c);
            this.lat = getLocation.getLatitude();
            this.lng = getLocation.getLongitude();
            Log.d("TAG", lat + " " + lng + " inside GetData");

            /*final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

            if (gpsTracker.canGetLocation()) {

                this.lat = gpsTracker.getLatitude();
                this.lng = gpsTracker.getLongitude();
            } else {
                Log.d("TAG", " no gps");
                DialogGps dialogGps = new DialogGps();
                dialogGps.show(getFragmentManager(), "GPS Alert");
                //gpsTracker.showSettingsAlert();
            }*/
            Log.d("TAG", lat + " " + lng + "inside getData");
        }

        public void UpdateUI(WeatherData getData) {
            temp.setText(" " + getData.getTemperature());
            temp_unit.setText("C");
            sky_desc.setText(getData.getDesc());
            city_text.setText(GetCity(lat, lng));
            hourly.setText(getData.getHrs());
            date_day.setText(getData.getMydate());
            cloud.setText("Pressure : " + getData.getPres());
            Log.d("TAG", getData.getMax() + " " + getData.getMin() + "max min");
            Log.d("TAG", getData.getPres() + " " + "pressure");
            precip.setText("Precipitation : " + getData.getPrec());
            humidity.setText("Humidity : " + getData.getHumid());
            dew.setText("Dew Point : " + getData.getDewp());
            max_temp.setText("Max.T : " + getData.getMax());
            min_temp.setText("Min.T : " + getData.getMin());
            updateTimeOnEachSecond();

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = new ProgressDialog(getApplicationContext());
            progressDialog.setMessage("Loading");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
        }


        @Override
        protected String doInBackground(Void... params) {

            Log.d("TAG", lat + "" + lng);

            RequestBuilder weather = new RequestBuilder();
            Request request = new Request();
            request.setLat(lat + "");
            request.setLng(lng + "");
            request.setUnits(Request.Units.UK);
            request.setLanguage(Request.Language.ENGLISH);
            request.addExcludeBlock(Request.Block.CURRENTLY);
            request.removeExcludeBlock(Request.Block.CURRENTLY);
            weather.getWeather(request, new Callback<WeatherResponse>() {
                @Override
                public void success(WeatherResponse weatherResponse, Response response) {
                    Log.d("TAG", "Temp: " + weatherResponse.getCurrently().getTemperature());
                    Log.d("TAG", "Summary: " + weatherResponse.getCurrently().getSummary());
                    Log.d("TAG", "Hourly Sum: " + weatherResponse.getHourly().getSummary());
                    weatherData.setTemperature(weatherResponse.getCurrently().getTemperature());
                    weatherData.setDesc(weatherResponse.getCurrently().getSummary());
                    String img = weatherResponse.getCurrently().getIcon();
                    //icon.setImageResource(Integer.parseInt(img));
                    //setIcon(img);
                    weatherData.setHrs(weatherResponse.getHourly().getSummary());
                    weatherData.setMydate(java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                    weatherData.setPres(weatherResponse.getCurrently().getPressure());
                    weatherData.setPrec(weatherResponse.getCurrently().getPrecipIntensity());
                    weatherData.setHumid(weatherResponse.getCurrently().getHumidity());
                    weatherData.setDewp(weatherResponse.getCurrently().getDewPoint());
                    weatherData.setMax(weatherResponse.getCurrently().getTemperatureMax());
                    weatherData.setMin(weatherResponse.getCurrently().getTemperatureMin());
                    UpdateUI(weatherData);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d("TAG", "Error while calling: " + retrofitError.getUrl());
                    Log.d("TAG", retrofitError.toString());
                }
            });


            return null;
        }
    }
}

