package models.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val temp: Int,
    val date: String,
    val time: String,
    val humidity: Double,
    val city: String,
    val city_name:String,
    val condition: String,
)