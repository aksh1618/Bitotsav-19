package `in`.bitotsav.events.api

import `in`.bitotsav.events.data.Event
import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EventService {
    @GET("getAllEvents")
    fun getAllAsync(): Deferred<Response<List<Event>>>

    @POST("getEventById")
    fun getByIdAsync(@Body body: Any): Deferred<Response<Event>>

    companion object {
        //        TODO: Add custom client
        val api by lazy {
            get().koin.get<Retrofit>()
                .create(EventService::class.java)
        }
    }
}