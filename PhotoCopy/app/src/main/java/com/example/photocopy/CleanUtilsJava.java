package com.example.photocopy;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Method;

/**
 * java常用类
 * lijiawei
 * 2017-7-7
 */

public class CleanUtilsJava {

    /**
     * 获取SD路径
     */
    public static String[] getExtSDCardPath(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            return (String[]) invoke;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    //出去标点符号正则表达式
    public static String removePunctuation(String str) {
        str = str.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]", "");
        return str;
    }
}
