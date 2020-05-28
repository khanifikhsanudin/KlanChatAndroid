package me.xanip.klanchat.ui.chat

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import me.xanip.klanchat.database.model.StatusResponse
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.utils.NetworkUtil

class ChatPresenter(private var context: Context, private var mainView: ChatView.MainView): ChatView.MainPresenter {
    override fun fetchSeen(thread: String, memberId: Int, cursor: String?, limit: Int, page: Int) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.get(EndPoints.SEEN_CHAT)
                .setPriority(Priority.HIGH)
                .addHeaders(EndPoints.API_HEADER)
                .addQueryParameter("thread", thread)
                .addQueryParameter("member_id", memberId.toString())
                .addQueryParameter("cursor", cursor)
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("page", page.toString())
                .build().getAsObject(
                    SeenResponse::class.java,
                    object : ParsedRequestListener<SeenResponse> {
                        override fun onResponse(response: SeenResponse) {
                            if (response.status) {
                                mainView.onSeenFetched(response)
                            } else {
                                mainView.onFailedFetchSeen(response.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            mainView.onFailedFetchSeen(anError.errorDetail)
                        }
                    })
        } else {
            mainView.onFailedFetchSeen("Internet Offline")
        }
    }

    override fun fetchUnread(thread: String, memberId: Int, cursor: String) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.get(EndPoints.UNREAD_CHAT)
                .setPriority(Priority.HIGH)
                .addHeaders(EndPoints.API_HEADER)
                .addQueryParameter("thread", thread)
                .addQueryParameter("member_id", memberId.toString())
                .addQueryParameter("cursor", cursor)
                .build().getAsObject(
                    UnreadResponse::class.java,
                    object : ParsedRequestListener<UnreadResponse> {
                        override fun onResponse(response: UnreadResponse) {
                            if (response.status) {
                                mainView.onUnreadFetched(response)
                            } else {
                                mainView.onFailedFetchUnread(response.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            mainView.onFailedFetchUnread(anError.errorDetail)
                        }
                    })
        } else {
            mainView.onFailedFetchUnread("Internet Offline")
        }
    }

    override fun sendMessage(thread: String, memberId: Int, targetId: Int, text: String) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.post(EndPoints.INSERT_CHAT)
                .setPriority(Priority.HIGH)
                .addHeaders(EndPoints.API_HEADER)
                .addBodyParameter("thread", thread)
                .addBodyParameter("member_id", memberId.toString())
                .addBodyParameter("target_id", targetId.toString())
                .addBodyParameter("text", text)
                .build().getAsObject(
                    SendResponse::class.java,
                    object : ParsedRequestListener<SendResponse> {
                        override fun onResponse(response: SendResponse) {
                            if (response.status) {
                                mainView.onMessageSent(response)
                            } else {
                                mainView.onFailedSendMessage(response.message)
                            }
                        }

                        override fun onError(anError: ANError) {
                            mainView.onFailedSendMessage(anError.errorDetail)
                        }
                    })
        } else {
            mainView.onFailedSendMessage("Internet Offline")
        }
    }

    override fun readAll(thread: String, memberId: Int) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            AndroidNetworking.post(EndPoints.READALL_CHAT)
                .setPriority(Priority.HIGH)
                .addHeaders(EndPoints.API_HEADER)
                .addBodyParameter("thread", thread)
                .addBodyParameter("member_id", memberId.toString())
                .build().getAsObject(
                    StatusResponse::class.java,
                    object : ParsedRequestListener<StatusResponse> {
                        override fun onResponse(response: StatusResponse) {
//                            mainView.onOffline("berhasil")
                        }
                        override fun onError(anError: ANError) {
//                            mainView.onOffline("gagal ${anError.errorDetail}")
                        }
                    })
        } else {
//            mainView.onOffline("gagal offline")
        }
    }
}