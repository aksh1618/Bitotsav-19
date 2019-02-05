package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.sendFcmTokenToServer
import `in`.bitotsav.profile.api.AuthenticationService
import `in`.bitotsav.profile.data.RegistrationFields
import `in`.bitotsav.profile.utils.*
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val authService: AuthenticationService
) : BaseViewModel("RegVM") {

    // TODO: Should this be initialized or lateinit ?
    var fields = RegistrationFields()
    val nextStep = NonNullMutableLiveData(1)
    val registrationError = NonNullMutableLiveData("")
    val waiting = NonNullMutableLiveData(false)
    val allDone = NonNullMutableLiveData(false)
    val loggedIn = NonNullMutableLiveData(false)

    fun fetchCollegeList() {
        scope.launch(Dispatchers.IO)
        {
            try {
                fields.collegeOptions.postValue(
                    fetchCollegeListAsync(authService)
                        .await()
                )
                Log.v(TAG, "College options fetched")
            } catch (e: Exception) {
                Log.w(TAG, e.message, e)
            }
        }
    }

    // Common
    private val anyErrors
        get() = when (nextStep.value) {
            2 -> fields.stepTwoFields
            3 -> fields.stepThreeFields
            else -> fields.stepOneFields
        }
            .apply {
                // Check each field and set error text if blank
                // Can't use validations for this because they are checked only when
                // text changes, which doesn't happen if user tries proceeding without
                // entering anything. A way to check instead for no modifications
                // (dirty bit ?) might be better, and allow validations to work.
                forEach {
                    it.text.value.isBlank().onTrue {
                        it.errorText.value = "Required."
                    }
                }
            }
            .any {
                // Check for any non-empty error texts
                it.errorText.value.isNotEmpty().onTrue {
                    Log.v(
                        TAG,
                        "Step ${nextStep.value}: ${it.text.value}: ${it.errorText.value}"
                    )
                }
            }.onFalse {
                // No non-empty error texts found
                Log.v(TAG, "All validations succeeded for step ${nextStep.value}")
            }

    fun completeStepOne(recaptchaResponseToken: String) {
        scope.launch {
            try {

                registerAsync(
                    authService,
                    fields.name.text.value,
                    fields.phone.text.value,
                    fields.email.text.value,
                    fields.password.text.value,
                    recaptchaResponseToken
                ).await()
                nextStep.value = 2

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
                    authService,
                    fields.email.text.value,
                    fields.phoneOtp.text.value,
                    fields.emailOtp.text.value
                ).await()
                nextStep.value = 3

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

    fun completeStepThree() {
        scope.launch {
            try {

                saveParticipantAsync(
                    authService,
                    fields.email.text.value,
                    fields.password.text.value,
                    fields.gender.text.value,
                    fields.college.text.value,
                    fields.rollNo.text.value,
                    fields.source.text.value,
                    fields.yearOptions.indexOf(fields.year.text.value) + 1
                ).await()
                allDone.value = true

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
                allDone.value.onFalse {
                    // Stop waiting if there was an exception, otherwise keep waiting
                    // for login.
                    waiting.value = false
                }
            }
        }
    }

    fun login() {
        scope.launch {
            try {

                delay(1000) // Server requires some time before login
                loginAsync(
                    authService,
                    fields.email.text.value,
                    fields.password.text.value
                ).await()
                syncUserAndRun { sendFcmTokenToServer() }
                loggedIn.value = true

            } catch (e: Exception) {
                Log.e("$TAG::login", "Unable to auto-login after registration", e)
            } finally {

                nextStep.value = 4

            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel destroyed")
        super.onCleared()
    }

    fun next() {
        Log.v(TAG, "Attempting step ${nextStep.value}")
        if (anyErrors) {
            registrationError.value = "Error(s) in some field(s)"
            return
        }
        registrationError.value = ""
        waiting.value = true
    }
}
