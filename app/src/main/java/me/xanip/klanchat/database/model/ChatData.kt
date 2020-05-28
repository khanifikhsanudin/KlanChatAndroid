package me.xanip.klanchat.database.model

data class ChatData(
    val id: Int?,
    val thread: String?,
    val member_id: Int?,
    val target_id: Int?,
    val text: String?,
    val active: Int?,
    val seen: Int?,
    val created_at: String?,
    val updated_at: String?,
    val me: Boolean?
)