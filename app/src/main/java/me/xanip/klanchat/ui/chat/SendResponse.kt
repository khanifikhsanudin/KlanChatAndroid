package me.xanip.klanchat.ui.chat

import me.xanip.klanchat.database.model.ChatData

data class SendResponse(
    val status: Boolean,
    val message: String,
    val data: ChatData?
)