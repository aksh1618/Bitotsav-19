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
    val championshipTeam: String? = null,
    val day1: Boolean = false,
    val day2: Boolean = false,
    val day3: Boolean = false,
    val merchandise: Boolean = false,
    val accommodation: Boolean = false
) : KoinComponent {
    var score: Int = runBlocking { championshipTeam?.let { get<ChampionshipTeamRepository>().getScoreByName(it) } ?: 0 }
    // [{"eventId":{eventName:"eventName",teamName:"teamName",rank:"rank"**}}]  Note: All values are strings
    var teams: Map<String, Map<String, String>>
    //    [{id:{"email": email, "name": name}}]
    // Championship team members
    var members: Map<String, Map<String, String>> = getTeamMembers()

    // {eventId:{bitId:Name}}
    var eventMembers: Map<String, Map<String, String>>

    init {
        val (userTeams, eventTeams) = getUserTeams()
        teams = userTeams
        eventMembers = eventTeams
//        Log.d("TEST", (eventMembers["12"]?.map { (_, name) ->
//            name
//        } ?: emptyList()).toString())
    }

    private fun getUserTeams(): Pair<Map<String, Map<String, String>>, Map<String, Map<String, String>>> {
        val userTeams = mutableMapOf<String, Map<String, String>>()
        val eventTeams = mutableMapOf<String, Map<String, String>>()
        val teams = runBlocking { get<NonChampionshipTeamRepository>().getAllUserTeams() }
        teams?.forEach { team ->
            val eventName = runBlocking { get<EventRepository>().getNameById(team.eventId) }
            eventName?.let {
                userTeams[team.eventId.toString()] = mapOf(
                    "eventName" to eventName,
                    "teamName" to team.name,
                    "rank" to team.rank.toString()
                )
                eventTeams[team.eventId.toString()] = team.members
            }
        }
        return Pair(userTeams.toMap(), eventTeams.toMap())
//        return userTeams.toMap()
    }

    private fun getTeamMembers(): Map<String, Map<String, String>> {
        return CurrentUser.teamMembers ?: mapOf()
    }

//    private fun getEventMembers(): Map<String, Map<String, String>> {
//        val teams = runBlocking { get<NonChampionshipTeamRepository>().getAllUserTeams() }
//    }

    fun getChampionshipTeamMembers() =
        members.map { (id, member) ->
            ChampionshipTeamMember(
                id,
                member.getValue("name"),
                member.getValue("email")
            )
        }

    fun getRegistrationHistory() =
        teams.map { (eventId, registration) ->
            RegistrationHistoryItem(
                eventId.toInt(),
                registration.getValue("eventName"),
                registration.getValue("teamName"),
                when(registration.getValue("rank")) {
                    "1" -> "1st"
                    "2" -> "2nd"
                    "3" -> "3rd"
                    else -> "--"
                },
                eventMembers[eventId]?.map { (_, name) ->
                    name
                } ?: emptyList()
            )
        }
}