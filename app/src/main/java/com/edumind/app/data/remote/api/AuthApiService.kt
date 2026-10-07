package com.edumind.app.data.remote.api

import com.edumind.app.data.model.request.RefreshRequest
import com.edumind.app.data.model.response.RefreshResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {
    @Headers("No-Auth: true")
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequest): Call<RefreshResponse>
}
