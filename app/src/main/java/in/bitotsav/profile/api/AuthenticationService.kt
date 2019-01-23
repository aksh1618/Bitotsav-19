package `in`.bitotsav.profile.api

import `in`.bitotsav.shared.Singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationService {
    @POST("login")
    fun login(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("register")
    fun register(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("verify")
    fun verify(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("saveparticipant")
    fun saveParticipant(@Body body: Any): Deferred<Response<Map<String, String>>>

    companion object {
//        TODO: Add custom client
        val api by lazy { Retrofit.Builder()
            .baseUrl(Singleton.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(Singleton.gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(AuthenticationService::class.java)
        }
    }
}