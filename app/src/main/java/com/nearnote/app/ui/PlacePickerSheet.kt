package com.nearnote.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nearnote.app.location.PlaceSearchHelper
import kotlinx.coroutines.launch

@Composable
fun PlacePickerSheet(
    initialPlace: String = "",
    onPlaceSelected: (PlaceSearchHelper.Place) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val helper = remember { PlaceSearchHelper(context) }

    var searchQuery by remember { mutableStateOf(initialPlace) }
    var results by remember { mutableStateOf<List<PlaceSearchHelper.Place>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun performSearch() {
        if (searchQuery.isBlank()) {
            results = emptyList()
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            val places = helper.searchPlaces(searchQuery)
            results = places
            isLoading = false
            if (places.isEmpty()) {
                errorMessage = "No places found. Try a different search."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Search for a place",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Place name") },
                placeholder = { Text("e.g., 'Central Park'") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = ::performSearch,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(top = 4.dp)
            ) {
                Text("Search")
            }
        }

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFF9C3E00),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (results.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { "${it.latitude},${it.longitude}" }) { place ->
                    PlaceResultCard(
                        place = place,
                        onSelect = {
                            onPlaceSelected(place)
                            onDismiss()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }
}

@Composable
private fun PlaceResultCard(
    place: PlaceSearchHelper.Place,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF14293F)
            )
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4D5966)
            )
            Text(
                text = "%.4f, %.4f".format(place.latitude, place.longitude),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280)
            )
            TextButton(onClick = onSelect) {
                Text("Use this place")
            }
        }
    }
}
