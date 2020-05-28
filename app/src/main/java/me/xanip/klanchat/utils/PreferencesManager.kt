package me.xanip.klanchat.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import me.xanip.klanchat.database.member.MemberData
import me.xanip.klanchat.database.model.ChatHelper
import me.xanip.klanchat.database.model.ReadHelper
import me.xanip.klanchat.global.Constants


class PreferencesManager {

    var prefs: SharedPreferences = preferences!!

    var fcmToken: String?
        get() = preferences!!.getString(Constants.PREF_FCM_TOKEN, "")
        set(fcmToken) {
            preferences!!.edit(commit = true) {
                putString(Constants.PREF_FCM_TOKEN, fcmToken)
            }
        }

    var memberData: MemberData?
        get() {
            val data = preferences!!.getString(Constants.PREF_MEMBER_DATA, "")
            return Gson().fromJson(data, MemberData::class.java)
        }
        set(memberData) {
            preferences!!.edit(commit = true) {
                putString(Constants.PREF_MEMBER_DATA, Gson().toJson(memberData))
            }
        }

    var totalNotifchat: Int
        get() = preferences!!.getInt(Constants.PREF_TOTAL_NOTIFCHAT, 0)
        set(total) {
            preferences!!.edit(commit = true) {
                putInt(Constants.PREF_TOTAL_NOTIFCHAT, total)
            }
        }

    var chatHelper: ChatHelper?
        get() {
            val data = preferences!!.getString(Constants.PREF_CHAT_HELPER, "")
            return Gson().fromJson(data, ChatHelper::class.java)
        }
        set(chatHelper) {
            preferences!!.edit(commit = true) {
                putString(Constants.PREF_CHAT_HELPER, Gson().toJson(chatHelper))
            }
        }

    var readHelper: ReadHelper?
        get() {
            val data = preferences!!.getString(Constants.PREF_READ_HELPER, "")
            return Gson().fromJson(data, ReadHelper::class.java)
        }
        set(readHelper) {
            preferences!!.edit(commit = true) {
                putString(Constants.PREF_READ_HELPER, Gson().toJson(readHelper))
            }
        }

    var threadActive: String?
        get() = preferences!!.getString(Constants.PREF_THREAD_ACTIVE, "")
        set(fcmToken) {
            preferences!!.edit(commit = true) {
                putString(Constants.PREF_THREAD_ACTIVE, fcmToken)
            }
        }

    companion object {
        var preferences: SharedPreferences? = null
            private set

        private lateinit var mContext: Context

        fun init(context: Context): PreferencesManager {
            preferences = context.getSharedPreferences("this", Context.MODE_PRIVATE)
            mContext = context
            return PreferencesManager()
        }
    }
}