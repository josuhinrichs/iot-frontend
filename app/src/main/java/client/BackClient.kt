package client

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BackClient {

    @PUT("/weather")
    suspend fun getWeatherInfo(@Body requestBody: RequestBody,): Response<ResponseBody>
    // requestBody: {  latitude: number; longitude: number; token: string;}

    @GET("/tuya/{deviceID}/status")
    suspend fun getDeviceStatus(@Path("deviceID") deviceID:String,): Response<ResponseBody>

    @POST("/tuya/{deviceId}/switch/{value}")
    suspend fun deviceCommand(@Path("deviceID") deviceID:String,@Path("value") value:Boolean): Response<ResponseBody>

    @POST("/user")
    suspend fun getUser(@Body requestBody: RequestBody,): Response<ResponseBody>
    // requestBody: { firebase_token: string }

    @PUT("/user/notification")
    suspend fun setAlertValue(@Body requestBody: RequestBody,): Response<ResponseBody>
    //  requestBody: {
    //  "token": string, -> token do usuário
    //  "type": string, -> nome do alerta ("alerta_calor", "alerta_frio", "alerta_sol", "alerta_chuva" ou "alerta_hidratacao")
    //   "value": boolean -> valor do alerta (se deve notificar ou não)
    //    }

}

