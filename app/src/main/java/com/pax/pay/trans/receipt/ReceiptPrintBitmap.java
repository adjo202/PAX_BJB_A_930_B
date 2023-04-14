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
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.pax.pay.app.FinancialApplication;
import com.pax.up.bjb.R;

/**
 * 图片打印
 */
public class ReceiptPrintBitmap extends AReceiptPrint {
    private static ReceiptPrintBitmap receiptPrintBitmap;

    private ReceiptPrintBitmap() {

    }

    public synchronized static ReceiptPrintBitmap getInstance() {
        if (receiptPrintBitmap == null) {
            receiptPrintBitmap = new ReceiptPrintBitmap();
        }

        return receiptPrintBitmap;
    }

    public int print(String bitmapStr, PrintListener listener) {
        this.listener = listener;

        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.wait_process));
        }

        // 将json传入的String转换成Bitmap
        byte[] bitmapArray;
        bitmapArray = Base64.decode(bitmapStr, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);

        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        }

        printBitmap(bitmap);
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

}
