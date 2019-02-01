package `in`.bitotsav.shared.utils

import android.annotation.SuppressLint
import androidx.databinding.BindingAdapter
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
