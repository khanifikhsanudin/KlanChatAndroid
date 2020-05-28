package me.xanip.klanchat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.xanip.klanchat.database.headmessage.HeadMessageDao
import me.xanip.klanchat.database.headmessage.HeadMessageData
import me.xanip.klanchat.database.member.MemberDao
import me.xanip.klanchat.database.member.MemberData


@Database(entities = [MemberData::class, HeadMessageData::class], version = DB_VERSION)
abstract class AppDatabase: RoomDatabase() {

    abstract fun memberDao(): MemberDao

    abstract fun headMessageDao(): HeadMessageDao

    companion object {
        @Volatile
        private var databaseInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            databaseInstance
                ?: synchronized(this) {
                    databaseInstance ?: buildDatabaseInstance(
                            context
                        ).also {
                            databaseInstance = it
                        }
                }

        private fun buildDatabaseInstance(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}

const val DB_VERSION = 1
const val DB_NAME = "Klanchat.db"