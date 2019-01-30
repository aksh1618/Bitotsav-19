package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.loginAsync
import `in`.bitotsav.profile.utils.syncUserProfile
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LoginViewModel : ViewModel() {

    var mainColor: Int = 0

    val loading = MutableLiveData<Boolean>()
    val toastMessage = MutableLiveData<String>()
    val loginEmail = MutableLiveData<String>()
    val loginPassword = MutableLiveData<String>()
    val loginErrorText = MutableLiveData<String>()
    val loginEmailErrorText = MutableLiveData<String>()
    val loginPasswordErrorText = MutableLiveData<String>()
    val loggedIn = MutableLiveData<Boolean>()
    var token = ""

    init {
        loginEmail.value = ""
        loginPassword.value = ""
        loginErrorText.value = ""
        loginEmailErrorText.value = ""
        loginPasswordErrorText.value = ""
        loggedIn.value = false
        toastMessage.value = ""
    }

    fun login() {
        Log.d("Login pressed", "${loginEmailErrorText.value}")
        loginErrorText.value = ""

        // Invalid email
        if (!loginEmailErrorText.value.isNullOrEmpty()) return
        if (checkEmailOrPasswordEmpty()) return

        Log.d("Login pressed", "Attempting login...")

        scope.launch {
            try {
                loading.value = true
                loginAsync(loginEmail.value!!, loginPassword.value!!).await()
                Log.d("Login pressed", "Got token: ${CurrentUser.authToken}")
                setLoggedIn()
            } catch (exception: AuthException) {
                loading.value = false
                Log.e("Login Pressed", "${exception.stackTrace}")
                loginErrorText.value = exception.message
            } catch (exception: NullPointerException) {
                loading.value = false
                Log.e("Login pressed", "Email or password is null, somehow")
            } catch (exception: Exception) {
                loading.value = false
                Log.e("Login Pressed", "${exception.stackTrace}")
                toast(exception.message ?: "Unknown Error!!")
            }
        }
    }

    private fun checkEmailOrPasswordEmpty(): Boolean {
        var empty = false
        if (loginEmail.value.isNullOrEmpty()) {
            loginEmailErrorText.value = "Email required."
            empty = true
        }
        if (loginPassword.value.isNullOrEmpty()) {
            loginPasswordErrorText.value = "Password required."
            empty = true
        }
        return empty
    }

    fun setLoggedIn() {
        scope.launch {
            val profileFuture = syncUserProfile()
            // TODO: Is there a better way?
//            while (profileFuture.isDone.not()) {
//                Log.d("Logging in", "Waiting for user info sync")
//                delay(500)
//            }
            profileFuture.addListener(
                { loading.postValue(false); loggedIn.postValue(true) },
                { it?.run() }
//                { Handler(Looper.getMainLooper()).post { it?.run() } }
            )
//            loading.value = false
//            loggedIn.value = true
        }
        // TODO: Store (and retrieve) CurrentUser in some way.
    }

    private fun toast(message: String) {
        toastMessage.value = message
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}
