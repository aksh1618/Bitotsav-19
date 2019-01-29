package `in`.bitotsav.teams.nonchampionship.data

import androidx.room.Entity
import com.google.gson.annotations.Expose

@Entity(primaryKeys = ["eventId", "teamLeaderId"])
data class NonChampionshipTeam(
    val eventId: Int,
    val teamLeaderId: String,
    @Expose(serialize = false, deserialize = false) var name: String,
    val members: Map<String, String>,
    @Expose(serialize = false, deserialize = false) var rank: Int = 0,
    @Expose(serialize = false, deserialize = false) var isUserTeam: Boolean = false,
    // For updating user teams
    @Expose(serialize = false, deserialize = false) var isTemp: Boolean = false
)