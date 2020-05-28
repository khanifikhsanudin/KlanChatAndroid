package me.xanip.klanchat.ui.contact

import me.xanip.klanchat.database.member.MemberData

data class ContactResponse(
    val status: Boolean,
    val message: String,
    val data: List<MemberData>?
)