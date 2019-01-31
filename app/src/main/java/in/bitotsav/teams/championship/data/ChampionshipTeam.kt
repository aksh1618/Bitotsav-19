package `in`.bitotsav.teams.championship.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChampionshipTeam(
    @PrimaryKey @SerializedName("teamName") val name: String,
    @SerializedName("teamMembers") val members: Map<String, String>,
    @SerializedName("teamPoints") val totalScore: Int
// TODO: Get and insert rank here
)