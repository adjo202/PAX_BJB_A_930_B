/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:24
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.receipt;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.pax.dal.IPrinter;
import com.pax.dal.exceptions.PrinterDevException;
import com.pax.pay.app.FinancialApplication;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

abstract class AReceiptPrint {
    public static final String TAG = "AReceiptPrint";

    protected PrintListener listener;

    /**
     * 返回 -1 停止打印
     * 
     * @param bitmap
     * @return
     */
    protected int printBitmap(Bitmap bitmap) {
        IPrinter printer = FinancialApplication.getDal().getPrinter();
        try {
            printer.init();

            int gray = 100;
            String temp = FinancialApplication.getSysParam().get(SysParam.PRINT_GRAY);
            if (temp != null && temp.length() > 0) {
                gray = Integer.parseInt(temp);
            }
            printer.setGray(gray);
            printer.printBitmap(bitmap);

            return start(printer);

        } catch (PrinterDevException e) {
            Log.e(TAG, "", e);
        }

        return -1;
    }

    protected int printStr(String str) {
        IPrinter printer = FinancialApplication.getDal().getPrinter();
        try {
            printer.init();
            int gray = 100;
            String temp = FinancialApplication.getSysParam().get(SysParam.PRINT_GRAY);
            if (temp != null && temp.length() > 0) {
                gray = Integer.parseInt(temp);
            }
            printer.setGray(gray);
            printer.printStr(str, null);
            return start(printer);

        } catch (PrinterDevException e) {

            Log.e(TAG, "", e);
        }

        return -1;
    }

    private int start(IPrinter printer) {
        int result = 0;
        try {
            while (true) {
                result = 0;
                if (listener != null)
                    listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
                int ret = printer.start();
                // 打印机忙， 请等待
                if (ret == 1) {
                    SystemClock.sleep(100);
                    continue;
                } else if (ret == 2) {
                    if (listener != null) {
                        result = listener.onConfirm(null, FinancialApplication.getAppContext().getString(R.string.print_paper));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret == 8) {
                    if (listener != null) {
                        result = listener.onConfirm(null, FinancialApplication.getAppContext().getString(R.string.print_hot));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret == 9) {
                    if (listener != null) {
                        result = listener.onConfirm(null, FinancialApplication.getAppContext().getString(R.string.print_voltage));
                        if (result == PrintListener.CONTINUE) {
                            continue;
                        }
                    }
                    return -1;
                } else if (ret != 0) {
                    return -1;
                }

                return 0;
            }
        } catch (PrinterDevException e) {
            Log.e(TAG, "", e);
            return 0;
        }
    }

}
