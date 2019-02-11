package `in`.bitotsav.notification.fcm.api

import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
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
        val api: FcmTokenService by lazy {
            get().koin.get<Retrofit>()
                .create(FcmTokenService::class.java)
        }
    }
}