package com.nearnote.app.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceSearchHelper(private val context: Context) {
    private val geocoder = Geocoder(context)

    data class Place(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    suspend fun searchPlaces(query: String): List<Place> = withContext(Dispatchers.IO) {
        return@withContext try {
            geocoder.getFromLocationName(query, 10)
                ?.take(10)
                ?.mapNotNull { address ->
                    if (address.hasLatitude() && address.hasLongitude()) {
                        val name = buildString {
                            address.getAddressLine(0)?.takeIf { it.isNotEmpty() }?.let { append(it) }
                                ?: run { 
                                    address.featureName?.takeIf { it.isNotEmpty() }?.let { append(it) }
                                        ?: append("Location")
                                }
                        }
                        Place(
                            name = name.substringBefore(','),
                            latitude = address.latitude,
                            longitude = address.longitude,
                            address = address.getAddressLine(0) ?: name
                        )
                    } else {
                        null
                    }
                }
                .orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun reverseSearch(latitude: Double, longitude: Double): Place? = withContext(Dispatchers.IO) {
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
