package com.omniai.app.domain.model

data class Message(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)