package `in`.bitotsav.shared.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
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

@BindingAdapter("passwordToggleEnabled")
fun setPasswordToggleEnabled(view: TextInputLayout, isPass: Boolean) {
    view.isPasswordVisibilityToggleEnabled = isPass
}

@BindingAdapter("passwordToggleTint")
fun setPasswordToggleTint(view: TextInputLayout, color: ColorStateList) {
    view.setPasswordVisibilityToggleTintList(color)
}

@BindingAdapter("actvAdapter", "useAsSpinner", requireAll = false)
fun setActvAdapter(
    view: AutoCompleteTextView, entries: List<String>, asSpinnerOnly: Boolean = true
) {
    view.setEntries(entries)
    asSpinnerOnly.onTrue {
        view.keyListener = null
        view.setOnTouchListener { v, _ ->
            (v as AutoCompleteTextView).showDropDown()
            false
        }
    }

}

@BindingAdapter("centerInLayout")
fun setCenterInLayout(view: TextView, centerInLayout: Boolean) {
    if (centerInLayout) {
        (view.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.CENTER
        }
    }
}
