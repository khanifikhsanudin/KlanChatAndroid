package me.xanip.klanchat.database.headmessage

import androidx.lifecycle.LiveData

class HeadMessageRepository (private var headMessageDao: HeadMessageDao) {

    val allHeadMessage: LiveData<List<HeadMessageData>> = headMessageDao.getAll()

    fun getByThread(thread: String): LiveData<HeadMessageData> = headMessageDao.getByThread(thread)

    suspend fun insert(vararg data: HeadMessageData) {
        headMessageDao.insert(*data)
    }

    suspend fun update(data: HeadMessageData) {
        headMessageDao.update(data)
    }

    suspend fun delete(vararg data: HeadMessageData) {
        headMessageDao.delete(*data)
    }

    suspend fun clear() {
        headMessageDao.clear()
    }
}