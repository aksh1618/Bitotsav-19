package `in`.bitotsav

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

class MyJobService : JobService() {

    companion object {
        private const val TAG = "MyJobService"
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