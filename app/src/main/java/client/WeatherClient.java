package client;

public class WeatherClient extends BaseClient{
    public String apiURL;
    
    WeatherClient(){
        this.apiURL = this.baseURL + "/weather";
    }



    public String getCurrentWeather(){
        
    }
}
