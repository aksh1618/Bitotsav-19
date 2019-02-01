package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.registerAsync
import `in`.bitotsav.shared.network.NetworkException
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onTrue
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class RegistrationViewModel : BaseViewModel("RegistrationViewModel") {

    // Common
    private fun checkFieldsEmpty() =
        when (currentStep.value) {
            2 -> listOf(phoneOtp, emailOtp)
            3 -> listOf(gender, college, rollNo, source)
            else -> listOf(name, phone, email, password, passwordAgain)
        }
            // Not using sequence as all errors need to be set.
            .map {
                it.value.isEmpty().onTrue {
                    it.errorText.value = "Required"
                }
            }.any { it }

    // Step 1
    fun completeStepOne() {
        scope.launch {
            try {
                registerAsync(
                    name.value,
                    phone.value,
                    email.value,
                    password.value,
                    recaptchaResponse.value
                ).await()
                currentStep.value = 2
            } catch (e: NetworkException) {
                registrationError.value = e.message
                Log.e(TAG, null, e)
            } catch (e: AuthException) {
                registrationError.value = e.message
                Log.e(TAG, null, e)
            } catch (e: Exception) {
                registrationError.value = "Unknown Error Occurred :("
                Log.e(TAG, null, e)
            } finally {
                waiting.value = false
            }
        }
    }

    fun next() {
        Log.d(TAG, "Next Pressed")
        if (checkFieldsEmpty()) {
            Log.d(TAG, "Some field empty in step ${currentStep.value}")
            return
        }
        Log.d(TAG, "All fields found, start waiting")
        waiting.value = true
    }

    fun register() {
        toast("Nope")
    }

    // Common
    val currentStep = MutableLiveData<Int>().apply { value = 1 }
    val registrationError = MutableLiveData<String>().apply { value = "" }
    val waiting = MutableLiveData<Boolean>().apply { value = false }

    // Step One
    // Vars
//    val name = MutableLiveData<Pair<String,String>>()
    val name = MutableLiveDataWithErrorText<String>("")
    val phone = MutableLiveDataWithErrorText<String>("")
    val email = MutableLiveDataWithErrorText<String>("")
    // Min 6 chars
    val password = MutableLiveDataWithErrorText<String>("")
    // Min 6 chars
    val passwordAgain = MutableLiveDataWithErrorText<String>("")
    val recaptchaResponse = MutableLiveDataWithErrorText<String>("")

    // Step Two
    // 6 digitspublic void setTarget(@Nullable in.bitotsav.profile.ui.Registr
    val phoneOtp = MutableLiveDataWithErrorText<String>("")
    // 6 digits
    val emailOtp = MutableLiveDataWithErrorText<String>("")

    // Step Three
    // Male/Female/Other/Prefer not to say
    val gender = MutableLiveDataWithErrorText<String>("")
    val college = MutableLiveDataWithErrorText<String>("")
    val rollNo = MutableLiveDataWithErrorText<String>("")
    // Friends/Newspaper/Online/Others
    val source = MutableLiveDataWithErrorText<String>("")

    class MutableLiveDataWithErrorText<T>(
        private val defaultValue: T
    ) : MutableLiveData<T>() {
        val errorText = MutableLiveData<String>().apply { value = "" }
        override fun getValue(): T {
            return super.getValue() ?: defaultValue
        }
    }
}
