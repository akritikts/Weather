package silive.in.weather.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import silive.in.weather.Fragments.DialogGps;
import silive.in.weather.Models.GPSTracker;
import silive.in.weather.Models.WeatherData;
import silive.in.weather.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView city_text, temp, temp_unit, sky_desc, current_time, date_day, current_time_min, current_time_sec, hourly;
    double latitude, longitude;
    ImageView icon;
    ImageButton ref;
    TextView humidity, dew, cloud, precip, max_temp, min_temp;
    String APIKey = "5b29d34aeee88dc47264e71ed058a592";
    WeatherData weatherData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        weatherData = new WeatherData();


        ForecastApi.create(APIKey);
        final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            Log.d("TAG", latitude + " " + longitude + "inside onCreate");
        }
        new GetData(this).execute();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ref:
                new GetData(this).execute();
                //WeatherUpdate(latitude,longitude);
                break;
        }
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
                    }
                });

            }
        }, 0, 1000);

    }

    public void setIcon(String str) {
        //icon = (ImageView) findViewById(R.id.icon);

        /*if (str.contains("sun") && str.contains("clear")) {
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
        }*/
    }

    public String GetCity(double latitude, double longitude) {
        Log.d("TAG", latitude + " " + longitude + "one");
        /*if (latitude==0||longitude==0){
            latitude = getLatitude();
            longitude = getLongitude();
            Log.d("TAG",latitude+" "+longitude +"two");
            while (latitude==0||longitude==0){
                latitude = getLatitude();
                longitude = getLongitude();

            }
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


    //Class to get API data
    public class GetData extends AsyncTask<Void, Void, WeatherData> {
        ProgressDialog progressDialog;
        /* String desc;
         String pres, prec, humid, dewp, hrs, mydate;
         double max, min, temperature;*/
        private double lat, lng;


        public GetData() {

        }

        public GetData(Context c) {
            this.progressDialog = new ProgressDialog(c);
            final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

            if (gpsTracker.canGetLocation()) {

                this.lat = gpsTracker.getLatitude();
                this.lng = gpsTracker.getLongitude();
            } else {
                Log.d("TAG", " no gps");
                DialogGps dialogGps = new DialogGps();
                dialogGps.show(getFragmentManager(), "GPS Alert");
                //gpsTracker.showSettingsAlert();
            }
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
        protected void onPostExecute(WeatherData s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            UpdateUI(s);
        }


        @Override
        protected WeatherData doInBackground(Void... params) {
            //final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
            RequestBuilder weather = new RequestBuilder();
            Log.d("TAG", lat + "" + lng);


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
                    setIcon(img);
                    //city_text.setText(GetCity(lat, lng));
                    long time = weatherResponse.getCurrently().getTime();
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


            //WeatherUpdate(lat,lng);
            return weatherData;
        }
    }
}

