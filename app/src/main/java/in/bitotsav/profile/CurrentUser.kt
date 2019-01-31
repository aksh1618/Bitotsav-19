package `in`.bitotsav.profile

import `in`.bitotsav.shared.data.MapConverter.Companion.fromMapOfMap
import `in`.bitotsav.shared.data.MapConverter.Companion.toMapOfMap
import android.content.SharedPreferences
import org.koin.core.KoinComponent
import org.koin.core.get

//TODO: Store team members' emails here
object CurrentUser : KoinComponent {
    var name: String? = getFromPrefs("name")
        set(value) {
            value?.let {
                putInPrefs("name", value)
                field = value
            }
        }

    var bitotsavId: String? = getFromPrefs("bitotsavId")
        set(value) {
            value?.let {
                putInPrefs("bitotsavId", value)
                field = value
            }
        }

    var email: String? = getFromPrefs("email")
        set(value) {
            value?.let {
                putInPrefs("email", value)
                field = value
            }
        }

    var championshipTeamName: String? = getFromPrefs("teamName")
        set(value) {
            value?.let {
                putInPrefs("teamName", value)
                field = value
            }
        }

    var authToken: String? = getFromPrefs("authToken")
        set(value) {
            value?.let {
                putInPrefs("authToken", value)
                field = value
            }
        }

    var fcmToken: String? = getFromPrefs("fcmToken")
        set(value) {
            value?.let {
                putInPrefs("fcmToken", value)
                field = value
            }
        }

    val isLoggedIn: Boolean
        get() = authToken.isNullOrEmpty().not()

    // userTeams: {'eventId':{'leaderId':id, 'leaderName':name}}
    // **Note: eventId is string
    var userTeams: Map<String, Map<String, String>>? = getFromPrefs("userTeams")?.let { toMapOfMap(it) }
        set(value) {
            value?.let {
                putInPrefs("userTeams", fromMapOfMap(it))
                field = value
            }
        }

    //    [{id:{email, name}}]
    var teamMembers: Map<String, Map<String, String>>? = getFromPrefs("teamMembers")?.let { toMapOfMap(it) }
        set(value) {
            value?.let {
                putInPrefs("teamMembers", fromMapOfMap(it))
                field = value
            }
        }

    private fun getFromPrefs(key: String): String? {
        return get<SharedPreferences>().getString(key, null)
    }

    private fun putInPrefs(key: String, value: String) {
        get<SharedPreferences>().edit().putString(key, value).apply()
    }
}