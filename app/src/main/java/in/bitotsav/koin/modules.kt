package `in`.bitotsav.koin

import `in`.bitotsav.database.AppDatabase
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

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

/*
// TODO: Use this!
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
    factory { GsonConverterFactory.create(get<Gson>("custom_gson")) }
}*/
