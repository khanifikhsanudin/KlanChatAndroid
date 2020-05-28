package me.xanip.klanchat.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_main.*
import me.xanip.klanchat.R
import me.xanip.klanchat.database.headmessage.HeadMessageViewModel
import me.xanip.klanchat.database.member.MemberData
import me.xanip.klanchat.database.member.MemberViewModel
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.ui.login.LoginActivity
import me.xanip.klanchat.ui.message.MessageActivity
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.Utility
import me.xanip.klanchat.utils.intLiveData
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

class MainActivity : AppCompatActivity(), HomeView.MainView {

    private lateinit var loading: AlertDialog
    private lateinit var presenter: HomePresenter
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var headMessageViewModel: HeadMessageViewModel
    private lateinit var memberViewModel: MemberViewModel

    private lateinit var notifchatBadge: Badge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loading = Utility.setProgressDialog(this)
        presenter = HomePresenter(this, this)
        setupView()
    }

    private fun setupView() {
        val prefs = PreferencesManager.init(this)
        val memberData = prefs.memberData

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        if (memberData != null) {
            if (!memberData.img.isNullOrEmpty()) {
                tv_avatar.visibility = View.GONE
                Glide.with(this)
                    .load(memberData.img)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_avatar)
            }else {
                tv_avatar.setBackgroundColor(Color.parseColor(memberData.img_color))
                tv_avatar.text = memberData.name?.substring(0, 1)?:"?"
                tv_avatar.visibility = View.VISIBLE
            }
            tv_name.text = memberData.name
            tv_email.text = memberData.email

            presenter.syncronizeChat(memberData.id!!, memberData.fcm_token!!)
        }else {
            finish()
        }

        notifchatBadge = QBadgeView(this)
            .bindTarget(iv_message)
            .setBadgeGravity(Gravity.TOP or Gravity.END)
            .setGravityOffset(1f, true)
            .setBadgeTextSize(10f, true)
            .setShowShadow(true)

        iv_message.setOnClickListener {
            startActivity(Intent(this, MessageActivity::class.java))
        }

        ll_logout.setOnClickListener {
            showDialogLogout(memberData)
        }

        headMessageViewModel = ViewModelProvider(this).get(HeadMessageViewModel::class.java)
        memberViewModel = ViewModelProvider(this).get(MemberViewModel::class.java)

        prefs.prefs.intLiveData(Constants.PREF_TOTAL_NOTIFCHAT, 0)
            .observe(this, Observer {
                notifchatBadge.badgeText = if (it > 0) it.toString() else null
        })

    }

    private fun showDialogLogout(memberData: MemberData?) {
        AlertDialog.Builder(this)
            .setTitle("Logout Akun")
            .setMessage("Apakah anda ingin logout?")
            .setPositiveButton("LOGOUT") { _, _ ->
                memberData?.id?.let {
                    presenter.logout(it)
                }
            }.setCancelable(true)
            .setNegativeButton("BATAL", null)
            .create().show()
    }

    private fun logoutAccountResolver() {
        val logged = GoogleSignIn.getLastSignedInAccount(this)
        val pref = PreferencesManager.init(this)
        if (logged != null) {
            googleSignInClient.signOut().addOnCompleteListener {
                headMessageViewModel.clear()
                memberViewModel.clear()
                pref.totalNotifchat = 0
                pref.chatHelper = null
                pref.memberData = null
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }else {
            headMessageViewModel.clear()
            memberViewModel.clear()
            pref.totalNotifchat = 0
            pref.chatHelper = null
            pref.memberData = null
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onStartProgress() {
        loading.show()
    }

    override fun onStopProgress() {
        loading.dismiss()
    }

    override fun onSyncronized(response: SyncronizeResponse) {
        onStopProgress()
        PreferencesManager.init(this).totalNotifchat = response.notifchat?:0
        if (!response.headmessage.isNullOrEmpty()) {
            headMessageViewModel.insert(*response.headmessage.toTypedArray())
        }else {
            headMessageViewModel.clear()
        }
    }

    override fun onLoggedOut() {
        onStopProgress()
        logoutAccountResolver()
    }

    override fun onFailed(message: String?) {
        onStopProgress()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onOffline() {
        onStopProgress()
        Toast.makeText(this, "Anda Sedang Offline", Toast.LENGTH_LONG).show()
    }
}
