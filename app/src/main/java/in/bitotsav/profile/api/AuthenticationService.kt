package `in`.bitotsav.profile.api

import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
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
}