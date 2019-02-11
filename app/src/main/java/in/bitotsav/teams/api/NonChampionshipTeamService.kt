package `in`.bitotsav.teams.api

import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface NonChampionshipTeamService {
    @POST("getTeamDetails")
    fun getNonChampionshipTeamAsync(@Body body: Any): Deferred<Response<Map<String, Any>>>

    companion object {
        val api: NonChampionshipTeamService by lazy {
            get().koin.get<Retrofit>()
                .create(NonChampionshipTeamService::class.java)
        }
    }
}