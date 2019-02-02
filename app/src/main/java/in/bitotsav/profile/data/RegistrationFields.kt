package `in`.bitotsav.profile.data

import `in`.bitotsav.profile.utils.MutableLiveDataTextWithValidation
import `in`.bitotsav.profile.utils.NonNullMediatorLiveData
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.shared.utils.isLong
import `in`.bitotsav.shared.utils.isProperEmail

object RegistrationFields {

    // Validations
    private val requiredValidation: String.() -> Boolean = { isNotBlank() }
    private val isProperEmailValidation: String.() -> Boolean = { isProperEmail() }
    private val lengthEqualToTenValidation: String.() -> Boolean = { length == 10 }
    private val lengthGreaterThanFiveValidation: String.() -> Boolean = { length > 5 }
    private val numbersOnlyValidation: String.() -> Boolean = { isLong() }
    private val lengthEqualToSixValidation: String.() -> Boolean = { length == 6 }
    private val oneOfGenderOptionsValidation: String.() -> Boolean =
        { RegistrationFields.genderOptions.contains(this) }
    private val oneOfSourceOptionsValidation: String.() -> Boolean =
        { RegistrationFields.sourceOptions.contains(this) }

    // Required Validation for every field. Only helpful when user clears a field
    // after entering something.
    private val requiredValidationErrorPair = requiredValidation to "Required"

    // Step One
    val name = MutableLiveDataTextWithValidation(requiredValidationErrorPair)
    val phone = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        lengthEqualToTenValidation to "Invalid Phone Number"
    )
    val email = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        isProperEmailValidation to "Invalid Email"
    )
    // Min 6 chars
    val password = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        lengthGreaterThanFiveValidation to "At least 6 characters required"
    )

    // Step Two
    // 6 digits
    val phoneOtp = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        numbersOnlyValidation to "Numbers Only",
        lengthEqualToSixValidation to "Should be 6 digits"
    )
    // 6 digits
    val emailOtp = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        numbersOnlyValidation to "Numbers Only",
        lengthEqualToSixValidation to "Should be 6 digits"
    )


    // Step Three
    val gender = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        oneOfGenderOptionsValidation to "Please select from given options."
    )
    val college = MutableLiveDataTextWithValidation(requiredValidationErrorPair)
    val year = MutableLiveDataTextWithValidation(requiredValidationErrorPair)
    val rollNo = MutableLiveDataTextWithValidation(requiredValidationErrorPair)
    val source = MutableLiveDataTextWithValidation(
        requiredValidationErrorPair,
        oneOfSourceOptionsValidation to "Please select from given options."
    )

    // Male/Female/Other/Prefer not to say
    val genderOptions = listOf(
        "Male",
        "Female",
        "Other",
        "Prefer not to say"
    )
    // List of colleges (to be fetched)
    val collegeOptions = NonNullMutableLiveData(emptyList<String>())
    // Year options
    val yearOptions = listOf(
        "First",
        "Second",
        "Third",
        "Fourth",
        "Fifth"
    )
    // Friends/Newspaper/Online/Others
    val sourceOptions = listOf(
        "Friends",
        "Online",
        "Newspaper",
        "Others"
    )
}