package com.example.saludencasa

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.saludencasa.ApiServices.ApiServiceRuta
import com.example.saludencasa.ApiServices.ApiServiceRutaCiclismo
import com.example.saludencasa.ApiServices.ApiServiceRutaPie
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RutaClientes : Fragment(R.layout.fragment_ruta_clientes), MapEventsReceiver {
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var currentInfoWindow: MarkerInfoWindow? = null
    private var currentRoutePolyline: Polyline? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val latitud = arguments?.getDouble("latitud", 0.0)
        val longitud = arguments?.getDouble("longitud", 0.0)
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.mapView)
        requestPermissions()
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(16.0)
        mapView.setMultiTouchControls(true)
        val overlay = MapEventsOverlay(this)
        mapView.overlays.add(overlay)
        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        mapView.overlays.add(myLocationOverlay)
        mapView.overlays.add(overlay)
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchCity(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        if (latitud != null && longitud != null) {
            val geoPoint = GeoPoint(latitud, longitud)
            val marker = Marker(mapView)
            marker.position = geoPoint
            mapView.overlays.add(marker)
            val nombre = arguments?.getString("nombre")
            marker.title = nombre
            marker.setOnMarkerClickListener { _, _ ->
                currentInfoWindow?.close()
                marker.showInfoWindow()
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.ventana_cliente)
                val btnRuta = dialog.findViewById<Button>(R.id.btnRuta)
                btnRuta.setOnClickListener {
                    dialog.dismiss()
                    createRouteToLocation(latitud, longitud)
                }

                val nombreCliente = arguments?.getString("nombre")
                val tvCliente = dialog.findViewById<TextView>(R.id.tvCliente)
                tvCliente.text = nombreCliente
                val fotoCliente = arguments?.getString("foto")
                val imgFotoPerfilFrag = dialog.findViewById<CircleImageView>(R.id.imgFotoPerfilFrag)
                if (!fotoCliente.isNullOrEmpty()) {
                    context?.let {
                        Glide.with(it)
                            .load(fotoCliente)
                            .placeholder(R.drawable.foto_predeterminada)
                            .error(R.drawable.foto_predeterminada)
                            .into(imgFotoPerfilFrag)
                    }
                } else {
                    imgFotoPerfilFrag.setImageResource(R.drawable.foto_predeterminada)
                }
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)
                dialog.show()
                true
            }
        }

    }

    private fun createRouteToLocation(latitud: Double, longitud: Double) {
        clearCurrentRoute()
        val currentLocation = myLocationOverlay.myLocation
        if (currentLocation != null) {
            val start = "${currentLocation.longitude},${currentLocation.latitude}"
            val end = "$longitud,$latitud"
            val dialogView = layoutInflater.inflate(R.layout.dialog_mode_selection, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
            val btnSelect = dialogView.findViewById<Button>(R.id.btnSelect)
            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Seleccionar Modo de Transporte")
            val dialog = dialogBuilder.create()
            btnSelect.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                when (selectedId) {
                    R.id.radioDrive -> createRouteUsingApiServiceRuta(start, end, "driving-car", Color.BLUE)
                    R.id.radioWalk -> createRouteUsingApiServiceRutaPie(start, end, "foot-walking", Color.RED)
                    R.id.radioCiclismo -> createRouteUsingApiServiceRutaCiclismo(start, end, "cycling-regular", Color.GREEN)
                }
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun createRouteUsingApiServiceRutaCiclismo(start: String, end: String, mode: String, color: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiServiceRutaCiclismo::class.java)
                    .getRoute("5b3ce3597851110001cf62485b1ef7debff744ff8d6d95623880d5a3", start, end) // Reemplaza con tu API key

                if (call.isSuccessful) {
                    val routeResponse = call.body()
                    if (routeResponse != null) {
                        val coordinates = routeResponse.features.first().geometry.coordinates
                        requireActivity().runOnUiThread {
                            drawRouteOnMap(coordinates, color)
                        }
                    }
                } else {
                    Log.i("MapaFragment", "Error al obtener la ruta")
                }
            } catch (e: Exception) {
                Log.e("MapaFragment", "Error: ${e.message}")
            }
        }
    }

    private fun createRouteUsingApiServiceRutaPie(start: String, end: String, mode: String, color: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiServiceRutaPie::class.java)
                    .getRoute("5b3ce3597851110001cf62485b1ef7debff744ff8d6d95623880d5a3", start, end)

                if (call.isSuccessful) {
                    val routeResponse = call.body()
                    if (routeResponse != null) {
                        val coordinates = routeResponse.features.first().geometry.coordinates
                        requireActivity().runOnUiThread {
                            drawRouteOnMap(coordinates, color)
                        }
                    }
                } else {
                    Log.i("MapaFragment", "Error al obtener la ruta")
                }
            } catch (e: Exception) {
                Log.e("MapaFragment", "Error: ${e.message}")
            }
        }
    }

    private fun createRouteUsingApiServiceRuta(start: String, end: String, mode: String, color: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiServiceRuta::class.java)
                    .getRoute("5b3ce3597851110001cf62485b1ef7debff744ff8d6d95623880d5a3", start, end)

                if (call.isSuccessful) {
                    val routeResponse = call.body()
                    if (routeResponse != null) {
                        val coordinates = routeResponse.features.first().geometry.coordinates
                        requireActivity().runOnUiThread {
                            drawRouteOnMap(coordinates, color)
                        }
                    }
                } else {
                    Log.i("MapaFragment", "Error al obtener la ruta")
                }
            } catch (e: Exception) {
                Log.e("MapaFragment", "Error: ${e.message}")
            }
        }
    }

    private fun drawRouteOnMap(coordinates: List<List<Double>>, color: Int) {
        val polyLine = Polyline(mapView)
        val points = mutableListOf<GeoPoint>()

        for (coordinatePair in coordinates) {
            val latitude = coordinatePair[1]
            val longitude = coordinatePair[0]
            points.add(GeoPoint(latitude, longitude))
        }
        polyLine.setPoints(points)
        polyLine.color = color // Cambio de color según la variable "color"
        polyLine.width = 8f
        currentRoutePolyline = polyLine
        mapView.overlayManager.add(polyLine)
        mapView.invalidate()
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun clearCurrentRoute() {
        currentRoutePolyline?.let {
            mapView.overlayManager.remove(it)
            currentRoutePolyline = null
        }
    }


    private fun requestPermissions() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                1
            )
        }
    }
    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        val selectedPoint = p ?: return true
        updateLocationInfo(selectedPoint.latitude, selectedPoint.longitude)
        mapView.controller.setCenter(selectedPoint)
        mapView.invalidate()
        return true
    }
    private fun updateLocationInfo(latitude: Double, longitude: Double) {
    }
    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }
    private fun searchCity(cityName: String) {
        val geocoder = Geocoder(requireContext())
        val addressList = geocoder.getFromLocationName(cityName, 1)
        if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            val latitude = address.latitude
            val longitude = address.longitude
            val geoPoint = GeoPoint(latitude, longitude)
            mapView.controller.animateTo(geoPoint)
            updateLocationInfo(latitude, longitude)
        } else {
            Toast.makeText(requireContext(), "Ciudad no encontrada", Toast.LENGTH_SHORT).show()
        }
    }
}