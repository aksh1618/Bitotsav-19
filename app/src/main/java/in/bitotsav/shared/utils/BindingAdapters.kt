package `in`.bitotsav.shared.utils

import `in`.bitotsav.R
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.text.SpannableString
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
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

@BindingAdapter("backgroundTint")
fun setBackgroundTint(button: MaterialButton, color: Int) {
    button.backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("annotatedText")
fun setAnnotatedText(textView: TextView, stringRes: Int) {
    Log.d("BindingAdapters", textView.context.getText(stringRes).toString())
    textView.text = SpannableString(textView.context.getText(stringRes)).getAlignedText()
}

@BindingAdapter("hideOnClick", "rotateOnClick", "expandedColor")
fun hideOnClick(clicked: TextView, toHide: TextView, toRotate: ImageView, color:Int) {
    clicked.setOnClickListener {
        when (toHide.visibility) {
            View.GONE -> {
                toHide.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(toRotate, "rotation", 180f).apply {
                    duration = 300
                    start()
                }
                clicked.setTextColor(color)
                toRotate.imageTintList = ColorStateList.valueOf(color)
            }
            View.VISIBLE -> {
                toHide.visibility = View.GONE
                ObjectAnimator.ofFloat(toRotate, "rotation", 0f).apply {
                    duration = 200
                    start()
                }
                val normalColor = clicked.context.getColorCompat(R.color.textColor)
                clicked.setTextColor(normalColor)
                toRotate.imageTintList = ColorStateList.valueOf(normalColor)
            }
        }
    }
}

@BindingAdapter("uriOnClick")
fun openLinkOnClick(imageView: ImageView, uri: String) {
    imageView.setOnClickListener {
        it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    }
}
