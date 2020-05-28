package me.xanip.klanchat.ui.login

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import me.xanip.klanchat.global.Constants
import me.xanip.klanchat.global.EndPoints
import me.xanip.klanchat.utils.NetworkUtil
import org.json.JSONObject

class LoginPresenter(var context: Context, var mainView: LoginView.MainView): LoginView.MainPresenter {
    override fun doLogin(data: JSONObject) {
        if (NetworkUtil.instance!!.isNetworkAvailable(context.applicationContext)) {
            mainView.onStartProgress()
            AndroidNetworking.post(EndPoints.LOGIN)
                .setPriority(Priority.HIGH)
                .setTag(Constants.POST_LOGIN)
                .addHeaders(EndPoints.API_HEADER)
                .addJSONObjectBody(data)
                .build().getAsObject(
                    LoginResponse::class.java,
                    object : ParsedRequestListener<LoginResponse> {
                        override fun onResponse(response: LoginResponse) {
                            if (response.status) {
                                mainView.onSuccessLogin(response.data!!)
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