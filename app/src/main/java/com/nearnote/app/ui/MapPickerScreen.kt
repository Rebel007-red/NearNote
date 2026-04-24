package com.nearnote.app.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.nearnote.app.location.PlaceSearchHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
fun MapPickerScreen(
    initialLatitude: Double = 0.0,
    initialLongitude: Double = 0.0,
    radiusMeters: Int = 250,
    placeName: String = "",
    onPlaceSelected: (latitude: Double, longitude: Double, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val helper = remember { PlaceSearchHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hasFineLocation = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    var searchQuery by remember { mutableStateOf(placeName) }
    var selectedLatitude by remember { mutableDoubleStateOf(initialLatitude.takeIf { it != 0.0 } ?: 0.0) }
    var selectedLongitude by remember { mutableDoubleStateOf(initialLongitude.takeIf { it != 0.0 } ?: 0.0) }
    var isSearching by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var hasCenteredOnUser by remember { mutableStateOf(initialLatitude != 0.0 && initialLongitude != 0.0) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var touchDownX by remember { mutableStateOf(0f) }
    var touchDownY by remember { mutableStateOf(0f) }
    var touchMoved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
    }

    @SuppressLint("MissingPermission")
    fun recenterToUser() {
        if (!hasFineLocation) return
        isLocating = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    selectedLatitude = location.latitude
                    selectedLongitude = location.longitude
                    updateMapMarker(mapView, location.latitude, location.longitude, radiusMeters, "Your location")
                    mapView?.controller?.animateTo(GeoPoint(location.latitude, location.longitude))
                    mapView?.controller?.setZoom(17.0)
                    hasCenteredOnUser = true
                } else if (!hasCenteredOnUser) {
                    selectedLatitude = 28.6139
                    selectedLongitude = 77.2090
                    updateMapMarker(mapView, selectedLatitude, selectedLongitude, radiusMeters, "Selected location")
                    hasCenteredOnUser = true
                }
                isLocating = false
            }
            .addOnFailureListener {
                isLocating = false
                if (!hasCenteredOnUser) {
                    selectedLatitude = 28.6139
                    selectedLongitude = 77.2090
                    updateMapMarker(mapView, selectedLatitude, selectedLongitude, radiusMeters, "Selected location")
                    hasCenteredOnUser = true
                }
            }
    }

    LaunchedEffect(hasFineLocation, hasCenteredOnUser) {
        if (hasFineLocation && !hasCenteredOnUser) {
            recenterToUser()
        } else if (!hasFineLocation && !hasCenteredOnUser) {
            selectedLatitude = 28.6139
            selectedLongitude = 77.2090
            hasCenteredOnUser = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Map container (takes up remaining space)
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            // Map view - full background
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(16.0)
                        controller.setCenter(
                            GeoPoint(
                                if (selectedLatitude == 0.0) 28.6139 else selectedLatitude,
                                if (selectedLongitude == 0.0) 77.2090 else selectedLongitude
                            )
                        )
                        setOnTouchListener { v, event ->
                            when (event.actionMasked) {
                                android.view.MotionEvent.ACTION_DOWN -> {
                                    touchDownX = event.x
                                    touchDownY = event.y
                                    touchMoved = false
                                }

                                android.view.MotionEvent.ACTION_MOVE -> {
                                    if (!touchMoved) {
                                        val dx = kotlin.math.abs(event.x - touchDownX)
                                        val dy = kotlin.math.abs(event.y - touchDownY)
                                        if (dx > 16f || dy > 16f) {
                                            touchMoved = true
                                        }
                                    }
                                }
                            }

                            if (event.pointerCount == 1 && event.action == android.view.MotionEvent.ACTION_UP && !touchMoved) {
                                val projection = (v as? MapView)?.projection
                                val geoPoint = projection?.fromPixels(event.x.toInt(), event.y.toInt())
                                if (geoPoint != null) {
                                    selectedLatitude = geoPoint.latitude
                                    selectedLongitude = geoPoint.longitude
                                    updateMapMarker(this, geoPoint.latitude, geoPoint.longitude, radiusMeters, "Selected location")
                                }
                            }
                            false
                        }
                        updateMapMarker(
                            this,
                            if (selectedLatitude == 0.0) 28.6139 else selectedLatitude,
                            if (selectedLongitude == 0.0) 77.2090 else selectedLongitude,
                            radiusMeters,
                            if (hasFineLocation) "Your location" else "Selected location"
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Floating search bar overlay (stays on top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0xF0FFF4E8))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search place") },
                        placeholder = { Text("e.g., pharmacy") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                isSearching = true
                                scope.launch {
                                    val results = helper.searchPlaces(searchQuery)
                                    results.firstOrNull()?.let { place ->
                                        selectedLatitude = place.latitude
                                        selectedLongitude = place.longitude
                                        // Update marker first, then animate to avoid double-redraw
                                        updateMapMarker(mapView, place.latitude, place.longitude, radiusMeters, place.name)
                                        mapView?.controller?.animateTo(GeoPoint(place.latitude, place.longitude))
                                    }
                                    isSearching = false
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Go")
                    }
                    OutlinedButton(
                        onClick = { recenterToUser() },
                        enabled = hasFineLocation && !isLocating,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(if (isLocating) "Locating" else "My location")
                    }
                }
            }

            // Searching overlay (centered on map)
            if (isSearching) {
                Surface(
                    color = Color(0xAA000000),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }

        // Bottom info and confirm (outside Box, part of Column)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFDFBF7)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "%.4f, %.4f • %dm radius".format(selectedLatitude, selectedLongitude, radiusMeters),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onPlaceSelected(selectedLatitude, selectedLongitude, searchQuery.ifBlank { "Selected location" })
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private fun updateMapMarker(
    mapView: MapView?,
    latitude: Double,
    longitude: Double,
    radiusMeters: Int,
    label: String
) {
    if (mapView == null) return
    
    // Batch updates to reduce re-composition
    mapView.overlays.clear()

    // Add marker
    val marker = Marker(mapView).apply {
        position = GeoPoint(latitude, longitude)
        title = label
    }
    mapView.overlays.add(marker)

    // Add radius circle as polygon
    val polygon = Polygon(mapView).apply {
        val points = mutableListOf<GeoPoint>()
        val radiusInDegrees = radiusMeters / 111000.0
        for (i in 0..360) {
            val angle = i
            val rad = Math.toRadians(angle.toDouble())
            val lat = latitude + radiusInDegrees * Math.cos(rad)
            val lon = longitude + radiusInDegrees / Math.cos(Math.toRadians(latitude)) * Math.sin(rad)
            points.add(GeoPoint(lat, lon))
        }
        setPoints(points)
        outlinePaint.color = android.graphics.Color.parseColor("#FFC66D")
        outlinePaint.strokeWidth = 2f
        outlinePaint.alpha = 100
        fillPaint.color = android.graphics.Color.parseColor("#20FFC66D")
    }
    mapView.overlays.add(polygon)
    
    // Single invalidate call instead of multiple redraws
    mapView.invalidate()
}

