package silive.in.weather.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import silive.in.weather.R;

public class Splash extends AppCompatActivity {
    public double lat, lng;
    Context context;
    RelativeLayout splash;
    TextView text;
    ImageView image, cornr;
    //ViewAnimator viewAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = getApplicationContext();
        splash = (RelativeLayout) findViewById(R.id.splash);
        text = (TextView) findViewById(R.id.text);
        image = (ImageView) findViewById(R.id.image);
        cornr = (ImageView) findViewById(R.id.cornr);
        cornr.setVisibility(View.INVISIBLE);
        //image.startAnimation(AnimationUtils.loadAnimation(this,R.anim.splash_animation));
        /*ViewAnimator
                .animate(image)
                .dp().translationX(-30, 0)
                .descelerate()
                .duration(3000)
                .start();*/

        checkConnection();

    }


    public void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
       /* GPSTracker gps = new GPSTracker(context);
        if (gps.canGetLocation()) {
            lng = gps.getLongitude();
            lat = gps.getLatitude();
            // gps enabled
        } else {
            gps.showSettingsAlert();
        }
*/
        if (info == null) {
            //   Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            //no_net_connection.setVisibility(View.VISIBLE);
            Snackbar snackbar = Snackbar
                    .make(splash, "No internet connection!", Snackbar.LENGTH_LONG)
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
                    cornr.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Splash.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 4000);


        }

    }

}

