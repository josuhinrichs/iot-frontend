package client;
public class BaseClient extends DefaultHttpClient{
    public String baseURL;
    
    BaseClient(){
        this.baseURL = "http://localhost:9000"; // TODO: colocar como env
    }

}
