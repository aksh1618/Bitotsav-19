package `in`.bitotsav.profile.utils

fun String.isProperEmail() =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
