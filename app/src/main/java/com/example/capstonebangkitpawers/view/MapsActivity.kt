package com.example.capstonebangkitpawers.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstonebangkitpawers.BuildConfig.GMAPS_API
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentLocation: Location? = null
    private lateinit var placesClient: PlacesClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getUserLocation()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, GMAPS_API)
        }
        placesClient = Places.createClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocation()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                moveToUserLocation(it)
                searchNearbyVet(it)
            } ?: run {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToUserLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
    }

    private fun searchNearbyVet(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        val queries = listOf("dokter hewan", "vet clinic", "pet care", "pet", "klinik hewan")

        queries.forEach { query ->
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(RectangularBounds.newInstance(
                    LatLng(location.latitude - 0.05, location.longitude - 0.05), // Adjust the bounds as needed
                    LatLng(location.latitude + 0.05, location.longitude + 0.05)  // Radius search bounds
                ))
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(requestBuilder)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val response = task.result
                        response?.autocompletePredictions?.forEach { prediction ->
                            val placeId = prediction.placeId
                            fetchPlaceDetails(placeId)
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch nearby vet clinics", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val vetLocation = place.latLng
                if (vetLocation != null) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(vetLocation)
                            .title(place.name)
                            .snippet(place.address)
                    )
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching place details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
