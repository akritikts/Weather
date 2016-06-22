package silive.in.weather.Activities;

import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import silive.in.weather.Models.GPSTracker;
import silive.in.weather.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView city_text, temp, temp_unit, sky_desc, current_time, date_day, current_time_min, current_time_sec, hourly;
    double latitude, longitude;
    ImageView icon;
    ImageButton ref;
    TextView humidity,dew,cloud,precip,max_temp,min_temp;
    String APIKey = "5b29d34aeee88dc47264e71ed058a592";


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
        humidity = (TextView)findViewById(R.id.humidity);
        dew = (TextView)findViewById(R.id.dew);
        precip = (TextView)findViewById(R.id.precip);
        cloud = (TextView)findViewById(R.id.cloud);
        max_temp = (TextView)findViewById(R.id.max_temp);
        min_temp = (TextView)findViewById(R.id.min_temp);
        sky_desc = (TextView) findViewById(R.id.sky_desc);
        icon = (ImageView) findViewById(R.id.icon);
        ref = (ImageButton) findViewById(R.id.ref);
        ref.setOnClickListener(this);


        ForecastApi.create(APIKey);
        final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {

            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }
        new GetData(this).execute();

    }

    public void WeatherUpdate(final double lat,final double lng) {
        final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

        /*if (gpsTracker.canGetLocation()) {

            lat = gpsTracker.getLatitude();
            lng = gpsTracker.getLongitude();
        } else {
            Log.d("TAG", " no gps");
            gpsTracker.showSettingsAlert();
        }*/
        RequestBuilder weather = new RequestBuilder();
        Log.d("TAG", lat + "" + lng);

        Request request = new Request();
        request.setLat(lat + "");
        request.setLng(lng + "");
        request.setUnits(Request.Units.UK);
        request.setLanguage(Request.Language.ENGLISH);
        request.addExcludeBlock(Request.Block.CURRENTLY);
        request.removeExcludeBlock(Request.Block.CURRENTLY);
        /*final double alat, along;
        alat = lat;
        along = lng;*/
        //Log.d("TAG", alat + "" + along);

        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                Log.d("TAG", "Temp: " + weatherResponse.getCurrently().getTemperature());
                Log.d("TAG", "Summary: " + weatherResponse.getCurrently().getSummary());
                Log.d("TAG", "Hourly Sum: " + weatherResponse.getHourly().getSummary());
                double temperature = weatherResponse.getCurrently().getTemperature();
                temp.setText(" " + temperature);
                temp_unit.setText("C");
                String desc = weatherResponse.getCurrently().getSummary();
                sky_desc.setText(desc);
                String img = weatherResponse.getCurrently().getIcon();
                //icon.setImageResource(Integer.parseInt(img));
                setIcon(img);
                //Log.d("TAG", alat + "" + along);
                city_text.setText(gpsTracker.GetCity(lat, lng));
                long time = weatherResponse.getCurrently().getTime();
                String hrs = weatherResponse.getHourly().getSummary();
                hourly.setText(hrs);
                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                date_day.setText(mydate);
                String pres,prec,humid,dewp;
                double max,min;
                pres = weatherResponse.getCurrently().getPressure();
                prec = weatherResponse.getCurrently().getPrecipIntensity();
                humid = weatherResponse.getCurrently().getHumidity();
                dewp = weatherResponse.getCurrently().getDewPoint();
                max = weatherResponse.getCurrently().getTemperatureMax();
                min = weatherResponse.getCurrently().getTemperatureMin();
                cloud.setText("Pressure : "+pres);
                Log.d("TAG",max+" "+min);
                Log.d("TAG",pres+" ");
                precip.setText("Precipitation : "+prec);
                humidity.setText("Humidity : "+humid);
                dew.setText("Dew Point : "+dewp);
                max_temp.setText("Max.T : "+max);
                min_temp.setText("Min.T : "+min);
                updateTimeOnEachSecond();

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("TAG", "Error while calling: " + retrofitError.getUrl());
                Log.d("TAG", retrofitError.toString());
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ref:
                WeatherUpdate(latitude,longitude);
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

    //Class to get API data
    public class GetData extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;
        private double lat, lng;

        public GetData(Context c) {
            this.progressDialog = new ProgressDialog(c);
            final GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

            if (gpsTracker.canGetLocation()) {

                this.lat = gpsTracker.getLatitude();
                this.lng = gpsTracker.getLongitude();
            } else {
                Log.d("TAG", " no gps");
                gpsTracker.showSettingsAlert();
            }
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
            WeatherUpdate(lat,lng);
            return null;
        }
    }
}

