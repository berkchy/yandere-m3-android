package app.yande.re.util

import android.content.Context
import coil.ImageLoader
import app.yande.re.data.local.AppDatabase
import app.yande.re.data.local.SettingsDataStore
import app.yande.re.data.remote.NetworkModule
import app.yande.re.data.repository.PostRepository
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    private val settings = SettingsDataStore(context)
    private val okHttpClient: OkHttpClient = NetworkModule.okHttpClient
    val repository: PostRepository = PostRepository(
        api = NetworkModule.api,
        favoritesDao = db.favoritesDao(),
        downloadsDao = db.downloadsDao(),
        settingsDataStore = settings
    )

    val imageLoader: ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .crossfade(true)
        .respectCacheHeaders(false)
        .build()
}
