package `in`.bitotsav.notification.fcm

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.feed.data.Feed
import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.feed.data.FeedType
import `in`.bitotsav.notification.utils.*
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.shared.utils.*
import `in`.bitotsav.shared.workers.*
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent

private enum class UpdateType {
    EVENT,
    RESULT,
    ANNOUNCEMENT,
    PM,
    ALL_EVENTS,
    ALL_TEAMS
}

class DefaultFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    companion object {
        private const val TAG = "FirebaseMsgService"
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "From: ${remoteMessage?.from}")

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            val updateType: UpdateType
            try {
                updateType = UpdateType.valueOf(remoteMessage.data["type"] ?: return)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.message ?: "Illegal Argument exception")
                return
            }

            if (UpdateType.ALL_EVENTS == updateType) {
                scheduleUniqueWork<EventWorker>(
                    workDataOf("type" to EventWorkType.FETCH_ALL_EVENTS.name),
                    getWorkNameForEventWorker(EventWorkType.FETCH_ALL_EVENTS)
                )
//                scheduleWork<EventWorker>(workDataOf("type" to EventWorkType.FETCH_ALL_EVENTS.name))
                return
            }

            if (UpdateType.ALL_TEAMS == updateType) {
                scheduleUniqueWork<TeamWorker>(
                    workDataOf("type" to TeamWorkType.FETCH_ALL_TEAMS.name),
                    getWorkNameForTeamWorker(TeamWorkType.FETCH_ALL_TEAMS)
                )
//                scheduleWork<TeamWorker>(workDataOf("type" to TeamWorkType.FETCH_ALL_TEAMS.name))
                return
            }

            val title = remoteMessage.data["title"] ?: return
            val content = remoteMessage.data["content"] ?: return
            val timestamp = remoteMessage.data["timestamp"]?.toLong() ?: System.currentTimeMillis()
            val feedId = remoteMessage.data["feedId"]?.toLong() ?: return
            val feedType = FeedType.valueOf(updateType.name)
            var channel = Channel.valueOf(updateType.name)

            when (updateType) {
                UpdateType.ANNOUNCEMENT, UpdateType.PM -> {
                    val feed = Feed(
                        feedId,
                        title,
                        content,
                        feedType.name,
                        timestamp,
                        false,
                        null,
                        null
                    )
                    CoroutineScope(Dispatchers.IO).async {
                        get<FeedRepository>().insert(feed)
                    }
                    displayNotification(
                        title,
                        content,
                        timestamp,
                        channel,
                        getFeedPendingIntent(this),
                        this
                    )
                }
                else -> {
//                    UpdateType.EVENT or UpdateType.RESULT
                    val eventId = remoteMessage.data["eventId"]?.toInt() ?: return
                    val deferredIsStarred = CoroutineScope(Dispatchers.IO).async {
                        get<EventRepository>().isStarred(eventId)
                    }
                    val deferredEventName = CoroutineScope(Dispatchers.IO).async {
                        get<EventRepository>().getNameById(eventId)
                    }

                    if (updateType == UpdateType.EVENT) {
                        scheduleUniqueWork<EventWorker>(
                            workDataOf("type" to EventWorkType.FETCH_EVENT.name, "eventId" to eventId),
                            getWorkNameForEventWorker(EventWorkType.FETCH_EVENT, eventId)
                        )
//                        scheduleWork<EventWorker>(
//                            workDataOf("type" to EventWorkType.FETCH_EVENT.name, "eventId" to eventId)
//                        )
                    } else {
                        val eventWork = getWork<EventWorker>(
                            workDataOf("type" to EventWorkType.FETCH_EVENT.name, "eventId" to eventId)
                        )
                        val resultWork = getWork<ResultWorker>(
                            workDataOf("type" to ResultWorkType.RESULT.name, "eventId" to eventId)
                        )
                        WorkManager.getInstance().beginUniqueWork(
                            getWorkNameForResultWorker(ResultWorkType.RESULT, eventId),
                            ExistingWorkPolicy.REPLACE,
                            eventWork
                        ).then(resultWork).enqueue()
                    }

                    val isStarred = runBlocking { deferredIsStarred.await() } ?: return
                    val eventName = runBlocking { deferredEventName.await() } ?: return
                    val feed = Feed(
                        feedId,
                        title,
                        content,
                        feedType.name,
                        timestamp,
                        isStarred,
                        eventId,
                        eventName
                    )
                    CoroutineScope(Dispatchers.IO).async {
                        get<FeedRepository>().insert(feed)
                    }
                    if (isStarred) channel = Channel.STARRED
                    displayNotification(
                        title,
                        content,
                        timestamp,
                        channel,
                        getEventDetailPendingIntent(this, eventId),
                        applicationContext
                    )
                }
            }
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")
        token?.let {
            CurrentUser.fcmToken = token
            if (CurrentUser.isLoggedIn)
                sendFcmTokenToServer()
            return
        }
        Log.wtf(TAG, "Empty token generated!")
    }
}