package me.xanip.klanchat.ui.main

import me.xanip.klanchat.database.headmessage.HeadMessageData

data class SyncronizeResponse(
    val status: Boolean,
    val message: String,
    val notifchat: Int?,
    val headmessage: List<HeadMessageData>?
)