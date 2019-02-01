package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.data.RegistrationFields.college
import `in`.bitotsav.profile.data.RegistrationFields.email
import `in`.bitotsav.profile.data.RegistrationFields.emailOtp
import `in`.bitotsav.profile.data.RegistrationFields.gender
import `in`.bitotsav.profile.data.RegistrationFields.name
import `in`.bitotsav.profile.data.RegistrationFields.password
import `in`.bitotsav.profile.data.RegistrationFields.passwordAgain
import `in`.bitotsav.profile.data.RegistrationFields.phone
import `in`.bitotsav.profile.data.RegistrationFields.phoneOtp
import `in`.bitotsav.profile.data.RegistrationFields.rollNo
import `in`.bitotsav.profile.data.RegistrationFields.source
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.registerAsync
import `in`.bitotsav.shared.network.NetworkException
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onTrue
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class RegistrationViewModel : BaseViewModel("RegistrationViewModel") {

    val currentStep = MutableLiveData<Int>().apply { value = 1 }
    val registrationError = MutableLiveData<String>().apply { value = "" }
    val waiting = MutableLiveData<Boolean>().apply { value = false }
    val recaptchaResponse = MutableLiveData<String>().apply { value = "" }

    // Common
    private fun checkFieldsEmpty() =
        when (currentStep.value) {
            2 -> listOf(phoneOtp, emailOtp)
            3 -> listOf(gender, college, rollNo, source)
            else -> listOf(name, phone, email, password, passwordAgain)
        }
            // Not using sequence as all errors need to be set.
            .map {
                it.text.value.isEmpty().onTrue {
                    it.errorText.value = "Required"
                }
            }.any { it }

    // Step 1
    fun completeStepOne() {
        scope.launch {
            try {
                registerAsync(
                    name.text.value,
                    phone.text.value,
                    email.text.value,
                    password.text.value,
                    recaptchaResponse.value!!
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

}
