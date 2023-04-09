package com.example.chikuvpn.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chikuvpn.R
import com.example.chikuvpn.interfaces.NavItemClickListener
import com.example.chikuvpn.model.Server
import java.util.*

class ServerListRVAdapter(private val serverLists: ArrayList<Server>, private val mContext: Context) :
    RecyclerView.Adapter<ServerListRVAdapter.MyViewHolder>() {

    private val listener: NavItemClickListener = mContext as NavItemClickListener

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.servers_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: MyViewHolder, position: Int) {
        holder.serverCountry.text = serverLists[position].country
        Glide.with(mContext)
            .load(serverLists[position].flagUrl)
            .into(holder.serverIcon)

        holder.serverItemLayout.setOnClickListener { listener.clickedItem(position) }
    }

    override fun getItemCount(): Int {
        return serverLists.size
    }

    inner class MyViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverItemLayout: LinearLayout = itemView.findViewById(R.id.serverItemLayout)
        val serverIcon: ImageView = itemView.findViewById(R.id.iconImg)
        val serverCountry: TextView = itemView.findViewById(R.id.countryTv)
    }
}