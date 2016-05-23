package cn.wydewy.cugbnet.util;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by wydewy on 2016/5/2.
 */
public class BitmapCache implements ImageLoader.ImageCache {

    @Override
    public Bitmap getBitmap(String url) {
        return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {

    }
}
