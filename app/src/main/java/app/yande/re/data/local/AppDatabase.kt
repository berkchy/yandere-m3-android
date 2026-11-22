package app.yande.re.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.yande.re.model.DownloadRecord
import app.yande.re.model.FavoritePost

@Database(entities = [FavoritePost::class, DownloadRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun downloadsDao(): DownloadsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "yandere_m3.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
