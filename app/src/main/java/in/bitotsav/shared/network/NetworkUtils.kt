package `in`.bitotsav.shared.network

import androidx.work.*
import java.util.concurrent.TimeUnit

fun<T: Worker> scheduleWork(input: Data, clazz: Class<T>): OneTimeWorkRequest {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    return OneTimeWorkRequest.Builder(clazz)
        // Sets the input data for the ListenableWorker
        .setInputData(input)
        // If you want to delay the start of work by 60 seconds
        .setInitialDelay(10, TimeUnit.SECONDS)
        // Set a backoff criteria to be used when retry-ing
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        // Set additional constraints
        .setConstraints(constraints)
        .build()
}