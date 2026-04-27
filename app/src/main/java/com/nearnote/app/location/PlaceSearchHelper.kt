package com.nearnote.app.location

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Place search using Nominatim (OpenStreetMap) as the primary source — free, no API key required.
 * Falls back to Android Geocoder for reverse geocoding when needed.
 */
class PlaceSearchHelper(private val context: Context) {
    private val geocoder = Geocoder(context)

    data class Place(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    /** Forward geocoding: text query → list of Places, using Nominatim search API */
    suspend fun searchPlaces(query: String): List<Place> = withContext(Dispatchers.IO) {
        return@withContext try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search" +
                    "?q=$encoded&format=json&addressdetails=1&limit=8"

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("User-Agent", "NearNote/1.0 Android")
            conn.setRequestProperty("Accept-Language", "en")

            if (conn.responseCode != 200) return@withContext emptyList()

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val results = JSONArray(body)
            (0 until results.length()).mapNotNull { i ->
                val obj = results.getJSONObject(i)
                val lat = obj.optDouble("lat", Double.NaN)
                val lon = obj.optDouble("lon", Double.NaN)
                if (lat.isNaN() || lon.isNaN()) return@mapNotNull null

                val displayName = obj.optString("display_name", "")
                val name = displayName.split(",").firstOrNull()?.trim() ?: "Location"
                val address = displayName.split(",").take(3).joinToString(", ").trim()

                Place(name = name, latitude = lat, longitude = lon, address = address)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Reverse geocoding: coordinates → Place, using Nominatim reverse API */
    suspend fun reverseSearch(latitude: Double, longitude: Double): Place? = withContext(Dispatchers.IO) {
        // Try Nominatim first
        try {
            val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=$latitude&lon=$longitude&format=json&addressdetails=1"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("User-Agent", "NearNote/1.0 Android")
            conn.setRequestProperty("Accept-Language", "en")

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val obj = org.json.JSONObject(body)
                val displayName = obj.optString("display_name", "")
                if (displayName.isNotBlank()) {
                    val parts = displayName.split(",")
                    val name = parts.firstOrNull()?.trim() ?: "Location"
                    val address = parts.take(3).joinToString(", ").trim()
                    return@withContext Place(name = name, latitude = latitude, longitude = longitude, address = address)
                }
            }
        } catch (_: Exception) { }

        // Fallback to Android Geocoder
        return@withContext try {
            geocoder.getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.let { address ->
                    val name = address.getAddressLine(0)
                        ?.substringBefore(',')
                        ?: address.featureName
                        ?: "Location"
                    Place(
                        name = name,
                        latitude = address.latitude,
                        longitude = address.longitude,
                        address = address.getAddressLine(0) ?: name
                    )
                }
        } catch (e: Exception) {
            null
        }
    }
}
