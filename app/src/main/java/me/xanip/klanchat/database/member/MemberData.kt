package me.xanip.klanchat.database.member

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = MemberData.TABLE_NAME)
@Parcelize
data class MemberData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = ID)
    var id: Int? = null,

    @ColumnInfo(name = SOCIAL_ID)
    var social_id: String? = null,

    @ColumnInfo(name = FCM_TOKEN)
    var fcm_token: String? = null,

    @ColumnInfo(name = EMAIL)
    var email: String? = null,

    @ColumnInfo(name = NAME)
    var name: String? = null,

    @ColumnInfo(name = IMG)
    var img: String? = null,

    @ColumnInfo(name = IMG_COLOR)
    var img_color: String? = null

) : Parcelable {
    companion object {
        const val TABLE_NAME  = "member"
        const val ID          = "id"
        const val SOCIAL_ID   = "social_id"
        const val FCM_TOKEN   = "fcm_token"
        const val EMAIL       = "email"
        const val NAME        = "name"
        const val IMG         = "img"
        const val IMG_COLOR   = "img_color"
    }
}