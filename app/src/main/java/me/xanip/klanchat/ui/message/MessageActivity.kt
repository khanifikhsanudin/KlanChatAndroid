package me.xanip.klanchat.ui.message

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_message.*
import me.xanip.klanchat.R
import me.xanip.klanchat.adapter.HeadMessageAdapter
import me.xanip.klanchat.database.headmessage.HeadMessageViewModel
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.ui.chat.ChatActivity
import me.xanip.klanchat.ui.chat.ChatThread
import me.xanip.klanchat.ui.contact.ContactActivity
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.stringLiveData

class MessageActivity : AppCompatActivity(), MessageView.MainView {

    private lateinit var presenter: MessagePresenter
    private lateinit var headMessageViewModel: HeadMessageViewModel
    private lateinit var headMessageAdapter: HeadMessageAdapter

    private var firstSync = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        presenter = MessagePresenter(this, this)
        setupView()
    }

    private fun setupView() {
        val prefs = PreferencesManager.init(this)
        val memberData = prefs.memberData

        toolbar.setNavigationOnClickListener {
            finish()
        }

        fab_contact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
        headMessageAdapter = HeadMessageAdapter {
            startActivity(
                ChatActivity
                    .crateIntent(this,
                        ChatThread(
                            memberData?.id?:-1, it.from_id?:-1,
                            it.from_name?:"", it.from_img, it.from_img_color?:""
                        )
                    )
            )
        }
        rv_message.layoutManager = LinearLayoutManager(this)
        rv_message.adapter = headMessageAdapter

        headMessageViewModel = ViewModelProvider(this).get(HeadMessageViewModel::class.java)
        headMessageViewModel.allHeadMessage.observe(this, Observer { listHeadMessage ->
            headMessageAdapter.setData(listHeadMessage?: emptyList())
            if (listHeadMessage.isNullOrEmpty()) {
                val unreadCount = listHeadMessage.map { it.total_unread?:0 }.sum()
                prefs.totalNotifchat = unreadCount
                toolbar.subtitle = if (unreadCount > 0) "$unreadCount pesan belum dibaca" else ""
                if (firstSync) {
                    tv_empty.visibility = View.VISIBLE
                }else {
                    onStartProgress()
                    firstSync = true
                    memberData?.id?.let {memberId ->
                        presenter.getHeadMessage(memberId)
                    }
                }
            }else {
                tv_empty.visibility = View.GONE
                prefs.totalNotifchat = 0
                toolbar.subtitle = ""
            }
        })

        prefs.prefs.stringLiveData(Constants.PREF_READ_HELPER, "")
            .observe(this, Observer {
                if (!it.isNullOrEmpty() && headMessageAdapter.itemCount > 0) {
                    val readHelper = prefs.readHelper
                    readHelper?.let {
                        headMessageAdapter.getByThread(readHelper.thread?:"")?.let { headMessageData ->
                            if (headMessageData.created_at == readHelper.last && headMessageData.seen != 1) {
                                headMessageViewModel.update(headMessageData.copy(seen = 1))
                            }
                        }
                    }
                }
            })
    }

    override fun onStartProgress() {
        tv_empty.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    override fun onStopProgress() {
        progress_bar.visibility = View.GONE
    }

    override fun onHeadMessageLoaded(response: HeadMessageResponse) {
        onStopProgress()
        if (!response.data.isNullOrEmpty()) {
            headMessageViewModel.insert(*response.data.toTypedArray())
        }else {
            headMessageViewModel.clear()
            tv_empty.visibility = View.VISIBLE
        }
    }

    override fun onFailed(message: String?) {
        onStopProgress()
        if (headMessageAdapter.itemCount < 1) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onOffline() {
        onStopProgress()
        if (headMessageAdapter.itemCount < 1) {
            Toast.makeText(this, "Koneksi Internet Offline", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (headMessageAdapter.itemCount < 1) {
            val memberData = PreferencesManager.init(this).memberData
            memberData?.id?.let { memberId ->
                presenter.getHeadMessage(memberId)
            }
        }
    }
}
