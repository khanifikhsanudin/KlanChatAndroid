package me.xanip.klanchat.ui.main

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import me.xanip.klanchat.database.model.StatusResponse
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.utils.NetworkUtil

class HomePresenter(private var context: Context, private var mainView: HomeView.MainView): HomeView.MainPresenter {
    override fun syncronizeChat(memberId: Int, fcmToken: String) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.get(EndPoints.SYNCRONIZE_CHAT)
                .setPriority(Priority.HIGH)
                .setTag(Constants.POST_LOGIN)
                .addHeaders(EndPoints.API_HEADER)
                .addQueryParameter("member_id", memberId.toString())
                .addQueryParameter("fcm_token", fcmToken)
                .build().getAsObject(
                    SyncronizeResponse::class.java,
                    object : ParsedRequestListener<SyncronizeResponse> {
                        override fun onResponse(response: SyncronizeResponse) {
                            if (response.status) {
                                mainView.onSyncronized(response)
                            } else {
                                mainView.onFailed(response.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            mainView.onFailed(anError.errorDetail)
                        }
                    })
        } else {
            mainView.onOffline()
        }
    }

    override fun logout(memberId: Int) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            mainView.onStartProgress()
            AndroidNetworking.post(EndPoints.LOGOUT_MEMBER)
                .setPriority(Priority.HIGH)
                .setTag(Constants.POST_LOGIN)
                .addHeaders(EndPoints.API_HEADER)
                .addBodyParameter("id", memberId.toString())
                .build().getAsObject(
                    StatusResponse::class.java,
                    object : ParsedRequestListener<StatusResponse> {
                        override fun onResponse(response: StatusResponse) {
                            if (response.status) {
                                mainView.onLoggedOut()
                            } else {
                                mainView.onFailed(response.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            mainView.onFailed(anError.errorDetail)
                        }
                    })
        }else {
            mainView.onOffline()
        }
    }
}