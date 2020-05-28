package me.xanip.klanchat.ui.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.math.max
import kotlin.math.min

@Parcelize
data class ChatThread(
    val member_id: Int,
    val from_id: Int,
    val from_name: String,
    val from_img: String? = null,
    val from_img_color: String
) : Parcelable {

    val thread: String
        get() = "${min(member_id, from_id)}_${max(member_id, from_id)}"
}