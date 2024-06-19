package models.tuya
import kotlinx.serialization.Serializable


@Serializable
data class StatusResponse(
    val isTurnedOn: Boolean,
    val isOnline: Boolean
)