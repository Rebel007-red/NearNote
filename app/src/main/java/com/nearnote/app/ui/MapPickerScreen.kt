package com.nearnote.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.nearnote.app.location.OsrmRoutingHelper
import com.nearnote.app.location.PlaceSearchHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

// Alidade Smooth — modern, flat, Google Maps-like style (light mode)
private val ALIDADE_SMOOTH = object : OnlineTileSourceBase(
    "Alidade_Smooth", 0, 19, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.de/tiles/osmde/",
        "https://b.tile.openstreetmap.de/tiles/osmde/",
        "https://c.tile.openstreetmap.de/tiles/osmde/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + "$zoom/$x/$y.png"
    }
}

// CartoDB dark map tile source — free, no API key (custom style like Google Maps dark mode)
private val CARTO_DARK = object : OnlineTileSourceBase(
    "CartoDB_DarkMatter", 0, 19, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + "$zoom/$x/$y.png"
    }
}

// USGS Satellite imagery — aerial view
private val SATELLITE_VIEW = object : OnlineTileSourceBase(
    "USGS_Satellite", 0, 16, 256, ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + "$zoom/$y/$x"
    }
}

private val mapFallback = GeoPoint(28.6139, 77.2090)
private val poiChips = listOf("Pharmacy", "Fuel", "ATM", "Hospital", "Restaurant")

@Composable
fun MapPickerScreen(
    initialLatitude: Double = 0.0,
    initialLongitude: Double = 0.0,
    radiusMeters: Int = 250,
    placeName: String = "",
    onPlaceSelected: (latitude: Double, longitude: Double, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val helper = remember { PlaceSearchHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hasFineLocation = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    var searchQuery by remember { mutableStateOf(placeName) }
    var selectedLatitude by remember { mutableDoubleStateOf(initialLatitude.takeIf { it != 0.0 } ?: 0.0) }
    var selectedLongitude by remember { mutableDoubleStateOf(initialLongitude.takeIf { it != 0.0 } ?: 0.0) }
    var selectedName by remember { mutableStateOf(placeName.ifBlank { "Selected location" }) }
    var selectedAddress by remember { mutableStateOf("") }

    var currentLatitude by remember { mutableDoubleStateOf(0.0) }
    var currentLongitude by remember { mutableDoubleStateOf(0.0) }

    var isSearching by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var hasCenteredOnUser by remember { mutableStateOf(initialLatitude != 0.0 && initialLongitude != 0.0) }

    var suggestions by remember { mutableStateOf<List<PlaceSearchHelper.Place>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var touchDownX by remember { mutableStateOf(0f) }
    var touchDownY by remember { mutableStateOf(0f) }
    var touchMoved by remember { mutableStateOf(false) }

    // Route from OSRM (real road routing)
    var routePoints by remember { mutableStateOf<List<org.osmdroid.util.GeoPoint>>(emptyList()) }
    var routeDistanceKm by remember { mutableDoubleStateOf(0.0) }
    var routeEtaMinutes by remember { mutableStateOf(0) }
    var isFetchingRoute by remember { mutableStateOf(false) }

    // Map style selector: 0=Light (Alidade), 1=Dark (CartoDB), 2=Satellite
    var mapStyleMode by remember { mutableStateOf(0) }

    // Swap tile source when style changes
    LaunchedEffect(mapStyleMode) {
        val tileSource = when (mapStyleMode) {
            0 -> ALIDADE_SMOOTH    // Light mode (default)
            1 -> CARTO_DARK         // Dark mode
            2 -> SATELLITE_VIEW     // Satellite imagery
            else -> ALIDADE_SMOOTH
        }
        mapView?.setTileSource(tileSource)
        mapView?.invalidate()
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
    }

    fun setFallbackIfNeeded() {
        if (!hasCenteredOnUser) {
            selectedLatitude = mapFallback.latitude
            selectedLongitude = mapFallback.longitude
            selectedName = "Selected location"
            mapView?.controller?.setCenter(mapFallback)
            updateMapOverlays(
                mapView = mapView,
                latitude = selectedLatitude,
                longitude = selectedLongitude,
                radiusMeters = radiusMeters,
                label = selectedName
            )
            hasCenteredOnUser = true
        }
    }

    @SuppressLint("MissingPermission")
    fun recenterToUser(updateSelection: Boolean) {
        if (!hasFineLocation) return
        isLocating = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    mapView?.controller?.animateTo(GeoPoint(location.latitude, location.longitude))
                    mapView?.controller?.setZoom(17.0)
                    if (updateSelection) {
                        selectedLatitude = location.latitude
                        selectedLongitude = location.longitude
                        selectedName = "Your location"
                        updateMapOverlays(
                            mapView = mapView,
                            latitude = selectedLatitude,
                            longitude = selectedLongitude,
                            radiusMeters = radiusMeters,
                            label = selectedName
                        )
                    }
                    hasCenteredOnUser = true
                } else {
                    setFallbackIfNeeded()
                }
                isLocating = false
            }
            .addOnFailureListener {
                isLocating = false
                setFallbackIfNeeded()
            }
    }

    LaunchedEffect(hasFineLocation, hasCenteredOnUser) {
        if (hasFineLocation && !hasCenteredOnUser) {
            recenterToUser(updateSelection = true)
        } else if (!hasFineLocation && !hasCenteredOnUser) {
            setFallbackIfNeeded()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 2) {
            suggestions = emptyList()
            showSuggestions = false
            return@LaunchedEffect
        }
        delay(220)
        val results = helper.searchPlaces(searchQuery).take(5)
        suggestions = results
        showSuggestions = results.isNotEmpty()
    }

    LaunchedEffect(selectedLatitude, selectedLongitude) {
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) return@LaunchedEffect
        val place = helper.reverseSearch(selectedLatitude, selectedLongitude)
        if (place != null) {
            selectedAddress = place.address
            if (selectedName.isBlank() || selectedName == "Selected location" || selectedName == "Your location" || selectedName == "Pinned place") {
                selectedName = place.name
            }
        }
    }

    // Fetch real driving route via OSRM whenever origin or destination changes
    LaunchedEffect(currentLatitude, currentLongitude, selectedLatitude, selectedLongitude) {
        if (currentLatitude == 0.0 || currentLongitude == 0.0 ||
            selectedLatitude == 0.0 || selectedLongitude == 0.0) return@LaunchedEffect
        isFetchingRoute = true
        val result = OsrmRoutingHelper.getRoute(
            fromLat = currentLatitude, fromLon = currentLongitude,
            toLat = selectedLatitude, toLon = selectedLongitude
        )
        if (result != null) {
            routeDistanceKm = result.distanceMeters / 1000.0
            routeEtaMinutes = (result.durationSeconds / 60.0).toInt().coerceAtLeast(1)
            routePoints = result.polylinePoints
            updateMapOverlays(
                mapView = mapView,
                latitude = selectedLatitude,
                longitude = selectedLongitude,
                radiusMeters = radiusMeters,
                label = selectedName,
                snippet = selectedAddress,
                routePoints = result.polylinePoints
            )
        }
        isFetchingRoute = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        // Set default: Alidade Smooth (modern, Google Maps-like style)
                        setTileSource(ALIDADE_SMOOTH)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(16.5)

                        val initialPoint = GeoPoint(
                            if (selectedLatitude == 0.0) mapFallback.latitude else selectedLatitude,
                            if (selectedLongitude == 0.0) mapFallback.longitude else selectedLongitude
                        )
                        controller.setCenter(initialPoint)

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

                            if (event.actionMasked == android.view.MotionEvent.ACTION_UP && event.pointerCount == 1 && !touchMoved) {
                                val projection = (v as? MapView)?.projection
                                val geoPoint = projection?.fromPixels(event.x.toInt(), event.y.toInt())
                                if (geoPoint != null) {
                                    selectedLatitude = geoPoint.latitude
                                    selectedLongitude = geoPoint.longitude
                                    selectedName = "Pinned place"
                                    updateMapOverlays(
                                        mapView = this,
                                        latitude = geoPoint.latitude,
                                        longitude = geoPoint.longitude,
                                        radiusMeters = radiusMeters,
                                        label = selectedName
                                    )
                                }
                            }
                            false
                        }

                        updateMapOverlays(
                            mapView = this,
                            latitude = initialPoint.latitude,
                            longitude = initialPoint.longitude,
                            radiusMeters = radiusMeters,
                            label = if (hasFineLocation) "Your location" else "Selected location"
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xF7FFFFFF),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search drop location") },
                            placeholder = { Text("Area, landmark, shop") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Button(
                            onClick = {
                                if (searchQuery.isBlank()) return@Button
                                isSearching = true
                                scope.launch {
                                    val first = helper.searchPlaces(searchQuery).firstOrNull()
                                    if (first != null) {
                                        selectedLatitude = first.latitude
                                        selectedLongitude = first.longitude
                                        selectedName = first.name
                                        selectedAddress = first.address
                                        updateMapOverlays(
                                            mapView = mapView,
                                            latitude = first.latitude,
                                            longitude = first.longitude,
                                            radiusMeters = radiusMeters,
                                            label = first.name
                                        )
                                        mapView?.controller?.animateTo(GeoPoint(first.latitude, first.longitude))
                                        showSuggestions = false
                                    }
                                    isSearching = false
                                }
                            }
                        ) {
                            Text("Go")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(poiChips) { chip ->
                            OutlinedButton(
                                onClick = {
                                    searchQuery = chip
                                    isSearching = true
                                    scope.launch {
                                        val first = helper.searchPlaces(chip).firstOrNull()
                                        if (first != null) {
                                            selectedLatitude = first.latitude
                                            selectedLongitude = first.longitude
                                            selectedName = first.name
                                            selectedAddress = first.address
                                            updateMapOverlays(
                                                mapView = mapView,
                                                latitude = first.latitude,
                                                longitude = first.longitude,
                                                radiusMeters = radiusMeters,
                                                label = first.name
                                            )
                                            mapView?.controller?.animateTo(GeoPoint(first.latitude, first.longitude))
                                        }
                                        isSearching = false
                                    }
                                }
                            ) {
                                Text(chip)
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 6.dp
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                suggestions.forEach { place ->
                                    OutlinedButton(
                                        onClick = {
                                            selectedLatitude = place.latitude
                                            selectedLongitude = place.longitude
                                            selectedName = place.name
                                            selectedAddress = place.address
                                            searchQuery = place.name
                                            updateMapOverlays(
                                                mapView = mapView,
                                                latitude = place.latitude,
                                                longitude = place.longitude,
                                                radiusMeters = radiusMeters,
                                                label = place.name
                                            )
                                            mapView?.controller?.animateTo(GeoPoint(place.latitude, place.longitude))
                                            showSuggestions = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(place.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = place.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating buttons column — map style cycle + location recentre
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Map style cycle button (Light → Dark → Satellite)
                Surface(
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    color = when (mapStyleMode) {
                        0 -> Color(0xFFF3F4F6)  // Light: light gray bg
                        1 -> Color(0xFF1F2937)  // Dark: dark gray bg
                        2 -> Color(0xFF8B7355)  // Satellite: brown-ish bg
                        else -> Color.White
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    IconButton(onClick = { mapStyleMode = (mapStyleMode + 1) % 3 }) {
                        Text(
                            text = when (mapStyleMode) {
                                0 -> "☀"   // Light: sun
                                1 -> "🌙"   // Dark: moon
                                2 -> "🛰"   // Satellite: satellite
                                else -> "?"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = when (mapStyleMode) {
                                0 -> Color(0xFF111827)
                                1 -> Color.White
                                2 -> Color.White
                                else -> Color.Black
                            }
                        )
                    }
                }

                // Recenter to user location
                Surface(
                    shape = CircleShape,
                    shadowElevation = 8.dp,
                    color = when (mapStyleMode) {
                        0 -> Color.White
                        1 -> Color(0xFF1F2937)
                        2 -> Color(0xFF8B7355)
                        else -> Color.White
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    IconButton(onClick = { recenterToUser(updateSelection = false) }, enabled = hasFineLocation && !isLocating) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = when (mapStyleMode) {
                                    0 -> Color(0xFF2563EB)
                                    else -> Color.White
                                }
                            )
                        } else {
                            Text(
                                text = "◎",
                                style = MaterialTheme.typography.titleMedium,
                                color = when (mapStyleMode) {
                                    0 -> Color(0xFF111827)
                                    else -> Color.White
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isSearching) {
                Surface(
                    color = Color(0x88000000),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFCFCFD))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = selectedName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedAddress.isNotBlank()) {
                    Text(
                        text = selectedAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4B5563)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = if (isFetchingRoute) "... km"
                                   else if (routeDistanceKm > 0) String.format("%.1f km", routeDistanceKm)
                                   else "-- km",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF374151)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7ED)
                    ) {
                        Text(
                            text = if (isFetchingRoute) "... min"
                                   else if (routeEtaMinutes > 0) "$routeEtaMinutes min"
                                   else "-- min",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF9A3412)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "${radiusMeters}m",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF3730A3)
                        )
                    }
                }

                Text(
                    text = "Tap map to adjust pin",
                    style = MaterialTheme.typography.bodySmall,
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
                            val finalName = selectedName.ifBlank { searchQuery.ifBlank { "Selected location" } }
                            onPlaceSelected(selectedLatitude, selectedLongitude, finalName)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm location")
                    }
                }
            }
        }
    }
}

private fun updateMapOverlays(
    mapView: MapView?,
    latitude: Double,
    longitude: Double,
    radiusMeters: Int,
    label: String,
    snippet: String = "",
    routePoints: List<GeoPoint> = emptyList()
) {
    if (mapView == null) return
    mapView.overlays.clear()

    // Draw route polyline (Directions API equivalent via OSRM)
    if (routePoints.size >= 2) {
        val polyline = org.osmdroid.views.overlay.Polyline(mapView).apply {
            setPoints(routePoints)
            outlinePaint.color = android.graphics.Color.parseColor("#2563EB")  // Uber blue
            outlinePaint.strokeWidth = 8f
            outlinePaint.alpha = 200
            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
        }
        mapView.overlays.add(polyline)
    }

    val marker = Marker(mapView).apply {
        position = GeoPoint(latitude, longitude)
        title = label
        if (snippet.isNotBlank()) this.snippet = snippet
    }
    mapView.overlays.add(marker)

    val polygon = Polygon(mapView).apply {
        val points = mutableListOf<GeoPoint>()
        val radiusInDegrees = radiusMeters / 111000.0
        for (i in 0..360) {
            val rad = Math.toRadians(i.toDouble())
            val lat = latitude + radiusInDegrees * Math.cos(rad)
            val lon = longitude + radiusInDegrees / Math.cos(Math.toRadians(latitude)) * Math.sin(rad)
            points.add(GeoPoint(lat, lon))
        }
        setPoints(points)
        outlinePaint.color = android.graphics.Color.parseColor("#F97316")
        outlinePaint.strokeWidth = 2.5f
        outlinePaint.alpha = 170
        fillPaint.color = android.graphics.Color.parseColor("#22F97316")
    }
    mapView.overlays.add(polygon)
    mapView.invalidate()
}
