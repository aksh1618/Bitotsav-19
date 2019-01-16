package `in`.bitotsav.network.fcm

import `in`.bitotsav.MainActivity
import `in`.bitotsav.network.NetworkJobService
import `in`.bitotsav.notification.Channel
import `in`.bitotsav.notification.displayNotification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.firebase.jobdispatcher.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

//TODO("Complete this list")
private enum class UpdateType{
    EVENT,
    TEAM,
    RESULT,
    FEED
}

class DefaultFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseMsgService"
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // TODO(developer): Handle FCM messages here.
        Log.d(TAG, "From: ${remoteMessage?.from}")

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            val messageTitle = remoteMessage.data["title"] ?: return
            val messageBody = remoteMessage.data["message"] ?: return
            val timestamp = remoteMessage.data["timestamp"]?.toLong() ?: System.currentTimeMillis()
            val channel = Channel.valueOf(remoteMessage.data["channel"] ?: return)
            if (Channel.ANNOUNCEMENT == channel || Channel.PRIORITY == channel) {
                val intent = Intent(this, MainActivity::class.java)
                displayNotification(messageTitle, messageBody, timestamp, channel, intent, this)
            } else /* Check if data needs to be processed by long running job */ {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                TODO("Check for starred type here")
                val tag = channel.id
                var bundle = Bundle()
                bundle.putString("eventId", "event_id")
                scheduleJob(bundle, tag)
            }
        }

//        // Check if message contains a notification payload.
//        remoteMessage?.notification?.let {
//            Log.d(TAG, "Message Notification Body: ${it.body}")
//        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")
//        TODO("Check logged in status and send token if true")
        if (/*Check if user is logged in*/false) sendTokenToServer(token)
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob(bundle: Bundle, tag: String) {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        Log.d(TAG, "Scheduling new job")
        val random = Random()
        val timeDelay = random.nextInt(5)
        val myJob = dispatcher.newJobBuilder()
            .setService(NetworkJobService::class.java)
            .setTag(tag)
            .setRecurring(false)
            .setLifetime(Lifetime.FOREVER)
            .setTrigger(Trigger.executionWindow(timeDelay, 10))
            .setReplaceCurrent(false)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .setConstraints(
                Constraint.ON_ANY_NETWORK
            )
            .setExtras(bundle)
            .build()

        dispatcher.mustSchedule(myJob)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    fun sendTokenToServer(token: String?) {
//        TODO("Call this method on login")
        // TODO: Implement this method to send token to your app server.
        // Decide whether to use sharedPref or continue with this approach
        // https://codelabs.developers.google.com/codelabs/kotlin-coroutines/#0
        if (token.isNullOrEmpty()) {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    val token = task.result?.token

                    val msg = "InstanceID Token: $token"
                    Log.d(TAG, msg)
                    TODO("Send token here")

                })
        } else {
            TODO("Send token here")
        }
    }
}