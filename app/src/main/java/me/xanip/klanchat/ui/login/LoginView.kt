package me.xanip.klanchat.ui.login

import me.xanip.klanchat.database.member.MemberData
import org.json.JSONObject

class LoginView {
    interface MainPresenter {
        fun doLogin(data: JSONObject)
    }

    interface MainView {
        fun onStartProgress()
        fun onStopProgress()
        fun onSuccessLogin(memberData: MemberData)
        fun onFailed(message: String?)
        fun onOffline()
    }
}
