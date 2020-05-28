package me.xanip.klanchat.ui.chat

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.activity_chat.*
import me.xanip.klanchat.R
import me.xanip.klanchat.adapter.ChatAdapter
import me.xanip.klanchat.database.headmessage.HeadMessageData
import me.xanip.klanchat.database.headmessage.HeadMessageViewModel
import me.xanip.klanchat.database.model.ChatData
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.global.Extras
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.stringLiveData
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

class ChatActivity : AppCompatActivity(), ChatView.MainView {

    private val chatThread: ChatThread by lazy { intent.getParcelableExtra<ChatThread>(Extras.EXTRA_CHAT_THREAD) }

    private lateinit var presenter: ChatPresenter
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var headMessageViewModel: HeadMessageViewModel

    private var headMessageData: HeadMessageData? = null
    private var session: Long? = null
    private var firstSync = false
    private var blockFabDown = false
    private var timeOutToReadAll = 1
    private var atRuntime = false

    private val limitChat = 20
    private var cursorChat: String = ""

    private lateinit var newMessageBadge: Badge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        fab_down.hide()
        presenter = ChatPresenter(this, this)
        setupView()
    }

    private fun setupView() {
        val prefs = PreferencesManager.init(this)
        prefs.threadActive = chatThread.thread

        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = chatThread.from_name
        if (!chatThread.from_img.isNullOrEmpty()) {
            tv_from_avatar.visibility = View.GONE
            Glide.with(this)
                .load(chatThread.from_img)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_from_avatar)
        }else {
            tv_from_avatar.setBackgroundColor(Color.parseColor(chatThread.from_img_color))
            tv_from_avatar.text = chatThread.from_name.substring(0, 1)
            tv_from_avatar.visibility = View.VISIBLE
        }
        tv_from_name.text = chatThread.from_name

        chatAdapter = ChatAdapter { loadMoreData ->
            val page = loadMoreData.page?.toIntOrNull()
            page?.let {
                chatAdapter.setLoadingLoadMore(true)
                presenter.fetchSeen(chatThread.thread, chatThread.member_id, loadMoreData.cursor, limitChat, it)
            }
        }
        rv_chat.layoutManager = LinearLayoutManager(this)
        rv_chat.adapter = chatAdapter

        newMessageBadge = QBadgeView(this)
            .bindTarget(fab_down)
            .setBadgeGravity(Gravity.TOP or Gravity.END)
            .setGravityOffset(4f, true)
            .setBadgeTextSize(10f, true)
            .setShowShadow(false)

        session = prefs.chatHelper?.timemillis

        prefs.prefs.stringLiveData(Constants.PREF_CHAT_HELPER, "")
            .observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    val chatHelper = prefs.chatHelper
                    chatHelper?.let {
                        if (chatHelper.thread == chatThread.thread && firstSync && chatHelper.timemillis != session) {
                            session = chatHelper.timemillis
                            if (chatHelper.chat != null) {
                                if (!atRuntime) {
                                    startReadAll()
                                }else {
                                    if (timeOutToReadAll >=1){
                                        timeOutToReadAll = 2
                                    }
                                }
                                resolveNewChat(chatHelper.chat)
                            }else {
                                presenter
                                    .fetchUnread(chatThread.thread, chatThread.member_id, chatAdapter.getLastChatTime()?:"0")
                            }
                        }
                    }
                }
            })

        prefs.prefs.stringLiveData(Constants.PREF_READ_HELPER, "")
            .observe(this, Observer {
                if (!it.isNullOrEmpty() && firstSync) {
                    val readHelper = prefs.readHelper
                    readHelper?.let {
                        if (readHelper.thread == chatThread.thread && firstSync) {
                            val added = chatAdapter.addSeek(readHelper.last?:"")
                            if (added && isOnMinimalToBottom()) {
                                smoothToBottom()
                            }
                        }
                    }
                }
            })

        ed_message.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (ed_message.text.toString().trim().isNotEmpty() && firstSync) {
                    ll_send.isEnabled = true
                    iv_send.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.colorBaseOrange), PorterDuff.Mode.SRC_IN)
                }else {
                    ll_send.isEnabled = false
                    iv_send.setColorFilter(ContextCompat.getColor(this@ChatActivity, R.color.grey_med_dark), PorterDuff.Mode.SRC_IN)
                }
            }
        })
        ed_message.setOnClickListener {
            if (isOnMinimalToBottom()) {
                blockFabDown = true
                smoothToBottom()
            }
        }
        ed_message.requestFocus()

        rv_chat.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isOnMinimalToBottom() && !blockFabDown) {
                    fab_down.show()
                }else {
                    fab_down.hide()
                    newMessageBadge.badgeText = null
                }
            }
        })

        fab_down.setOnClickListener {
            if (chatAdapter.getNewMessagePosition() >= 0) {
                if ((rv_chat.layoutManager as LinearLayoutManager)
                        .findLastVisibleItemPosition() < chatAdapter.getNewMessagePosition()
                ) {
                    Handler().postDelayed({
                        rv_chat.scrollToPosition(chatAdapter.getNewMessagePosition())
                    }, 300)
                }else {
                    smoothToBottom()
                }
            }else {
                smoothToBottom()
            }
            newMessageBadge.badgeText = null
        }

        ll_send.isEnabled = false
        iv_send.setColorFilter(ContextCompat.getColor(this, R.color.grey_med_dark), PorterDuff.Mode.SRC_IN)
        ll_send.setOnClickListener {
            presenter.sendMessage(chatThread.thread, chatThread.member_id, chatThread.from_id, ed_message.text.toString().trim())
            ed_message.setText("")
        }

        headMessageViewModel = ViewModelProvider(this).get(HeadMessageViewModel::class.java)
        headMessageViewModel.getByThread(chatThread.thread).observe(this, Observer {
            headMessageData = it
        })

        btn_reload.setOnClickListener {
            onStartProgress()
            presenter.fetchSeen(chatThread.thread, chatThread.member_id, "", limitChat, 1)
        }

        onStartProgress()
        blockFabDown = true
        presenter.fetchSeen(chatThread.thread, chatThread.member_id, "", limitChat, 1)
    }

    private fun isOnMinimalToBottom(): Boolean {
        if ((rv_chat.layoutManager as LinearLayoutManager)
                .findLastVisibleItemPosition() > chatAdapter.itemCount - 3) {
            return true
        }
        return false
    }

    private fun smoothToBottom() {
        Handler().postDelayed({
            rv_chat.scrollToPosition(chatAdapter.itemCount - 1)
            blockFabDown = false
        }, 200)
    }

    private fun resolveNewChat(chatData: ChatData) {
        tv_empty.visibility = View.GONE
        chatAdapter.removeSeek()
        chatAdapter.addRangeDataBottom(listOf(chatData))
        chatAdapter.increaseNewMessage(1)
        if (!isOnMinimalToBottom()) {
            val count = (newMessageBadge.badgeText?.toIntOrNull()?:0) + 1
            newMessageBadge.badgeText = if (count > 0) count.toString() else null
        }else {
            smoothToBottom()
        }
    }

    private fun startReadAll() {
        atRuntime = true
        val handler = Handler()
        val runnable = object: Runnable {
            override fun run() {
                if (timeOutToReadAll >= 1) {
                    handler.postDelayed(this, 1000)
                    timeOutToReadAll -= 1
                }else {
//                    Toast.makeText(this@ChatActivity, "START READ ALL", Toast.LENGTH_SHORT).show()
                    presenter.readAll(chatThread.thread, chatThread.member_id)
                    timeOutToReadAll = 1
                    atRuntime = false
                }
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    override fun onStartProgress() {
        ll_reload.visibility = View.GONE
        tv_empty.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    override fun onStopProgress() {
        progress_bar.visibility = View.GONE
    }

    override fun onSeenFetched(response: SeenResponse) {
        onStopProgress()
        chatAdapter.setLoadingLoadMore(false)
        chatAdapter.removeLoadmore()

        val chats = mutableListOf<Any>()
        if (!response.data.isNullOrEmpty()) {
            tv_empty.visibility = View.GONE

            if (!firstSync) {
                val indexC: Int = if (response.data.size > limitChat) limitChat else response.data.size
                cursorChat = response.data[indexC - 1].created_at?:""
            }

            chats.addAll(response.data)
            val indexUread = response.data.indexOfFirst { it.me == false && it.seen?:0 <= 0 }
            if (indexUread >= 0) {
                chats.add(indexUread, response.data.size - indexUread)
                chatAdapter.removeNewMessage()
            }
            if (!response.next.isNullOrEmpty()) {
                chatAdapter.removeLoadmore()
                chats.add(0, ChatAdapter.LoadMoreData(response.next, cursorChat))
            }
            if (chatAdapter.itemCount < 1 && response.data.last().me == true && response.data.last().seen == 1) {
                chats.add("Dilihat")
            }
            chatAdapter.addRangeDataTop(chats)
            if (chatAdapter.getNewMessagePosition() >= 0) {
                val newMessagePosition = chatAdapter.getNewMessagePosition()
                val avaliableBellow = chatAdapter.itemCount - newMessagePosition
                Handler().postDelayed({
                    rv_chat.scrollToPosition(if (avaliableBellow <= 5) (chatAdapter.itemCount - 1) else newMessagePosition + 5)
                    blockFabDown = false //must be add this line for enable fab down
                }, 300)

            }else {
                if (!firstSync) {
                    smoothToBottom()
                }
            }

//            val aa = response.data
//                .map { Utility.getDateOnly(it.created_at?:"") }
//                .toSet().map { bb -> response.data.indexOfFirst {  Utility.getDateOnly(it.created_at?:"") == bb} }
//            Toast.makeText(this@ChatActivity, Gson().toJson(aa), Toast.LENGTH_SHORT).show()

        }else {
            if (chatAdapter.itemCount == 0) {
                tv_empty.visibility = View.VISIBLE
                blockFabDown = false //must be add this line for enable fab down
            }
        }
        if (!firstSync) {
            if (ed_message.text.toString().trim().isNotEmpty()) {
                ll_send.isEnabled = true
                iv_send.setColorFilter(ContextCompat.getColor(this, R.color.colorBaseOrange), PorterDuff.Mode.SRC_IN)
            }
            firstSync = true
        }
    }

    override fun onUnreadFetched(response: UnreadResponse) {
        onStopProgress()
        if (!response.data.isNullOrEmpty()) {
            tv_empty.visibility = View.GONE
            val chats = chatAdapter.filterTruthList(response.data)
            chatAdapter.addRangeDataBottom(chats)
            chatAdapter.increaseNewMessage(chats.size)
            if (!isOnMinimalToBottom()) {
                val count = (newMessageBadge.badgeText?.toIntOrNull()?:0) + response.data.size
                newMessageBadge.badgeText = if (count > 0) count.toString() else null
            }else {
                smoothToBottom()
            }
        }
    }

    override fun onMessageSent(response: SendResponse) {
        response.data?.let {
            tv_empty.visibility = View.GONE
            chatAdapter.removeNewMessage()
            chatAdapter.removeSeek()
            chatAdapter.addRangeDataBottom(listOf(it))
            smoothToBottom()
        }
    }

    override fun onFailedFetchSeen(message: String?) {
        onStopProgress()
        chatAdapter.setLoadingLoadMore(false)
        if (chatAdapter.itemCount < 1) {
            ll_reload.visibility = View.VISIBLE
        }
    }

    override fun onFailedFetchUnread(message: String?) {
        onStopProgress()
        Toast.makeText(this, "Sepertinya ada pesan masuk baru", Toast.LENGTH_LONG).show()
    }

    override fun onFailedSendMessage(message: String?) {
        Toast.makeText(this, "Beberapa pesan gagal dikirim.", Toast.LENGTH_LONG).show()
    }

    override fun onOffline(message: String?) {
        onStopProgress()
        Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun finish() {
        val prefs = PreferencesManager.init(this)
        prefs.threadActive = ""

        chatAdapter.getLastChat()?.let {
            if (headMessageData != null){
                headMessageData!!.copy(
                    total_unread = 0, member_id = it.member_id,
                    text = it.text, created_at = it.created_at,
                    updated_at = it.updated_at,
                    active = it.active, seen = if (chatAdapter.isHasSeek()) 1 else it.seen
                ).let { headMessage ->
                    if (headMessage.total_unread?:0 > 0) {
                        prefs.totalNotifchat -= headMessage.total_unread?:0
                    }
                    headMessageViewModel.update(headMessage)
                }
            }else {
                headMessageViewModel.insert(
                    HeadMessageData(
                        thread = it.thread, member_id = it.member_id, target_id = it.target_id,
                        text = it.text, active = it.active, seen = if (chatAdapter.isHasSeek()) 1 else it.seen,
                        created_at = it.created_at, updated_at = it.updated_at, from_id = chatThread.from_id,
                        from_name = chatThread.from_name, from_img = chatThread.from_img,
                        from_img_color = chatThread.from_img_color, total_unread = 0
                    )
                )
            }
        }
        super.finish()
    }

    companion object {
        fun crateIntent(context: Context, chatThread: ChatThread): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Extras.EXTRA_CHAT_THREAD, chatThread)
            return intent
        }
    }
}
