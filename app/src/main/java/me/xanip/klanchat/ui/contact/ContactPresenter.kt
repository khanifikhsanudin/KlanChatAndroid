package me.xanip.klanchat.ui.contact

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.utils.NetworkUtil

class ContactPresenter(private var context: Context, private var mainView: ContactView.MainView): ContactView.MainPresenter {
    override fun getMembers(memberId: Int) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.get(EndPoints.GET_MEMBER)
                .setPriority(Priority.HIGH)
                .setTag(Constants.POST_LOGIN)
                .addHeaders(EndPoints.API_HEADER)
                .addQueryParameter("id", memberId.toString())
                .build().getAsObject(
                    ContactResponse::class.java,
                    object : ParsedRequestListener<ContactResponse> {
                        override fun onResponse(response: ContactResponse) {
                            if (response.status) {
                                mainView.onMembersLoaded(response)
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