package com.pet.rsspaser.data.remote.api.middleware

import androidx.annotation.NonNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Locale

class InterceptorImpl : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = initializeHeader(chain)
        val request = builder.build()

        return chain.proceed(request)
    }

    private fun initializeHeader(@NonNull chain: Interceptor.Chain): Request.Builder {
        val originRequest = chain.request()

        return originRequest.newBuilder()
            .header("Accept", "application/json")
            .addHeader("Accept-Language", Locale.getDefault().language)
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Cache-Control", "no-store")
            .addHeader(KEY_TOKEN, TOKEN_TYPE + "appToken")
            .method(originRequest.method, originRequest.body)
    }

    companion object {
        private const val TOKEN_TYPE = "Bearer "
        private const val KEY_TOKEN = "Authorization"
    }
}
