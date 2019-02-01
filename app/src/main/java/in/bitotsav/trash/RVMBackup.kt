package `in`.bitotsav.trash

import androidx.lifecycle.MutableLiveData

// Common
val currentStep = MutableLiveData<Int>().apply { value = 1 }
val registerError = MutableLiveData<String>().apply { value = "" }
val waiting = MutableLiveData<Boolean>().apply { value = false }

// Step One
// Vars
val name = MutableLiveData<String>().apply { value = "" }
val phone = MutableLiveData<String>().apply { value = "" }
val email = MutableLiveData<String>().apply { value = "" }
// Min 6 chars
val password = MutableLiveData<String>().apply { value = "" }
// Min 6 chars
val passwordAgain = MutableLiveData<String>().apply { value = "" }
val token = MutableLiveData<String>().apply { value = "" }

// Errors
val nameError = MutableLiveData<String>().apply { value = "" }
val phoneError = MutableLiveData<String>().apply { value = "" }
val emailError = MutableLiveData<String>().apply { value = "" }
val passwordError = MutableLiveData<String>().apply { value = "" }
val passwordAgainError = MutableLiveData<String>().apply { value = "" }

// States

// Step Two
// 6 digits
val phoneOtp = MutableLiveData<String>().apply { value = "" }
// 6 digits
val emailOtp = MutableLiveData<String>().apply { value = "" }

val phoneOtpError = MutableLiveData<String>().apply { value = "" }
val emailOtpError = MutableLiveData<String>().apply { value = "" }


// Step Three
// Male/Female/Other/Prefer not to say
val gender = MutableLiveData<String>().apply { value = "" }
val college = MutableLiveData<String>().apply { value = "" }
val rollNo = MutableLiveData<String>().apply { value = "" }
// Friends/Newspaper/Online/Others
val source = MutableLiveData<String>().apply { value = "" }

val genderError = MutableLiveData<String>().apply {
    value = ""
} // Male/Female/Other/Prefer not to say
val collegeError = MutableLiveData<String>().apply { value = "" }
val rollNoError = MutableLiveData<String>().apply { value = "" }
val sourceError = MutableLiveData<String>().apply {
    value = ""
} // Friends/Newspaper/Online/Others