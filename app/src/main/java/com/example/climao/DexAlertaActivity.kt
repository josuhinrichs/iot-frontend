package com.example.climao

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.climao.databinding.DexAlertaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class DexAlertaActivity : AppCompatActivity() {

    private lateinit var binding: DexAlertaBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DexAlertaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocationPermission()

        binding.btnGetLocation.setOnClickListener {
            fetchLocationAndCep()
        }
    }

    private fun fetchLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
            return
        }
    }

    private fun fetchLocationAndCep() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Se permissões de localização não forem concedidas, solicite-as novamente.
            fetchLocationPermission()
            return
        }

        // Obtenha a última localização conhecida do usuário
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            // Verifique se a localização é válida
            if (location != null) {
                // Obtenha os detalhes do endereço da localização atual
                GlobalScope.launch(Dispatchers.Main) {
                    val address = getAddressFromLocation(location.latitude, location.longitude)
                    // Atualize os TextViews com os detalhes do endereço
                    binding.enderecoRua.text = address.thoroughfare
                    binding.endereco.text = buildAddressString(address)
                    binding.cepInput.setText(address.postalCode)

                }
            }
        }
    }

    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Address {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses!![0]
    }

    private suspend fun getPostalCodeFromLocation(latitude: Double, longitude: Double): Address {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses!![0]
    }

    private fun buildAddressString(address: Address): String {
        val cidade = address.locality ?: "Cidade desconhecida"
        val estado = address.adminArea ?: "Estado desconhecido"
        val bairro = address.subLocality ?: "Bairro desconhecido"
        return "$bairro, $cidade - $estado"
    }


}
