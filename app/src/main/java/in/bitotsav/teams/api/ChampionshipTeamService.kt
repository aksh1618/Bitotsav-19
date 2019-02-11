package `in`.bitotsav.teams.api

import `in`.bitotsav.teams.championship.data.ChampionshipTeam
import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChampionshipTeamService {
    @POST("getBCTeamByName")
    fun getChampionshipTeamByNameAsync(@Body body: Any): Deferred<Response<ChampionshipTeam>>

    @GET("getAllBCTeams")
    fun getAllChampionshipTeamsAsync(): Deferred<Response<List<ChampionshipTeam>>>

    companion object {
        val api: ChampionshipTeamService by lazy {
            get().koin.get<Retrofit>()
                .create(ChampionshipTeamService::class.java)
        }
    }
}