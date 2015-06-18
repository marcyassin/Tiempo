 package yassin.marc.tiempo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


 public class MainActivity extends Activity {

     public static final  String TAG = MainActivity.class.getSimpleName();
     private CurrentConditions mCurrentConditions;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String apiKey = "b1f515c59b0f17d22079cb5d891725ad";
        double latitude = 37.8267;
        double longitude = -122.423;
        String foreCastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude ;


        if (isNetworkAvailable()) {


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(foreCastURL).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {

                        String jsonData = response.body().string();

                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {
                            mCurrentConditions = getCurrentDetails(jsonData);
                        } else {
                            alertUserAboutErrror();

                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }
            });
        }

        else{
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "Main UI code is running!");

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
