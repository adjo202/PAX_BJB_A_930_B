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

import java.util.ArrayList;
import java.util.List;

/**
 * 交易明细打印
 *
 * @author Steven.W
 */
public class ReceiptPrintTransDetail extends AReceiptPrint {
    private static ReceiptPrintTransDetail receiptPrintTransDetail;

    private ReceiptPrintTransDetail() {

    }

    public synchronized static ReceiptPrintTransDetail getInstance() {
        if (receiptPrintTransDetail == null) {
            receiptPrintTransDetail = new ReceiptPrintTransDetail();
        }

        return receiptPrintTransDetail;
    }

    public int print(String title, List<TransData> list, PrintListener listener) {
        this.listener = listener;
        int count = 0;

        if (listener != null)
            listener.onShowMessage(null, FinancialApplication.getAppContext().getString(R.string.print_wait));
        // 打印明细主信息
        ReceiptGeneratorTransDetail receiptGeneratorTransDetail =new ReceiptGeneratorTransDetail();;
        int ret = 0;
        boolean isFirst = true;

        List<TransData> details = new ArrayList<>();
        for (TransData data : list) {
            details.add(data);
            count++;
            if (count == list.size() || count % 20 == 0) {
//                if (count != list.size() && count % 20 == 0) {
                    receiptGeneratorTransDetail = new ReceiptGeneratorTransDetail(details,
                            false, isFirst, title);
                    isFirst = false;

//                } else {

//                    receiptGeneratorTransDetail = new ReceiptGeneratorTransDetail(details, true);

//                }
                ret = printBitmap(receiptGeneratorTransDetail.generate());

                if (ret != 0) {
                    break;
                }
                details = new ArrayList<>();
            }
        }

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }
}
