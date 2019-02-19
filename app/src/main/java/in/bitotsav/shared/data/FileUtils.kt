package `in`.bitotsav.shared.data

import android.content.Context
import org.koin.core.context.GlobalContext.get

fun getJsonStringFromFile(rawResource: Int): String {
    return get().koin.get<Context>().resources.openRawResource(rawResource)
        .bufferedReader()
        .use { it.readText() }
}