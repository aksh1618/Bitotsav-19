package `in`.bitotsav.profile.api

import `in`.bitotsav.shared.Singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthenticationService {
    @POST("login")
    fun loginAsync(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("register")
    fun registerAsync(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("verify")
    fun verifyAsync(@Body body: Any): Deferred<Response<Map<String, String>>>

    @POST("saveparticipant")
    fun saveParticipantAsync(@Body body: Any): Deferred<Response<Map<String, String>>>

    @GET("getCollegeList")
    fun getCollegeListAsync(): Deferred<Response<Map<String, List<String>>>>

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