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
 * 结算凭单打印
 * 
 * @author Steven.W
 * 
 */
public class ReceiptPrintSettle extends AReceiptPrint {
    private static ReceiptPrintSettle receiptPrintSettle;

    private ReceiptPrintSettle() {

    }

    public synchronized static ReceiptPrintSettle getInstance() {
        if (receiptPrintSettle == null) {
            receiptPrintSettle = new ReceiptPrintSettle();
        }

        return receiptPrintSettle;
    }

    public int print(String rmbResult, String frnResult, TransTotal transTotal, PrintListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        }
        ReceiptGeneratorSettle receiptGeneratorSettle = new ReceiptGeneratorSettle(rmbResult, frnResult, transTotal);
        int ret = printBitmap(receiptGeneratorSettle.generate());

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
