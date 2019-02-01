package `in`.bitotsav.shared.utils

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
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

fun Boolean.onTrue(block: () -> Unit): Boolean {
    if (this) block.invoke()
    return this
}

fun Boolean.onFalse(block: () -> Unit): Boolean {
    if (not()) block.invoke()
    return this
}

fun Boolean.Companion.or(vararg booleans: Boolean) = booleans.any { it }

fun String.isLong() = this.toLongOrNull() != null

fun String.isProperEmail() =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
