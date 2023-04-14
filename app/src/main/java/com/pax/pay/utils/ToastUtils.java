package com.pax.pay.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.pay.app.FinancialApplication;
import com.pax.up.bjb.R;

public class ToastUtils {

    /** 之前显示的内容 */
    private static String oldMsg;
    /** Toast对象 */
    private static Toast toast = null;
    /** 第一次时间 */
    private static long oneTime = 0;
    /** 第二次时间 */
    private static long twoTime = 0;

    public static void showMessage(@StringRes int strId) {
        showMessage( FinancialApplication.getAppContext(), FinancialApplication.getAppContext().getString(strId));
    }

    public static void showMessage(String message) {
        showMessage(FinancialApplication.getAppContext(), message);
    }

    public static void showMessage(Context context, String message) {
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.toast_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.message);
        if (toast == null) {
            textView.setText(message);
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);// 设置Toast屏幕居中显示
            toast.setView(view);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();

            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                textView.setText(message);
                toast.setView(view);
                toast.show();
            }
        }

        oneTime = twoTime;
    }
}
