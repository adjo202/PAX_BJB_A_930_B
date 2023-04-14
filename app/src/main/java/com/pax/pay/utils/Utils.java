package com.pax.pay.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.app.MacroDefine;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.currency.Currency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final String TAG = "Utils";

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 把密度转换为像素
     */
    public static int dip2px(Context context, float px) {
        final float scale = getScreenDensity(context);
        return (int) (px * scale + 0.5);
    }

    public static void install(Context context, String name, String path) {
        File file = new File(path + name);
        try (
            InputStream in = context.getAssets().open(name);
            FileOutputStream out = new FileOutputStream(file)
        ) {
            int count = 0;
            byte[] tmp = new byte[1024];
            while ((count = in.read(tmp)) != -1) {
                out.write(tmp, 0, count);
            }
            //Runtime.getRuntime().exec("chmod 777 " + path + name);  //removed by richard 20170823, for issue ANDROIDUPI-38
            //This operation is not necessary for our application.
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
    }

    /**
     * 获取主秘钥索引
     * 
     * @param index
     *            0~99的主秘钥索引值
     * @return 1~100的主秘钥索引值
     */
    public static int getMainKeyIndex(int index) {
        byte mode = MacroDefine.MAINKEY_INDEX_MODE;
        if (mode == 0x01) {
            return index * 2 + 1;
        } else if (mode == 0x02) {
            if (FinancialApplication.getSysParam().get(SysParam.EX_PINPAD).equals(Constant.PAD_INTERNAL)) {
                return index * 2 + 1;
            }
            return index * 2;
        } else {
            return index * 2 + 1;
        }
    }

    /**
     * 返回当前屏幕是否为竖屏。
     * 
     * @param context
     * @return 当且仅当当前屏幕为竖屏时返回true,否则返回false。
     */
    public static boolean isScreenOrientationPortrait(Context context) { //PORTRAIT 竖排| LANDSCAPE 横排
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static byte[] str2Bcd(String str) {
        return FinancialApplication.getConvert().strToBcd(str, IConvert.EPaddingPosition.PADDING_LEFT);
    }

    public static String bcd2Str(byte[] bcd) {
        return FinancialApplication.getConvert().bcdToStr(bcd);
    }

    public static long parseLongSafe(String longStr, long safeValue) {
        if (longStr == null)
            return safeValue;
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return safeValue;
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }



    public static boolean checkTime(String startTime, String endTime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(startTime);
            Date date2 = sdf.parse(endTime);
            if(date1.before(date2)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isValidJSON(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }


    public static String toMoneyFormat(String money) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        return FinancialApplication.getConvert().amountMinUnitToMajor(money, currency.getCurrencyExponent(),true);
    }



}
