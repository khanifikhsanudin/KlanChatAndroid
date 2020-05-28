package me.xanip.klanchat.database.headmessage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HeadMessageDao {

    @Query("SELECT * FROM headmessage ORDER BY created_at DESC")
    fun getAll(): LiveData<List<HeadMessageData>>

    @Query("SELECT * FROM headmessage WHERE thread = :thread LIMIT 1")
    fun getByThread(thread: String): LiveData<HeadMessageData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: HeadMessageData)

    @Update
    suspend fun update(data: HeadMessageData)

    @Delete
    suspend fun delete(vararg data: HeadMessageData)

    @Query("DELETE FROM headmessage")
    suspend fun clear()
}