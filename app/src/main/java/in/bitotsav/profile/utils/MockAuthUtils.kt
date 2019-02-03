// FIXME: Remove after creating proper tests.
//package `in`.bitotsav.profile.utils
//
//import `in`.bitotsav.profile.api.AuthenticationService
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.delay
//
//private const val TAG = "AuthUtils"
//
//class AuthException(message: String) : Exception(message)
//
////POST - /login - body: {email, password}
////502 - Server error
////403 - Incorrect credentials
////200 - Success with {token} to be sent
//fun loginAsync(
//    authService: AuthenticationService,
//    email: String,
//    password: String
//) = CoroutineScope(Dispatchers.Main).async { delay(1000) }
//
////POST - /register - body: {g-recaptcha-response, email, phno, name, password}
////403 - Captcha failed
////502 - Server error
////409 - Email Id is already registered
////200 - Success - OTP Sent
//fun registerAsync(
//    authService: AuthenticationService,
//    name: String,
//    phone: String,
//    email: String,
//    password: String,
//    recaptchaResponse: String
//) = CoroutineScope(Dispatchers.Main).async { delay(1000) }
//
////POST - /verify - body: {email, phoneOtp, emailOtp}
////403 - OTP incorrect
////400 - Payload modified, i.e email is incorrect
////502 - Server error
////200 - Success
//fun verifyAsync(
//    authService: AuthenticationService,
//    email: String,
//    phoneOtp: String,
//    emailOtp: String
//) = CoroutineScope(Dispatchers.Main).async { delay(1000) }
//
////POST - /saveparticipant - body: {email, gender, college, rollno, source, year, password}
////502 - Server error
////200 - Success with Bitotsav Id in {data}
//fun saveParticipantAsync(
//    authService: AuthenticationService,
//    email: String,
//    password: String,
//    gender: String,
//    college: String,
//    rollNo: String,
//    source: String,
//    year: Int
//) = CoroutineScope(Dispatchers.Main).async { delay(1000) }
//
////GET - /getCollegeList
////200 - Object containing colleges
//fun fetchCollegeListAsync(
//    authService: AuthenticationService
//) = CoroutineScope(Dispatchers.IO).async {
//    delay(1000)
//    return@async listOf("Fake college")
//}
