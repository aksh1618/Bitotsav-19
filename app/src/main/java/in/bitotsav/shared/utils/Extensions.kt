package `in`.bitotsav.shared.utils

import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <A>Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
    map { async { f(it) } }.forEach { it.await() }
}

inline fun <T : ViewDataBinding> T.executeAfter(block: T.() -> Unit) {
    block()
    executePendingBindings()
}