package com.shkcodes.aurora.ui.auth

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.ui.auth.AuthContract.Constant.AUTH_URL
import com.shkcodes.aurora.ui.auth.AuthContract.State.Loading

interface AuthContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(Loading)

    sealed class State {
        object Loading : State()
        data class Error(val message: String) : State()
        data class RequestToken(val authorization: Authorization) : State() {
            val authorizationUrl = "$AUTH_URL${authorization.token}"
        }
    }

    sealed class Intent {
        object Retry : Intent()
        data class RequestAccessToken(
            val authorizationResponse: String
        ) : Intent()
    }

    object Constant {
        const val AUTH_URL = "https://api.twitter.com/oauth/authorize?oauth_token="
    }
}
