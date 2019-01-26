package `in`.bitotsav.shared.network

import androidx.work.*
import java.util.concurrent.TimeUnit

inline fun <reified T : Worker> scheduleWork(input: Data) {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(T::class.java)
        // Sets the input data for the ListenableWorker
        .setInputData(input)
        // If you want to delay the start of work by 60 seconds
        .setInitialDelay(2, TimeUnit.SECONDS)
        // Set a backoff criteria to be used when retry-ing
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        // Set additional constraints
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance().enqueue(oneTimeWorkRequest)
}

inline fun <reified T : Worker> getWork(input: Data): OneTimeWorkRequest {
    val constraints: Constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()
    return OneTimeWorkRequest.Builder(T::class.java)
        // Sets the input data for the ListenableWorker
        .setInputData(input)
        // If you want to delay the start of work by 60 seconds
        .setInitialDelay(2, TimeUnit.SECONDS)
        // Set a backoff criteria to be used when retry-ing
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        // Set additional constraints
        .setConstraints(constraints)
        .build()
}