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
import com.pax.pay.trans.model.TransTotal;
import com.pax.up.bjb.R;

/**
 * 总计单打印
 * 
 * @author Steven.W
 * 
 */
public class ReceiptPrintTotal extends AReceiptPrint {
    private static ReceiptPrintTotal receiptPrintTotal;
    private boolean settle = false;

    private ReceiptPrintTotal() {

    }

    public synchronized static ReceiptPrintTotal getInstance() {
        if (receiptPrintTotal == null) {
            receiptPrintTotal = new ReceiptPrintTotal();
        }

        return receiptPrintTotal;
    }

    public int print(String title, TransTotal transTotal, PrintListener listener, boolean settle) {
        this.listener = listener;
        this.settle = settle;

        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        }
        ReceiptGeneratorTotal receiptGeneratorTotal = new ReceiptGeneratorTotal(title, transTotal, settle);
        printBitmap(receiptGeneratorTotal.generate());
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

    public int printLastSettle(String title, TransTotal transTotal, PrintListener listener, boolean settle, boolean dup) {
        this.listener = listener;
        this.settle = settle;

        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        }
        ReceiptGeneratorTotal receiptGeneratorTotal = new ReceiptGeneratorTotal(title, transTotal, settle, dup);
        printBitmap(receiptGeneratorTotal.generate());
        if (listener != null) {
            listener.onEnd();
        }
        return 0;
    }

}
