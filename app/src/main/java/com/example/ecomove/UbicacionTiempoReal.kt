package com.example.ecomove

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UbicacionTiempoReal : Menu(), OnMapReadyCallback {

    private lateinit var mapaGoogle: GoogleMap
    private lateinit var baseDatos: DatabaseReference
    private lateinit var autenticacion: FirebaseAuth

    private lateinit var sucursalMasCercana: LatLng
    private var distanciaMinima = Double.MAX_VALUE
    private var ubicacionUsuario: LatLng = LatLng(0.0, 0.0) // Proveer un valor por defecto
    private var mostrandoRutaMasCercana = false

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.activity_ubicacion_tiempo_real)

        autenticacion = FirebaseAuth.getInstance()
        baseDatos = FirebaseDatabase.getInstance().getReference("sucursales")

        val fragmentoMapa = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        fragmentoMapa.getMapAsync(this)

        findViewById<Button>(R.id.button_ruta_mas_cercana).setOnClickListener {
            if (mostrandoRutaMasCercana) {
                mapaGoogle.clear()
                mostrandoRutaMasCercana = false
                findViewById<Button>(R.id.button_ruta_mas_cercana).text = "Ruta Más Cercana"
                mostrarSucursales(ubicacionUsuario)
            } else {
                obtenerUbicacionActual { ubicacion ->
                    ubicacion?.let {
                        ubicacionUsuario = LatLng(it.latitude, it.longitude)
                        mapaGoogle.clear()
                        mostrarSucursales(ubicacionUsuario)
                        mostrarRuta(ubicacionUsuario, sucursalMasCercana, true)
                        mostrandoRutaMasCercana = true
                        findViewById<Button>(R.id.button_ruta_mas_cercana).text = "Dejar de Ver"
                    } ?: Toast.makeText(this, "No se pudo obtener la ubicación en tiempo real.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<ImageView>(R.id.menu_button).setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.button_logout).setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        val dialogView = layoutInflater.inflate(R.layout.dialogo_confirmacion_cerrar_sesion, null)
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        dialogView.findViewById<Button>(R.id.botonCerrarSesion).setOnClickListener {
            cerrarSesion()
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.botonCancelar).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun obtenerUbicacionActual(callback: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val clienteUbicacion = LocationServices.getFusedLocationProviderClient(this)
            clienteUbicacion.lastLocation.addOnSuccessListener { ubicacion ->
                callback(ubicacion)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onMapReady(mapa: GoogleMap) {
        mapaGoogle = mapa
        mapaGoogle.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapaGoogle.isMyLocationEnabled = true
            obtenerUbicacionActual { ubicacion ->
                ubicacion?.let {
                    ubicacionUsuario = LatLng(it.latitude, it.longitude)
                    mapaGoogle.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionUsuario, 15f))
                    mostrarSucursales(ubicacionUsuario)
                } ?: run {
                    ubicacionUsuario = LatLng(-35.42084945954186, -71.67803695962358)
                    mapaGoogle.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionUsuario, 15f))
                    mostrarSucursales(ubicacionUsuario)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        baseDatos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mostrarSucursales(ubicacionUsuario)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UbicacionTiempoReal", "DatabaseError: ${error.message}")
            }
        })

        mapaGoogle.setOnMarkerClickListener { marker ->
            val intent = Intent(this, Localizacion::class.java)
            intent.putExtra("sucursalId", marker.tag as String)
            intent.putExtra("sucursalTitulo", marker.title)
            startActivity(intent)
            true
        }
    }

    private fun mostrarSucursales(ubicacionUsuario: LatLng) {
        distanciaMinima = Double.MAX_VALUE
        var sucursalMasCercanaKey: String? = null
        val markers = mutableListOf<Marker>()

        baseDatos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mapaGoogle.clear()
                for (snapshotSucursal in snapshot.children) {
                    val sucursal = snapshotSucursal.getValue(Sucursal::class.java)
                    if (sucursal != null) {
                        val position = LatLng(sucursal.latitude, sucursal.longitude)
                        val distancia = calcularDistancia(ubicacionUsuario, position)
                        val opcionesMarcador = MarkerOptions().position(position).title(sucursal.name)

                        if (distancia < distanciaMinima) {
                            distanciaMinima = distancia
                            sucursalMasCercana = position
                            sucursalMasCercanaKey = snapshotSucursal.key
                        }

                        val marker = mapaGoogle.addMarker(opcionesMarcador)
                        marker?.tag = snapshotSucursal.key
                        markers.add(marker!!)
                    }
                }

                // Cambiar color de todos los marcadores después de determinar el más cercano
                for (marker in markers) {
                    if (marker.tag == sucursalMasCercanaKey) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    } else {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UbicacionTiempoReal", "DatabaseError: ${error.message}")
            }
        })
    }

    private fun mostrarRuta(origen: LatLng, destino: LatLng, esMasCercana: Boolean) {
        val url = obtenerURLDireccion(origen, destino)
        Log.d("GetDirection", "URL: $url")

        CoroutineScope(Dispatchers.IO).launch {
            val resultado = obtenerDireccion(url)
            withContext(Dispatchers.Main) {
                procesarResultadoDireccion(resultado, esMasCercana, origen, destino)
            }
        }
    }

    private fun obtenerURLDireccion(origen: LatLng, destino: LatLng): String {
        val origenLatLng = "origin=${origen.latitude},${origen.longitude}"
        val destinoLatLng = "destination=${destino.latitude},${destino.longitude}"
        val modo = "mode=driving"
        val parametros = "$origenLatLng&$destinoLatLng&$modo"
        val salida = "json"
        val claveApi = "AIzaSyDzfqehGv2zwCs53Tw7GCQme3cv2-_VIhY"
        return "https://maps.googleapis.com/maps/api/directions/$salida?$parametros&key=$claveApi"
    }

    private suspend fun obtenerDireccion(url: String): String {
        return withContext(Dispatchers.IO) {
            val conexion = URL(url).openConnection() as HttpURLConnection
            conexion.connect()
            val flujo = conexion.inputStream
            val lector = BufferedReader(InputStreamReader(flujo))
            val buffer = StringBuffer()
            var linea: String?
            while (lector.readLine().also { linea = it } != null) {
                buffer.append(linea)
            }
            buffer.toString()
        }
    }

    private fun procesarResultadoDireccion(resultado: String?, esMasCercana: Boolean, origen: LatLng, destino: LatLng) {
        Log.d("GetDirection", "Response: $resultado")
        try {
            val jsonObject = JSONObject(resultado)
            val rutas = jsonObject.getJSONArray("routes")
            if (rutas.length() == 0) {
                Log.e("GetDirection", "No routes found")
                return
            }
            val puntos = rutas.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
            val ruta = decodificarPoly(puntos)
            val color = if (esMasCercana) android.graphics.Color.GREEN else android.graphics.Color.BLUE
            val opcionesPolylinea = PolylineOptions().addAll(ruta).color(color).width(10f)
            mapaGoogle.addPolyline(opcionesPolylinea)

            val distanciaTotal = calcularDistancia(origen, destino)
            val tiempoEstimado = rutas.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text")

            Toast.makeText(this@UbicacionTiempoReal, "Distancia: $distanciaTotal metros, Tiempo estimado: $tiempoEstimado", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("GetDirection", "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun calcularDistancia(origen: LatLng, destino: LatLng): Double {
        val resultados = FloatArray(1)
        Location.distanceBetween(
            origen.latitude, origen.longitude,
            destino.latitude, destino.longitude,
            resultados
        )
        return resultados[0].toDouble()
    }

    private fun decodificarPoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(p)
        }
        return poly
    }

    private fun cerrarSesion() {
        autenticacion.signOut()
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    data class Sucursal(
        val existencias: Int = 0,
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val name: String = ""
    )
}
