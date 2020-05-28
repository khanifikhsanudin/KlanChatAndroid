package me.xanip.klanchat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact.*
import me.xanip.klanchat.R
import me.xanip.klanchat.database.member.MemberData

class ContactAdapter(
    private val onClickItem: (MemberData) -> Unit
): RecyclerView.Adapter<ContactAdapter.Holder>() {

    private val listMemberData = mutableListOf<MemberData>()

    fun setData(list: List<MemberData>) {
        listMemberData.clear()
        listMemberData.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    }

    override fun getItemCount(): Int = listMemberData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindItem(listMemberData[position])
    }

    inner class Holder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindItem(memberData: MemberData) {
            containerView.setOnClickListener { onClickItem(memberData) }
            if (!memberData.img.isNullOrEmpty()) {
                tv_avatar.visibility = View.GONE
                Glide.with(containerView.context.applicationContext)
                    .load(memberData.img)
                    .transition(withCrossFade())
                    .into(iv_avatar)
            }else {
                tv_avatar.setBackgroundColor(Color.parseColor(memberData.img_color))
                tv_avatar.text = memberData.name?.substring(0, 1)?:"?"
                tv_avatar.visibility = View.VISIBLE
            }
            tv_name.text = memberData.name?:"Tanpa Nama"
            tv_email.text = memberData.email
        }
    }
}