package com.example.kilimovision.model

data class DiseaseTreatment(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val symptoms: List<String> = emptyList(),
    val recommendedProducts: List<String> = emptyList(),
    val preventionTips: List<String> = emptyList(),
    val imageUrl: String = ""
)