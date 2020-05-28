package me.xanip.klanchat.ui.chat

import me.xanip.klanchat.database.model.ChatData

data class SeenResponse(
    val status: Boolean,
    val message: String,
    val prev: String?,
    val next: String?,
    val data: List<ChatData>?
)