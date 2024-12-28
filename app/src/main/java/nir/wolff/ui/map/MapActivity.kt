package nir.wolff.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nir.wolff.R
import nir.wolff.databinding.ActivityMapBinding
import nir.wolff.model.Alert
import nir.wolff.model.Group
import nir.wolff.model.GroupMember
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private var groupMembersListener: ListenerRegistration? = null

    companion object {
        private const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PIKUD_HAOREF_API_URL = "https://www.oref.org.il/WarningMessages/alert/alerts.json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up refresh button
        binding.refreshButton.setOnClickListener {
            refreshAlerts()
        }

        checkLocationPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            
            // Get current location and move camera
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
        
        setupGroupMembersListener()
        refreshAlerts() // Initial load of alerts
    }

    private fun setupGroupMembersListener() {
        val groupId = intent.getStringExtra("group_id") ?: return
        
        groupMembersListener = firestore.collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                map.clear() // Clear existing markers
                
                val group = snapshot?.toObject(Group::class.java)
                group?.members?.forEach { member ->
                    // For testing, generate random locations near the user's location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let { userLocation ->
                            val lat = userLocation.latitude + (Math.random() - 0.5) * 0.01
                            val lng = userLocation.longitude + (Math.random() - 0.5) * 0.01
                            
                            val memberLocation = LatLng(lat, lng)
                            map.addMarker(
                                MarkerOptions()
                                    .position(memberLocation)
                                    .title(member.email)
                                    .snippet("Status: ${member.status}")
                            )
                        }
                    }
                }
            }
    }

    private fun refreshAlerts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(PIKUD_HAOREF_API_URL)
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Referer", "https://www.oref.org.il/")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    withContext(Dispatchers.Main) {
                        responseData?.let { processAlerts(it) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching alerts", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Error fetching alerts: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processAlerts(jsonData: String) {
        try {
            val json = JSONObject(jsonData)
            val alerts = json.optJSONArray("data") ?: return

            map.clear() // Clear existing markers and circles

            for (i in 0 until alerts.length()) {
                val alert = alerts.getJSONObject(i)
                val data = alert.getString("data")
                val areas = data.split(",")

                // For each area, create a circle on the map
                areas.forEach { area ->
                    // Here you would need to map the area name to coordinates
                    // This is a simplified example using random coordinates near the user's location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val lat = it.latitude + (Math.random() - 0.5) * 0.1
                            val lng = it.longitude + (Math.random() - 0.5) * 0.1

                            map.addCircle(
                                CircleOptions()
                                    .center(LatLng(lat, lng))
                                    .radius(2000.0) // 2km radius
                                    .strokeColor(Color.RED)
                                    .fillColor(Color.argb(70, 255, 0, 0))
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing alerts", e)
            Toast.makeText(this, "Error processing alerts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        map.isMyLocationEnabled = true
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val currentLatLng = LatLng(it.latitude, it.longitude)
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupMembersListener?.remove()
    }
}
