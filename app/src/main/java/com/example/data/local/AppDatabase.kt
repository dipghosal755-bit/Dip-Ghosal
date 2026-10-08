package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmergencyEntity::class,
        LocationHistoryEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyDao(): EmergencyDao
    abstract fun locationDao(): LocationDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_sos_guardian.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { appDb ->
                                appDb.contactDao().insertContact(
                                    ContactEntity(
                                        name = "Mom",
                                        phone = "+1 (555) 019-2831",
                                        relationship = "Mother",
                                        isPrimary = true
                                    )
                                )
                                appDb.contactDao().insertContact(
                                    ContactEntity(
                                        name = "Campus Safety Patrol",
                                        phone = "+1 (555) 911-0022",
                                        relationship = "Campus Security",
                                        isPrimary = false
                                    )
                                )
                                appDb.contactDao().insertContact(
                                    ContactEntity(
                                        name = "Alex Johnson",
                                        phone = "+1 (555) 349-1102",
                                        relationship = "Friend",
                                        isPrimary = false
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
