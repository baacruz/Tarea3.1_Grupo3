package com.example.geolocalizacion32.Repository

import android.content.Context
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.geolocalizacion32.Configuracion.ApiConfig
import com.example.geolocalizacion32.Configuracion.Gabinetes
import org.json.JSONException
import org.json.JSONObject

class GabineteRepository(private val context: Context) {

    private val queue = Volley.newRequestQueue(context)

    fun fetchGabinetes(onResult: (List<Gabinetes>) -> Unit) {
        val url = ApiConfig.EndPointGet
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                val list = mutableListOf<Gabinetes>()
                for (i in 0 until response.length()) {
                    try {
                        val obj = response.getJSONObject(i)
                        val gabinete = Gabinetes().apply {
                            id = obj.optInt("id")
                            nombre = obj.optString("nombre")
                            direccion = obj.optString("direccion")
                            latitud = obj.optString("latitud")
                            longitud = obj.optString("longitud")
                            submask = obj.optString("submask")
                            gateway = obj.optString("gateway")
                        }
                        list.add(gabinete)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                onResult(list)
            },
            { error ->
                Toast.makeText(context, "Error al obtener gabinetes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonArrayRequest)
    }

    fun createGabinete(
        nombre: String,
        direccion: String,
        latitud: String,
        longitud: String,
        submask: String,
        gateway: String,
        onSuccess: () -> Unit
    ) {
        val url = ApiConfig.EndPointPost
        val params = JSONObject().apply {
            put("nombre", nombre)
            put("direccion", direccion)
            put("latitud", latitud)
            put("longitud", longitud)
            put("submask", submask)
            put("gateway", gateway)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                val message = response.optString("message", "Procesado")
                val success = response.optBoolean("issuccess", false)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (success) onSuccess()
            },
            { error ->
                Toast.makeText(context, "Error en la petición: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        request.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(request)
    }

    fun updateGabinete(
        id: Int,
        nombre: String,
        direccion: String,
        latitud: String,
        longitud: String,
        submask: String,
        gateway: String,
        onSuccess: () -> Unit
    ) {
        val url = ApiConfig.EndPointUpdate + "?id=" + id
        
        val params = JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("direccion", direccion)
            put("latitud", latitud)
            put("longitud", longitud)
            put("submask", submask)
            put("gateway", gateway)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                val message = response.optString("message", "Actualizado")
                val success = response.optBoolean("issuccess", false)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                if (success) onSuccess()
            },
            { error ->
                var errorMessage = "Error al actualizar"
                if (error.networkResponse != null) {
                    try {
                        errorMessage = String(error.networkResponse.data, Charsets.UTF_8)
                    } catch (e: Exception) { e.printStackTrace() }
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
        request.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(request)
    }

    fun deleteGabinete(id: Int, onSuccess: () -> Unit) {
        // Se agrega el ID tanto en la URL como en el cuerpo
        val url = ApiConfig.EndPointDelete + "?id=" + id
        val params = JSONObject().apply {
            put("id", id)
        }
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            params,
            { response ->
                val success = response.optBoolean("issuccess", false)
                val message = response.optString("message", "Respuesta del servidor")

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                if (success) {
                    onSuccess()
                }
            },
            { error ->
                var errorMessage = "Error al eliminar gabinete"

                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        errorMessage = String(error.networkResponse.data, Charsets.UTF_8)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
        request.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(request)
    }
}
