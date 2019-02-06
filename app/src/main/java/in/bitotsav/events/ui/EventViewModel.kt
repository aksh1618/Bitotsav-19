package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRegistrationMember
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.events.utils.Member
import `in`.bitotsav.events.utils.deregisterForEventAsync
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepository: EventRepository
) : BaseViewModel("EventVM") {

    val currentEvent = MutableLiveData<Event>()
    val isUserRegistered = NonNullMutableLiveData(false)

    var numMembersOptions = listOf<String>()
    val numMembersString = NonNullMutableLiveData("1")
    val membersToRegister by lazy {
        mutableListOf(
            EventRegistrationMember(
                1,
                CurrentUser.bitotsavId!!.substring(5),
                CurrentUser.email!!
            )
        )
    }

    val registrationError = NonNullMutableLiveData("")
    val waiting = NonNullMutableLiveData(false)
    private val anyErrors: Boolean
        get() = membersToRegister
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

    fun setCurrentEvent(id: Int) {
        scope.launch {
            with(eventRepository.getById(id)) {
                val event = this@with
                event?.let {
                    currentEvent.value = event
                    isUserRegistered.value = when (CurrentUser.isLoggedIn) {
                        true -> event.id.toString() in CurrentUser.userTeams!!
                        false -> false
                    }
                    numMembersOptions = (event.minimumMembers..event.maximumMembers)
                        .map { it.toString() }
                    numMembersString.value = event.minimumMembers.toString()
                    generateMembersToRegister(event.minimumMembers)
                }
            }
            registrationError.value = ""
            waiting.value = false
        }
    }

    fun generateMembersToRegister(numMembers: Int) {
        if (numMembers == membersToRegister.size) return
        while (numMembers < membersToRegister.size) {
            membersToRegister.removeAt(numMembers)
        }
        while (numMembers > membersToRegister.size) {
            membersToRegister.add(EventRegistrationMember(membersToRegister.size + 1))
        }
        membersToRegister.forEach {
            Log.d(
                TAG,
                "Member for recycler view: ${it.bitotsavId.text.value}, ${it.bitotsavId.errorText.value}"
            )
        }
    }

    private fun attemptRegistration() {
        scope.launch {
            try {

                val members = membersToRegister.map {
                    Member("B19/" + it.bitotsavId.text.value, it.email.text.value)
                }
                Log.d(TAG, members.toString())
                waiting.value = false
//                registerForEventAsync(
//                    CurrentUser.authToken!!,
//                    currentEvent.value!!.id,
//                    CurrentUser.bitotsavId!!,
//                    members
//                ).await()
//                syncUserAndRun {
//                    isUserRegistered.value = true
//                    waiting.value = false
//                    toast("Registration successful !!")
//                }

            } catch (e: Exception) {
                error(e.message ?: "Some error occurred :( Try again.")
                Log.e(TAG, e.message, e)
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
                    CurrentUser.bitotsavId!!
                ).await()

                syncUserAndRun {
                    isUserRegistered.value = false
                    waiting.value = false
                    toast("Deregistration successful !!")
                }

            } catch (e: Exception) {
                error(e.message ?: "Some error occurred :( Try again.")
                Log.e(TAG, e.message, e)
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
        isUserRegistered.value.onTrue { toast("Already registered!"); return }
        waiting.value = true
        attemptRegistration()
    }

    fun deregister() {
        isUserRegistered.value.onFalse { toast("Not registered!"); return }
        attemptDeregistration()
    }

}
