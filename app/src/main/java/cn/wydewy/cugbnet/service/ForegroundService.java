package cn.wydewy.cugbnet.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.wydewy.cugbnet.CugbNetApplication;
import cn.wydewy.cugbnet.MainActivity;
import cn.wydewy.cugbnet.R;
import cn.wydewy.cugbnet.util.Constant;


public class ForegroundService extends Service {

    private static final Class[] mStartForegroundSignature = new Class[]{
            int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[]{
            boolean.class};
    private static final int LOGIN_OK = 0;
    private static final int LOGOUT_OK = 1;
    private static final int PASSWORD_ERROR = 2;
    private static final int LOGIN_FAILED = 3;
    private static final int NETWORK_ERROR = 4;

    private CugbNetApplication application;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            SharedPreferences preferences = getSharedPreferences("logInfo",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            switch (msg.what) {
                case LOGIN_OK:
                    application.isLogin = true;
                    editor.putBoolean("isLogin", true);
                    editor.commit();
                    break;
                case LOGOUT_OK:
                    application.isLogin = false;
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    break;
                case PASSWORD_ERROR:
                    application.isLogin = false;
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    break;
                case NETWORK_ERROR:
                    application.isLogin = false;
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_FAILED:
                    application.isLogin = false;
                    editor.putBoolean("isLogin", false);
                    editor.commit();
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    break;
            }
            showNotification();
        }
    };
    private RequestQueue queue;
    private ServiceBroadcastReceiver bReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(getApplicationContext());
        application = (CugbNetApplication) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (bReceiver == null) {
            initButtonReceiver();
        }
        showNotification();
        return START_STICKY;
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        mRemoteViews.setImageViewResource(R.id.image, R.mipmap.wydewy);
        //点击的事件处理
        Intent buttonIntent = new Intent(Constant.ACTION_BUTTON);
        if (application.isLogin) {
            mRemoteViews.setTextViewText(R.id.msg, getString(R.string.connected));
            mRemoteViews.setTextViewText(R.id.btn, getString(R.string.logout));
            mRemoteViews.setTextColor(R.id.btn, 0xffff0000);
            buttonIntent.putExtra(Constant.INTENT_BUTTONID_TAG, Constant.BUTTON_LOGOUT_ID);
        } else {
            mRemoteViews.setTextViewText(R.id.msg, getString(R.string.not_connected));
            mRemoteViews.setTextViewText(R.id.btn, getString(R.string.login));
            mRemoteViews.setTextColor(R.id.btn, 0xff0000ff);
            buttonIntent.putExtra(Constant.INTENT_BUTTONID_TAG, Constant.BUTTON_LOGIN_ID);
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
        startForeground(1, notify);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (bReceiver != null) {
            unregisterReceiver(bReceiver);
        }
    }

    /**
     * 带按钮的通知栏点击广播接收
     */
    public void initButtonReceiver() {
        bReceiver = new ServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }

    public class ServiceBroadcastReceiver extends BroadcastReceiver {

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

    /**
     * auth
     *
     * @param action
     */
    public void auth(final String action) {
        if (application.username == null || ("".equals(application.username))
                || (application.password == null) || ("".equals(application.password))) {
            SharedPreferences preferences = getSharedPreferences("logInfo",
                    Context.MODE_PRIVATE);
            application.username = preferences.getString("username", "");
            application.password = preferences.getString("password", "");
        };
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.AUTH_HOST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO Auto-generated method stub
                        Log.i("TAG", "---" + response);
                        if (action.equals("login")) {
                            if (response.contains("login_ok")) {
                                Log.i("TAG", response + "login_ok" + application.password);
                                handler.sendMessage(handler
                                        .obtainMessage(LOGIN_OK));
                            } else if (response.contains("Password is error")) {
                                handler.sendMessage(handler
                                        .obtainMessage(PASSWORD_ERROR));
                            } else {
                                handler.sendMessage(handler
                                        .obtainMessage(LOGIN_FAILED));
                            }
                        } else {
                            handler.sendMessage(handler
                                    .obtainMessage(LOGOUT_OK));
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("TAG", "网络错误，response+登录失败！"+error.toString());
                Log.i("TAG", application.username+" "+application.password);
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
        if (queue == null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        }
        queue.add(stringRequest);
    }
}