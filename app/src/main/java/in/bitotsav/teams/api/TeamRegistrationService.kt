package `in`.bitotsav.teams.api

import `in`.bitotsav.shared.Singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface TeamRegistrationService {
    @POST("eventRegistration")
    fun registerForEventAsync(
        @Header("token") authHeaderValue: String,
        @Body body: Any
    ): Deferred<Response<Map<String, String>>>

    @GET("eventDeregistration/{eventId}/{bitId}")
    fun deregisterForEventAsync(
        @Header("token") authHeaderValue: String,
        @Path("eventId") eventId: Int,
        @Path("bitId") bitId: String
    ): Deferred<Response<Map<String, String>>>

    @POST("championship")
    fun registerForChampionshipAsync(
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
            .create(TeamRegistrationService::class.java)
        }
    }
}