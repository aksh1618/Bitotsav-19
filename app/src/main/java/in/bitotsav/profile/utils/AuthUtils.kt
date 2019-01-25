package `in`.bitotsav.profile.utils

import `in`.bitotsav.profile.api.AuthenticationService
import `in`.bitotsav.shared.network.NetworkException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "AuthUtils"

class AuthException(message: String): Exception(message)

//TODO: Client side verification of inputs

//POST - /login - body: {email, password}
//502 - Server error
//403 - Incorrect credentials
//200 - Success with {token} to be sent
fun login(email: String, password: String): Deferred<String> {
    return CoroutineScope(Dispatchers.Main).async {
        val body = mapOf("email" to email, "password" to password)
        val request = AuthenticationService.api.login(body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "User:$email logged in")
            return@async response.body()?.get("token")
                ?: throw AuthException("Empty token received from the server." +
                        " Contact the tech team if this issue persists")
        } else {
            when (response.code()) {
                403 -> throw AuthException("Incorrect email and/or password")
                else -> throw NetworkException("Server is currently facing some issues. Try again later")
            }
        }
    }
}

//POST - /register - body: {g-recaptcha-response, email, phno, name, password}
//403 - Captcha failed
//502 - Server error
//409 - Email Id is already registered
//200 - Success - OTP Sent
fun register(
    email: String,
    phno: Long,
    name: String,
    password: String,
    g_recaptcha_response: String
): Deferred<Boolean> {
    return CoroutineScope(Dispatchers.Main).async {
        val body = mapOf(
            "email" to email,
            "phno" to phno,
            "name" to name,
            "password" to password,
            "g-recaptcha-response" to g_recaptcha_response
        )
        val request =  AuthenticationService.api.register(body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Registration Stage 1 complete")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw AuthException("Captcha verification failed")
                409 -> throw AuthException("Email id is already registered")
                else -> throw NetworkException("Server is currently facing some issues. Try again later")
            }
        }
    }
}

//POST - /verify - body: {email, phoneOtp, emailOtp}
//403 - OTP incorrect
//400 - Payload modified, i.e email is incorrect
//502 - Server error
//200 - Success
fun verify(email: String, phoneOtp: String, emailOtp: String): Deferred<Boolean> {
    return CoroutineScope(Dispatchers.Main).async {
        val body = mapOf(
            "email" to email,
            "phoneOtp" to phoneOtp,
            "emailOtp" to emailOtp
        )
        val request = AuthenticationService.api.verify(body)
        val response = request.await()
        if (response.code() == 200) {
            Log.d(TAG, "Registration Stage 2: OTP verification complete")
            return@async true
        } else {
            when (response.code()) {
                403 -> throw AuthException("Incorrect OTP")
                400 -> throw AuthException("You are running a modded app." +
                        " Install the original one from Google Play Store")
                else -> throw NetworkException("Server is currently facing some issues. Try again later")
            }
        }
    }
}

//POST - /saveparticipant - body: {email, gender, college, rollno, source, year}
//502 - Server error
//200 - Success with Bitotsav Id in {data}
fun saveParticipant(
    email: String,
    gender: String,
    college: String,
    rollNo: String,
    source: String,
    year: Int
): Deferred<String> {
    return CoroutineScope(Dispatchers.Main).async {
        val body = mapOf(
            "email" to email,
            "gender" to gender,
            "college" to college,
            "rollno" to rollNo,
            "source" to source,
            "year" to year
        )
        val request = AuthenticationService.api.saveParticipant(body)
        val response = request.await()
        if (response.code() == 200) {
            val bitotsavId = response.body()?.get("data") ?: throw AuthException("Bitotsav ID not generated." +
                    " Contact the tech team if this issue persists")
            Log.d(TAG, "Registration complete. BitotsavId: $bitotsavId")
            return@async bitotsavId
        } else {
            throw NetworkException("Server is currently facing some issues. Try again later")
        }
    }
}

//GET - /getCollegeList
//200 - Object containing colleges
fun fetchCollegeList(): Deferred<Any> {
    return CoroutineScope(Dispatchers.IO).async {
        val request = AuthenticationService.api.getCollegeList()
        val response = request.await()
        if (response.code() == 200) {
            val colleges = response.body()?.get("colleges") ?: throw NetworkException("List of colleges is empty")
            TODO("Insert into some entity")
        } else {
            throw Exception("Unable to get list of colleges from the server")
        }
    }
}
