package models.weather
import kotlinx.serialization.Serializable

@Serializable
data class Forecast(
    val date: String,
    val weekday: String,
    val max: Int,
    val min: Int,
    val cloudiness: Int,
    val rain: Double,
    val rain_probability: Int,
    val wind_speedy: String,
    val description: String,
    val condition: String
)