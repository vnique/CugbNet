package cn.wydewy.cugbnet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wydewy.cugbnet.service.ForegroundService;
import cn.wydewy.cugbnet.util.Constant;
import cn.wydewy.cugbnet.util.UpdateManager;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOGIN_OK = 0x0001;
    private static final int PASSWORD_ERROR = 0x0002;
    private static final int LOGOUT_OK = 0x0003;
    private static final int NETWORK_ERROR = 0x0004;
    private static final int SAVE_OUTLINE_OK = 0x0005;
    private static final int IS_THE_LATEST = 0x0006;
    private static final int NOT_THE_LATEST = 0x0007;
    private static final int CHECK_UPDATE = 0x0008;
    private static final int LOGIN_FAILED = 0x0009;
    private static final int UPLOAD_COMPLETED = 0x0010;
    private static final int SAVE_BG_PATH = 0x0012;
    private static final int UPLOAD_FAILED = 0x0011;

    private static final int REQUEST_IMAGE = 1;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tv_username)
    EditText tvUsername;
    @Bind(R.id.tv_password)
    EditText tvPassword;
    @Bind(R.id.btn_sign)
    Button btnSign;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.tv_state)
    TextView tvState;
    @Bind(R.id.tv_result)
    TextView tvResult;
    @Bind(R.id.bg)
    ImageView bg;
    @Bind(R.id.nav_view)
    NavigationView navView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.ad_view)
    AdView adView;

    private boolean isLogin;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            SharedPreferences preferences = getSharedPreferences("logInfo",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            switch (msg.what) {
                case LOGIN_OK:
                    isLogin = true;
                    editor.putBoolean("isLogin", true);
                    editor.commit();
                    loginView();
                    save("在线");
                    break;
                case LOGOUT_OK:
                    isLogin = false;
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    logoutView();
                    break;
                case PASSWORD_ERROR:
                    isLogin = false;
                    tvState.setText(getString(R.string.not_connected));
                    logoutView();
                    break;
                case NETWORK_ERROR:
                    isLogin = false;
                    tvState.setText(getString(R.string.not_connected));
                    logoutView();
                    break;
                case LOGIN_FAILED:
                    isLogin = false;
                    tvState.setText(getString(R.string.not_connected));
                    logoutView();
                    break;
                case IS_THE_LATEST:
                    Toast.makeText(MainActivity.this, getString(R.string.is_the_latest), Toast.LENGTH_SHORT).show();
                    break;
                case NOT_THE_LATEST:
                    UpdateManager updateManager = new UpdateManager(MainActivity.this);
                    if (downloadInfo != null && !"".equals(downloadInfo.get("url")) && !"".equals(downloadInfo.get("name"))) {
                        updateManager.setDownloadInfo(downloadInfo);
                        Log.i("TAG", "来更新");
                        updateManager.showNoticeDialog();

                    }
                    break;
                case CHECK_UPDATE:
                    checkNewVersion(false);
                    break;
                case SAVE_BG_PATH:
                    editor.putString("bg_path", bg_path);
                    editor.commit();
                    break;
            }

//            showButtonNotify();
            application.isLogin = isLogin;
            startService(new Intent(MainActivity.this, ForegroundService.class));
        }
    };

    private ProgressDialog mProgressDialog;

    private String rand_url;
    private String bg_path;
    private InterstitialAd mInterstitialAd;
    private NotificationManager mNotificationManager;
    private CugbNetApplication application;
    private ButtonBroadcastReceiver bReceiver;
    private String isshowad = "0";

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
                        rand_url = response;
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("TAG", "网络错误，response+登录失败！");
                tvResult.setText(getString(R.string.cugb_not_connected) + error);
                handler.sendMessage(handler
                        .obtainMessage(NETWORK_ERROR));
            }

        });
        queue.add(stringRequest);
    }

    private void loginView() {
        tvState.setText(getString(R.string.connected));
        tvResult.setText(getString(R.string.internet_is_ok));
        btnSign.setBackgroundColor(Color.RED);
        btnSign.setText(getString(R.string.logout));
        setContentClickable(false);
    }

    private void logoutView() {
        setContentClickable(true);
        tvState.setText(getString(R.string.not_connected));
        tvResult.setText(getString(R.string.not_connected));
        btnSign.setBackgroundColor(Color.parseColor("#33b5e5"));
        btnSign.setText(getString(R.string.login));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        queue = Volley.newRequestQueue(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        handler.sendEmptyMessageDelayed(CHECK_UPDATE, 1000);

        application = (CugbNetApplication) getApplication();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setSavedBg();
    }

    private void initAd() {
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        SharedPreferences preferences = getSharedPreferences("logInfo",
                Context.MODE_PRIVATE);
        boolean isFirst = preferences.getBoolean("isFirst", false);
        SharedPreferences.Editor editor = preferences.edit();

        if (isFirst) {
            if ("1".equals(isshowad)) {
                AdRequest bannerAdRequest = new AdRequest.Builder().build();
                // Start loading the ad in the background.
                Log.i("adView", "load");
                adView.loadAd(bannerAdRequest);
            } else {
            }
        } else {
            editor.putBoolean("isFirst", true);
            editor.commit();
        }

    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        if (bReceiver != null) {
            unregisterReceiver(bReceiver);
        }
        super.onPause();
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        initButtonReceiver();

        SharedPreferences preferences = getSharedPreferences("logInfo",
                Context.MODE_PRIVATE);
        boolean isLoginDB = preferences.getBoolean("isLogin", false);
        username = preferences.getString("username", "");
        password = preferences.getString("password", "");
        tvUsername.setText(username);
        tvPassword.setText(password);

        if ((!TextUtils.isEmpty(username)) && (!TextUtils.isEmpty(password))) {
//            showButtonNotify();
            application.isLogin = isLoginDB;
            startService(new Intent(this, ForegroundService.class));
            application.username = username;
            application.password = password;
        }

        if (isLoginDB) {
            loginView();
            auth("login");
        } else {
            logoutView();
        }

    }

    /**
     * 设置已经保存的背景
     */
    private void setSavedBg() {
        //设置背景图片
        SharedPreferences preferences = getSharedPreferences("userInfo",
                Context.MODE_PRIVATE);
        bg_path = preferences.getString("bg_path", "");
        try {
            Bitmap bp = BitmapFactory.decodeFile(bg_path);
            if (bp != null) {
//                Toast.makeText(MainActivity.this, bg_path+"Seccess", Toast.LENGTH_SHORT).show();
                bg.setImageBitmap(bp);
            } else {

            }
        } catch (OutOfMemoryError e) {

        }

    }

    private void setBg(final String path) {
        if (bg != null && !"".equals(path)) {
            SharedPreferences preferences = getSharedPreferences("userInfo",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("bg_path", path);
            editor.commit();
            try {
                bg.setImageBitmap(BitmapFactory.decodeFile(path));
//                Toast.makeText(MainActivity.this, path+"Seccess", Toast.LENGTH_SHORT).show();
            } catch (OutOfMemoryError e) {

            }
        } else {

        }
    }

    @OnClick({R.id.toolbar, R.id.tv_username, R.id.tv_password, R.id.btn_sign, R.id.fab, R.id.tv_state})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar:
                break;
            case R.id.tv_username:
                break;
            case R.id.tv_password:
                break;
            case R.id.btn_sign:
                username = tvUsername.getText().toString();
                application.username = username;
                password = tvPassword.getText().toString();
                application.password = password;
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_input_username),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_input_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences preferences = getSharedPreferences("logInfo",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.commit();

                if (isLogin) {
                    save("离线");
                    btnSign.setText(getString(R.string.logout) + "...");
                    auth("logout");
                } else {
                    btnSign.setText(getString(R.string.login) + "...");
                    auth("login");
                }

                break;
            case R.id.fab:
                Intent intent1 = new Intent(this, SpecailActivity.class);
                startActivity(intent1);

                break;
            case R.id.tv_state:
                break;

        }
    }


    /**
     * set input enable
     */
    private void setContentClickable(boolean b) {
        // TODO Auto-generated method stub
        tvUsername.setEnabled(b);
        tvPassword.setEnabled(b);
    }

    /**
     * Request Queue
     */
    private RequestQueue queue;
    /**
     * username
     */
    protected String username;
    /**
     * password
     */
    protected String password;

    /**
     * auth
     *
     * @param action
     */
    public void auth(final String action) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.AUTH_HOST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        Log.i("TAG", "---" + response);
                        if (action.equals("login")) {
                            if (response.contains("login_ok")) {
                                Log.i("TAG", response + "login_ok" + password);
                                handler.sendMessage(handler
                                        .obtainMessage(LOGIN_OK));
                            } else if (response.contains("Password is error")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.password_error),
                                        Toast.LENGTH_SHORT).show();
                                handler.sendMessage(handler
                                        .obtainMessage(PASSWORD_ERROR));
                            } else {
                                handler.sendMessage(handler
                                        .obtainMessage(LOGIN_FAILED));

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.logout_successfully),
                                    Toast.LENGTH_SHORT).show();
                            handler.sendMessage(handler
                                    .obtainMessage(LOGOUT_OK));
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("TAG", "网络错误，response+登录失败！");
                tvResult.setText(getString(R.string.cugb_not_connected) + error);
                handler.sendMessage(handler
                        .obtainMessage(NETWORK_ERROR));
            }

        }) {
            protected Map<String, String> getParams() {
                // 在这里设置需要post的参数
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", application.username);
                map.put("password", application.password);
                map.put("action", action);
                map.put("ac_id", "1");
                map.put("save_me", "0");
                map.put("ajax", "1");
                return map;
            }
        };
        queue.add(stringRequest);
    }


    private void save(final String state) {
        String url = "http://www.wydewy.cn/xyw/save.php";
        // TODO Auto-generated method stub
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("TAG", response + "保存成功！" + password);
                        if (state.equals("离线")) {
                            handler.sendMessage(handler.obtainMessage(SAVE_OUTLINE_OK));
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("TAG", error.toString() + "保存失败！" + password);
                if (state.equals("离线")) {
                    handler.sendMessage(handler.obtainMessage(SAVE_OUTLINE_OK));
                }
            }

        }) {
            protected Map<String, String> getParams() {
                // 在这里设置需要post的参数
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", application.username);
                map.put("password", application.password);
                map.put("state", state);
                String app_level = getCurrentVersion();
                map.put("app_level", app_level);
                return map;
            }
        };
        queue.add(stringRequest);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            SharedPreferences preferences = getSharedPreferences("data",
                    Context.MODE_PRIVATE);
            tvUsername.setText(preferences.getString("username", ""));
            tvPassword.setText(preferences.getString("password", ""));

            return true;
        }

        if (id == R.id.action_load_web_bg) {
            showWebBg();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showWebBg() {
        getFlashBg();
        /**
         * 1. 创建一个RequestQueue对象。
         2. 创建一个ImageLoader对象。
         3. 在布局文件中添加一个NetworkImageView控件。
         4. 在代码中获取该控件的实例。
         5. 设置要加载的图片地址。
         */
        RequestQueue mQueue = Volley.newRequestQueue(this);
        ImageRequest imageRequest = new ImageRequest(rand_url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        bg.setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                bg.setImageResource(R.mipmap.bg_mangod);
            }
        });
        mQueue.add(imageRequest);
    }


    /**
     * @param
     * @return
     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ad) {
            // Handle the camera action
            // Create the InterstitialAd and set the adUnitId.

            showInterstitial();

        } else if (id == R.id.nav_detail) {
            startActivity(new Intent(MainActivity.this, DetailActivity.class));
        } else if (id == R.id.nav_gallery) {
            selectImage();
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(MainActivity.this, UpdateActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_update) {
            checkNewVersion(true);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void selectImage() {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示调用相机拍照
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大图片选择数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        // 默认选择图片,回填选项(支持String ArrayList)
        ArrayList<String> defaultDataArray = new ArrayList<>();
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
//            Toast.makeText(MainActivity.this, "seccess", Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
//                Toast.makeText(MainActivity.this, "seccess:"+paths.size(), Toast.LENGTH_SHORT).show();
                if (paths.size() > 0) {
//                    handler.sendEmptyMessageDelayed(SAVE_BG_PATH, 1000);
                    setBg(paths.get(0));
                }
            }
        }
    }


    //更新相关
    public HashMap<String, String> downloadInfo = new HashMap<String, String>();

    public void checkNewVersion(final boolean showTip) {
        String url = Constant.GET_VERSION_INFO;

        // Toast.makeText(this,"检查更新...",Toast.LENGTH_LONG).show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.i("TAG", response.toString());
                try {
                    String version = response.getString("version");
                    String url = response.getString("url");
                    String name = response.getString("name");
                    isshowad = response.getString("isshowad");
                    downloadInfo.put("url", url);
                    downloadInfo.put("name", name);
                    initAd();
                    try {
                        float newVesrion = Float.parseFloat(version);
                        float currentVesrion = Float.parseFloat(getCurrentVersion());
                        Log.i("TAG", newVesrion + "-----" + currentVesrion);

                        if (newVesrion > currentVesrion) {
                            handler.sendMessage(handler.obtainMessage(NOT_THE_LATEST));
                        } else if (newVesrion == currentVesrion) {
                            if (showTip) {
                                handler.sendMessage(handler.obtainMessage(IS_THE_LATEST));
                            }
                        }

                    } catch (NumberFormatException e) {

                    }

                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }

    private String getCurrentVersion() {

        int versionCode = 0;
        String versionName = null;
        try {
            // 获取软件版本号，
            versionCode = this.getPackageManager().getPackageInfo("cn.wydewy.cugbnet", 0).versionCode;
            versionName = this.getPackageManager().getPackageInfo("cn.wydewy.cugbnet", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 通知栏按钮点击事件对应的ACTION
     */
    public final static String ACTION_BUTTON = "cn.wydewy.cugbnet.intent.action.ButtonClick";
    public final static int BUTTON_LOGIN_ID = 0;
    public final static int BUTTON_LOGOUT_ID = 1;

    /**
     * 带按钮的通知栏
     */
    public void showButtonNotify() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        mRemoteViews.setImageViewResource(R.id.image, R.mipmap.wydewy);
        //API3.0 以上的时候显示按钮，否则消失
        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        if (isLogin) {
            mRemoteViews.setTextViewText(R.id.msg, getString(R.string.connected));
            mRemoteViews.setTextViewText(R.id.btn, getString(R.string.logout));
            mRemoteViews.setTextColor(R.id.btn, 0xffff0000);
            buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_LOGOUT_ID);
        } else {
            mRemoteViews.setTextViewText(R.id.msg, getString(R.string.not_connected));
            mRemoteViews.setTextViewText(R.id.btn, getString(R.string.login));
            mRemoteViews.setTextColor(R.id.btn, 0xff0000ff);
            buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_LOGIN_ID);
        }
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_auth = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn, intent_auth);

        //点击的意图ACTION是跳转到Intent
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContent(mRemoteViews)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("点击可以认证")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.mipmap.wydewy);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        //会报错，还在找解决思路
//		notify.contentView = mRemoteViews;
//		notify.contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        mNotificationManager.notify(200, notify);
    }


    /**
     * 带按钮的通知栏点击广播接收
     */
    public void initButtonReceiver() {
        bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }


    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.ACTION_BUTTON)) {
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(Constant.INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case Constant.BUTTON_LOGIN_ID:
                        auth("login");
                        break;
                    case Constant.BUTTON_LOGOUT_ID:
                        auth("logout");
                        break;
                }
            }
        }


    }
}
