package `in`.bitotsav.network

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

class NetworkJobService : JobService() {

    companion object {
        private const val TAG = "NetworkJobService"
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d(TAG, "onStartJob")
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob")
        return true
    }
}