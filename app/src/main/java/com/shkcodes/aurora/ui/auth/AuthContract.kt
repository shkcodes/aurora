package com.shkcodes.aurora.ui.auth

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.auth.AuthContract.Constant.AUTH_URL
import com.shkcodes.aurora.ui.auth.AuthContract.State.Loading
import com.shkcodes.aurora.ui.auth.AuthContract.State.RequestToken

interface AuthContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(Loading)

    sealed class State {
        object Loading : State()
        data class RequestToken(val token: String) : State() {
            val authorizationUrl = "$AUTH_URL$token"
        }
    }

    sealed class Intent {
        object Init : Intent()
        data class RequestAccessToken(
            val tokenState: RequestToken,
            val authorizationResponse: String
        ) : Intent()
    }

    object Constant {
        const val AUTH_URL = "https://api.twitter.com/oauth/authorize?oauth_token="
    }
}
