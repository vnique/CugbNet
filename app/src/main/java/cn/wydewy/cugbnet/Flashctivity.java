package cn.wydewy.cugbnet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wydewy.cugbnet.util.BitmapCache;
import cn.wydewy.cugbnet.util.ComTools;
import cn.wydewy.cugbnet.util.Constant;

public class Flashctivity extends AppCompatActivity {

    @Bind(R.id.imageView)
    NetworkImageView imageView;
    private String url;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        ButterKnife.bind(this);

        /**
         * 1. 创建一个RequestQueue对象。
          2. 创建一个ImageLoader对象。
         3. 在布局文件中添加一个NetworkImageView控件。
         4. 在代码中获取该控件的实例。
         5. 设置要加载的图片地址。
         */
        queue = Volley.newRequestQueue(this);
        getFlashBg();
        ImageLoader imageLoader = new ImageLoader(queue, new BitmapCache());
        imageView.setDefaultImageResId(R.mipmap.bg_mangod);
        imageView.setErrorImageResId(R.mipmap.bg_mangod);
        imageView.setImageUrl(url,
                imageLoader);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                startActivity(new Intent(Flashctivity.this,
                        MainActivity.class));
                Flashctivity.this.finish();
            }
        }, 2000);
    }


    /**
     * 获得闪页背景
     */
    private void getFlashBg() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.GET_BG,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        Log.i("TAG", "---" + response);

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("TAG", "网络错误，response+登录失败！");
            }

        });
        queue.add(stringRequest);
    }
}
