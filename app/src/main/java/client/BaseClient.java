package client;


import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class BaseClient {
    public String baseURL;
    public RequestQueue queue;
    
    BaseClient(Context context){
        this.baseURL = "http://localhost:9000"; // TODO: ver melhor forma de colocar esse endereço
        this.queue = Volley.newRequestQueue(context);
    }

}
