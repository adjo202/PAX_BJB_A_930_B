package com.pax.view.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;

import com.pax.device.Device;
import com.pax.pay.PaymentActivity;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.TransContext;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class DialogUtils {
    /**
     * 提示错误信息
     * 
     * @param msg
     * @param listener
     * @param timeout
     */
    public static void showErrMessage(final Context context, Handler handler, final String title, final String msg,
            final OnDismissListener listener, final int timeout) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    return;
                }
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, timeout);
                dialog.setTitleText(title);
                dialog.setContentText(msg);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnDismissListener(listener);
                Device.beepErr();
            }
        });
    }

    /**
     * 单行提示成功信息
     * 
     * @param title
     * @param listener
     * @param timeout
     */
    public static void showSuccMessage(final Context context, Handler handler, final String title,
            final OnDismissListener listener, final int timeout) {
        handler.post(new Runnable() { //Causes the Runnable r to be added to the message queue
            @Override
            public void run() {
                if (context == null) {
                    return;
                }
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.SUCCESS_TYPE, timeout);
                dialog.showContentText(false);
                dialog.setTitleText(TransContext.getInstance().getCurrentContext()
                        .getString(R.string.trans_succ_liff, title));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnDismissListener(listener);
                Device.beepOk();
            }
        });
    }



    /**
     * 退出当前应用
     */
    public static void showExitAppDialog(final Context context) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);

        dialog.show();
        dialog.setNormalText(context.getString(R.string.exit_app));
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);

        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
                Device.enableStatusBar(true);
                Device.enableHomeRecentKey(true);
                android.os.Process.killProcess(android.os.Process.myPid());*/


                Intent intent = new Intent(context, PaymentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
                Device.enableStatusBar(true);
                Device.enableHomeRecentKey(true);

            }
        });
    }

    private static void gotoLauncher(Context context){

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.swi.launcherbjb", "com.swi.launcherbjb.MyActivity"));
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 应用更新或者参数更新提示，点击确定则进行直接结算
     */
    public static void showUpdateDialog(final Context context, final Handler handler, final String prompt) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);

        dialog.show();
        dialog.setNormalText(prompt);
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                new SettleTrans(context, handler, null).execute();
            }
        });
    }

    public static void showUpdateDialog(final Context context, final String prompt, OnCustomClickListener listener) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(listener);
        dialog.show();
        dialog.setNormalText(prompt);
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }
}
