package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
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
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_OFF
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var mark: Marker? = null
    private val TAG = "LocationFragment"


    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        val latitude = 30.097613
        val longitude = 31.265958
        val latLng = LatLng(latitude, longitude)
        val zoom = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        map.addMarker(MarkerOptions().position(latLng))
        mapLongClick(map)
        mapPoiClick(map)
        mapStyle(map)
        enableMyLocation()
    }


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


//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
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
    }

    private fun mapPoiClick(map: GoogleMap) {

//        mark?.remove()
        map.setOnPoiClickListener {
            mark?.remove()
            mark = map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            mark?.showInfoWindow()
        }

    }

    private fun mapStyle(map: GoogleMap) {

        try {
            map.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_style) })

        } catch (e: Exception) {
            Log.e(TAG, "Style parsing failed.")

        }
    }

    private fun enableMyLocation() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {


//            _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            activity?.let {
                requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
            return
        }
        map.isMyLocationEnabled = true
    }

    ///




}

