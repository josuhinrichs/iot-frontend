package client;

import android.content.Context;

public class WeatherClient extends BaseClient{
    public String apiURL;
    
    WeatherClient(Context context){
        super(context);
        this.apiURL = this.baseURL + "/weather";
    }



    public String getCurrentWeather(){
        double lon = -3.7;
        double lat = -38.5;
        String url = "/%f/%f".format(lon, lat);
        String response = "";
        
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.apiURL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        response = "Response is: " + response.substring(0,500);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                response = "That didn't work!";
            }
        });
    }
}
