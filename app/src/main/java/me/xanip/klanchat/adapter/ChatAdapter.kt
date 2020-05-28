package me.xanip.klanchat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_bubble_left.*
import kotlinx.android.synthetic.main.item_bubble_right.*
import kotlinx.android.synthetic.main.item_chat_loadmore.*
import kotlinx.android.synthetic.main.item_chat_new_message.*
import kotlinx.android.synthetic.main.item_chat_seek.*
import me.xanip.klanchat.R
import me.xanip.klanchat.database.model.ChatData
import me.xanip.klanchat.utils.Utility

class ChatAdapter(
    private val onClickLoadMore: (LoadMoreData) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem = mutableListOf<Any>()

    fun setData(list: List<Any>) {
        listItem.clear()
        listItem.addAll(list)
        notifyDataSetChanged()
    }

    fun addRangeDataTop(list: List<Any>) {
        listItem.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

    fun addRangeDataBottom(list: List<Any>) {
        listItem.addAll(listItem.size, list)
        notifyItemRangeInserted(listItem.size, list.size)
    }

    fun setLoadingLoadMore(isLoading: Boolean) {
        val index = listItem.indexOfFirst { it is LoadMoreData }
        if (index >= 0) {
            (listItem[index] as LoadMoreData).isLoading = isLoading
            notifyItemChanged(index)
        }
    }

    fun getLastChatTime(): String? {
        return listItem
            .filterIsInstance<ChatData>()
            .lastOrNull()?.created_at
    }

    fun getLastChat(): ChatData? {
        return listItem.filterIsInstance<ChatData>().lastOrNull()
    }

    fun getNewMessagePosition(): Int {
        return listItem.indexOfFirst { it is Int }
    }

    fun increaseNewMessage(num: Int) {
        val index = listItem.indexOfFirst { it is Int }
        if (index >= 0) {
            listItem[index] = (listItem[index] as Int) + num
            notifyItemChanged(index)
        }
    }

    fun isHasSeek(): Boolean {
        return listItem.any { it is String }
    }

    fun addSeek(last: String): Boolean {
        val lastChat = listItem.lastOrNull { it is ChatData } as ChatData?
        if (listItem.last() is ChatData && lastChat != null && lastChat.me == true && lastChat.created_at == last) {
            listItem.add("Dilihat")
            notifyItemInserted(listItem.size - 1)
            return true
        }else {
            return false
        }
    }

    fun removeLoadmore() {
        val index = listItem.indexOfFirst { it is LoadMoreData }
        if (index >= 0) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeNewMessage() {
        val index = listItem.indexOfFirst { it is Int }
        if (index >= 0) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeSeek() {
        val index = listItem.indexOfFirst { it is String }
        if (index >= 0) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun filterTruthList(list: List<ChatData>): List<ChatData> {
        val thisChat = listItem.filterIsInstance<ChatData>().map { it.id }
        return list.filter { it.id !in thisChat }
    }

    override fun getItemViewType(position: Int): Int {
        return when(val item = listItem[position]) {
            is ChatData      -> if(item.me == true) RIGHT_TYPE else LEFT_TYPE
            is LoadMoreData  -> LOADMORE_TYPE
            is Int           -> NEW_MESSAGE_TYPE
            is String        -> SEEK_TYPE
            else             -> error("Tipe View tidak diketahui")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RIGHT_TYPE        -> {
                ChatRightHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_bubble_right, parent, false))
            }
            LEFT_TYPE         -> {
                ChatLeftHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_bubble_left, parent, false))
            }
            LOADMORE_TYPE     -> {
                ChatLoadMoreHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_loadmore, parent, false))
            }
            NEW_MESSAGE_TYPE  -> {
                ChatNewMessageHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_new_message, parent, false))
            }
            SEEK_TYPE -> {
                ChatSeekHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_seek, parent, false))
            }
            else              -> error("Tipe View tidak diketahui")
        }
    }

    override fun getItemCount(): Int = listItem.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when(position) {
//            0 -> Utility.setMargins(
//                holder.itemView, holder.itemView.marginLeft,
//                holder.itemView.context.resources.getDimension(R.dimen._12sdp).toInt(),
//                holder.itemView.marginRight, holder.itemView.marginBottom
//            )
//            listItem.size - 1 -> Utility.setMargins(
//                holder.itemView, holder.itemView.marginLeft,
//                holder.itemView.marginTop, holder.itemView.marginRight,
//                holder.itemView.context.resources.getDimension(R.dimen._12sdp).toInt()
//            )
//            else -> Utility.setMargins(
//                holder.itemView, holder.itemView.marginLeft,
//                holder.itemView.marginTop, holder.itemView.marginRight,
//                holder.itemView.marginBottom
//            )
//        }
        when(val item = listItem[position]) {
            is ChatData      -> {
                if(item.me == true) {
                    (holder as ChatRightHolder).bindItem(item)
                }else {
                    (holder as ChatLeftHolder).bindItem(item)
                }
            }
            is LoadMoreData  -> { (holder as ChatLoadMoreHolder).bindItem(item) }
            is Int           -> { (holder as ChatNewMessageHolder).bindItem(item) }
            is String        -> { (holder as ChatSeekHolder).bindItem(item) }
        }
    }

    inner class ChatRightHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        fun bindItem(chatData: ChatData) {
            tv_text_right.text = "${chatData.text}${containerView.context.getString(R.string.text_helper)}"
            tv_time_right.text = Utility.getTimeClock(chatData.created_at?:"")
        }
    }

    inner class ChatLeftHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        fun bindItem(chatData: ChatData) {
            tv_text_left.text = "${chatData.text}${containerView.context.getString(R.string.text_helper)}"
            tv_time_left.text = Utility.getTimeClock(chatData.created_at?:"")
        }
    }

    inner class ChatLoadMoreHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bindItem(data: LoadMoreData) {
            cv_loadmore.setOnClickListener { onClickLoadMore(data) }
            if (data.isLoading) {
                cv_loadmore.visibility = View.INVISIBLE
                progress_bar_loadmore.visibility = View.VISIBLE
            }else {
                progress_bar_loadmore.visibility = View.GONE
                cv_loadmore.visibility = View.VISIBLE
            }
        }
    }

    inner class ChatNewMessageHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        fun bindItem(data: Int) {
            tv_new_message.text = "$data PESAN BELUM DIBACA"
        }
    }

    inner class ChatSeekHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        fun bindItem(data: String) {
            tv_seek.text = data
        }
    }

    companion object {
        const val RIGHT_TYPE = 0
        const val LEFT_TYPE  = 1
        const val LOADMORE_TYPE  = 2
        const val NEW_MESSAGE_TYPE = 3
        const val SEEK_TYPE = 4
        const val DATE_TYPE = 5
    }

    data class LoadMoreData(
        val page: String? = null,
        val cursor: String?= null,
        var isLoading: Boolean = false
    )


}