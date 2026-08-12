package com.example.talkmy.domain.models

data class Task(
    var id: Int = 0,
    var note: String,
    val date: Long = System.currentTimeMillis()
)