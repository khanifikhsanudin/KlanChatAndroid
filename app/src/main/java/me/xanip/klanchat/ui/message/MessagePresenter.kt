package me.xanip.klanchat.ui.message

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.utils.NetworkUtil

class MessagePresenter(private var context: Context, private var mainView: MessageView.MainView): MessageView.MainPresenter {
    override fun getHeadMessage(memberId: Int) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.get(EndPoints.HEADMESSAGE_CHAT)
                .setPriority(Priority.HIGH)
                .setTag(Constants.POST_LOGIN)
                .addHeaders(EndPoints.API_HEADER)
                .addQueryParameter("member_id", memberId.toString())
                .build().getAsObject(
                    HeadMessageResponse::class.java,
                    object : ParsedRequestListener<HeadMessageResponse> {
                        override fun onResponse(response: HeadMessageResponse) {
                            if (response.status) {
                                mainView.onHeadMessageLoaded(response)
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
}