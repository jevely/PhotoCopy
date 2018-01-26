package com.example.photocopy

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.util.*

/**
 * 获取所有图片类
 * lijiawei
 * 2017-7-26
 */
class FindPhotoTool private constructor() {

    private val photoList by lazy {
        mutableListOf<String>()
    }

    companion object {
        private var findPhotoTool: FindPhotoTool? = null
        fun getInstence(): FindPhotoTool {
            if (findPhotoTool == null) {
                synchronized(FindPhotoTool::class.java) {
                    if (findPhotoTool == null) {
                        findPhotoTool = FindPhotoTool()
                    }
                }
            }
            return findPhotoTool!!
        }

        fun destory() {
            findPhotoTool = null
        }
    }

    private var isFind = true

    //获取所有图片
    fun findAllPhoto(context: Context, photoCallBack: PhotoCallBack) {
        Thread(FindPhotoThr(context, photoCallBack)).start()
    }

    private inner class FindPhotoThr(val context: Context, val photoCallBack: PhotoCallBack) : Runnable {

        override fun run() {
            try {
                if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                    return
                }
                photoList.clear()

                val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(imageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? ",
                        arrayOf("image/jpeg", "image/png"), MediaStore.Images.Media.DATE_MODIFIED)
                val outSD = CleanUtilsJava.getExtSDCardPath(context)[0]
                if (cursor != null) {
                    while (cursor.moveToNext() && isFind) {
                        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)) ?: continue

                        val file = File(path)

                        if (!file.exists()) {
                            continue
                        }

                        if (file.length() == 0L) {
                            continue
                        }

                        //排除外置SD卡
                        if (!file.absolutePath.contains(outSD)) {
                            continue
                        }
                        photoList.add(file.absolutePath)
                    }
                    Collections.reverse(photoList)
                    photoCallBack.callBack(photoList)
                    cursor.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface PhotoCallBack {
        fun callBack(photoList: MutableList<String>)
    }

}