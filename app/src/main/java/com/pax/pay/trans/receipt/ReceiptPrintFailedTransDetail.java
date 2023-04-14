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
import com.pax.pay.trans.model.TransData;
import com.pax.up.bjb.R;

import java.util.List;

/**
 * 失败交易明细凭单打印
 * 
 * @author Steven.W
 * 
 */
public class ReceiptPrintFailedTransDetail extends AReceiptPrint {
    private static ReceiptPrintFailedTransDetail receiptPrintFailedTransDetail;

    private ReceiptPrintFailedTransDetail() {

    }

    public synchronized static ReceiptPrintFailedTransDetail getInstance() {
        if (receiptPrintFailedTransDetail == null) {
            receiptPrintFailedTransDetail = new ReceiptPrintFailedTransDetail();
        }

        return receiptPrintFailedTransDetail;
    }

    public int print(List<TransData> failedTransList, PrintListener listener) {
        this.listener = listener;

        if (listener != null)
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        ReceiptGeneratorFailedTransDetail receiptGeneratorFailedTransDetail = new ReceiptGeneratorFailedTransDetail(
                failedTransList);
        int ret = printBitmap(receiptGeneratorFailedTransDetail.generate());
        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
