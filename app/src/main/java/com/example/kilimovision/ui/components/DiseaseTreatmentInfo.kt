package com.example.kilimovision.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kilimovision.model.DiseaseTreatment

@Composable
fun DiseaseTreatmentInfo(
    treatment: DiseaseTreatment
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Disease Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = if (expanded) "Hide" else "Show",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Disease image if available
                    if (treatment.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = treatment.imageUrl,
                            contentDescription = "Disease image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Description
                    Text(
                        text = "Description",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = treatment.description,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Symptoms
                    if (treatment.symptoms.isNotEmpty()) {
                        Text(
                            text = "Symptoms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        treatment.symptoms.forEach { symptom ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = symptom,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Prevention tips
                    if (treatment.preventionTips.isNotEmpty()) {
                        Text(
                            text = "Prevention Tips",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        treatment.preventionTips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = tip,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}