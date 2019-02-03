package `in`.bitotsav.profile.utils

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.api.AuthenticationService
import `in`.bitotsav.shared.exceptions.NetworkException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "AuthUtils"

class AuthException(message: String) : Exception(message)

//POST - /login - body: {email, password}
//502 - Server error
//403 - Incorrect credentials
//200 - Success with {token} to be sent
fun loginAsync(
    authService: AuthenticationService,
    email: String,
    password: String
) = CoroutineScope(Dispatchers.Main).async {
    val body = mapOf("email" to email, "password" to password)
    val request = authService.loginAsync(body)
    val response = request.await()
    if (response.code() == 200) {
        Log.d(TAG, "User:$email logged in")
        CurrentUser.authToken = response.body()?.get("token")
            ?: throw AuthException(
                "Empty token received from the server." +
                        " Contact the tech team if this issue persists"
            )
    } else {
        Log.d(TAG, "${response.code()}")
        when (response.code()) {
            403 -> throw AuthException("Incorrect email and/or password")
            else -> throw NetworkException(
                "Server is currently facing some issues. Try again later"
            )
        }
    }
}

//POST - /register - body: {g-recaptcha-response, email, phno, name, password}
//403 - Captcha failed
//502 - Server error
//409 - Email Id is already registered
//200 - Success - OTP Sent
fun registerAsync(
    authService: AuthenticationService,
    name: String,
    phone: String,
    email: String,
    password: String,
    recaptchaResponse: String
) = CoroutineScope(Dispatchers.Main).async {
    val body = mapOf(
        "email" to email,
        "phno" to phone,
        "name" to name,
        "password" to password,
        "g-recaptcha-response" to recaptchaResponse
    )
    val request = authService.registerAsync(body)
    val response = request.await()
    when (response.code()) {
        200 -> Log.d(TAG, "Registration Stage 1 complete")
        403 -> throw AuthException("Captcha verification failed")
        409 -> throw AuthException("Email id is already registered")
        else -> throw NetworkException(
            "Server is currently facing some issues. Try again later"
        )
    }
}

//POST - /verify - body: {email, phoneOtp, emailOtp}
//403 - OTP incorrect
//400 - Payload modified, i.e email is incorrect
//502 - Server error
//200 - Success
fun verifyAsync(
    authService: AuthenticationService,
    email: String,
    phoneOtp: String,
    emailOtp: String
) = CoroutineScope(Dispatchers.Main).async {
    val body = mapOf(
        "email" to email,
        "phoneOtp" to phoneOtp,
        "emailOtp" to emailOtp
    )
    val request = authService.verifyAsync(body)
    val response = request.await()
    when (response.code()) {
        200 -> Log.d(TAG, "Registration Stage 2: OTP verification complete")

        403 -> throw AuthException("Incorrect OTP(s)")
        400 -> throw AuthException(
            "LOL 'Hacker', install the original app from Google Play Store"
        )
        else -> throw NetworkException(
            "Server is currently facing some issues. Try again later"
        )
    }
}

//POST - /saveparticipant - body: {email, gender, college, rollno, source, year, password}
//502 - Server error
//200 - Success with Bitotsav Id in {data}
fun saveParticipantAsync(
    authService: AuthenticationService,
    email: String,
    password: String,
    gender: String,
    college: String,
    rollNo: String,
    source: String,
    year: Int
) = CoroutineScope(Dispatchers.Main).async {
    val body = mapOf(
        "email" to email,
        "password" to password,
        "gender" to gender,
        "college" to college,
        "rollno" to rollNo,
        "source" to source,
        "year" to year
    )
    val request = authService.saveParticipantAsync(body)
    val response = request.await()
    if (response.code() == 200) {
        val bitotsavId = response.body()?.get("data") ?: throw AuthException(
            "Bitotsav ID not generated." +
                    " Contact the tech team if this issue persists"
        )
        Log.d(TAG, "Registration complete. BitotsavId: $bitotsavId")
        return@async bitotsavId
    } else {
        throw NetworkException(
            "Server is currently facing some issues. Try again later"
        )
    }
}

//GET - /getCollegeList
//200 - Object containing colleges
fun fetchCollegeListAsync(
    authService: AuthenticationService
) = CoroutineScope(Dispatchers.IO).async {
    val request = authService.getCollegeListAsync()
    val response = request.await()
    if (response.code() == 200) {
        return@async response.body()?.get("colleges")
            ?: throw NetworkException("List of colleges is empty")
    } else {
        throw Exception("Unable to get list of colleges from the server")
    }
}
