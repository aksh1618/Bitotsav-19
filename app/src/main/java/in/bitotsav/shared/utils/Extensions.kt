package `in`.bitotsav.shared.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <A>Collection<A>.forEachParallel(f: suspend (A) -> Unit): Unit = runBlocking {
    map { async { f(it) } }.forEach { it.await() }
}