package com.pax.pay.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;

import com.pax.dal.IScanner;
import com.pax.dal.IScanner.IScanListener;
import com.pax.dal.entity.EScannerType;
import com.pax.device.Device;
import com.pax.up.bjb.R;
import com.pax.view.dialog.DispExDeviceDialog;

public class ScanCodeUtils {
    private Context context;
    private Handler handler;
    private String qrCode;
    private IScanner scanner;

    private DispExDeviceDialog dialog;// 外置扫码dialog
    private boolean isScanRunning = false;// 扫码程序isStart

    public static final int SCAN_CODE_END = 0x01;

    private static ScanCodeUtils scanCodeUtils;

    public static synchronized ScanCodeUtils getInstance() {
        if (scanCodeUtils == null) {
            scanCodeUtils = new ScanCodeUtils();
        }
        return scanCodeUtils;
    }

    private ScanCodeUtils() {

    }

    /**
     * start scan code
     * @param context
     * @param handler
     */
    public void start(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
        this.scanner = Device.getScanner();
        this.qrCode = null;

        initView();
        scanCode();
    }

    /**
     * close scan code,handler send the instruction.
     * @param handler
     */
    public void close(Handler handler) {
        if (dialog != null) {
            dialog.dismiss();
        }

        scanner = Device.getScanner();
        scanner.close();

        if (!isScanRunning) {
            handler.sendEmptyMessage(SCAN_CODE_END);
        }
    }

    /**
     * get the result of scan(just once)
     * @return
     */
    public String getQrCode() {
        String code = qrCode;
        qrCode = null;
        return code;
    }

    // Update UI
    private void initView() {
        // 外置扫码枪时更新UI
        if (Device.getScannerType() == EScannerType.EXTERNAL) {
            dialog = new DispExDeviceDialog(context, R.drawable.ex_scan_code, context.getResources().getString(
                    R.string.prompt_ex_scan_code_device), 60);
            dialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK && isScanRunning) {
                        dialogInterface.dismiss();
                    }
                    return false;
                }
            });
            dialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface arg0) {
                    scanner.close();
                }
            });

            dialog.show();
        } else {
            // TODO
        }
    }

    // start scan
    private void scanCode() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                scanner.close();
                scanner.open();// 耗时操作
                isScanRunning = true;
                scanner.start(new IScanListener() {

                    @Override
                    public void onCancel() {
                        scanner.close();
                    }

                    @Override
                    public void onFinish() {
                        isScanRunning = false;
                        close(handler);
                    }

                    @Override
                    public void onRead(String content) {
                        qrCode = content;
                    }
                });
            }
        }).start();

    }

}
