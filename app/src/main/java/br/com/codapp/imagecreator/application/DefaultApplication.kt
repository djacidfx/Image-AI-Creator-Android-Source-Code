package br.com.codapp.imagecreator.application

import android.app.Application
import br.com.codapp.imagecreator.data.di.dataModule
import br.com.codapp.imagecreator.feature.di.homeModule
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DefaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        startKoin {
            androidContext(applicationContext)
            modules(
                arrayListOf(
                    dataModule,
                    homeModule
                )
            )
        }
    }
}