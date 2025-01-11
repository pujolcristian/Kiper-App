package com.kiper.core.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class ConditionalLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val contentType = request.body?.contentType()?.toString()

        if (contentType?.contains("audio") == true || contentType?.contains("video") == true) {
            Log.d("ConditionalLogging", "Skipping logging for binary request.")
            return chain.proceed(request)
        }

        // Proceed with the request and log the response
        val response = chain.proceed(request)

        // Use peekBody to log without consuming the response
        val responseBody = response.peekBody(1024 * 1024) // Up to 1 MB of the response body
        Log.d("ConditionalLogging", "Response: ${responseBody.string()}")

        return response // Return the original response for further processing
    }
}
