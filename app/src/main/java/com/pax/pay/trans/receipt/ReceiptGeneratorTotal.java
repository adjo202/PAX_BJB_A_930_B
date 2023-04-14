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
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.pax.dal.entity.ETermInfoKey;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransTotal;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 交易总计凭单生成器
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorTotal extends ReceiptGeneratorBase implements IReceiptGenerator {
    public static final String TAG = "ReceiptGeneratorTotal";

    boolean settle = false;
    boolean duplicate = false;
    String title;
    TransTotal transTotal;

    public ReceiptGeneratorTotal(String title, TransTotal transTotal, boolean settle) {
        this.title = title;
        this.transTotal = transTotal;
        this.settle = settle;
    }

    public ReceiptGeneratorTotal(String title, TransTotal transTotal, boolean settle, boolean dup) {
        this.title = title;
        this.transTotal = transTotal;
        this.settle = settle;
        this.duplicate = dup;
    }

    @Override
    public Bitmap generate() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        Context context = FinancialApplication.getAppContext();
        page.setTypeFace(TYPE_FACE);
        String temp = "";
        // 凭单抬头
        Bitmap icon = getImageFromAssetsFile("bjb1.jpg");
        page.addLine().addUnit(icon, EAlign.CENTER);
        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign
                .CENTER);
        String title = "";
        if (settle) {
            title = "SETTLEMENT";
        } else {
            title = "SUMMARY REPORT";
        }
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(title)
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));

        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        // 商户编号
        page.addLine()
                .addUnit(context.getString(R.string.receipt_merchant_code), FONT_NORMAL)
                .addUnit(transTotal.getMerchantID(), FONT_NORMAL, EAlign.RIGHT);

        // 终端编号
        page.addLine()
                .addUnit(context.getString(R.string.receipt_terminal_code_space), FONT_NORMAL)
                .addUnit(transTotal.getTerminalID(), FONT_NORMAL, EAlign.RIGHT);

        //Operator ID
        page.addLine()
                .addUnit(context.getString(R.string.receipt_oper_id_space), FONT_NORMAL)
                .addUnit(transTotal.getOperatorID(), FONT_NORMAL, EAlign.RIGHT);

        // 批次号
        page.addLine()
                .addUnit(context.getString(R.string.receipt_batch_num_space), FONT_NORMAL)
                .addUnit(String.format("%06d", Long.parseLong(transTotal.getBatchNo())),
                        FONT_NORMAL,
                        EAlign.RIGHT);

        // 日期时间
        String date = transTotal.getDate();
        String time = transTotal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        temp = yearDate.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
        page.addLine()
                .addUnit(context.getString(R.string.receipt_date), FONT_NORMAL, 1.0f)
                .addUnit(temp, FONT_NORMAL, EAlign.RIGHT, 1.5f);

        page.addLine().addUnit(context.getString(R.string.dividing_line), FONT_NORMAL, EAlign
                .CENTER);

//        Mini statement
//        Total : 2
//        Amount :Rp. 0,-
//        Fee : Rp. 1000,-
//        setor, Tarik, Transfer, Pbb, Mpn, Pulsa, Infosaldo, Ministement
        //denny add
        long setorNum, setorAmt, setorFee;
        //setor tunai
        setorNum = transTotal.getTransTotalAmt()[0][0];
        setorAmt = transTotal.getTransTotalAmt()[0][1];
        setorFee = transTotal.getTransTotalAmt()[0][2];
        page.addLine().addUnit("Setor Tunai", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+setorNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(setorAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (setorFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //tarik tunai
        long tarikNum, tarikAmt, tarikFee;
        tarikNum = transTotal.getTransTotalAmt()[1][0];
        tarikAmt = transTotal.getTransTotalAmt()[1][1];
        tarikFee = transTotal.getTransTotalAmt()[1][2];
        page.addLine().addUnit("Tarik Tunai", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+tarikNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(tarikAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (tarikFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //TRANSFER
        long transferNum, transferAmt, transferFee;
        transferNum = transTotal.getTransTotalAmt()[2][0];
        transferAmt = transTotal.getTransTotalAmt()[2][1];
        transferFee = transTotal.getTransTotalAmt()[2][2];
        page.addLine().addUnit("Transfer Antar Bank", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+transferNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (transferAmt), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (transferFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //TRANSFER SESAMA
        long trfSesamaNum, trfSesamaAmt, trfSesamaFee;
        trfSesamaNum = transTotal.getTransTotalAmt()[8][0];
        trfSesamaAmt = transTotal.getTransTotalAmt()[8][1];
        trfSesamaFee = transTotal.getTransTotalAmt()[8][2];
        page.addLine().addUnit("Transfer Antar Rekening", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+trfSesamaNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (trfSesamaAmt), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (trfSesamaFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //Pbb
        long pbbNum, pbbAmt, pbbFee;
        pbbNum = transTotal.getTransTotalAmt()[3][0];
        pbbAmt = transTotal.getTransTotalAmt()[3][1];
        pbbFee = transTotal.getTransTotalAmt()[3][2];
        page.addLine().addUnit("PBB-P2", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+pbbNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (pbbAmt), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (pbbFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //Mpn
        long mpnNum, mpnAmt, mpnFee;
        mpnNum = transTotal.getTransTotalAmt()[4][0];
        mpnAmt = transTotal.getTransTotalAmt()[4][1];
        mpnFee = transTotal.getTransTotalAmt()[4][2];
        page.addLine().addUnit("MPN-G2", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+mpnNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (mpnAmt), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (mpnFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //Pulsa
        long pulsaNum, pulsaAmt, pulsaFee;
        pulsaNum = transTotal.getTransTotalAmt()[5][0];
        pulsaAmt = transTotal.getTransTotalAmt()[5][1];
        pulsaFee = transTotal.getTransTotalAmt()[5][2];
        //page.addLine().addUnit("Pulsa/Data", FONT_NORMAL);
        page.addLine().addUnit("Prabayar", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+pulsaNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(pulsaAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (pulsaFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //Infosaldo
        long infosaldoNum, infosaldoAmt, infosaldoFee;
        infosaldoNum = transTotal.getTransTotalAmt()[6][0];
        infosaldoFee = transTotal.getTransTotalAmt()[6][2];
        page.addLine().addUnit("Info Saldo", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+infosaldoNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor("0", currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (infosaldoFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        // Ministement
        long ministatementNum, ministatementkAmt, ministatementFee;
        ministatementNum = transTotal.getTransTotalAmt()[7][0];
        ministatementFee = transTotal.getTransTotalAmt()[7][2];
        page.addLine().addUnit("Ministatement", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ministatementNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor("0", currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (ministatementFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //PDAM
        long pdamNum, pdamkAmt, pdamFee;
        pdamNum = transTotal.getTransTotalAmt()[9][0];
        pdamkAmt = transTotal.getTransTotalAmt()[9][1];
        pdamFee = transTotal.getTransTotalAmt()[9][2];
        page.addLine().addUnit("PDAM", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+pdamNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(pdamkAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (pdamFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //pascabayar
        long pascabayarNum, pascabayarkAmt, pascabayarFee;
        pascabayarNum = transTotal.getTransTotalAmt()[10][0];
        pascabayarkAmt = transTotal.getTransTotalAmt()[10][1];
        pascabayarFee = transTotal.getTransTotalAmt()[10][2];
        page.addLine().addUnit("Pascabayar", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+pascabayarNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(pascabayarkAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (pascabayarFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        //E-Samsat
        long samsatNum, samsatkAmt, samsatFee;
        samsatNum = transTotal.getTransTotalAmt()[11][0];
        samsatkAmt = transTotal.getTransTotalAmt()[11][1];
        samsatFee = transTotal.getTransTotalAmt()[11][2];
        page.addLine().addUnit("E-Samsat", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+samsatNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(samsatkAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (samsatFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));


        //BPJS TK Registration
        long BPJSTkRegistrationNum, BPJSTkRegistrationAmt, BPJSTkRegistrationFee;
        BPJSTkRegistrationNum = transTotal.getTransTotalAmt()[16][0];
        BPJSTkRegistrationFee = transTotal.getTransTotalAmt()[16][1];
        BPJSTkRegistrationAmt = transTotal.getTransTotalAmt()[16][2];
        page.addLine().addUnit("BPJS Ketenagakerjaan Pendaftaran", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+BPJSTkRegistrationNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(BPJSTkRegistrationAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " +
                                FinancialApplication.getConvert().amountMinUnitToMajor(
                                        String.valueOf(BPJSTkRegistrationFee), currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));



        //BPJS TK Pembayaran
        long BPJSTkPembayaranNum, BPJSTkPembayaranAmt, BPJSTkPembayaranFee;
        BPJSTkPembayaranNum = transTotal.getTransTotalAmt()[17][0];
        BPJSTkPembayaranFee = transTotal.getTransTotalAmt()[17][1];
        BPJSTkPembayaranAmt = transTotal.getTransTotalAmt()[17][2];
        page.addLine().addUnit("BPJS Ketenagakerjaan Pembayaran", FONT_NORMAL);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Count")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+BPJSTkPembayaranNum)
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Amount")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+ getAmount(String.valueOf(BPJSTkPembayaranAmt)))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Fee")
                        .setWeight(3)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(": "+currency.getName() + " " +
                                FinancialApplication.getConvert().amountMinUnitToMajor(
                                        String.valueOf(BPJSTkPembayaranFee),
                                        currency.getCurrencyExponent(), true))
                        .setWeight(10)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText("--------------------------------").setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));


















        if (duplicate) {
            page.addLine()
                    .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                    .addUnit(context.getString(R.string.receipt_print_agian), FONT_SMALL,
                            EAlign.RIGHT,
                            (float) 1.4);
        } else {
            page.addLine()
                    .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                    .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_SMALL,
                            EAlign.RIGHT,
                            (float) 1.4);
        }


        // 类型/笔数/金额
//        page.addLine().addUnit(context.getString(R.string.receipt_type), FONT_BIG, (float) 2)
//                .addUnit(context.getString(R.string.receipt_count), FONT_BIG, EAlign.CENTER,
//                        (float) 1)
//                .addUnit(context.getString(R.string.receipt_amount), FONT_BIG, EAlign.RIGHT,
//                        (float) 3);
//
//        page.addLine().addUnit(context.getString(R.string.receipt_trans_type_sale), FONT_NORMAL,
//                (float) 2)
//                .addUnit(transTotal.getSaleTotalNum() + transTotal.getFrnSaleTotalNum() + "",
//                        FONT_NORMAL, EAlign.CENTER, (float) 1)
//                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
//                                (transTotal.getSaleTotalAmt()
//                                + transTotal.getFrnSaleTotalAmt()), currency.getCurrencyExponent(),
//                        true), FONT_NORMAL, EAlign.RIGHT, (float) 3);
//
//        page.addLine().addUnit(context.getString(R.string.receipt_trans_type_comp), FONT_NORMAL,
//                (float) 2)
//                .addUnit(transTotal.getPreAuthCmpTotalNum() + transTotal.getFrnPreAuthCmpTotalNum
//                        () + "", FONT_NORMAL, EAlign.CENTER, (float) 1)
//                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
//                                (transTotal.getPreAuthCmpTotalAmt()
//                                + transTotal.getFrnPreAuthCmpTotalAmt()), currency
//                                .getCurrencyExponent(),
//                        true), FONT_NORMAL, EAlign.RIGHT, (float) 3);
//
//        //print INSTALLMENT
//        page.addLine().addUnit(context.getString(R.string.receipt_trans_type_install),
//                FONT_NORMAL, (float) 2)
//                .addUnit(transTotal.getInstallTotalNum() + "", FONT_NORMAL, EAlign.CENTER,
//                        (float) 1)
//                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
//                                (transTotal.getInstallTotalAmt()),
//                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, (float)
//                        3);
//
//        //print COUPON
//        page.addLine().addUnit(context.getString(R.string.receipt_trans_type_coupon),
//                FONT_NORMAL, (float) 2)
//                .addUnit(transTotal.getCouponSaleTotalNum() + "", FONT_NORMAL, EAlign.CENTER,
//                        (float) 1)
//                .addUnit(FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
//                                (transTotal.getCouponSaleTotalAmt()),
//                        currency.getCurrencyExponent(), true), FONT_NORMAL, EAlign.RIGHT, (float)
//                        3);



        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);
        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    private String getTerminalandAppVersion() {

        Map<ETermInfoKey, String> map = FinancialApplication.getDal().getSys().getTermInfo();

        return map.get(ETermInfoKey.MODEL) + " " + FinancialApplication.version;
    }

    Currency currency = FinancialApplication.getSysParam().getCurrency();

    private String getAmount(String amt) {
        try {
            String amtSubs = amt;
            /*if (amt.equals("0")){

            }else {
                amtSubs = amt.substring(0, amt.length() - 2);
            }*/

            long amount = Long.parseLong(amtSubs);
            String temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);
            temp = currency.getName() + " " + temp;
            return temp;
        } catch (Exception e) {
            return "";
        }
    }
}
