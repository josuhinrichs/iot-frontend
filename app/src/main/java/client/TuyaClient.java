package client;

import android.content.Context;

public class TuyaClient extends  BaseClient{
    public String apiURL;

    TuyaClient(Context context){
        super(context);
        this.apiURL = this.baseURL + "/tuya";
    }

  //  public getPlugStatus(){

    // }

    // public turnPlugON(){

    //  }
    //  public turnPlugOFF(){

    //  }

}
