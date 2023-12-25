package br.com.codapp.imagecreator.data.di

import androidx.room.Room
import br.com.codapp.imagecreator.BuildConfig
import br.com.codapp.imagecreator.data.local.db.AiDb
import com.aallam.openai.client.OpenAI
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AiDb::class.java, "ImageAi"
        ).build()
    }

    single { OpenAI(BuildConfig.API_KEY) }
    single { get<AiDb>().imageDao() }
}