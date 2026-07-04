package com.carlosarancibia.playfit.data.remote

import com.carlosarancibia.playfit.BuildConfig
import com.carlosarancibia.playfit.data.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    private var _authManager: AuthManager? = null

    fun attachAuthManager(manager: AuthManager) {
        _authManager = manager
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url

        val manager = _authManager
        val token = manager?.cachedAccessToken

        val newUrl = url.newBuilder()
            .addQueryParameter("device_id", manager?.deviceId ?: "")
            .build()

        val request = if (token != null) {
            original.newBuilder()
                .url(newUrl)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        } else {
            original.newBuilder()
                .url(newUrl)
                .header("Content-Type", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
