package client

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BackClient {

    @PUT("/weather")
    suspend fun getWeatherInfo(@Body requestBody: RequestBody,): Response<ResponseBody>

    @GET("/tuya/{deviceID}/status")
    suspend fun getDeviceStatus(@Path("deviceID") deviceID:String,): Response<ResponseBody>

    @POST("/user")
    suspend fun getUser(@Body requestBody: RequestBody,): Response<ResponseBody>

}

