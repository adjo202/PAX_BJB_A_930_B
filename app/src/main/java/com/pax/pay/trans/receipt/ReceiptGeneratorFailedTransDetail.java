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
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.OfflineStatus;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 脱机交易上送失败明细单生成器
 * 
 * @author Steven.W
 * 
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorFailedTransDetail extends ReceiptGeneratorBase implements IReceiptGenerator {
    private List<TransData> failedTransList;
    private Context context;

    public ReceiptGeneratorFailedTransDetail(List<TransData> failedTransList) {
        this.failedTransList = failedTransList;
    }

    @Override
    public Bitmap generate() {
        context = FinancialApplication.getAppContext();
        List<TransData> failedList = new ArrayList<>();
        List<TransData> rejectList = new ArrayList<>();

        for (TransData data : failedTransList) {

            if (data.getSendFailFlag() == OfflineStatus.OFFLINE_ERR_SEND) {
                failedList.add(data);
            }
            if (data.getSendFailFlag() == OfflineStatus.OFFLINE_ERR_RESP) {
                rejectList.add(data);
            }
        }
        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        page.setTypeFace(TYPE_FACE);

        genterateFailedMainInfo(page);
        generateFailedData(page, failedList);

        generateRejectMainInfo(page);
        generateRejectData(page, rejectList);

        page.addLine().addUnit("\n\n\n", FONT_NORMAL);

        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    /**
     * 生成是否交易明细单主信息
     * 
     * @return
     */
    private void genterateFailedMainInfo(IPage page) {
        SysParam sysParam = FinancialApplication.getSysParam();
        String temp = "";
        // 凭单抬头
        page.addLine()
            .addUnit(context.getString(R.string.offline_trans_send_failed), FONT_BIG, EAlign.CENTER)
            .addUnit(getImageFromAssetsFile("receipt_ums.png"), EAlign.CENTER);
        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign.CENTER);

        // 商户编号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_merchant_code), FONT_NORMAL)
            .addUnit(sysParam.get(SysParam.MERCH_ID), FONT_NORMAL, EAlign.RIGHT);

        // 终端编号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_terminal_code_space), FONT_NORMAL)
            .addUnit(sysParam.get(SysParam.TERMINAL_ID), FONT_NORMAL, EAlign.RIGHT);
        //操作员号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_oper_id_space), FONT_NORMAL)
            .addUnit(TransContext.getInstance().getOperID(), FONT_NORMAL, EAlign.RIGHT);

        // 批次号
        page.addLine()
            .addUnit(context.getString(R.string.receipt_batch_num_space), FONT_NORMAL)
            .addUnit(String.format("%06d", Long.parseLong(sysParam.get(SysParam.BATCH_NO))),
                    FONT_NORMAL, EAlign.RIGHT);

        // 日期时间
        String date = Device.getDate();
        String time = Device.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        temp = yearDate.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
        page.addLine()
            .addUnit(context.getString(R.string.receipt_date), FONT_NORMAL, 1.0f)
            .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, 1.5f);

        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign.CENTER);

        page.addLine().addUnit(context.getString(R.string.receipt_failed_trans_details), FONT_NORMAL, EAlign.LEFT);
        // 交易信息
        page.addLine().addUnit("VOUCHER", FONT_NORMAL, (float) 2)
                .addUnit("TYPE", FONT_NORMAL, EAlign.CENTER, (float) 1)
                .addUnit("AMOUNT", FONT_NORMAL, EAlign.RIGHT, (float) 3);
        page.addLine().addUnit("CARD NO", FONT_NORMAL, (float) 2);
    }

    /**
     * 生成失败交易明细
     */
    private void generateFailedData(IPage page, List<TransData> list) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String temp = "";
        for (TransData transData : list) {
            String transType = transData.getTransType();
            String type = "";
            if (transType.equals(ETransType.EC_SALE.toString())) {
                type = "E";
            } else if (transType.equals(ETransType.SALE.toString())
                    || transType.equals(ETransType.SETTLE_ADJUST_TIP.toString())) {
                type = "S";
            } else if (transType.equals(ETransType.OFFLINE_SETTLE.toString())
                    || transType.equals(ETransType.SETTLE_ADJUST.toString())) {
                type = "L";
            }

            // 流水号/交易类型/金额
            long amount = Long.parseLong(transData.getAmount());
            temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);

            long transNo;
            if (transType.equals(ETransType.SETTLE_ADJUST_TIP.toString())) {
                transNo = transData.getOrigTransNo();
            } else {
                transNo = transData.getTransNo();
            }

            page.addLine().addUnit(String.format("%06d", transNo), FONT_NORMAL, (float) 2)
                    .addUnit(type, FONT_NORMAL, EAlign.CENTER, (float) 1)
                    .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, (float) 3);

            // 卡号/授权号
            temp = transData.getPan();
            page.addLine().addUnit(temp, FONT_NORMAL, (float) 3);
        }

    }

    /**
     * 生成脱机交易上送被平台拒绝的凭单主信息
     * 
     * @return
     */
    private void generateRejectMainInfo(IPage page) {
        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign.CENTER);
        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string
                        .receipt_rejuct_trans_details), FONT_NORMAL,
                EAlign.LEFT);
        // 交易信息
        page.addLine().addUnit("VOUCHER", FONT_NORMAL, (float) 2)
                .addUnit("TYPE", FONT_NORMAL, EAlign.CENTER, (float) 1)
                .addUnit("AMOUNT", FONT_NORMAL, EAlign.RIGHT, (float) 3);
        page.addLine().addUnit("CARD NO", FONT_NORMAL, (float) 2);

    }

    /**
     * 生成脱机交易上送被平台拒绝的明细
     * 
     * @return
     */
    private void generateRejectData(IPage page, List<TransData> rejectTransDataList) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String temp = "";
        for (TransData transData : rejectTransDataList) {
            String transType = transData.getTransType();
            String type = "";
            if (transType.equals(ETransType.EC_SALE.toString())) {
                type = "E";
            } else if (transType.equals(ETransType.SALE.toString())) {
                type = "S";
            } else if (transType.equals(ETransType.OFFLINE_SETTLE.toString())
                    || transType.equals(ETransType.SETTLE_ADJUST.toString())
                    || transType.equals(ETransType.SETTLE_ADJUST_TIP.toString())) {
                type = "L";
            }

            // 流水号/交易类型/金额
            long amount = Long.parseLong(transData.getAmount());
            temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);
            page.addLine().addUnit(String.format("%06d", transData.getTransNo()), FONT_NORMAL, (float) 2)
                    .addUnit(type, FONT_NORMAL, EAlign.CENTER, (float) 1)
                    .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, (float) 3);

            // 卡号/授权号
            temp = transData.getPan();
            page.addLine().addUnit(temp, FONT_NORMAL, (float) 3);
        }
    }

}
