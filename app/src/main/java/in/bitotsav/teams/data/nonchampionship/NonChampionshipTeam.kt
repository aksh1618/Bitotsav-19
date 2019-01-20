package `in`.bitotsav.teams.data.nonchampionship

import androidx.room.Entity
import com.google.gson.annotations.Expose

@Entity(primaryKeys = ["eventId", "teamLeaderId"])
data class NonChampionshipTeam(
    val eventId: Int,
    val teamLeaderId: String,
    @Expose(serialize = false, deserialize = false) val name: String,
    val members: Map<String, String>,
    @Expose(serialize = false, deserialize = false) val rank: Int = 0,
    @Expose(serialize = false, deserialize = false) val isUserTeam: Boolean = false
)