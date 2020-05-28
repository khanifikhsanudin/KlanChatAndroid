package me.xanip.klanchat.ui.contact

class ContactView {
    interface MainPresenter {
        fun getMembers(memberId: Int)
    }

    interface MainView {
        fun onStartProgress()
        fun onStopProgress()
        fun onMembersLoaded(response: ContactResponse)
        fun onFailed(message: String?)
        fun onOffline()
    }
}