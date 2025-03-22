package com.example.kilimovision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kilimovision.model.Seller

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDetailsDialog(
    seller: Seller,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(seller.name) },
        text = {
            Column {
                Text("Address: ${seller.address}")
                Text("Phone: ${seller.phone}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Available Products:", fontWeight = FontWeight.Bold)
                seller.products.forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(product.name)
                            Text("Price: KES${product.price}")
                            Text(
                                text = if (product.availability) "In Stock" else "Out of Stock",
                                color = if (product.availability)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}