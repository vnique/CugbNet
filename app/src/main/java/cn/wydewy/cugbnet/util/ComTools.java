package cn.wydewy.cugbnet.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wydewy on 2016/5/2.
 */
public class ComTools {

    //通过这种方式保存在本地的图片，是可以看到的

    public static void saveBitmap(Bitmap mBitmap, Context cxt, String bitmapName) {
        FileOutputStream fos = null;
        try {
            fos = cxt.openFileOutput(bitmapName, Context.MODE_PRIVATE);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
// 这里是保存文件产生异常
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
// fos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }


    public static Bitmap getBitmap(String fileName, Context cxt) {
        String bitmapName = fileName.substring(fileName.lastIndexOf("/") + 1);
        FileInputStream fis = null;
        try {
            fis = cxt.openFileInput(bitmapName);
            byte[] b = new byte[fis.available()];
            fis.read(b);
            fis.close();
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
// 这里是读取文件产生异常
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
// fis流关闭异常
                    e.printStackTrace();
                }
            }
        }
// 读取产生异常，返回null
        return null;
    }


    /**
     * 判断本地的私有文件夹里面是否存在当前名字的文件
     */
    public static boolean isFileExist(String fileName, Context cxt) {
        String bitmapName = fileName.substring(fileName.lastIndexOf("/") + 1);
        List<String> nameLst = Arrays.asList(cxt.fileList());
        if (nameLst.contains(bitmapName)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
