package me.xanip.klanchat.database.member

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.xanip.klanchat.database.AppDatabase

class MemberViewModel(application: Application): AndroidViewModel(application) {

    private var repository: MemberRepository
    val allMemberData: LiveData<List<MemberData>>

    init {
        val memberDao = AppDatabase.getInstance(application).memberDao()
        repository = MemberRepository(memberDao)
        allMemberData = repository.allMemberData
    }

    fun insert(vararg data: MemberData) = GlobalScope.launch {
        repository.insert(*data)
    }

    fun update(data: MemberData) = GlobalScope.launch {
        repository.update(data)
    }

    fun delete(vararg data: MemberData) = GlobalScope.launch {
        repository.delete(*data)
    }

    fun clear() = GlobalScope.launch {
        repository.clear()
    }
}