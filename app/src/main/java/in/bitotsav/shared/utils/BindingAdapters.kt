package `in`.bitotsav.shared.utils

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("errorText")
fun setErrorText(view: TextInputLayout, text: String) {
    view.error = text
}