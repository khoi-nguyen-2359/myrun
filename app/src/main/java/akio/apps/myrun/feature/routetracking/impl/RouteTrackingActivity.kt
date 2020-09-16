package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.activity.BaseInjectionActivity
import akio.apps.common.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.common.AppPermissions
import akio.apps.myrun.feature.common.MapPresentation
import akio.apps.myrun.feature.common.toGoogleLatLng
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

class RouteTrackingActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }
    private val viewModel: RouteTrackingViewModel by lazy { getViewModel() }

    private lateinit var mapView: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
    }

    override fun onStart() {
        super.onStart()

        requireLocationPermissions()
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
        }
    }

    private fun requireLocationPermissions() {
        if (AppPermissions.areLocationPermissionsGranted(this)) {
            onLocationPermissionsReady()
            return
        }

        AppPermissions.requestLocationPermissions(this, null, RC_LOCATION_PERMISSIONS) {
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsReady() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment).getMapAsync { map ->
            this.mapView = map
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            observeEvent(viewModel.mapInitLocation) { initLocation ->
                mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(initLocation.toGoogleLatLng(), MapPresentation.MAP_DEFAULT_ZOOM_LEVEL))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        verifyPermissions(permissions)
    }

    private fun verifyPermissions(permissions: Array<String>) {
        if (AppPermissions.areLocationPermissionsGranted(this)) {
            onLocationPermissionsReady()
            return
        }

        AppPermissions.showRequiredPermissionsMissingDialog(this, permissions) {
            finish()
        }
    }

    companion object {
        const val RC_LOCATION_PERMISSIONS = 1
    }
}