package `in`.bitotsav.profile.data

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

@Entity
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val championshipTeam: String? = null
) : KoinComponent {
    var score: Int = runBlocking { championshipTeam?.let { get<ChampionshipTeamRepository>().getScoreByName(it) } ?: 0 }
    // [{"eventId":{eventName:"eventName",teamName:"teamName",rank:"rank"**}}]  Note: All values are strings
    var teams: Map<String, Map<String, String>> = getUserTeams()
    //    [{id:{email, name}}]
    var members: Map<String, Map<String, String>> = getTeamMembers()

    private fun getUserTeams(): Map<String, Map<String, String>> {
        val userTeams = mutableMapOf<String, Map<String, String>>()
        val teams = runBlocking { get<NonChampionshipTeamRepository>().getAllUserTeams() }
        teams?.forEach { team ->
            val eventName = runBlocking { get<EventRepository>().getNameById(team.eventId) }
            eventName?.let {
                userTeams[team.eventId.toString()] = mapOf(
                    "eventName" to eventName,
                    "teamName" to team.name,
                    "rank" to team.rank.toString()
                )
            }
        }
        return userTeams.toMap()
    }

    private fun getTeamMembers(): Map<String, Map<String, String>> {
        return CurrentUser.teamMembers ?: mapOf()
    }
}