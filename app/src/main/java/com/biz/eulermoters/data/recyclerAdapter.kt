package com.biz.eulermoters.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.biz.eulermoters.databinding.RecyclerItemBinding
import com.bumptech.glide.Glide
import java.io.File

class recyclerAdapter(
    private var context: Context,
    private var list: ArrayList<File>,
    private var onClickInterface: OnClickInterface,
) : RecyclerView.Adapter<recyclerAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickInterface {
        fun onitemClick(pos: Int)
    }

    inner class ViewHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: recyclerAdapter.ViewHolder, position: Int) {
        try {
            if (list.isNotEmpty()){
                holder.binding.videotitle.text = list[position].name
                val uri = Uri.fromFile(list[position])
                Glide.with(context)
                    .load(uri)
                    .thumbnail(0.1f)
                    .into(holder.binding.thumbnail)

                holder.itemView.setOnClickListener {
                    onClickInterface.onitemClick(position)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int):recyclerAdapter.ViewHolder {
        val v =  RecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }
}