package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_OFF
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var mark: Marker? = null
    private val TAG = "LocationFragment"

    private val FORGROUND_PERMITION_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container, false
        )
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        checkPermissionsAndStartGeofencing()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun onLocationSelected() {
        mark?.let {
            _viewModel.reminderSelectedLocationStr.value = it.title
            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude
        }
        findNavController().popBackStack()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID

            }
            R.id.normal -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL


            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN

            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true

    }

    private fun mapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            createMark(it)
        }
    }

    private fun mapPoiClick(map: GoogleMap) {

        map.setOnPoiClickListener {
            mark?.remove()
            mark = map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            mark?.showInfoWindow()
        }

    }

    private fun mapStyle(map: GoogleMap) {

        try {
            map.setMapStyle(context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it,
                    R.raw.map_style
                )
            })

        } catch (e: Exception) {
            Log.e(TAG, "Style parsing failed.")

        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                FORGROUND_PERMITION_CODE)
            return
        }
        map.isMyLocationEnabled = true
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val lastLocationTask = fusedLocationProviderClient.lastLocation
        // On completion, zoom to the user location and add marker
        lastLocationTask.addOnCompleteListener(requireActivity()) {

            if (it.isSuccessful) {
                val taskResult = it.result
                taskResult?.run {

                    val latLng = LatLng(latitude, longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            15f
                        )
                    )
                    createMark(latLng)
                }
            }
        }
    }

    private fun createMark(it: LatLng) {
        val snippet = String.format(
            Locale.getDefault(),
            """the longitude is %1$.5f,the latitude is %2$.5f""", it.longitude, it.latitude
        )
        mark?.remove()
        mark = map.addMarker(
            MarkerOptions().position(it).title("Dropped Pin").snippet(snippet).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val zoom = 15f
        var latitude = -34.0
        var longitude = 151.0
        var latLng = LatLng(latitude, longitude)

        mark?.let {
            createMark(LatLng(it.position.latitude, it.position.longitude))
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        mapLongClick(map)
        mapPoiClick(map)
        mapStyle(map)
        enableMyLocation()
    }

    ///
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if(requestCode == FORGROUND_PERMITION_CODE){
            if (grantResults.isEmpty() ||
                grantResults[0] == PackageManager.PERMISSION_DENIED
            ) {
                makeSnackBarWithSettingAction()
            }
            else {
                _viewModel.showToast.value = "colling"
                enableMyLocation()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if(this::map.isInitialized){
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                enableMyLocation()
            }
            else{

                makeSnackBarWithSettingAction()
            }

        }
    }

    private fun makeSnackBarWithSettingAction() {
        Snackbar.make(
            requireView(),
            "grant location permission in order to play this game.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("settings") {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
    }
}

