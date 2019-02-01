package `in`.bitotsav.shared.utils

import android.os.Build

inline fun runOnMinApi(apiInt: Int, block: () -> Unit) {
    if (Build.VERSION.SDK_INT > apiInt) {
        block.invoke()
    }
}