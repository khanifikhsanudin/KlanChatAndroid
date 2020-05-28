package me.xanip.klanchat.global

object EndPoints {

    private const val BASE_URL       =  "http://139.59.106.46/khanif/klanchat/rest-api/"
    val API_HEADER = mapOf(
        "Content-Type"   to "application/json",
        "api_key" to "5c93288d94b71134e61f74e95739f0d1f692390c"
    )

    const val LOGIN             = "${BASE_URL}member/insert"
    const val UPDATE_MEMBER     = "${BASE_URL}member/update"
    const val DELETE_MEMBER     = "${BASE_URL}member/delete"
    const val LOGOUT_MEMBER     = "${BASE_URL}member/logout"
    const val GET_MEMBER        = "${BASE_URL}member"

    const val INSERT_CHAT       = "${BASE_URL}chat/insert"
    const val UPDATE_CHAT       = "${BASE_URL}chat/update"
    const val DELETE_CHAT       = "${BASE_URL}chat/delete"
    const val SYNCRONIZE_CHAT   = "${BASE_URL}chat/syncronize"
    const val HEADMESSAGE_CHAT  = "${BASE_URL}chat/headmessage"
    const val SEEN_CHAT         = "${BASE_URL}chat/seen"
    const val UNREAD_CHAT       = "${BASE_URL}chat/unread"
    const val READALL_CHAT      = "${BASE_URL}chat/readall"
}