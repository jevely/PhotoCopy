package com.example.photocopy

import android.content.Context
import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.iobit.amccleaner.booster.cleaner.tools.ImageLoaderTool

/**
 * Created by Administrator on 2018/1/26 0026.
 */
class PhotoAdapter(val context: Context, val photoList: MutableList<String>, val photoClickCallBack: PhotoClickCallBack) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {


    override fun onBindViewHolder(holder: PhotoViewHolder?, position: Int) {
        holder?.setData(photoList[position])
        holder?.iv_ll!!.setOnClickListener {
            photoClickCallBack.clickCallBack(position)
        }
    }

    override fun getItemCount(): Int = photoList.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.photorecyclerview, parent, false)
        val viewHolder = PhotoViewHolder(view)
        return viewHolder
    }


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var photo_iv: ImageView
        var iv_ll: LinearLayout

        init {
            photo_iv = view.findViewById(R.id.photo_iv)
            iv_ll = view.findViewById(R.id.iv_ll)
        }

        fun setData(path: String) {
            ImageLoaderTool.getInstence().loadImage(path, photo_iv)
        }
    }

    interface PhotoClickCallBack {
        fun clickCallBack(position: Int)
    }

}