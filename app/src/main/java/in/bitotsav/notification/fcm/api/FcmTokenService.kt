package `in`.bitotsav.notification.fcm.api

import `in`.bitotsav.shared.Singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmTokenService {
    @POST("addFCMToken")
    fun addFcmTokenAsync(
        @Header("token") authHeaderValue: String,
        @Body body: Any
    ): Deferred<Response<Map<String, String>>>

    @POST("removeFCMToken")
    fun removeFcmTokenAsync(
        @Header("token") authHeaderValue: String,
        @Body body: Any
    ): Deferred<Response<Map<String, String>>>

    companion object {
//        TODO: Add custom client
        val api by lazy { Retrofit.Builder()
            .baseUrl(Singleton.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(Singleton.gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(FcmTokenService::class.java)
        }
    }
}