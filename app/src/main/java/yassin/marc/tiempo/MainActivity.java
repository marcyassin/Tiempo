 package yassin.marc.tiempo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import butterknife.ButterKnife;
import butterknife.InjectView;


 // Add option to view in celsius
 // Add searchbar for picking to see other cities' weather
 // add 5 day forecast to bottom of the screen
 // make UI more original


 public class MainActivity extends Activity {

     public static final  String TAG = MainActivity.class.getSimpleName();
     private CurrentConditions mCurrentConditions;
     private CurrentConditions dayOne;
     private CurrentConditions dayTwo;
     private CurrentConditions dayThree;
     private CurrentConditions dayFour;
     private CurrentConditions dayFive;
     private CurrentConditions daySix;


     @InjectView(R.id.timeLabel) TextView mTimeLabel;
     @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
     @InjectView(R.id.humidityValue) TextView mHumidityValue;
     @InjectView(R.id.precipValue) TextView mPrecipChance;
     @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
     @InjectView(R.id.iconImageView) ImageView mIconImageView;
     @InjectView(R.id.refreshButton) ImageView mRefreshButton;
     @InjectView(R.id.progressBar) ProgressBar mProgressBar;
     @InjectView(R.id.locationLabel) TextView mLocationLabel;
     @InjectView(R.id.maxTemp) TextView mMaxTempLabel;
     @InjectView(R.id.minTemp) TextView mMinTempLabel;
     @InjectView(R.id.firstDayHiTemp) TextView mDayOneHiTemp;
     @InjectView(R.id.firstDayLowTemp) TextView mDayOneLowTemp;
     @InjectView(R.id.firstDaySummary) TextView mDayOneSummary;




     String locationLabel;


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
         getLocationName(latitude,longitude);

         mRefreshButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getForecast(latitude, longitude);
             }
         });

         getForecast(latitude, longitude);
        Log.d(TAG, "Main UI code is running!");

    }



     private CurrentConditions getCurrentDetails(String jsonData) throws JSONException {
         JSONObject forecast = new JSONObject(jsonData);
         String timezone = forecast.getString("timezone");
         Log.i(TAG,"From JSON: " + timezone);

         // Current Day objects //////////////////////////////////////////////////////////////

         JSONObject currently = forecast.getJSONObject("currently");

         CurrentConditions currentConditions = new CurrentConditions();

         currentConditions.setHumidity(currently.getDouble("humidity"));
         currentConditions.setTime(currently.getLong("time"));
         currentConditions.setIcon(currently.getString("icon"));
         currentConditions.setPrecipChance(currently.getDouble("precipProbability"));
         currentConditions.setSummary(currently.getString("summary"));
         currentConditions.setTemperature(currently.getDouble("temperature"));
         currentConditions.setTimeZone(timezone);

         JSONObject daily = forecast.getJSONObject("daily");
         JSONArray data = daily.getJSONArray("data");
         JSONObject p = (JSONObject)data.get(0);

         currentConditions.setTemperatureMax(p.getDouble("temperatureMax"));
         currentConditions.setTemperatureMin(p.getDouble("temperatureMin"));

         Log.d(TAG, currentConditions.getFormattedTime());


         return currentConditions;

     }

     private CurrentConditions getDayDetails(String jsonData, int day) throws JSONException {
         //Day objects //////////////////////////////////////////////////////////////

         CurrentConditions dayConditions = new CurrentConditions();

         JSONObject forecast = new JSONObject(jsonData);

         //String timezone = forecast.getString("timezone");
         //Log.i(TAG,"From JSON: " + timezone);

         JSONObject daily = forecast.getJSONObject("daily");
         JSONArray data = daily.getJSONArray("data");
         JSONObject p = (JSONObject)data.get(day);

         dayConditions.setTemperatureMax(p.getDouble("temperatureMax"));
         dayConditions.setTemperatureMin(p.getDouble("temperatureMin"));
         dayConditions.setIcon(p.getString("icon"));
         dayConditions.setSummary(p.getString("summary"));

         //Log.d(TAG, dayConditions.getFormattedTime());


         return dayConditions;

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
                             dayOne = getDayDetails(jsonData, 1);
                             dayTwo = getDayDetails(jsonData, 2);
                             dayThree = getDayDetails(jsonData, 3);
                             dayFour = getDayDetails(jsonData, 4);
                             dayFive = getDayDetails(jsonData, 5);
                             daySix = getDayDetails(jsonData,6);

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

     private void updateDisplay() {
         //Current Day conditions..
         Drawable drawable = getResources().getDrawable(mCurrentConditions.getIconId());
         mTemperatureLabel.setText(mCurrentConditions.getTemperature() + "");
         mTimeLabel.setText("Last updated at " + mCurrentConditions.getFormattedTime() + "");
         mHumidityValue.setText(mCurrentConditions.getHumidity() + "");
         mPrecipChance.setText(mCurrentConditions.getPrecipChance() + "%");
         mSummaryLabel.setText(mCurrentConditions.getSummary());
         mIconImageView.setImageDrawable(drawable);
         mLocationLabel.setText(locationLabel);
         mMaxTempLabel.setText(mCurrentConditions.getTemperatureMax() + "");
         mMinTempLabel.setText(mCurrentConditions.getTemperatureMin() + "");

         //WeekDay Forecast objects..
         mDayOneHiTemp.setText(dayOne.getTemperatureMax() + "");
         mDayOneLowTemp.setText(dayOne.getTemperatureMin()+"");
         mDayOneSummary.setText(dayOne.getSummary());

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

     private void getLocationName(double lat, double longit){
         Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());

         try {
             List<Address> addresses = geoCoder.getFromLocation(lat, longit, 1);

             if (addresses.size() > 0) {
                 locationLabel = addresses.get(0).getAddressLine(1);
             }
         }
         catch (IOException e1) {
             e1.printStackTrace();
         }

     }


 }
