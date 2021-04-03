/*Enter city name with no spaces. To use country name to disambiguate city name,
  use city,country or city,country_abbrev
*/

package com.example.weatherjson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity {

	 private EditText location, country, temperature, humidity, pressure;
	 private String queryString;
	 private String APPID = "1937c3565d027796ab90ecade26ee182";
	 private Weather weather = new Weather();
	
	//messages from background thread contain data for UI
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			
			  weather = (Weather)msg.obj;
			  country.setText(weather.getCountry());
		      temperature.setText(weather.getTemperature());
		      humidity.setText(weather.getHumidity());
		      pressure.setText(weather.getPressure());
		      location.setText("");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		location = (EditText)findViewById(R.id.editText1);
		country = (EditText)findViewById(R.id.editText2);
		temperature = (EditText)findViewById(R.id.editText3);
		humidity = (EditText)findViewById(R.id.editText4);
		pressure = (EditText)findViewById(R.id.editText5);
	    
	}
	
	//when button is clicked, start background thread
	public void open(View view){
	      queryString = location.getText().toString() + "&APPID=" + APPID;
      
	      Thread t = new Thread(background);
		  t.start();
	}

	//thread connects to Weather Api, gets response code, JSON search results,
	//places data into Log and sends messages to display data on UI
	Runnable background = new Runnable() {
		public void run(){
			
			StringBuilder builder = new StringBuilder();

			InputStream is = null;

			String Url = "http://api.openweathermap.org/data/2.5/weather?q=" + queryString;

			try {
				URL url = new URL(Url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				int response = conn.getResponseCode();
				Log.e("JSON", "The response is: " + response);
				//if response code not 200, end thread
				if (response != 200) return;
				is = conn.getInputStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			}	catch(IOException e) {}
			finally {
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {}
				}
			}

			//convert StringBuilder to String
			String readJSONFeed = builder.toString();
			Log.e("JSON", readJSONFeed);

		
			//decode JSON
			try {		
			JSONObject reader = new JSONObject(readJSONFeed);
			
			JSONObject sys = reader.getJSONObject("sys");
			weather.setCountry(sys.getString("country"));

			JSONObject main = reader.getJSONObject("main");
			
			//convert temperature from absolute zero to normal Celtius reading
			String str = main.getString("temp");
			float temp = Float.parseFloat(str) - 273.15f;
			weather.setTemperature(String.format("%.2f C", temp));
			weather.setPressure(main.getString("pressure"));
			weather.setHumidity(main.getString("humidity"));

			//send values to main thread
			 Message msg = handler.obtainMessage();
			 msg.obj = weather;
			 handler.sendMessage(msg);
				
			} catch (JSONException e) {e.getMessage();
				e.printStackTrace();
			} 
		}
	
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
}
