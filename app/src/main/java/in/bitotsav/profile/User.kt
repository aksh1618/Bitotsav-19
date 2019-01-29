package `in`.bitotsav.profile

import `in`.bitotsav.shared.data.MapConverter.Companion.fromMapOfMap
import `in`.bitotsav.shared.data.MapConverter.Companion.toMapOfMap
import android.content.SharedPreferences
import org.koin.core.KoinComponent
import org.koin.core.get

//TODO: Store team members' emails here
object User : KoinComponent {
    var name: String? = null
        get() = get("name")
        set(value) {
            value?.let {
                set("name", value)
                field = value
            }
        }

    var bitotsavId: String? = null
        get() = get("bitotsavId")
        set(value) {
            value?.let {
                set("bitotsavId", value)
                field = value
            }
        }

    var email: String? = null
        get() = get("email")
        set(value) {
            value?.let {
                set("email", value)
                field = value
            }
        }

    var championshipTeamName: String? = null
        get() = get("teamName")
        set(value) {
            value?.let {
                set("teamName", value)
                field = value
            }
        }

    var authToken: String? = null
        get() = get("authToken")
        set(value) {
            value?.let {
                set("authToken", value)
                field = value
            }
        }

    var fcmToken: String? = null
        get() = get("fcmToken")
        set(value) {
            value?.let {
                set("fcmToken", value)
                field = value
            }
        }

    // userTeams: {'eventId':{'leaderId':id, 'leaderName':name}}
    // **Note: eventId is string
//    TODO: Initialize on login
    var userTeams: Map<String, Map<String, String>>? = null
        get() = get("userTeams")?.let {
            toMapOfMap(it)
        }
        set(value) {
            value?.let {
                set("userTeams", fromMapOfMap(it))
                field = value
            }
        }

    val isLoggedIn: Boolean
        get() = !get("authToken").isNullOrEmpty()

    private fun get(key: String): String? {
        return get<SharedPreferences>().getString(key, null)
    }

    private fun set(key: String, value: String) {
        get<SharedPreferences>().edit().putString(key, value).apply()
    }
}