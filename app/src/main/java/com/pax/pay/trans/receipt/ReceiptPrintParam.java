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

import com.pax.pay.app.FinancialApplication;
import com.pax.up.bjb.R;

/**
 * 参数打印
 * 
 */
public class ReceiptPrintParam extends AReceiptPrint {
    private static ReceiptPrintParam receiptPrintParam;

    private ReceiptPrintParam() {

    }

    public synchronized static ReceiptPrintParam getInstance() {
        if (receiptPrintParam == null) {
            receiptPrintParam = new ReceiptPrintParam();
        }

        return receiptPrintParam;
    }

    public int print(String title, PrintListener listener) {
        this.listener = listener;

        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        }
        ReceiptGeneratorParam receiptGeneratorParam = new ReceiptGeneratorParam(title);
        printBitmap(receiptGeneratorParam.generate());
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

}
