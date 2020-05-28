package me.xanip.klanchat.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_message.*
import me.xanip.klanchat.R
import me.xanip.klanchat.database.headmessage.HeadMessageData
import me.xanip.klanchat.utils.PreferencesManager
import me.xanip.klanchat.utils.Utility

class HeadMessageAdapter(
    private val onClickItem: (HeadMessageData) -> Unit
): RecyclerView.Adapter<HeadMessageAdapter.Holder>() {

    private val listHeadMessageData = mutableListOf<HeadMessageData>()

    fun setData(list: List<HeadMessageData>) {
        listHeadMessageData.clear()
        listHeadMessageData.addAll(list)
        notifyDataSetChanged()
    }

    fun getByThread(thread: String): HeadMessageData? {
        return listHeadMessageData.firstOrNull { it.thread == thread }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun getItemCount(): Int = listHeadMessageData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindItem(listHeadMessageData[position])
    }

    inner class Holder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(headMessageData: HeadMessageData) {
            containerView.setOnClickListener { onClickItem(headMessageData) }
            if (!headMessageData.from_img.isNullOrEmpty()) {
                tv_avatar.visibility = View.GONE
                Glide.with(containerView.context.applicationContext)
                    .load(headMessageData.from_img)
                    .transition(withCrossFade())
                    .into(iv_avatar)
            }else {
                tv_avatar.setBackgroundColor(Color.parseColor(headMessageData.from_img_color))
                tv_avatar.text = headMessageData.from_name?.substring(0, 1)?:"?"
                tv_avatar.visibility = View.VISIBLE
            }
            tv_name.text = headMessageData.from_name?:"Tanpa Nama"

            val prefs = PreferencesManager.init(containerView.context)
            if (headMessageData.member_id == prefs.memberData?.id) {
                tv_message.text = "Anda: " + headMessageData.text
                if (headMessageData.seen?:0 > 0) {
                    iv_seek.visibility = View.VISIBLE
                }else {
                    iv_seek.visibility = View.GONE
                }
            }else {
                iv_seek.visibility = View.GONE
                tv_message.text = headMessageData.text
            }

            tv_time.text = Utility.getTimeClockHeadMessage(headMessageData.created_at?:"")

            (headMessageData.total_unread?:0).let {
                if (it > 0) {
                    tv_total_unread.visibility = View.VISIBLE
                    tv_total_unread.text = it.toString()
                }else {
                    tv_total_unread.text = ""
                    tv_total_unread.visibility = View.INVISIBLE
                }
            }
        }
    }
}