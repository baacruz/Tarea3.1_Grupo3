package com.example.geolocalizacion32.ViewModel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.geolocalizacion32.Configuracion.Gabinetes
import com.example.geolocalizacion32.Repository.GabineteRepository

class GabineteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GabineteRepository(application)
    
    val gabinetesList = mutableStateListOf<Gabinetes>()

    fun refreshGabinetes() {
        repository.fetchGabinetes { list ->
            gabinetesList.clear()
            gabinetesList.addAll(list)
        }
    }

    fun createGabinete(
        nombre: String,
        direccion: String,
        latitud: String,
        longitud: String,
        submask: String,
        gateway: String
    ) {
        repository.createGabinete(nombre, direccion, latitud, longitud, submask, gateway) {
            refreshGabinetes()
        }
    }

    fun updateGabinete(
        id: Int,
        nombre: String,
        direccion: String,
        latitud: String,
        longitud: String,
        submask: String,
        gateway: String
    ) {
        repository.updateGabinete(id, nombre, direccion, latitud, longitud, submask, gateway) {
            refreshGabinetes()
        }
    }

    fun deleteGabinete(id: Int) {
        repository.deleteGabinete(id) {
            refreshGabinetes()
        }
    }
}
