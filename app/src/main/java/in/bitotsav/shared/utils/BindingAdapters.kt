package `in`.bitotsav.shared.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.AutoCompleteTextViewBindingAdapter
import androidx.databinding.adapters.SpinnerBindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("errorText")
fun setErrorText(view: TextInputLayout, text: String) {
    view.error = text
}

@SuppressLint("NewApi")
@BindingAdapter("android:autofillHints")
fun setAutofillHints(view: TextInputLayout, autofillHints: String) {
    runOnMinApi(26) {
        view.setAutofillHints(autofillHints)
    }
}

@SuppressLint("NewApi")
@BindingAdapter("android:importantForAutofill")
fun setImportantForAutofill(view: TextInputLayout, isImportant: Boolean) {
    runOnMinApi(26) {
        isImportant
            .onTrue {
                view.importantForAutofill = TextInputLayout.IMPORTANT_FOR_AUTOFILL_NO
            }
            .onFalse {
                view.importantForAutofill = TextInputLayout.IMPORTANT_FOR_AUTOFILL_YES
            }
    }
}

@BindingAdapter("passwordHidingEnabled")
fun setPasswordHidingEnabled(view: TextInputEditText, isPass: Boolean) {
    isPass.onTrue { view.transformationMethod = PasswordTransformationMethod.getInstance() }
}

@BindingAdapter("passwordToggleEnabled")
fun setPasswordToggleEnabled(view: TextInputLayout, isPass: Boolean) {
    Log.wtf("passwordToggle", "Yep, got: $isPass")
    view.isPasswordVisibilityToggleEnabled = isPass
}

@BindingAdapter("passwordToggleTint")
fun setPasswordToggleTint(view: TextInputLayout, color: ColorStateList) {
    view.setPasswordVisibilityToggleTintList(color)
}
