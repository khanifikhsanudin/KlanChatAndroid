package me.xanip.klanchat.ui.login

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.xanip.klanchat.database.member.MemberData

@Parcelize
data class LoginResponse(
    val status: Boolean,
    val message: String,
    val data: MemberData?
) : Parcelable