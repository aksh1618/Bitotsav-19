package `in`.bitotsav.shared.utils

import android.os.Build

inline fun forMinApi(apiInt: Int, block: () -> Unit) {
    if (Build.VERSION.SDK_INT > apiInt) {
        block.invoke()
    }
}