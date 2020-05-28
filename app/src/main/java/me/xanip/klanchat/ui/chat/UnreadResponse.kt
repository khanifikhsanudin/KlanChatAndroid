package me.xanip.klanchat.ui.chat

import me.xanip.klanchat.database.model.ChatData

data class UnreadResponse(
    val status: Boolean,
    val message: String,
    val data: List<ChatData>?
)