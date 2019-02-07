package `in`.bitotsav.teams.data

import `in`.bitotsav.profile.utils.MutableLiveDataTextWithValidation
import `in`.bitotsav.shared.utils.isLong
import `in`.bitotsav.shared.utils.isProperEmail

class RegistrationMember (val index: Int, id: String = "", email: String = "") {

    private val requiredValidation: String.() -> Boolean = { isNotBlank() }
    private val requiredValidationErrorPair = requiredValidation to "Required"
    private val isProperEmailValidation: String.() -> Boolean = { isProperEmail() }
    private val numbersOnlyValidation: String.() -> Boolean = { isLong() }
    private val lengthEqualToFiveValidation: String.() -> Boolean = { length == 5 }

    val bitotsavId = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        numbersOnlyValidation to "Enter the part after 'BT19/'",
        lengthEqualToFiveValidation to "Enter last 5 digits only",
        defaultText = id,
        defaultErrorText = ""
    )

    val email = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        isProperEmailValidation to "Invalid Email",
        defaultText = email,
        defaultErrorText = ""
    )

}

data class Member(val memberId: String, val memberEmail: String)
