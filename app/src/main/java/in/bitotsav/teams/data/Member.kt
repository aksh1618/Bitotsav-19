package `in`.bitotsav.teams.data

import androidx.annotation.Keep

//Exclude Member from proguard to allow gson serialization
@Keep
data class Member(val memberId: String, val memberEmail: String)