package `in`.bitotsav.shared.utils

import `in`.bitotsav.R
import android.content.Context
import android.content.Intent
import android.text.Annotation
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <A> Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
    map { async { f(it) } }.forEach { it.await() }
}

inline fun <T : ViewDataBinding> T.executeAfter(block: T.() -> Unit) {
    block()
    executePendingBindings()
}

fun String.toast(context: Context) {
    if (isNotEmpty()) Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun Context.getColorCompat(colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

inline fun Boolean.onTrue(block: () -> Unit): Boolean {
    if (this) block.invoke()
    return this
}

inline fun Boolean.onFalse(block: () -> Unit): Boolean {
    if (not()) block.invoke()
    return this
}

fun Boolean.Companion.or(vararg booleans: Boolean) = booleans.any { it }

fun String.isLong() = this.toLongOrNull() != null

fun String.isProperEmail() =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun AutoCompleteTextView.setEntries(entries: List<String>) {
    setAdapter(ArrayAdapter(context, R.layout.item_spinner, entries))
}

inline fun <T> LiveData<T>.setObserver(
    lifecycleOwner: LifecycleOwner,
    crossinline block: (T) -> Unit
) {
    observe(lifecycleOwner, Observer {
        block.invoke(it)
    })
}

fun Context.shareText(shareTitle: String, textToShare: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textToShare)
    }
    startActivity(Intent.createChooser(shareIntent, shareTitle))
}

fun SpannableString.getAlignedText() =
    apply {
        getSpans(0, length, Annotation::class.java)
            .filter { it.key == "alignment" && it.value == "end" }
            .forEach {
                setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    this.getSpanStart(it),
                    this.getSpanEnd(it),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
    }

fun Context.onConfirmation(action: String, block: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("$action Confirmations")
        .setMessage("Are you sure you want to proceed with $action?")
        .setPositiveButton("Yes") { _, _ ->
            block.invoke()
        }
        .setNegativeButton("No") { _, _ ->
            "$action cancelled".toast(this)
        }
        .create()
        .show()
}

fun String.showInfoDialog(context: Context) {
    AlertDialog.Builder(context)
        .setMessage(this)
        .setPositiveButton("OK", null)
        .setNeutralButton("Help") { _, _ ->
            "Contact our team (Info -> Contact Us)".toast(context)
        }
        .create()
        .show()
}

fun RecyclerView.setupWithFab(fab: FloatingActionButton) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            when {
                dy > 0 -> fab.hide()
                else -> fab.show()
            }
        }
    })
}