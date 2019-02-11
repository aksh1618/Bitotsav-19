package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.events.utils.Member
import `in`.bitotsav.events.utils.deregisterForEventAsync
import `in`.bitotsav.events.utils.registerForEventAsync
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import `in`.bitotsav.teams.data.RegistrationMember
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class EventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : BaseViewModel("EventVM") {

    val user = userRepository.get()

    val currentEvent = MutableLiveData<Event>()
    val isUserRegistered = NonNullMutableLiveData(false)

    val numMembersOptions = NonNullMutableLiveData(listOf("1"))
    val numMembersString = NonNullMutableLiveData("1")
    val membersToRegister = mutableListOf<RegistrationMember>()

    val registrationError = NonNullMutableLiveData("")
    val deregistrationError = NonNullMutableLiveData("")
    val waiting = NonNullMutableLiveData(false)
    private val anyErrors: Boolean
        get() = Boolean.or(
            (membersToRegister.distinctBy {
                Pair(it.bitotsavId.text.value, it.email.text.value)
            }.size < membersToRegister.size).onTrue {
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

    fun setCurrentEvent(id: Int) {
        scope.launch {
            with(eventRepository.getById(id)) {
                val event = this@with
                event?.let {
                    currentEvent.value = event
                    user.value?.let {
                        prepareForRegistration(event)
                    } ?: run {
                        isUserRegistered.value = false
                    }
                }
            }
        }
    }

    fun prepareForRegistration(event: Event) {
        registrationError.value = ""
        waiting.value = false
        numMembersOptions.value = (event.minimumMembers..event.maximumMembers)
            .map { it.toString() }
        numMembersString.value = event.minimumMembers.toString()
        // To reset errors
        membersToRegister.clear()
        generateMembersToRegister(event.minimumMembers)
    }

    fun generateMembersToRegister(numMembers: Int) {
        if (membersToRegister.isEmpty()) {
            membersToRegister.add(
                RegistrationMember(
                    1,
                    user.value!!.id.substring(5),
                    user.value!!.email
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
                    ("BT19/" + it.bitotsavId.text.value) != user.value!!.id ||
                            it.email.text.value != user.value!!.email
                }
                .apply {
                    (size == numMembersString.value.toInt() - 1).onFalse {
                        error("You must one of the members.")
                        Log.e(TAG, "Attempted registration without own entry.")
                        waiting.value = false
                        return@launch
                    }
                }
                .map { Member(("BT19/" + it.bitotsavId.text.value), it.email.text.value) }
            try {

                registerForEventAsync(
                    CurrentUser.authToken!!,
                    currentEvent.value!!.id,
                    user.value!!.id,
                    members
                ).await()
                syncUserAndRun {
                    currentEvent.value?.setStarred()
                    isUserRegistered.value = true
                    waiting.value = false
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
                waiting.value = false
            }
        }
    }

    private fun attemptDeregistration() {
        scope.launch {
            try {

                deregisterForEventAsync(
                    CurrentUser.authToken!!,
                    currentEvent.value!!.id,
                    user.value!!.id
                ).await()

                syncUserAndRun {
                    isUserRegistered.value = false
                    waiting.value = false
                }

            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException, is SocketTimeoutException -> {
                        deregistrationError.value = "Unable to reach bitotsav.in"
                    }
                    else -> {
                        deregistrationError.value =
                            e.message ?: "Some error occurred :( Try again."
                    }
                }
                Log.e(TAG, e.message ?: "Unknown Error", e)
                waiting.value = false
            }
        }
    }

    private fun error(errorText: String) {
        registrationError.value = errorText
    }

    fun register() {
        error("")
        anyErrors.onTrue { return }
        isUserAlreadyRegistered.onTrue { toast("Already registered!"); return }
        waiting.value = true
        attemptRegistration()
    }

    fun deregister() {
        deregistrationError.value = ""
        isUserAlreadyRegistered.onFalse { toast("Not registered!"); return }
        waiting.value = true
        attemptDeregistration()
    }

    val isUserAlreadyRegistered
        get() = currentEvent.value?.id.toString() in user.value?.teams ?: mapOf()

}
