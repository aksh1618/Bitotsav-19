package `in`.bitotsav.shared

import `in`.bitotsav.database.AppDatabase
import android.content.Context
import androidx.room.Room
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver

object Singleton {
    val dispatcher = SingletonHolder<FirebaseJobDispatcher, Context> {
        FirebaseJobDispatcher(GooglePlayDriver(it))
    }

    val database = SingletonHolder<AppDatabase, Context> {
        Room.databaseBuilder(it.applicationContext,
            AppDatabase::class.java, "App.db")
            .build()
    }
}