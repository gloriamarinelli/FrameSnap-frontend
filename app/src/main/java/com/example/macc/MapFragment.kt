
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.macc.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MapFragment : Fragment(), OnMapReadyCallback {
    private val FINE_PERMISSION_CODE = 1
    private lateinit var myMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mapReady = false // Flag to track if the map is ready

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_fragment, container, false)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        mapReady = true // Set flag to true when map is ready

        // Remove existing markers
        myMap.clear()

        // Add casual points spaced approximately 20 km apart in Rome
        val rome = LatLng(41.9028, 12.4964) // Updated coordinates for Rome
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rome, 10f)) // Zoom in to Rome

        val numberOfPoints = 7
        val distanceBetweenPoints = 6.0 // in kilometers

        for (i in 1..numberOfPoints) {
            val angle = (i.toDouble() / numberOfPoints) * 2 * Math.PI
            val latOffset = distanceBetweenPoints / 111.32 * Math.sin(angle)
            val lngOffset =
                distanceBetweenPoints / (111.32 * Math.cos(rome.latitude)) * Math.cos(angle)

            val casualPoint = LatLng(rome.latitude + latOffset, rome.longitude + lngOffset)
            myMap.addMarker(MarkerOptions().position(casualPoint).title("Casual Point $i"))
        }

        // Call getLastLocation only if map is ready
        if (::fusedLocationProviderClient.isInitialized && mapReady) {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        }

        if (!::myMap.isInitialized) {
            // If map is not initialized yet, wait until it's ready
            return
        }

        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                // Add a blue marker for the user's location
                val userLatLng = LatLng(location.latitude, location.longitude)
                myMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is denied, please allow the permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
