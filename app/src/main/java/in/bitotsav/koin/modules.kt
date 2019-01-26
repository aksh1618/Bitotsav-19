package `in`.bitotsav.koin

import `in`.bitotsav.database.AppDatabase
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.events.ui.ScheduleViewModel
import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.shared.Singleton
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import androidx.room.Room
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// TODO: Should modules conform to package division?
val repositoriesModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "App.db")
            .build()
    }
    factory { EventRepository(get<AppDatabase>().eventDao()) }
    factory { FeedRepository(get<AppDatabase>().feedDao()) }
    factory { ChampionshipTeamRepository(get<AppDatabase>().championshipTeamDao()) }
    factory { NonChampionshipTeamRepository(get<AppDatabase>().nonChampionshipTeamDao()) }
}

/*// TODO: Use this!
val retrofitModule = module {

    single("custom_gson") {
        GsonBuilder().addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                val expose = fieldAttributes.getAnnotation(Expose::class.java)
                return expose != null && !expose.deserialize
            }

            override fun shouldSkipClass(aClass: Class<*>): Boolean {
                return false
            }
        }).create()//!! <- Is this needed?
    }
//    factory { GsonConverterFactory.create(get<Gson>("custom_gson")) }
    factory { Retrofit.Builder()
        .baseUrl(Singleton.baseUrl)
        .addConverterFactory(GsonConverterFactory.create(get<Gson>("custom_gson")))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build() }
}*/


val viewModelsModule = module {
    viewModel { ScheduleViewModel(get()) }
}
