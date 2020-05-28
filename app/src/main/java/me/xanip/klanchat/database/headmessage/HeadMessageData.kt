package me.xanip.klanchat.database.headmessage

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = HeadMessageData.TABLE_NAME, indices = [Index(value = [HeadMessageData.THREAD], unique = true)])
@Parcelize
data class HeadMessageData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id: Int? = null,

    @ColumnInfo(name = THREAD)
    var thread: String? = null,

    @ColumnInfo(name = MEMBER_ID)
    var member_id: Int? = null,

    @ColumnInfo(name = TARGET_ID)
    var target_id: Int? = null,

    @ColumnInfo(name = TEXT)
    var text: String? = null,

    @ColumnInfo(name = ACTIVE)
    var active: Int? = null,

    @ColumnInfo(name = SEEN)
    var seen: Int? = null,

    @ColumnInfo(name = CREATED_AT)
    var created_at: String? = null,

    @ColumnInfo(name = UPDATED_AT)
    var updated_at: String? = null,

    @ColumnInfo(name = FROM_ID)
    var from_id: Int? = null,

    @ColumnInfo(name = FROM_NAME)
    var from_name: String? = null,

    @ColumnInfo(name = FROM_IMG)
    var from_img: String? = null,

    @ColumnInfo(name = FROM_IMG_COLOR)
    var from_img_color: String? = null,

    @ColumnInfo(name = TOTAL_UNREAD)
    var total_unread: Int? = null

) : Parcelable {
    companion object {
        const val TABLE_NAME     = "headmessage"
        const val ID             = "id"
        const val THREAD         = "thread"
        const val MEMBER_ID      = "member_id"
        const val TARGET_ID      = "target_id"
        const val TEXT           = "text"
        const val ACTIVE         = "active"
        const val SEEN           = "seen"
        const val CREATED_AT     = "created_at"
        const val UPDATED_AT     = "updated_at"
        const val FROM_ID        = "from_id"
        const val FROM_NAME      = "from_name"
        const val FROM_IMG       = "from_img"
        const val FROM_IMG_COLOR = "from_img_color"
        const val TOTAL_UNREAD   = "total_unread"
    }
}