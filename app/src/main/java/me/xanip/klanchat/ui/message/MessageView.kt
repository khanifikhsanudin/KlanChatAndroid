package me.xanip.klanchat.ui.message

class MessageView {
    interface MainPresenter {
        fun getHeadMessage(memberId: Int)
    }

    interface MainView {
        fun onStartProgress()
        fun onStopProgress()
        fun onHeadMessageLoaded(response: HeadMessageResponse)
        fun onFailed(message: String?)
        fun onOffline()
    }
}