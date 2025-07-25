package com.turutaexpress.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, comment: String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Califica tu experiencia") },
        text = {
            Column {
                Text("¿Cómo calificarías el servicio?")
                RatingBar(
                    currentRating = rating,
                    onRatingChanged = { rating = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Deja un comentario (opcional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment) },
                enabled = rating > 0
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RatingBar(
    maxRating: Int = 5,
    currentRating: Float,
    onRatingChanged: (Float) -> Unit,
    starColor: Color = Color(0xFFFFC107)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..maxRating) {
            IconButton(onClick = { onRatingChanged(i.toFloat()) }) {
                Icon(
                    imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rating $i",
                    tint = if (i <= currentRating) starColor else Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}