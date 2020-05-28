package me.xanip.klanchat.ui.chat

class ChatView {
    interface MainPresenter {
        fun fetchSeen(thread: String, memberId: Int, cursor: String?, limit: Int, page: Int)
        fun fetchUnread(thread: String, memberId: Int, cursor: String)
        fun sendMessage(thread: String, memberId: Int, targetId: Int, text: String)
        fun readAll(thread: String, memberId: Int)
    }

    interface MainView {
        fun onStartProgress()
        fun onStopProgress()
        fun onSeenFetched(response: SeenResponse)
        fun onUnreadFetched(response: UnreadResponse)
        fun onMessageSent(response: SendResponse)
        fun onFailedFetchSeen(message: String?)
        fun onFailedFetchUnread(message: String?)
        fun onFailedSendMessage(message: String?)
        fun onOffline(message: String?)
    }
}