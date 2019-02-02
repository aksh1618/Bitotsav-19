package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.data.RegistrationFields.college
import `in`.bitotsav.profile.data.RegistrationFields.email
import `in`.bitotsav.profile.data.RegistrationFields.emailOtp
import `in`.bitotsav.profile.data.RegistrationFields.gender
import `in`.bitotsav.profile.data.RegistrationFields.name
import `in`.bitotsav.profile.data.RegistrationFields.password
import `in`.bitotsav.profile.data.RegistrationFields.phone
import `in`.bitotsav.profile.data.RegistrationFields.phoneOtp
import `in`.bitotsav.profile.data.RegistrationFields.rollNo
import `in`.bitotsav.profile.data.RegistrationFields.source
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.registerAsync
import `in`.bitotsav.profile.utils.verifyAsync
import `in`.bitotsav.shared.network.NetworkException
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import android.util.Log
import kotlinx.coroutines.launch

class RegistrationViewModel : BaseViewModel("RegistrationViewModel") {

    val currentStep = NonNullMutableLiveData(1)
    val registrationError = NonNullMutableLiveData("")
    val waiting = NonNullMutableLiveData(false)

    // Common
    private val anyErrors
        get() = when (currentStep.value) {
            2 -> listOf(phoneOtp, emailOtp)
            3 -> listOf(gender, college, rollNo, source)
            else -> listOf(name, phone, email, password)
        }
            .any {
                it.errorText.value.isNotEmpty().onTrue {
                    Log.v(TAG, "Step ${currentStep.value}: ${it.text}: ${it.errorText}")
                }
            }.onFalse {
                Log.v(TAG, "All validations succeeded for step ${currentStep.value}")
            }

    // Step 1
    fun completeStepOne(recaptchaResponseToken: String) {
        scope.launch {
            try {

                registerAsync(
                    name.text.value,
                    phone.text.value,
                    email.text.value,
                    password.text.value,
                    recaptchaResponseToken
                ).await()
                currentStep.value = 2

            } catch (e: NetworkException) {
                registrationError.value = e.message ?: "Network Error"
                Log.e(TAG, null, e)
            } catch (e: AuthException) {
                registrationError.value = e.message ?: "Authentication Error"
                Log.e(TAG, null, e)
            } catch (e: Exception) {
                registrationError.value = "Unknown Error :("
                Log.e(TAG, null, e)
            } finally {
                waiting.value = false
            }
        }
    }

    fun completeStepTwo() {
        scope.launch {
            try {

                verifyAsync(
                    email.text.value,
                    phoneOtp.text.value,
                    emailOtp.text.value
                ).await()
                currentStep.value = 3

            } catch (e: NetworkException) {
                registrationError.value = e.message ?: "Network Error"
                Log.e(TAG, null, e)
            } catch (e: AuthException) {
                registrationError.value = e.message ?: "Authentication Error"
                Log.e(TAG, null, e)
            } catch (e: Exception) {
                registrationError.value = "Unknown Error :("
                Log.e(TAG, null, e)
            } finally {
                waiting.value = false
            }
        }
    }

    fun next() {
        Log.v(TAG, "Attempting step ${currentStep.value}")
        if (anyErrors) { return }
        waiting.value = true
    }
}
