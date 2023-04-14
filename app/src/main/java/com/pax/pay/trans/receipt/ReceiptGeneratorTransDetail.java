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
import android.graphics.Typeface;

import com.pax.abl.utils.PanUtils;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 交易明细凭单生成器
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorTransDetail extends ReceiptGeneratorBase implements IReceiptGenerator {
    private boolean isLast = false;
    private boolean isMainInfoAndTransDetail = false;
    private String title = null;
    private List<TransData> transDatasList;

    /**
     * 生成交易明细时，用此构造方法
     */
    public ReceiptGeneratorTransDetail(List<TransData> transDatasList, boolean isLast, boolean
            isMainInfoAndTransDetail, String title) {
        this.transDatasList = transDatasList;
        this.isLast = isLast;
        this.isMainInfoAndTransDetail = isMainInfoAndTransDetail;//生成明细单主信息
        this.title = title;
    }

    public ReceiptGeneratorTransDetail(List<TransData> transDatasList, boolean isLast) {
        this.transDatasList = transDatasList;
        this.isLast = isLast;
    }

    public ReceiptGeneratorTransDetail() {
        //
    }

    @Override
    public Bitmap generate() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        Context context = FinancialApplication.getAppContext();
        page.setTypeFace(TYPE_FACE);
        SysParam sysParam = FinancialApplication.getSysParam();
        String temp = "";
        String temp2 = "";

        if (isMainInfoAndTransDetail) {
            // 凭单抬头
            page.addLine()
                    .addUnit(getImageFromAssetsFile("bjb1.jpg"), EAlign.CENTER);
            page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign
                    .CENTER);

            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("DETAIL REPORT")
                            .setAlign(EAlign.CENTER)
                            .setFontSize(FONT_NORMAL)
                            .setTextStyle( Typeface.BOLD)
                            .setWeight(3.0f));

            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));


            // 商户编号
            page.addLine()
                    .addUnit(context.getString(R.string.receipt_merchant_code), FONT_NORMAL)
                    .addUnit(sysParam.get(SysParam.MERCH_ID), FONT_NORMAL, EAlign.RIGHT);

            // 终端编号
            page.addLine()
                    .addUnit(context.getString(R.string.receipt_terminal_code_space), FONT_NORMAL)
                    .addUnit(sysParam.get(SysParam.TERMINAL_ID), FONT_NORMAL, EAlign.RIGHT);

            //Operator ID
            page.addLine()
                    .addUnit(context.getString(R.string.receipt_oper_id_space), FONT_NORMAL)
                    .addUnit(TransContext.getInstance().getOperID(), FONT_NORMAL, EAlign.RIGHT);

            // 批次号
            page.addLine()
                    .addUnit(context.getString(R.string.receipt_batch_num_space), FONT_NORMAL)
                    .addUnit(String.format("%06d", Long.parseLong(FinancialApplication.getSysParam().get
                            (SysParam.BATCH_NO))), FONT_NORMAL, EAlign.RIGHT);

            // 日期时间
            String date = Device.getDate();
            String time = Device.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String yearDate = sdf.format(new java.util.Date());

            temp = yearDate.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring
                    (6) + " "


                    + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
            page.addLine()
                    .addUnit(context.getString(R.string.receipt_date), FONT_NORMAL, 1.0f)
                    .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, 1.5f);

            page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign
                    .CENTER);

            // 交易信息
            page.addLine().addUnit("VOUCHER", FONT_NORMAL, (float) 2)
                    .addUnit("TYPE", FONT_NORMAL, EAlign.CENTER, (float) 1)
                    .addUnit("AMOUNT", FONT_NORMAL, EAlign.RIGHT, (float) 3);
            page.addLine().addUnit("CARD NO", FONT_NORMAL, (float) 2).addUnit("AUTH NO",
                    FONT_NORMAL,
                    EAlign.RIGHT);
        }

        for (TransData transData : transDatasList) {
            String transType = transData.getTransType();
            // 交易类型对应的标志转换
            String type = "";
            if (    transType.equals(ETransType.SALE.toString()) ||
                    transType.equals(ETransType.COUPON_SALE.toString()) ||
                    transType.equals(ETransType.SETTLE_ADJUST_TIP.toString()) ||
                    transType.equals(ETransType.QR_SALE.toString())) {
                type = "S";
            } else if (transType.equals(ETransType.AUTHCM.toString())) {
                type = "P";
            } else if (transType.equals(ETransType.AUTH_SETTLEMENT.toString())) {
                type = "C";
            } else if (transType.equals(ETransType.REFUND.toString())
                    || transType.equals(ETransType.QR_REFUND.toString())) {
                type = "R";
            } else if (transType.equals(ETransType.EC_SALE.toString())) {
                type = "E";
            } else if (transType.equals(ETransType.OFFLINE_SETTLE.toString())
                    || transType.equals(ETransType.SETTLE_ADJUST.toString())) {
                type = "L";
            } else if (transType.equals(ETransType.EC_CASH_LOAD.toString())
                    || transType.equals(ETransType.EC_LOAD.toString())
                    || transType.equals(ETransType.EC_TRANSFER_LOAD.toString())) {
                type = "Q";
            } else {
                type = "N";
            }

            //sandy
            long amount = 0;
            if(transType.equals(ETransType.COUPON_SALE.toString()))
                 amount = Long.parseLong(transData.getActualPayAmount());
            else {
                String amountTxt;
                /*if (transData.getTransTypeEnum().equals(ETransType.SETOR_TUNAI) || transData.getTransTypeEnum().equals(ETransType.TARIK_TUNAI)
                    || transData.getTransTypeEnum().equals(ETransType.INQ_PULSA_DATA)  ){
                    amountTxt = transData.getAmount().substring(0, transData.getAmount().length() - 2);
                }else {
                    amountTxt = transData.getAmount();
                }*/
                if (transType.equals(ETransType.INQ_PULSA_DATA.toString())) {
                    amountTxt = transData.getSellPrice();
                } else {
                    amountTxt = transData.getAmount();
                }
                amount = Long.parseLong(amountTxt);
            }


            temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);
            if (transType.equals(ETransType.SETTLE_ADJUST_TIP.toString())) {
                temp2 = String.format("%06d", transData.getOrigTransNo());
            } else {
                temp2 = String.format("%06d", transData.getTransNo());
            }
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
            page.addLine().addUnit(temp2, FONT_NORMAL, (float) 2).addUnit(type, FONT_NORMAL,
                    EAlign.CENTER, (float) 1)
                    .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, (float) 3);

            // 卡号/授权号

            // 卡号
            if (transType.equals(ETransType.EC_SALE.toString())) {
                temp = transData.getPan();
            } else if (transType.equals(ETransType.QR_SALE.toString())
                    || transType.equals(ETransType.QR_VOID.toString())
                    || transType.equals(ETransType.QR_REFUND.toString())) {
                temp = transData.getC2b();
            } else {
                temp = PanUtils.maskedCardNo(ETransType.valueOf(transType), transData.getPan());
                if (!transData.getIsOnlineTrans()) {
                    temp = transData.getPan();
                }
            }

            if (transType.equals(ETransType.SETTLE_ADJUST_TIP.toString())) {
                temp2 = transData.getOrigAuthCode() == null ? "" : transData.getOrigAuthCode();
            } else {
                temp2 = transData.getAuthCode() == null ? "" : transData.getAuthCode();
            }
            page.addLine().addUnit(temp, FONT_NORMAL, (float) 3).addUnit(temp2, FONT_NORMAL,
                    EAlign.RIGHT);
        }

        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_SMALL,
                        EAlign.RIGHT,
                        (float) 1.4);

        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);

        if (isLast) {
            //最后一次走纸
            page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);

        }

        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    private String getTerminalandAppVersion() {

        Map<ETermInfoKey, String> map = FinancialApplication.getDal().getSys().getTermInfo();

        return map.get(ETermInfoKey.MODEL) + " " + FinancialApplication.version;
    }
}
