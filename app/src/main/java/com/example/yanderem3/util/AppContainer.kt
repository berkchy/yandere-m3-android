package com.example.yanderem3.util

import android.content.Context
import coil.ImageLoader
import com.example.yanderem3.data.local.AppDatabase
import com.example.yanderem3.data.local.SettingsDataStore
import com.example.yanderem3.data.remote.NetworkModule
import com.example.yanderem3.data.repository.PostRepository
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    private val settings = SettingsDataStore(context)
    private val okHttpClient: OkHttpClient = NetworkModule.okHttpClient
    val repository: PostRepository = PostRepository(
        api = NetworkModule.api,
        favoritesDao = db.favoritesDao(),
        settingsDataStore = settings
    )

    val imageLoader: ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .crossfade(true)
        .respectCacheHeaders(false)
        .build()
}
