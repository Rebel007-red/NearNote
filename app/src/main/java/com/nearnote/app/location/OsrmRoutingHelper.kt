package com.nearnote.app.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches a driving route from OSRM (Open Source Routing Machine) — free, no API key required.
 * Uses the public demo server: router.project-osrm.org
 */
object OsrmRoutingHelper {

    data class RouteResult(
        val distanceMeters: Double,    // total road distance in metres
        val durationSeconds: Double,   // total road driving time in seconds
        val polylinePoints: List<GeoPoint>  // decoded route geometry
    )

    suspend fun getRoute(
        fromLat: Double, fromLon: Double,
        toLat: Double, toLon: Double
    ): RouteResult? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "$fromLon,$fromLat;$toLon,$toLat" +
                    "?overview=full&geometries=polyline"

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("User-Agent", "NearNote/1.0 Android")

            if (conn.responseCode != 200) return@withContext null

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            if (json.getString("code") != "Ok") return@withContext null

            val route = json.getJSONArray("routes").getJSONObject(0)
            val distance = route.getDouble("distance")
            val duration = route.getDouble("duration")
            val encodedPolyline = route.getString("geometry")

            RouteResult(
                distanceMeters = distance,
                durationSeconds = duration,
                polylinePoints = decodePolyline(encodedPolyline)
            )
        } catch (e: Exception) {
            null
        }
    }

    // Decodes Google/OSRM encoded polyline format
    private fun decodePolyline(encoded: String): List<GeoPoint> {
        val poly = mutableListOf<GeoPoint>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(GeoPoint(lat / 1e5, lng / 1e5))
        }
        return poly
    }
}
