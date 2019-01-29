package `in`.bitotsav.feed.api

import `in`.bitotsav.feed.data.Feed
import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedService {
    @GET("getAllFeeds")
    fun getAllFeedsAsync(): Deferred<Response<List<Feed>>>

    @POST("getFeedsAfter")
    fun getFeedsAfterAsync(@Body body: Any): Deferred<Response<List<Feed>>>

    companion object {
        val api: FeedService by lazy {
            get().koin.get<Retrofit>()
                .create(FeedService::class.java)
        }
    }
}