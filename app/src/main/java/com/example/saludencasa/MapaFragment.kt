package com.example.saludencasa
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.bumptech.glide.Glide
import com.example.saludencasa.ApiServices.ApiServiceRuta
import com.example.saludencasa.ApiServices.ApiServiceRutaCiclismo
import com.example.saludencasa.ApiServices.ApiServiceRutaPie
import com.example.saludencasa.Constante.urlUbicMedic
import com.google.gson.annotations.SerializedName
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET



import java.io.IOException



// interfaz de servicio para definir llamadas a la API
interface ApiService {
    @GET("Trabajador/") // endpoint
    suspend fun getTrabajadores(): List<Trabajador>
}

data class Trabajador(
    @SerializedName("id_trabajador") val idTrabajador: Int,
    @SerializedName("id_cliente") val idClienteTrabajador: Int,
    @SerializedName("cliente") val cliente: String,
    @SerializedName("profesiones") val profesiones: List<String>,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double,
    @SerializedName("estadoid") val estadoid: String,
    @SerializedName("foto") val foto: String?
)


class MapaFragment : Fragment(R.layout.fragment_mapa), MapEventsReceiver {

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var currentInfoWindow: MarkerInfoWindow? = null
    private var currentRoutePolyline: Polyline? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val trabajadores = apiService.getTrabajadores()
                withContext(Dispatchers.Main) {
                    for (trabajador in trabajadores) {
                        if (trabajador.estadoid == "Aceptado") {
                            val geoPoint = GeoPoint(trabajador.latitud, trabajador.longitud)
                            val marker = Marker(mapView)
                            marker.position = geoPoint
                            marker.title = "ID: ${trabajador.idTrabajador}, Trabajador: ${trabajador.cliente}"
                            marker.setOnMarkerClickListener { _, _ ->
                                currentInfoWindow?.close()
                                marker.showInfoWindow()
                                true
                            }
                            marker.infoWindow = object : MarkerInfoWindow(R.layout.custom_info_window, mapView) {
                                override fun onOpen(item: Any) {
                                    val layout = mView as View
                                    val tvCliente = layout.findViewById<TextView>(R.id.tvCliente)
                                    val txtProfesion = layout.findViewById<TextView>(R.id.txtprofesion)
                                    val btnSolicitar = layout.findViewById<Button>(R.id.btnSolicitar)
                                    val btnRuta = layout.findViewById<Button>(R.id.btnRuta)
                                    val ivFoto = layout.findViewById<CircleImageView>(R.id.imgFotoPerfilFrag)
                                    val markerTitle = (item as Marker).title
                                    val titleParts = markerTitle.split(", ")
                                    val cliente = titleParts[1]
                                    val profesiones = trabajador.profesiones.joinToString(", ")
                                    tvCliente.text = cliente
                                    txtProfesion.text = profesiones

                                    if (!trabajador.foto.isNullOrEmpty()) {
                                        context?.let {
                                            Glide.with(it)
                                                .load(trabajador.foto)
                                                .placeholder(R.drawable.foto_predeterminada)
                                                .into(ivFoto)
                                        }
                                    } else {
                                        ivFoto.setImageResource(R.drawable.foto_predeterminada)
                                    }

                                    val miActividad = activity as? MainActivity3
                                    val idCliente = miActividad?.getMiVariableGlobal()

                                    btnSolicitar.setOnClickListener {
                                        val intent = Intent(activity, PerfilTrabajador_vistaCliente::class.java)
                                        intent.putExtra("idCliente",idCliente)
                                        intent.putExtra("idTrabajador",trabajador.idTrabajador)
                                        intent.putExtra("idClienteTrabajador",trabajador.idClienteTrabajador)
                                        startActivity(intent)
                                    }
                                    btnRuta.setOnClickListener {
                                        createRouteToWorker(trabajador)
                                    }

                                    currentInfoWindow?.close()
                                    currentInfoWindow = this
                                }

                                override fun onClose() {
                                    if (currentInfoWindow == this) {
                                        currentInfoWindow = null
                                    }
                                }
                            }
                            mapView.overlays.add(marker)
                            marker.setOnMarkerClickListener { _, _ ->
                                currentInfoWindow?.close()
                                marker.showInfoWindow()
                                true
                            }
                        }
                    }
                    mapView.invalidate()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mapView.addOnFirstLayoutListener(object : MapView.OnFirstLayoutListener {
            override fun onFirstLayout(v: View, left: Int, top: Int, right: Int, bottom: Int) {
                val startPoint = myLocationOverlay.myLocation
                if (startPoint != null) {
                    updateLocationInfo(startPoint.latitude, startPoint.longitude)
                }
            }
        })

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
    }

    private fun createRouteToWorker(worker: Trabajador) {
        clearCurrentRoute()
        val currentLocation = myLocationOverlay.myLocation
        if (currentLocation != null) {
            val start = "${currentLocation.longitude},${currentLocation.latitude}"
            val end = "${worker.longitud},${worker.latitud}"
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
    private fun clearCurrentRoute() {
        currentRoutePolyline?.let {
            mapView.overlayManager.remove(it)
            currentRoutePolyline = null
        }
    }
    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        val selectedPoint = p ?: return true
        updateLocationInfo(selectedPoint.latitude, selectedPoint.longitude)
        mapView.controller.setCenter(selectedPoint)
        mapView.invalidate()
        return true
    }
    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
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
    private fun updateLocationInfo(latitude: Double, longitude: Double) {
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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


