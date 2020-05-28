package me.xanip.klanchat.database.member

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemberDao {

    @Query("SELECT * FROM member")
    fun getData(): LiveData<List<MemberData>>

    @Query("SELECT * FROM member")
    fun getDirectData(): List<MemberData>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: MemberData)

    @Update
    suspend fun update(data: MemberData)

    @Delete
    suspend fun delete(vararg data: MemberData)

    @Query("DELETE FROM member")
    suspend fun clear()
}