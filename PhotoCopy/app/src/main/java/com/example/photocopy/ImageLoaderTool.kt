package com.iobit.amccleaner.booster.cleaner.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.LruCache
import android.widget.ImageView
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore


/**
 * 本地图片加载类
 * lijiawei
 * 2017-7-13
 */
class ImageLoaderTool private constructor() {

    companion object {
        private var imageloader: ImageLoaderTool? = null
        fun getInstence(): ImageLoaderTool {
            if (imageloader == null) {
                synchronized(ImageLoaderTool::class.java) {
                    if (imageloader == null) {
                        imageloader = ImageLoaderTool()
                    }
                }
            }
            return imageloader!!
        }

        fun destroy() {
            imageloader = null
        }
    }

    private lateinit var lruCache: LruCache<String, Bitmap>

    private lateinit var linkedList: LinkedList<Runnable>

    private lateinit var mThreadPool: ExecutorService

    private lateinit var mLunThread: Thread
    private lateinit var mLunHandler: Handler

    private var mUIHandler: Handler? = null

    private val mLunSemaphore = Semaphore(0)

    private lateinit var threadPoolSemaphore: Semaphore

    init {
        init(3)
    }

    private fun init(threadNum: Int) {
        linkedList = LinkedList()

        mLunThread = object : Thread() {
            override fun run() {
                Looper.prepare()
                mLunHandler = object : Handler() {
                    override fun handleMessage(msg: Message) {

                        try {
                            //通知任务队列
                            mThreadPool.execute(linkedList.removeLast())
                            threadPoolSemaphore.acquire()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                mLunSemaphore.release()
                Looper.loop()
            }
        }
        mLunThread.start()

        val maxMemery = Runtime.getRuntime().maxMemory().toInt()
        val lrnMemery = maxMemery / 10
        lruCache = object : LruCache<String, Bitmap>(lrnMemery) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.rowBytes * value.height
        }

        mThreadPool = Executors.newFixedThreadPool(threadNum)
        threadPoolSemaphore = Semaphore(threadNum)
    }

    /**
     * 加载图片（小图）
     */
    fun loadImage(path: String, imageView: ImageView) {
        imageView.tag = path

        if (mUIHandler == null) {
            mUIHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    val imgBind = msg.obj as ImgBind
                    if (imgBind.imageView.tag.toString() == (imgBind.path)) {
                        imgBind.imageView.setImageBitmap(imgBind.bitmap)
                    }
                }
            }
        }

        loadImageFromLruCache(path, imageView)
    }

    private fun loadImageFromLruCache(key: String, imageView: ImageView) {
        val bitmap = lruCache.get(key)
        if (bitmap != null) {
            refreshImageview(key, bitmap, imageView)
        } else {
            addTask(Runnable {
                try {
                    //获取图片 剪裁图片
                    val getBitmap = BitmapFactory.decodeFile(key, getBitmapOptions(key, imageView))


                    //存入缓存
                    if (lruCache.get(key) == null && getBitmap != null) {
                        lruCache.put(key, getBitmap)

                        refreshImageview(key, getBitmap, imageView)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        System.gc()
                        //获取图片 剪裁图片
                        val getBitmap = BitmapFactory.decodeFile(key, getBitmapOptions(key, imageView))

                        //存入缓存
                        if (lruCache.get(key) == null && getBitmap != null) {
                            lruCache.put(key, getBitmap)

                            refreshImageview(key, getBitmap, imageView)
                        }
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }

                }
                threadPoolSemaphore.release()
            })
        }
    }

    /**
     * 获取bitmap缩放的sc
     */
    private fun getOptionsSc(imageView: ImageView, width: Int, height: Int): Int {
        val imgX = imageView.width
        val text = if (width > height) width else height
        return if (text > imgX) {
            if (imgX != 0) {
                val sc = (text / imgX).toFloat()
                sc.toInt()
            } else {
                val sc = (text / 240).toFloat()
                sc.toInt()
            }
        } else {
            2
        }
    }

    /**
     * 获取bitmap缩放的options
     */
    private fun getBitmapOptions(key: String, imageView: ImageView): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(key, options)
        val bitmapWidth = options.outWidth
        val bitmapHeight = options.outHeight
        options.inSampleSize = getOptionsSc(imageView, bitmapWidth, bitmapHeight)
        options.inJustDecodeBounds = false
        return options
    }

    /**
     * 加入任务
     */
    @Synchronized private fun addTask(loadImageThread: Runnable?) {
        try {

            if (loadImageThread != null) {
                linkedList.add(loadImageThread)

                mLunHandler.sendEmptyMessage(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 通知handler设置图片
     */
    private fun refreshImageview(key: String, bitmap: Bitmap, imageView: ImageView) {
        val message = Message()
        val imgBind = ImgBind()
        imgBind.path = key
        imgBind.bitmap = bitmap
        imgBind.imageView = imageView
        message.obj = imgBind
        mUIHandler?.sendMessage(message)
    }

    private class ImgBind {
        lateinit var bitmap: Bitmap
        lateinit var path: String
        lateinit var imageView: ImageView
    }
}