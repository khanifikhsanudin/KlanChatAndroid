package me.xanip.klanchat.ui.message

import me.xanip.klanchat.database.headmessage.HeadMessageData

data class HeadMessageResponse(
    val status: Boolean,
    val message: String,
    val data: List<HeadMessageData>?
)