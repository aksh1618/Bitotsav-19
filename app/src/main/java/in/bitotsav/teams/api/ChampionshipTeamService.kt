package `in`.bitotsav.teams.api

import `in`.bitotsav.teams.championship.data.ChampionshipTeam
import kotlinx.coroutines.Deferred
import org.koin.core.context.GlobalContext.get
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface ChampionshipTeamService {
    @GET("getAllBCTeams")
    fun getAllChampionshipTeamsAsync(): Deferred<Response<List<ChampionshipTeam>>>

    companion object {
        //        TODO: Add custom client
        val api by lazy {
            get().koin.get<Retrofit>()
                .create(ChampionshipTeamService::class.java)
        }
    }
}