package models.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val by: String,
    val valid_key: Boolean,
    val results: Results,
    val execution_time: Int,
    val from_cache: Boolean
)