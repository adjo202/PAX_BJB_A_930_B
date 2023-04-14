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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import com.pax.device.Device;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.model.TransTotal;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;

/**
 * 结算凭单生成器
 * 
 * @author Steven.W
 * 
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorSettle extends  ReceiptGeneratorBase implements IReceiptGenerator {

    private String rmbResult;
    private String frnResult;
    private TransTotal total;

    public ReceiptGeneratorSettle(String rmbResult, String frnResult, TransTotal total) {
        this.rmbResult = rmbResult;
        this.frnResult = frnResult;
        this.total = total;
    }

    @Override
    public Bitmap generate() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        Context context = FinancialApplication.getAppContext();
        page.setTypeFace(TYPE_FACE);
        SysParam sysParam = FinancialApplication.getSysParam();
        String temp = "";
        long totalNum;
        long totalAmt;

        // 凭单抬头
        page.addLine().addUnit(context.getString(R.string.trans_settle_total), FONT_BIG, EAlign.CENTER);
        //page.addUnit(getImageFromAssetsFile("receipt_ums.png"), EAlign.CENTER);
        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign.CENTER);

        // 商户名称
        /**
        page.addLine()
            .addUnit(context.getString(R.string.receipt_merchant_name), FONT_NORMAL)
            .addUnit(sysParam.get(SysParam.MERCH_EN), FONT_NORMAL, EAlign.RIGHT);
        **/
        // 商户编号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_en_merchant_code), FONT_NORMAL)
            .addUnit(sysParam.get(SysParam.MERCH_ID), FONT_NORMAL, EAlign.RIGHT);

        // 终端编号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_en_terminal_code_space), FONT_NORMAL)
            .addUnit(sysParam.get(SysParam.TERMINAL_ID), FONT_NORMAL, EAlign.RIGHT);

        // 操作员
        page.addLine()
            .addUnit(context.getString(R.string.receipt_en_oper_id_space), FONT_NORMAL)
            .addUnit(TransContext.getInstance().getOperID(), FONT_NORMAL, EAlign.RIGHT, 1.5f);

        // 批次号
        page.addLine()
                .addUnit(context.getString(R.string.receipt_en_batch_num_colon), FONT_NORMAL)
                .addUnit(String.format("%06d", Long.parseLong(sysParam.get(SysParam.BATCH_NO))),
                        FONT_NORMAL, EAlign.RIGHT);

        // 日期时间
        String date = Device.getDate();
        String time = Device.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        temp = yearDate.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
        page.addLine().addUnit(context.getString(R.string.receipt_en_date), FONT_NORMAL, 1.0f)
                .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, 1.5f);

        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign.CENTER);

        // 类型/笔数/金额
        page.addLine().addUnit(context.getString(R.string.receipt_type), FONT_NORMAL, (float) 2.4)
                .addUnit(context.getString(R.string.receipt_count), FONT_NORMAL, EAlign.CENTER, (float) 0.8)
                .addUnit(context.getString(R.string.receipt_amount), FONT_NORMAL, EAlign.RIGHT, (float) 2);

        /*********************************************** 内卡 *******************************************************/
        // Sale total.
        totalNum = total.getSaleTotalNum() + total.getFrnSaleTotalNum();
        totalAmt = total.getSaleTotalAmt() + total.getFrnSaleTotalAmt();
        page.addLine()
                .addUnit(context.getString(R.string.sale_trans).toUpperCase(), FONT_NORMAL, (float) 2.4)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, (float) 0.8)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);

        // Tip total.
        /*
        totalNum = total.getSaleTotalTipNum() + total.getFrnSaleTotalTipNum();
        totalAmt = total.getSaleTotalTipAmt() + total.getFrnSaleTotalTipAmt();
        page.addLine()
                .addUnit(context.getString(R.string.receipt_amount_tip).toUpperCase(), FONT_NORMAL, 2.4f)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, 0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);
        */

        // Refund total.
        /*
        totalNum = total.getRefundTotalNum() + total.getFrnRefundTotalNum();
        totalAmt = total.getRefundTotalAmt() + total.getFrnRefundTotalAmt();
        page.addLine()
                .addUnit(context.getString(R.string.trans_refund).toUpperCase(), FONT_NORMAL, 2.4f)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, 0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(
                        String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);
        */

        // Auth complete and Auth complete advice total.
        totalNum = total.getAuthCmpTotalNum() + total.getFrnAuthCmpTotalNum();
        totalAmt = total.getAuthCmpTotalAmt() + total.getFrnAuthCmpTotalAmt();
        page.addLine()
                .addUnit(context.getString(R.string.auth_cmp).toUpperCase(), FONT_NORMAL, 2.4f)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, 0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);

        // Offline total.
        /*
        totalNum = total.getOfflineTotalNum() + total.getFrnOfflineTotalNum();
        totalAmt = total.getOfflineTotalAmt() + total.getFrnOfflineTotalAmt();
        page.addLine()
                .addUnit(context.getString(R.string.offline_trans).toUpperCase(), FONT_NORMAL, 2.4f)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, 0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);
        */

        // Installment total.
        totalNum = total.getInstallTotalNum() + total.getFrnInstallTotalNum();
        totalAmt = total.getInstallTotalAmt() + total.getFrnInstallTotalAmt();
        page.addLine()
                .addUnit(context.getString(R.string.install).toUpperCase(), FONT_NORMAL, 2.4f)
                .addUnit(totalNum + "", FONT_NORMAL, EAlign.CENTER, 0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(totalAmt),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);


        //print COUPON
        page.addLine().addUnit(context.getString(R.string.receipt_trans_type_coupon),FONT_NORMAL, 2.4f)
                .addUnit(total.getCouponSaleTotalNum() + "", FONT_NORMAL, EAlign.CENTER,0.8f)
                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(total.getCouponSaleTotalAmt()),
                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, 2f);



        page.addLine().addUnit("\n\n\n\n\n\n", FONT_NORMAL);
        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

}
