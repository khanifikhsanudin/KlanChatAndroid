package me.xanip.klanchat.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import me.xanip.klanchat.R
import me.xanip.klanchat.database.headmessage.HeadMessageViewModel
import me.xanip.klanchat.database.member.MemberData
import me.xanip.klanchat.database.member.MemberViewModel
import me.xanip.klanchat.ui.main.MainActivity
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.Utility
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), LoginView.MainView {

    private lateinit var headMessageViewModel: HeadMessageViewModel
    private lateinit var memberViewModel: MemberViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loading: AlertDialog
    private lateinit var presenter: LoginView.MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loading = Utility.setProgressDialog(this)
        presenter = LoginPresenter(this, this)
        setupUI()
    }

    private fun setupUI() {

        toolbar.setNavigationOnClickListener { finish() }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        mbtn_login_google.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, SIGN_GOOGLE)
        }
        checkAvailableAccount()
        headMessageViewModel = ViewModelProvider(this).get(HeadMessageViewModel::class.java)
        memberViewModel = ViewModelProvider(this).get(MemberViewModel::class.java)
    }

    private fun checkAvailableAccount() {
        val logged = GoogleSignIn.getLastSignedInAccount(this)
        if (logged != null) {
            onLoggedInGoogle(logged)
        }
    }

    private fun onLoggedInGoogle(gsia: GoogleSignInAccount) {
        val memberData = MemberData(
            social_id  = gsia.id,
            name       = gsia.displayName?:"tanpa nama",
            email      = gsia.email,
            img        = gsia.photoUrl?.toString()
        )
        loading.show()
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    presenter.doLogin(JSONObject(Gson().toJson(memberData)))
                } else {
                    val token = task.result?.token
                    memberData.fcm_token = token
                    token?.let {
                        PreferencesManager.init(baseContext).fcmToken = it
                    }
                    presenter.doLogin(JSONObject(Gson().toJson(memberData)))
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_GOOGLE) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    onLoggedInGoogle(account)
                }else {
                    Toast.makeText(this@LoginActivity, "Your Google Sign is NUll", Toast.LENGTH_SHORT).show()
                }
            }catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    override fun onStartProgress() {
        loading.show()
    }

    override fun onStopProgress() {
        loading.dismiss()
    }

    override fun onSuccessLogin(memberData: MemberData) {
        headMessageViewModel.clear()
        memberViewModel.clear()
        onStopProgress()
        PreferencesManager.init(this).let {
            it.chatHelper = null
            it.totalNotifchat = 0
            it.memberData = memberData
        }
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onFailed(message: String?) {
        onStopProgress()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOffline() {
        onStopProgress()
        Toast.makeText(this, "Koneksi Internet Offline", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SIGN_GOOGLE = 1001
    }
}
