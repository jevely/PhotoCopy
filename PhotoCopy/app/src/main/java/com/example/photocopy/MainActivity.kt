package com.example.photocopy

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import java.io.File

class MainActivity : AppCompatActivity(), FindPhotoTool.PhotoCallBack, PhotoAdapter.PhotoClickCallBack {

    private lateinit var photo_recyclerview: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private val photoList by lazy {
        mutableListOf<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    fun init() {
        photo_recyclerview = findViewById(R.id.photo_recyclerview)
        photo_recyclerview.layoutManager = GridLayoutManager(this, 3)
        FindPhotoTool.getInstence().findAllPhoto(this, this)
    }

    //照片回调
    override fun callBack(photoList: MutableList<String>) {
        this.photoList.addAll(photoList)
        handler.sendEmptyMessage(0)
    }

    var handler: Handler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            photoAdapter = PhotoAdapter(this@MainActivity, photoList, this@MainActivity)
            photo_recyclerview.adapter = photoAdapter
        }
    }

    //照片点击
    override fun clickCallBack(position: Int) {
        val photoPath = photoList[position]
        val photoFile = File(photoPath)
        val newPath = CleanUtilsJava.getExtSDCardPath(this)[0] + File.separator + "NewPhoto" + File.separator
        val newFilePath = newPath + photoFile.name
        val newFile = File(newFilePath)
        photoFile.copyTo(newFile)
        Toast.makeText(this, "图片已经复制过去", Toast.LENGTH_SHORT).show()
    }
}
