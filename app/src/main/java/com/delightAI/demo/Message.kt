package com.delightAI.demo

import java.util.UUID

data class Message(
    val isUser: Boolean,
    val text: String,
    val id: String = UUID.randomUUID().toString()
)