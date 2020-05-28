package me.xanip.klanchat.database.headmessage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.xanip.klanchat.database.AppDatabase

class HeadMessageViewModel (application: Application): AndroidViewModel(application) {

    private var repository: HeadMessageRepository
    val allHeadMessage: LiveData<List<HeadMessageData>>

    init {
        val headMessageDao = AppDatabase.getInstance(application).headMessageDao()
        repository = HeadMessageRepository(headMessageDao)
        allHeadMessage = repository.allHeadMessage
    }

    fun getByThread(thread: String): LiveData<HeadMessageData> {
        return repository.getByThread(thread)
    }

    fun insert(vararg data: HeadMessageData) = GlobalScope.launch {
        repository.insert(*data)
    }

    fun update(data: HeadMessageData) = GlobalScope.launch {
        repository.update(data)
    }

    fun delete(vararg data: HeadMessageData) = GlobalScope.launch {
        repository.delete(*data)
    }

    fun clear() = GlobalScope.launch {
        repository.clear()
    }
}