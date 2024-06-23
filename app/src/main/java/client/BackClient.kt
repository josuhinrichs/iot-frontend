package client

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BackClient {

    @GET("/weather/{lat}/{lon}")
    suspend fun getWeatherInfo(@Path("lat") lat:Number, @Path("lon") lon: Number): Response<ResponseBody>

    @GET("/tuya/{deviceID}/status")
    suspend fun getDeviceStatus(@Path("deviceID") deviceID:String,): Response<ResponseBody>
}

