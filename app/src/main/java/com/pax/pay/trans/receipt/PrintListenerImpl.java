/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 11:24
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.receipt;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.Handler;

import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class PrintListenerImpl implements PrintListener {

    private Handler handler;
    private Context context;

    public PrintListenerImpl(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    private CustomAlertDialog showMsgdialog;
    private CustomAlertDialog confirmDialog;

    @Override
    public void onShowMessage(final String title, final String message) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (showMsgdialog == null) {

                    showMsgdialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);
                    showMsgdialog.show();
                    showMsgdialog.setCancelable(false);
                    showMsgdialog.setTitleText(title);
                    showMsgdialog.setContentText(message);

                } else {
                    showMsgdialog.setTitleText(title);
                    showMsgdialog.setContentText(message);
                }
            }
        });
    }

    private ConditionVariable cv;
    private int result = -1;

    @Override
    public int onConfirm(final String title, final String message) {
        cv = new ConditionVariable();
        result = -1;
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (confirmDialog != null) {
                    confirmDialog.dismiss();
                }
                confirmDialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
                confirmDialog.show();
                confirmDialog.setTitleText(title);
                confirmDialog.setContentText(message);
                confirmDialog.setCancelable(false);
                confirmDialog.setCanceledOnTouchOutside(false);
                confirmDialog.showCancelButton(true);
                confirmDialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        result = 1;
                        if (cv != null) {
                            cv.open();
                        }
                    }
                });
                confirmDialog.showConfirmButton(true);
                confirmDialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        result = 0;
                        if (cv != null) {
                            cv.open();
                        }
                    }
                });
                confirmDialog.show();
            }
        });
        cv.block();
        return result;
    }

    @Override
    public void onEnd() {
        if (showMsgdialog != null) {
            showMsgdialog.dismiss();
        }
        if (confirmDialog != null) {
            confirmDialog.dismiss();
        }
    }

}
