package client

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface WeatherClient {

    @GET("/weather/{lat}/{lon}")
    suspend fun getWeatherInfo(@Path("lat") lat:Number, @Path("lon") lon: Number): Response<ResponseBody>
}

