package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.deleteFcmTokenFromServer
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.MutableLiveDataTextWithValidation
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import `in`.bitotsav.teams.data.Member
import `in`.bitotsav.teams.data.RegistrationMember
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import `in`.bitotsav.teams.utils.registerForChampionshipAsync
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext.get
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProfileViewModel(userRepository: UserRepository) : BaseViewModel("ProfileVM") {

    companion object {
        const val CHAMPIONSHIP_MIN_TEAM_SIZE = 6
        const val CHAMPIONSHIP_MAX_TEAM_SIZE = 8
    }

    val championshipTeamRegistered = NonNullMutableLiveData(false)
    val numMembersOptions = NonNullMutableLiveData(listOf("6"))
    val numMembersString = NonNullMutableLiveData("6")

    val user = userRepository.get()
    val waitingForLogout = NonNullMutableLiveData(false)
    val loggedOut = NonNullMutableLiveData(false)

    private val requiredValidation: String.() -> Boolean = { isNotBlank() }
    var teamName = MutableLiveDataTextWithValidation(
        requiredValidation to "Required"
    )
    val membersToRegister = mutableListOf<RegistrationMember>()
    val registrationError = NonNullMutableLiveData("")
    val waitingForRegistration = NonNullMutableLiveData(false)
    private val anyErrors: Boolean
        get() = Boolean.or(
            teamName.apply {
                text.value.isBlank().onTrue {
                    teamName.errorText.value = "Required."
                }
            }.errorText.value.isNotBlank().onTrue {
                error("Error(s) in some field(s)")
            },
            (membersToRegister.distinctBy {
                Pair(it.bitotsavId, it.email)
            }.size < numMembersString.value.toInt()).onTrue {
                error("Duplicate Entries")
            },
            membersToRegister
                .apply {
                    forEach {
                        it.bitotsavId.text.value.isBlank().onTrue {
                            it.bitotsavId.errorText.value = "Required."
                        }
                        it.email.text.value.isBlank().onTrue {
                            it.email.errorText.value = "Required."
                        }
                    }
                }
                .any { member ->
                    Boolean.or(
                        member.bitotsavId.errorText.value.isNotBlank(),
                        member.email.errorText.value.isNotBlank()
                    )
                }.onTrue { error("Error(s) in some field(s)") }
        )

    fun prepareForRegistration() {
        registrationError.value = ""
        waitingForRegistration.value = false
        numMembersOptions.value = (CHAMPIONSHIP_MIN_TEAM_SIZE..CHAMPIONSHIP_MAX_TEAM_SIZE)
            .map { it.toString() }
        numMembersString.value = CHAMPIONSHIP_MIN_TEAM_SIZE.toString()
        // To reset errors
        teamName = MutableLiveDataTextWithValidation(
            requiredValidation to "Required"
        )
        membersToRegister.clear()
        generateMembersToRegister(CHAMPIONSHIP_MIN_TEAM_SIZE)
    }

    fun generateMembersToRegister(numMembers: Int) {
        if (membersToRegister.isEmpty()) {
            membersToRegister.add(
                RegistrationMember(
                    1,
                    user.value?.id?.substring(5) ?: "",
                    user.value?.email ?: ""
                )
            )
        }
        if (numMembers == membersToRegister.size) return
        while (numMembers < membersToRegister.size) {
            membersToRegister.removeAt(numMembers)
        }
        while (numMembers > membersToRegister.size) {
            membersToRegister.add(RegistrationMember(membersToRegister.size + 1))
        }
    }

    private fun attemptRegistration() {
        scope.launch {
            val members = membersToRegister
                .filter {
                    ("BT19/" + it.bitotsavId.text.value) != CurrentUser.bitotsavId ||
                            it.email.text.value != CurrentUser.email
                }
                .apply {
                    (size == numMembersString.value.toInt() - 1).onFalse {
                        error("You must one of the members.")
                        Log.e(TAG, "Attempted registration without own entry.")
                        waitingForRegistration.value = false
                        return@launch
                    }
                }
                .map { Member(("BT19/" + it.bitotsavId.text.value), it.email.text.value) }
            try {

                registerForChampionshipAsync(
                    CurrentUser.authToken!!,
                    teamName.text.value,
                    members
                ).await()
                syncUserAndRun {
                    championshipTeamRegistered.value = true
                    waitingForRegistration.value = false
                }

            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException, is SocketTimeoutException -> {
                        error("Unable to reach bitotsav.in")
                    }
                    else -> {
                        error(e.message ?: "Some error occurred :( Try again.")
                    }
                }
                Log.e(TAG, members.toString(), e)
                waitingForRegistration.value = false
            }
        }
    }

    private fun error(errorText: String) {
        registrationError.value = errorText
    }


    fun register() {
        error("")
        anyErrors.onTrue { return }
        championshipTeamRegistered.value.onTrue { toast("Already registered!"); return }
        waitingForRegistration.value = true
        attemptRegistration()
    }

    fun syncUser() {
        syncUserAndRun { Log.d(TAG, "${user.value?.name}'s profile synced") }
    }

    fun logout() {
        waitingForLogout.value = true
        deleteFcmTokenFromServer()
        CurrentUser.clearAllFields()
        // Delete previous FCM token to avoid conflicts
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                } catch (e: IOException) {
                    Log.e(TAG, e.message ?: "IO Exception", e)
                }
            }
        }
        scope.launch {
            withContext(Dispatchers.IO) {
                get().koin.get<UserRepository>().delete()
                get().koin.get<NonChampionshipTeamRepository>().cleanupUserTeams()
                loggedOut.postValue(true)
            }
        }
    }
}