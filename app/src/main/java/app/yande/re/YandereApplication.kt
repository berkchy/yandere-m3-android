package app.yande.re

import android.app.Application
import app.yande.re.util.AppContainer

class YandereApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
