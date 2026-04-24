package com.nearnote.app.ui

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
import com.nearnote.app.location.PlaceSearchHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
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

    var searchQuery by remember { mutableStateOf(placeName) }
    var selectedLatitude by remember { mutableDoubleStateOf(initialLatitude.takeIf { it != 0.0 } ?: 28.6139) }
    var selectedLongitude by remember { mutableDoubleStateOf(initialLongitude.takeIf { it != 0.0 } ?: 77.2090) }
    var isSearching by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
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
                        controller.setZoom(16.0)
                        controller.setCenter(GeoPoint(selectedLatitude, selectedLongitude))
                        setOnTouchListener { v, event ->
                            if (event.action == android.view.MotionEvent.ACTION_UP) {
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
                        updateMapMarker(this, selectedLatitude, selectedLongitude, radiusMeters, "Current location")
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

