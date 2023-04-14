package com.pax.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 通用参数存储
 * 
 * @author Steven.W
 * 
 */
public class GeneralParam {
    // PIN密钥
    public static final String TPK = "TPK";
    // MAC密钥
    public static final String TAK = "TAK";
    // DES密钥
    public static final String TDK = "TDK";

    private static final String CONFIG_FIEL_NAME = "generalParam";
    private Context context;
    private static GeneralParam generalParam;

    private GeneralParam(Context context) {
        this.context = context;
    }

    public static synchronized GeneralParam getInstance(Context context) {
        if (generalParam == null) {
            generalParam = new GeneralParam(context);
        }

        return generalParam;
    }

    public String get(String key) {
        String value;
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_FIEL_NAME, Context.MODE_PRIVATE);
        value = sharedPreferences.getString(key, null);
        return value;
    }

    public void set(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_FIEL_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

}
