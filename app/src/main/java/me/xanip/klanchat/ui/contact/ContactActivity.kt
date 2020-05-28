package me.xanip.klanchat.ui.contact

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_contact.*
import me.xanip.klanchat.R
import me.xanip.klanchat.adapter.ContactAdapter
import me.xanip.klanchat.database.member.MemberViewModel
import me.xanip.klanchat.ui.chat.ChatActivity
import me.xanip.klanchat.ui.chat.ChatThread
import me.xanip.klanchat.utils.PreferencesManager

class ContactActivity : AppCompatActivity(), ContactView.MainView {

    private lateinit var presenter: ContactPresenter
    private lateinit var memberViewModel: MemberViewModel
    private lateinit var contactAdapter: ContactAdapter

    private var firstSync = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        presenter = ContactPresenter(this, this)
        setupView()
    }

    private fun setupView() {
        val prefs = PreferencesManager.init(this)
        val memberData = prefs.memberData

        toolbar.setNavigationOnClickListener {
            finish()
        }
        contactAdapter = ContactAdapter {
            startActivity(ChatActivity
                .crateIntent(this,
                    ChatThread(
                        memberData?.id?:-1, it.id?:-1,
                        it.name?:"", it.img, it.img_color?:""
                    )
                )
            )
            finish()
        }
        rv_contact.layoutManager = LinearLayoutManager(this)
        rv_contact.adapter = contactAdapter

        memberViewModel = ViewModelProvider(this).get(MemberViewModel::class.java)
        memberViewModel.allMemberData.observe(this, Observer {
            contactAdapter.setData(it?: emptyList())
            if (it.isNullOrEmpty()) {
                if (firstSync) {
                    Toast.makeText(this, "Data Member Kosong", Toast.LENGTH_LONG).show()
                }else {
                    onStartProgress()
                }
            }
            if (!firstSync) {
                firstSync = true
                memberData?.id?.let { memberId ->
                    presenter.getMembers(memberId)
                }
            }
        })
    }

    override fun onStartProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onStopProgress() {
        progress_bar.visibility = View.GONE
    }

    override fun onMembersLoaded(response: ContactResponse) {
        onStopProgress()
        if (!response.data.isNullOrEmpty()) {
            memberViewModel.insert(*response.data.toTypedArray())
        }else {
            memberViewModel.clear()
        }
    }

    override fun onFailed(message: String?) {
        onStopProgress()
        if (contactAdapter.itemCount < 1) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onOffline() {
        onStopProgress()
        if (contactAdapter.itemCount < 1) {
            Toast.makeText(this, "Koneksi Internet Offline", Toast.LENGTH_LONG).show()
        }
    }
}
