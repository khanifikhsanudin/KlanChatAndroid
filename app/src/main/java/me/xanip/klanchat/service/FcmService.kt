package me.xanip.klanchat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.xanip.klanchat.R
import me.xanip.klanchat.database.headmessage.HeadMessageViewModel
import me.xanip.klanchat.database.model.ChatHelper
import me.xanip.klanchat.database.model.ReadHelper
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.ui.login.LoginResponse
import me.xanip.klanchat.ui.main.MainActivity
import me.xanip.klanchat.ui.main.MainApp
import me.xanip.klanchat.ui.main.SyncronizeResponse
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.Utility
import java.net.URL
import java.util.*

class FcmService: FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"
    private lateinit var headMessageViewModel: HeadMessageViewModel

    override fun onCreate() {
        headMessageViewModel = HeadMessageViewModel(application)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID: Int = Random().nextInt(3000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        var intent = Intent(this, MainActivity::class.java)
        when(remoteMessage.data["aksi"]) {
            "chat"  -> {
//                intent = ChatActivity
//                    .createIntent(this, remoteMessage.data["data"]?:"")
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.data["judul"])
            .setContentText(remoteMessage.data["pesan"])
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(remoteMessage.data["pesan"]))
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setLights(Color.RED, 1000, 300)
            .setContentIntent(pendingIntent)

        if (remoteMessage.data["avatar"] != null) {
            try {
                val url = URL(remoteMessage.data["avatar"])
                val image: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                notificationBuilder.setLargeIcon(image)
            }catch (e: Exception) {
                Log.d("Error", e.message?:"Unknown")
            }
        }

        if (remoteMessage.data["gambar"] != null) {
            try {
                val url = URL(remoteMessage.data["gambar"])
                val image: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
            }catch (e: Exception) {
                Log.d("Error", e.message?:"Unknown")
            }
        }

        if (remoteMessage.data["waktu"] != null) {
            val timeMilliSec = Utility.getTimeMilliSec(remoteMessage.data["waktu"]!!)
            if (timeMilliSec > 0) {
                notificationBuilder.setWhen(timeMilliSec)
            }
        }

        if (remoteMessage.data["aksi"] != null) {
            val prefs =PreferencesManager.init(baseContext)
            when(remoteMessage.data["aksi"]) {
                 "chat" -> {
                    prefs.memberData?.id?.let { memberId ->
                        synchronizeChat(memberId)
                    }
                    remoteMessage.data["data"]?.let { data ->
                        var helperThread = ""
                        try {
                            val helper = Gson().fromJson(data, ChatHelper::class.java)
                            prefs.chatHelper = helper
                            helperThread = helper.thread?:""
                        }catch (e: Exception) {
                            Log.d("FCM", "Error: ${e}")
                        }

                        if (MainApp.isInBackground != false && prefs.threadActive != helperThread) {
                            notificationManager.notify(notificationID, notificationBuilder.build())
                        }
                    }
                }
                "readall" -> {
                    remoteMessage.data["data"]?.let { data ->
                        try {
                            val helper = Gson().fromJson(data, ReadHelper::class.java)
                            prefs.readHelper = helper
                        }catch (e: Exception) {
                            Log.d("FCM", "Error: ${e}")
                        }
                    }
                }
                else -> {
                    notificationManager.notify(notificationID, notificationBuilder.build())
                }
            }
        }else {
            notificationManager.notify(notificationID, notificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName: CharSequence = "New notification"
        val adminChannelDescription = "Device to device notification"
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    override fun onNewToken(token: String) {
        GlobalScope.launch {
            PreferencesManager.init(baseContext).fcmToken = token
            val memberData = PreferencesManager.init(baseContext).memberData
            memberData?.id?.let { memberId ->
                updateFcmToServer(memberId, token)
            }
        }
        super.onNewToken(token)
    }

    private fun updateFcmToServer(id: Int, fcm: String) {
        AndroidNetworking.post(EndPoints.UPDATE_MEMBER)
            .setPriority(Priority.HIGH)
            .addHeaders(EndPoints.API_HEADER)
            .addBodyParameter("id", id.toString())
            .addBodyParameter("fcm_token", fcm)
            .build().getAsObject(
                LoginResponse::class.java,
                object : ParsedRequestListener<LoginResponse> {
                    override fun onResponse(response: LoginResponse) {}
                    override fun onError(anError: ANError) {}
                })
    }

    private fun synchronizeChat(id: Int) {
        AndroidNetworking.get(EndPoints.SYNCRONIZE_CHAT)
            .setPriority(Priority.HIGH)
            .addHeaders(EndPoints.API_HEADER)
            .addQueryParameter("member_id", id.toString())
            .build().getAsObject(
                SyncronizeResponse::class.java,
                object : ParsedRequestListener<SyncronizeResponse> {
                    override fun onResponse(response: SyncronizeResponse) {
                        if (response.status) {
                            if (response.notifchat != null) {
                                PreferencesManager.init(baseContext)
                                    .totalNotifchat = response.notifchat
                            }
                            if (!response.headmessage.isNullOrEmpty()) {
                                headMessageViewModel.insert(*response.headmessage.toTypedArray())
                            }else {
                                headMessageViewModel.clear()
                            }
                        }
                    }
                    override fun onError(anError: ANError) {}
                })
    }
}