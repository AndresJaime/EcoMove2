package com.example.ecomove

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var goToLocationButton: Button
    private lateinit var realTimeLocationButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var database: DatabaseReference
    private var userUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userUID = intent.getStringExtra("USER_UID")

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        goToLocationButton = findViewById(R.id.button_location)
        realTimeLocationButton = findViewById(R.id.button_realtime_location)
        changePasswordButton = findViewById(R.id.button_change_password)

        goToLocationButton.setOnClickListener {
            goToUserLocation()
        }

        realTimeLocationButton.setOnClickListener {
            startActivity(Intent(this, UbicacionTiempoReal::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, CambiarContraseña::class.java))
        }

        database = FirebaseDatabase.getInstance().getReference("locations")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Mostrar una ubicación de ejemplo
        val exampleLocation = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(exampleLocation).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(exampleLocation))
    }

    override fun onLocationChanged(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
        googleMap.addMarker(MarkerOptions().position(userLocation).title("You are here"))

        userUID?.let { uid ->
            val userLocationMap = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )
            database.child(uid).setValue(userLocationMap)
        }
    }

    private fun goToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            val userLocation = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
