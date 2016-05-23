package cn.wydewy.cugbnet.util;

import android.graphics.Bitmap;
import android.os.Environment;

/**
 * Created by wydewy on 2016/5/1.
 */
public class Constant {
    public static final String HOST = "http://www.wydewy.cn/xyw";
    public static final String GETVERSION = "/getVersion.php";
    public static final String GET_VERSION_INFO = "http://www.wydewy.cn/xyw/getVersionInfo.php";
    public static final java.lang.String FORCE_OUT = "file:///android_asset/forceOut.html";
    public static final String AUTH_HOST = "http://gw.cugb.edu.cn:804/include/auth_action.php";
    public static final String GET_BG = "http://www.wydewy.cn/xyw/getBg.php";

    public static final String BG_SAVE_PATH = Environment.getExternalStorageState() + "CugbNet/Bg/";

    public static final String UPLOAD = "/upload/";
    public static final java.lang.String DETAIL = "http://202.204.105.195:803/srun_portal_pc_succeed.php";


    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 通知栏按钮点击事件对应的ACTION
     */
    public final static String ACTION_BUTTON = "cn.wydewy.cugbnet.intent.action.ButtonClick";
    public final static int BUTTON_LOGIN_ID = 0;
    public final static int BUTTON_LOGOUT_ID = 1;
}

