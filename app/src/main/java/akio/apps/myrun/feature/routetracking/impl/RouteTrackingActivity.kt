package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.activity.BaseInjectionActivity
import akio.apps.common.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.common.AppPermissions.locationPermissions
import akio.apps.myrun.feature.common.CheckLocationServiceDelegate
import akio.apps.myrun.feature.common.CheckRequiredPermissionsDelegate
import akio.apps.myrun.feature.common.MapPresentation
import akio.apps.myrun.feature.common.toGoogleLatLng
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.launch

class RouteTrackingActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }
    private val viewModel: RouteTrackingViewModel by lazy { getViewModel() }

    private lateinit var mapView: GoogleMap

    private val checkLocationServiceDelegate by lazy { CheckLocationServiceDelegate(this, emptyList(), RC_LOCATION_SERVICE, onLocationServiceAvailable) }

    private val checkRequiredPermissionsDelegate by lazy { CheckRequiredPermissionsDelegate(this, RC_LOCATION_PERMISSIONS, locationPermissions, onLocationPermissionsGranted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
        }
    }

    override fun onStart() {
        super.onStart()

        // onStart: check location permissions -> check location service availability -> allow user to use this screen
        checkRequiredPermissionsDelegate.requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkRequiredPermissionsDelegate.verifyPermissionsResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkLocationServiceDelegate.verifyLocationServiceResolutionResult(requestCode, resultCode)
    }

    @SuppressLint("MissingPermission")
    private fun onLocationRequirementsReady() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment).getMapAsync { map ->
            this.mapView = map
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            observeEvent(viewModel.mapInitLocation) { initLocation ->
                mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(initLocation.toGoogleLatLng(), MapPresentation.MAP_DEFAULT_ZOOM_LEVEL))
            }
        }
    }

    private val onLocationPermissionsGranted = {
        lifecycleScope.launch {
            checkLocationServiceDelegate.checkLocationServiceAvailability(this@RouteTrackingActivity)
        }
    }

    private val onLocationServiceAvailable = { onLocationRequirementsReady() }

    companion object {
        const val RC_LOCATION_SERVICE = 1
        const val RC_LOCATION_PERMISSIONS = 2
    }
}