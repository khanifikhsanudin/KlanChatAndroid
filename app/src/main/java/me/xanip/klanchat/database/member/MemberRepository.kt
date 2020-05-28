package me.xanip.klanchat.database.member

import androidx.lifecycle.LiveData

class MemberRepository (private var memberDao: MemberDao) {

    val allMemberData: LiveData<List<MemberData>> = memberDao.getData()

    suspend fun insert(vararg data: MemberData) {
        memberDao.insert(*data)
    }

    suspend fun update(data: MemberData) {
        memberDao.update(data)
    }

    suspend fun delete(vararg data: MemberData) {
        memberDao.delete(*data)
    }

    suspend fun clear() {
        memberDao.clear()
    }
}