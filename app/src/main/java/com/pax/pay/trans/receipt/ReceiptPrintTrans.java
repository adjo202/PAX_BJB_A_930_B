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

import android.util.Log;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

/**
 * 打印交易凭单打印
 * 
 * @author Steven.W
 * 
 */
public class ReceiptPrintTrans extends AReceiptPrint {
    private static ReceiptPrintTrans receiptPrinterTrans;

    private ReceiptPrintTrans() {

    }

    public synchronized static ReceiptPrintTrans getInstance() {
        if (receiptPrinterTrans == null) {
            receiptPrinterTrans = new ReceiptPrintTrans();
        }

        return receiptPrinterTrans;
    }

    public int print(TransData transData, boolean isRePrint, PrintListener listener) {
        this.listener = listener;
        int ret = 0;
        int receiptNum = getVoucherNum();
        if (listener != null)
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));

        for (int i = 0; i < receiptNum; i++) {
            IReceiptGenerator receiptGeneratorTrans = new ReceiptGeneratorTrans(transData, i, receiptNum, isRePrint);
            int result;
            Log.i("abdul", "cek receiptnum = " + receiptNum + " now = " + i);
            if (i == 1 && listener != null && !isRePrint) {
                Log.i("abdul", "masuk print customer copy");
                result = listener.onConfirm("Print Customer Copy ?", null);
                if (result == 1) {
                    return ret;
                }
            }else if (i == 2 && listener != null && !isRePrint) {
                Log.i("abdul", "masuk print bank copy");
                result = listener.onConfirm("Print Bank Copy ?", null);
                if (result == 1) {
                    return ret;
                }
            }

            ret = printBitmap(receiptGeneratorTrans.generate());
            if (ret == -1) {
                break;
            }
            if (isRePrint)
                break;
        }
        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }

    private int getVoucherNum() {

        int receiptNum = 0;
        String temp = FinancialApplication.getSysParam().get(SysParam.PRINT_VOUCHER_NUM);
        if (temp != null)
            receiptNum = Integer.parseInt(temp);
        if (receiptNum < 1 || receiptNum > 3) // 打印联数只能为1-3
            receiptNum = 2;

        return receiptNum;
    }

}
