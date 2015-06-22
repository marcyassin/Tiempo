 package yassin.marc.tiempo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


 public class MainActivity extends Activity {

     public static final  String TAG = MainActivity.class.getSimpleName();
     private CurrentConditions mCurrentConditions;
     @InjectView(R.id.timeLabel) TextView mTimeLabel;
     @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
     @InjectView(R.id.humidityValue) TextView mHumidityValue;
     @InjectView(R.id.precipValue) TextView mPrecipChance;
     @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
     @InjectView(R.id.iconImageView) ImageView mIconImageView;
     @InjectView(R.id.refreshButton) ImageView mRefreshButton;
     @InjectView(R.id.progressBar) ProgressBar mProgressBar;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
         mProgressBar.setVisibility(View.INVISIBLE);

         LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         final double longitude = location.getLongitude();
         final double latitude = location.getLatitude();



//         final double latitude = location.latitude;//40.6764000;
//         final double longitude = location.longitude;//-73.8124980;
         System.out.println("latitude: "+latitude);
         System.out.println("longitude: " + longitude);





         mRefreshButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getForecast(latitude, longitude);
             }
         });

         getForecast(latitude, longitude);



        Log.d(TAG, "Main UI code is running!");

    }

     private void updateDisplay() {
         mTemperatureLabel.setText(mCurrentConditions.getTemperature() + "");
         mTimeLabel.setText("At " + mCurrentConditions.getFormattedTime() + " it will be");
         mHumidityValue.setText(mCurrentConditions.getHumidity() + "");
         mPrecipChance.setText(mCurrentConditions.getPrecipChance() + "%");
         mSummaryLabel.setText(mCurrentConditions.getSummary());
         Drawable drawable = getResources().getDrawable(mCurrentConditions.getIconId());
         mIconImageView.setImageDrawable(drawable);




     }

     private CurrentConditions getCurrentDetails(String jsonData) throws JSONException {
         JSONObject forecast = new JSONObject(jsonData);
         String timezone = forecast.getString("timezone");
         Log.i(TAG,"From JSON: " + timezone);

         JSONObject currently = forecast.getJSONObject("currently");

         CurrentConditions currentConditions = new CurrentConditions();
         currentConditions.setHumidity(currently.getDouble("humidity"));
         currentConditions.setTime(currently.getLong("time"));
         currentConditions.setIcon(currently.getString("icon"));
         currentConditions.setPrecipChance(currently.getDouble("precipProbability"));
         currentConditions.setSummary(currently.getString("summary"));
         currentConditions.setTemperature(currently.getDouble("temperature"));
         currentConditions.setTimeZone(timezone);

         Log.d(TAG, currentConditions.getFormattedTime());


         return currentConditions;

     }

     private void getForecast(double latitude, double longitude){

         String apiKey = "b1f515c59b0f17d22079cb5d891725ad";
         String foreCastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude ;
         if (isNetworkAvailable()) {

             toggleRefresh();


             OkHttpClient client = new OkHttpClient();
             Request request = new Request.Builder().url(foreCastURL).build();

             Call call = client.newCall(request);
             call.enqueue(new Callback() {
                 @Override
                 public void onFailure(Request request, IOException e) {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             toggleRefresh();

                         }
                     });
                     alertUserAboutErrror();


                 }

                 @Override
                 public void onResponse(Response response) throws IOException {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             toggleRefresh();

                         }
                     });
                     try {

                         String jsonData = response.body().string();

                         Log.v(TAG, jsonData);

                         if (response.isSuccessful()) {
                             mCurrentConditions = getCurrentDetails(jsonData);
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     updateDisplay();

                                 }
                             });

                         } else {
                             alertUserAboutErrror();

                         }

                     } catch (IOException e) {
                         Log.e(TAG, "Exception caught: ", e);
                     } catch (JSONException e) {
                         Log.e(TAG, "Exception caught: ", e);
                     }

                 }
             });
         }

         else{
             Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
         }
     }

     private void toggleRefresh() {
         if (mProgressBar.getVisibility() == View.INVISIBLE){
             mProgressBar.setVisibility(View.VISIBLE);
             mRefreshButton.setVisibility(View.INVISIBLE);

         }
         else {
             mProgressBar.setVisibility(View.INVISIBLE);
             mRefreshButton.setVisibility(View.VISIBLE);

         }

     }

     private boolean isNetworkAvailable() {
         ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = manager.getActiveNetworkInfo();

         boolean isAvailable= false;
         if (networkInfo != null && networkInfo.isConnected()){
             isAvailable = true;
         }
         return isAvailable;
     }

     private void alertUserAboutErrror() {
         AlertDialogFragment dialog = new AlertDialogFragment();
         dialog.show(getFragmentManager(),"error_dialog");

     }


 }
