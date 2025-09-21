package com.example.myapplication.ui.incident

data class Incident(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String,
    val description: String,
    val dateTime: String,
    val timestamp: Long
)