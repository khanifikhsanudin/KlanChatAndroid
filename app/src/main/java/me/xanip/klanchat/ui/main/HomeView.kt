package me.xanip.klanchat.ui.main

class HomeView {
    interface MainPresenter {
        fun syncronizeChat(memberId: Int, fcmToken: String)
        fun logout(memberId: Int)
    }

    interface MainView {
        fun onStartProgress()
        fun onStopProgress()
        fun onSyncronized(response: SyncronizeResponse)
        fun onLoggedOut()
        fun onFailed(message: String?)
        fun onOffline()
    }
}