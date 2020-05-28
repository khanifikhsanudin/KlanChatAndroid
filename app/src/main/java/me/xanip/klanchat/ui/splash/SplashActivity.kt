package me.xanip.klanchat.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import me.xanip.klanchat.R
import me.xanip.klanchat.ui.login.LoginActivity
import me.xanip.klanchat.ui.main.MainActivity
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.Utility

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = PreferencesManager.init(this)
        prefs.chatHelper = null
        prefs.threadActive = null

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    token?.let {
                        prefs.fcmToken = it
                    }
                }
            }

        Handler().postDelayed({
            if (prefs.memberData != null) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3500)
    }
}
