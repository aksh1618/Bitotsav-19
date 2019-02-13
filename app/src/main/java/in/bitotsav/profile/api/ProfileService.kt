package `in`.bitotsav.profile.api

import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

interface ProfileService {
    @GET("getParticipantDetails")
    fun getParticipantDetailsAsync(@Header("token") authHeaderValue: String): Deferred<Response<Map<String, Any>>>

    @GET("getPaymentDetails")
    fun getPaymentDetailsAsync(@Header("token") authHeaderValue: String): Deferred<Response<Map<String, Boolean>>>

    companion object {
        val api: ProfileService by lazy {
            get().koin.get<Retrofit>()
                .create(ProfileService::class.java)
        }
    }
}