package models.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize @Serializable
data class User(
    val firebase_token:String,
    val device_id: String,
    val localizacao: String?,

    val titulo_alerta: String?,
    val corpo_alerta: String?,
    val tipo_alerta: String?,
    val timestamp_alerta: String?,

    val alerta_calor: Boolean,
    val alerta_frio: Boolean,
    val alerta_sol: Boolean,
    val alerta_chuva: Boolean,
    val alerta_hidratacao: Boolean,
) : Parcelable