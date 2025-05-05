
package com.example.kilimovision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kilimovision.model.Consultation
import com.example.kilimovision.model.Seller
import com.example.kilimovision.viewmodel.ProfileViewModel
import com.example.kilimovision.model.Review
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDetailsDialog(
    seller: Seller,
    profileViewModel: ProfileViewModel, // Add this parameter
    onDismiss: () -> Unit
) {

    // Properly collect StateFlow as State
    val reviews by profileViewModel.sellerReviews.collectAsState()

    //UI State
    var showReviewDialog by remember { mutableStateOf(false) }

    // Load reviews when dialog is shown
    LaunchedEffect(seller.id) {
        profileViewModel.loadSellerReviews(seller.id)
    }

    if (showReviewDialog) {
        ReviewDialog(
            sellerId = seller.id,
            sellerName = seller.name,
            profileViewModel = profileViewModel,
            onDismiss = { showReviewDialog = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(seller.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
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
                            Text("Price: KES ${product.price}")
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

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews section
                Text("Reviews (${reviews.size}):", fontWeight = FontWeight.Bold)

                if (reviews.isEmpty()) {
                    Text("No reviews yet", style = MaterialTheme.typography.bodyMedium)
                } else {
                    reviews.forEach { review ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Display star rating
                                    Row {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (review.verified) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verified Purchase",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (review.productPurchased.isNotEmpty()) {
                                    Text(
                                        text = "Product: ${review.productPurchased}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(review.comment)

                                Text(
                                    text = review.date?.toDate()?.let {
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                                    } ?: "Recent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Leave a Review")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(
    sellerId: String,
    sellerName: String,
    profileViewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var productPurchased by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review $sellerName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Rating:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = productPurchased,
                    onValueChange = { productPurchased = it },
                    label = { Text("Product Purchased") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (rating > 0) {
                        profileViewModel.addReview(
                            sellerId = sellerId,
                            rating = rating,
                            comment = comment,
                            productPurchased = productPurchased
                        )
                        onDismiss()
                    }
                },
                enabled = rating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}