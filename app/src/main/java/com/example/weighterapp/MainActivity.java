package com.example.weighterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RequestQueue requestQueue;
    private TextView tvDescription;
    private TextView tvTemperature;
    private TextView tvCity;
    private TextView tvDay;
    private TextView tvHoure;
    private TextView pm;
    private TextView tvTMi;
    private TextView tvTM;
    private TextView tvP;
    private TextView tvHum;
    private LinearLayout lv;
    private ImageView iconView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolabr = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolabr);
        requestQueue = Volley.newRequestQueue(this);
        connectToApi("Rabat");
        tvDescription = (TextView) findViewById(R.id.description);

       tvTemperature = (TextView) findViewById(R.id.degree);

        tvCity = (TextView) findViewById(R.id.city);

        tvDay = (TextView) findViewById(R.id.day);

        pm = (TextView) findViewById(R.id.dORn);

       tvHoure = (TextView) findViewById(R.id.hour);

        tvTMi = (TextView) findViewById(R.id.tmin);

        tvTM = (TextView) findViewById(R.id.tmax);

        tvP = (TextView) findViewById(R.id.pression);

        tvHum = (TextView) findViewById(R.id.humidite);

        lv = (LinearLayout) findViewById(R.id.main_list);
        iconView = (ImageView) findViewById(R.id.icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,query);
                connectToApi(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.search){
            Toast.makeText(getApplicationContext(),"search clicked",Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void dowloadImage(String icon){
        String apiURI = String.format("https://openweathermap.org/img/wn/%s@2x.png",icon);
        ImageRequest imageRequest = new ImageRequest(apiURI, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Log.d(TAG,"there is image here");
                iconView.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"ERROR");
                error.printStackTrace();
            }
        });
        requestQueue.add(imageRequest);
    }

    public void connectToApi(String query){
        String apiURI = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s",query,getString(R.string.api_key));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiURI, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,"there is results");
                showResults(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
    public void showResults(JSONObject weatherData){
        try{
            final Double toC = -273.15;
            JSONObject weather = weatherData.getJSONArray("weather").getJSONObject(0);
            JSONObject main = weatherData.getJSONObject("main");
            String cityNameAndMain = weatherData.getString("name") + " | " + weather.getString("main");
            String temperature = (int)(main.getDouble("temp") + toC)  + "°C";
            String tmin = (int)(main.getDouble("temp_min") + toC) + " °C";
            String tmax = (int)(main.getDouble("temp_max") + toC) + " °C";
            String pressure = main.getInt("pressure") + " hPa";
            String humidity = main.getInt("humidity") + "";
            String fullDay = batshTimeToString(weatherData.getLong("dt"));
            Log.d(TAG, fullDay);
            String[] dayData = fullDay.split(" ");
            // --------------------------------- //
            tvDescription.setText(weather.getString("description"));
            tvTemperature.setText(temperature);
            tvCity.setText(cityNameAndMain);
            tvDay.setText(dayData[0].toUpperCase());
            pm.setText(dayData[2]);
            tvHoure.setText(dayData[1]);
            tvTMi.setText(tmin);
            tvTM.setText(tmax);
            tvP.setText(pressure);
            tvHum.setText(humidity);
            // --------------------------------- background image //

            String mainStr = weather.getString("main");
            if(mainStr.equals("Clouds")){
                lv.setBackground(getDrawable(R.drawable.clouds));
            }else if(mainStr.equals("Thunderstorm")){
                lv.setBackground(getDrawable(R.drawable.thunderstorm2));
            }else if(mainStr.equals("Drizzle")){
                lv.setBackground(getDrawable(R.drawable.drizzle));
            }else if(mainStr.equals("Rain")){
                lv.setBackground(getDrawable(R.drawable.rain));
            }else if(mainStr.equals("Snow")){
                lv.setBackground(getDrawable(R.drawable.snow));
            }else if(mainStr.equals("Clear")){
                lv.setBackground(getDrawable(R.drawable.clear));
            }else{
                lv.setBackground(getDrawable(R.drawable.clear));
            }
            dowloadImage(weather.getString("icon"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String batshTimeToString(Long batchTime){
        Date dt = new Date (batchTime * 1000);
        SimpleDateFormat sfd = new SimpleDateFormat("EEEE hh:mm a", Locale.US);
        return sfd.format(dt);
    }
}
