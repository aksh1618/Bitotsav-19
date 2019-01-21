package `in`.bitotsav.teams.data.championship

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChampionshipTeam(
    @PrimaryKey @SerializedName("teamName") val name: String,
    @SerializedName("teamMembers") val members: Map<String, String>,
    @SerializedName("teamPoints") val totalScore: Int
//    TODO: Define the entity
)