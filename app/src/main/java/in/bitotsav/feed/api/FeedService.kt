package `in`.bitotsav.feed.api

import `in`.bitotsav.feed.data.Feed
import `in`.bitotsav.shared.Singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedService {
    @GET("getAllFeeds")
    fun getAllFeeds(): Deferred<Response<List<Feed>>>

    @POST("getFeedsAfter")
    fun getFeedsAfter(@Body body: Any): Deferred<Response<List<Feed>>>

    companion object {
//        TODO: Add custom client
        val api by lazy { Retrofit.Builder()
            .baseUrl(Singleton.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(Singleton.gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(FeedService::class.java)
        }
    }
}