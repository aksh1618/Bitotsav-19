package `in`.bitotsav.profile

import `in`.bitotsav.shared.data.MapConverter.Companion.fromMap
import `in`.bitotsav.shared.data.MapConverter.Companion.fromMapOfMap
import `in`.bitotsav.shared.data.MapConverter.Companion.toMap
import `in`.bitotsav.shared.data.MapConverter.Companion.toMapOfMap
import android.content.SharedPreferences
import org.koin.core.KoinComponent
import org.koin.core.get

object CurrentUser : KoinComponent {
    var name: String? = getFromPrefs("name")
        set(value) {
            putInPrefs("name", value)
            field = value
        }

    var bitotsavId: String? = getFromPrefs("bitotsavId")
        set(value) {
            putInPrefs("bitotsavId", value)
            field = value
        }

    var email: String? = getFromPrefs("email")
        set(value) {
            putInPrefs("email", value)
            field = value
        }

    var championshipTeamName: String? = getFromPrefs("teamName")
        set(value) {
            putInPrefs("teamName", value)
            field = value
        }

    var authToken: String? = getFromPrefs("authToken")
        set(value) {
            putInPrefs("authToken", value)
            field = value
        }

    var fcmToken: String? = getFromPrefs("fcmToken")
        set(value) {
            putInPrefs("fcmToken", value)
            field = value
        }

    val isLoggedIn: Boolean
        get() = authToken.isNullOrEmpty().not()

    // userTeams: {'eventId':{'leaderId':id, 'leaderName':name}}
    // **Note: eventId is string
    var userTeams: Map<String, Map<String, String>>? = getFromPrefs("userTeams")?.let { toMapOfMap(it) }
        set(value) {
            value?.let {
                putInPrefs("userTeams", fromMapOfMap(it))
            } ?: putInPrefs("userTeams", null)
            field = value
        }

    //    [{id:{email, name}}]
    var teamMembers: Map<String, Map<String, String>>? = getFromPrefs("teamMembers")?.let { toMapOfMap(it) }
        set(value) {
            value?.let {
                putInPrefs("teamMembers", fromMapOfMap(it))
            } ?: putInPrefs("teamMembers", null)
            field = value
        }

    //    {day1,day2,day3,merchandise,accommodation}
    @Suppress("UNCHECKED_CAST")
    var paymentDetails: Map<String, Boolean>? =
        getFromPrefs("paymentDetails")?.let { toMap(it) as Map<String, Boolean> }
        set(value) {
            value?.let {
                putInPrefs("paymentDetails", fromMap(value as Map<String, String>))
            } ?: putInPrefs("paymentDetails", null)
            field = value
        }

    fun clearAllFields() {
        authToken = null
        fcmToken = null
        bitotsavId = null
        name = null
        email = null
        teamMembers = null
        userTeams = null
        championshipTeamName = null
        paymentDetails = null
    }

    private fun getFromPrefs(key: String): String? {
        return get<SharedPreferences>().getString(key, null)
    }

    private fun putInPrefs(key: String, value: String?) {
        get<SharedPreferences>().edit().putString(key, value).apply()
    }

    override fun toString(): String {
        return """
            Name: $name
            Id: $bitotsavId
            Email: $email
            TeamName: $championshipTeamName
            UserTeams: $userTeams
            Members: $teamMembers
            Payment: $paymentDetails
        """.trimIndent()
    }
}