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

import static com.pax.pay.utils.Controllers.PLN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.utils.PanUtils;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.Controllers;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 交易凭单生成器
 *
 * @author Steven.W
 */
@SuppressLint({"SimpleDateFormat", "DefaultLocale"})
class ReceiptGeneratorTrans extends ReceiptGeneratorBase implements IReceiptGenerator {
    private static final String TAG = "ReceiptGeneratorTrans";

    int receiptNo = 0;
    private TransData transData;
    private boolean isRePrint = false;
    private int receiptMax = 0;
    private Device device;
    int enterMode;
    ETransType transType;
    Currency currency;
    // Sandy :
    // since BJB have a problem with PLN
    //so we gonne modify this when the transaction has a PLN value
    boolean isPLN = false;


    boolean iscetakUlangTrans = false;

    private static String[][] bankNames = {
            {"0102", FinancialApplication.getAppContext().getString(R.string.icbc),
                    FinancialApplication.getAppContext().getString(R.string.icbc_cup)},
            {"0103", FinancialApplication.getAppContext().getString(R.string.abc),
                    FinancialApplication.getAppContext().getString(R.string.abc_cup)},
            {"0104", FinancialApplication.getAppContext().getString(R.string.boc),
                    FinancialApplication.getAppContext().getString(R.string.boc_cup)},
            {"0105", FinancialApplication.getAppContext().getString(R.string.cbc),
                    FinancialApplication.getAppContext().getString(R.string.cbc_cup)},
            {"0100", FinancialApplication.getAppContext().getString(R.string.psbc),
                    FinancialApplication.getAppContext().getString(R.string.psbc_cup)},
            {"0301", FinancialApplication.getAppContext().getString(R.string.bcm),
                    FinancialApplication.getAppContext().getString(R.string.bcm_cup)},
            {"0302", FinancialApplication.getAppContext().getString(R.string.ccb),
                    FinancialApplication.getAppContext().getString(R.string.ccb_cup)},
            {"0303", FinancialApplication.getAppContext().getString(R.string.ceb),
                    FinancialApplication.getAppContext().getString(R.string.ceb_cup)},
            {"0304", FinancialApplication.getAppContext().getString(R.string.hxb),
                    FinancialApplication.getAppContext().getString(R.string.hxb_cup)},
            {"0305", FinancialApplication.getAppContext().getString(R.string.cmbc),
                    FinancialApplication.getAppContext().getString(R.string.cmbc_cup)},
            {"0306", FinancialApplication.getAppContext().getString(R.string.gdb),
                    FinancialApplication.getAppContext().getString(R.string.gdb_cup)},
            {"0307", FinancialApplication.getAppContext().getString(R.string.sdb),
                    FinancialApplication.getAppContext().getString(R.string.sdb_cup)},
            {"0308", FinancialApplication.getAppContext().getString(R.string.cmb),
                    FinancialApplication.getAppContext().getString(R.string.cmb_cup)},
            {"0309", FinancialApplication.getAppContext().getString(R.string.cib),
                    FinancialApplication.getAppContext().getString(R.string.cib_cup)},
            {"0310", FinancialApplication.getAppContext().getString(R.string.spdb),
                    FinancialApplication.getAppContext().getString(R.string.spdb_cup)},
            {"4802", FinancialApplication.getAppContext().getString(R.string.ums),
                    FinancialApplication.getAppContext().getString(R.string.ums_cup)},};

    public ReceiptGeneratorTrans() {

    }

    /**
     * @param transData        ：transData
     * @param currentReceiptNo ：生成第几张凭单，从0开始
     * @param isReprint        ：是否是重打印
     */
    public ReceiptGeneratorTrans(TransData transData, int currentReceiptNo, int receiptMax,
                                 boolean isReprint) {
        this.transData = transData;
        this.receiptNo = currentReceiptNo;
        this.isRePrint = isReprint;
        this.receiptMax = receiptMax;
        this.enterMode = transData.getEnterMode();
        this.transType = ETransType.valueOf(transData.getTransType());
        this.currency = FinancialApplication.getSysParam().getCurrency();
    }

    @Override
    public Bitmap generate() {
        return generateEnBitmap();
    }

    private Bitmap generateEnBitmap() {
        boolean isPrintSign = false;
        if (receiptNo > receiptMax) {
            receiptNo = 0;
        }
        if (receiptNo == 0 || ((receiptMax == 3) && receiptNo == 1)) {
            isPrintSign = true;
        }

        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        Context context = FinancialApplication.getAppContext();
        page.setTypeFace(TYPE_FACE);

        SysParam sysParam = FinancialApplication.getSysParam();
        String temp = "";

        // print header all transaksi


        String prodId = transData.getProduct_code();
        if(prodId != null){
            ProductData p = Controllers.getProductById(prodId);
            String productOperator = p.getOperator();
            if(productOperator.toUpperCase().trim().contains("PLN") )
                isPLN = true;
        }

        String theLogo = "";
        if(isPLN)
            theLogo = "indopay.png";
        else
            theLogo = "bjb1.jpg";



        switch (transType) {
            case CETAK_ULANG:
                printHeaderCetakUlang(page,theLogo);
                break;
            default:
                printHeader(page,theLogo);

                /*
                String prodId = transData.getProduct_code();
                if(prodId != null){
                    ProductData p = Controllers.getProductById(prodId);
                    String productOperator = p.getOperator();
                    if(productOperator.toUpperCase().trim().contains("PLN") )
                        printHeader(page,"indopay.png");
                    else
                        printHeader(page,"bjb1.jpg");
                }else {
                        printHeader(page,"bjb1.jpg");
                }*/


                break;
        }

        // print body transaksi
        switch (transType) {
            case BALANCE_INQUIRY:
                printBodyInfoSaldo(page,getEnTransType(ETransType.BALANCE_INQUIRY));
                break;
            case BALANCE_INQUIRY_2:
                printBodyInfoSaldo(page,getEnTransType(ETransType.BALANCE_INQUIRY_2));
                break;
            case TARIK_TUNAI:
                printBodyTarikTunai(page,getEnTransType(ETransType.TARIK_TUNAI));
                break;
            case TARIK_TUNAI_2:
                printBodyTarikTunai(page,getEnTransType(ETransType.TARIK_TUNAI_2));
                break;
            case SETOR_TUNAI:
                printBodySetorTunai(page);
                break;
            case PEMBUKAAN_REK:
                printBukaRekening(page);
                break;
            case PEMBATAL_REK:
                printBatalRekening(page);
                break;
            case TRANSFER:
                printBodyTransfer(page);
                break;
            case TRANSFER_2:
                printBodyTransfer(page);
                break;
            case OVERBOOKING:
                printBodyOverbooking(page);
                break;
            case OVERBOOKING_2:
                printBodyOverbooking(page);
                break;
            case DIRJEN_PAJAK:
                //printbodyDJP(page);
                if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJP(page);
                    } else {
                        printbodyDJP(page);
                    }
                } else {
                    printbodyDJP(page);
                }
                break;
            case DIRJEN_BEA_CUKAI:
                //printbodyDJBC(page);
                if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJBC(page);
                    } else {
                        printbodyDJBC(page);
                    }
                } else {
                    printbodyDJBC(page);
                }
                break;
            case DIRJEN_ANGGARAN:
                //printbodyDJA(page);
                if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJA(page);
                    } else {
                        printbodyDJA(page);
                    }
                } else {
                    printbodyDJA(page);
                }
                break;
            case CETAK_ULANG:
                reprintMPN(page);
                break;
            case MINISTATEMENT:
                printBodyMiniStatement(page);
                break;
            case CHANGE_PIN:
                printBodyChangePin(page);
                break;
            case INQ_PULSA_DATA:
            case PURCHASE_PULSA_DATA:
            case OVERBOOKING_PULSA_DATA:
                printBodyPulsaData(page);
                break;
            case PBB_PAY:
                printBodyPBB(page);
                break;
            case E_SAMSAT:
                printbodyEsamsat(page);
                break;
            case REDEEM_POIN_DATA_PAY:
                String F110 = transData.getField110();
                printDynamicText(page,F110);
                break;

            case PDAM_INQUIRY:
            case PASCABAYAR_INQUIRY:
            case PDAM_OVERBOOKING:
            case PASCABAYAR_OVERBOOKING:
            case BPJS_TK_PEMBAYARAN: //sandy added
            case BPJS_TK_PENDAFTARAN: //sandy added
                printBodyPascabayar(page);

                /*if (!StringUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equalsIgnoreCase("y")) {
                        printBodyPascabayarTimeouut(page);
                    }
                } else {
                    printBodyPascabayar(page);
                }*/
                break;
            default:
                break;
        }

        page.addLine().addUnit(" ", FONT_SMALL);
        if (transType == ETransType.PBB_PAY |
                transType == ETransType.DIRJEN_PAJAK |
                transType == ETransType.DIRJEN_BEA_CUKAI |
                transType == ETransType.DIRJEN_ANGGARAN |
                transType == ETransType.TRANSFER |
                transType == ETransType.TRANSFER_2 ) {
            try {
                if (transData.getPrintTimeout()!= null && transData.getPrintTimeout().equalsIgnoreCase("y")) {
                    printFooterTimeout(page);
                } else {
                    printFooter(page);
                }
            } catch (Exception e) {
                printFooter(page);
            }
        } else if (transType == ETransType.PURCHASE_PULSA_DATA || transType == ETransType.INQ_PULSA_DATA) {
            printFooterPulsaData(page);
        } else {

                printFooter(page);
        }

        if (isRePrint) {
            page.addLine()
                    .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                    .addUnit(context.getString(R.string.receipt_print_agian), FONT_SMALL,
                            EAlign.RIGHT,
                            (float) 1.4);
        } else {
            if (receiptMax == 3) {
                if (receiptNo == 0) {
                    page.addLine()
                            .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                            .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_SMALL,
                                    EAlign.RIGHT,
                                    (float) 1.4);
                } else if (receiptNo == 1) {
                    page.addLine()
                            .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                            .addUnit(context.getString(R.string.receipt_stub_user), FONT_SMALL,
                                    EAlign.RIGHT,
                                    (float) 1.4);
                } else {
                    page.addLine().addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT,
                            (float) 2)
                            .addUnit(context.getString(R.string.receipt_stub_acquire), FONT_SMALL,
                                    EAlign.RIGHT, (float) 1.4);
                }
            } else {
                if (receiptNo == 0) {
                    page.addLine()
                            .addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT, (float) 2)
                            .addUnit(context.getString(R.string.receipt_stub_merchant), FONT_SMALL,
                                    EAlign.RIGHT,
                                    (float) 1.4);
                } else {
                    page.addLine().addUnit(getTerminalandAppVersion(), FONT_SMALL, EAlign.LEFT,
                            (float) 2)
                            .addUnit(context.getString(R.string.receipt_stub_user), FONT_SMALL,
                                    EAlign.RIGHT, (float) 1.4);
                }
            }
        }

        page.addLine().addUnit("\n\n\n", FONT_NORMAL);

        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
//        return imgProcessing.pageToBitmap(page, 384);
        return imgProcessing.pageToBitmap(page, 420);
    }

    private void printHeader(IPage page,String logoFile) {
        SysParam sysParam = FinancialApplication.getSysParam();
        Bitmap icon = getImageFromAssetsFile(logoFile);
        page.addLine().addUnit(icon, EAlign.CENTER);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.MERCH_EN).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR1).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR2).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_merchant_code) + sysParam.get(SysParam.MERCH_ID))
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_terminal_code_space) + sysParam.get(SysParam.TERMINAL_ID))
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        String temp = "";
        String temp2 = "";
        temp = String.format("%06d", transData.getTransNo());
        temp2 = String.format("%06d", transData.getBatchNo());

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_trans_no) + temp)
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_batch_num) + temp2)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());
        temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4)
                + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_date) + temp)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
    }


    private void printHeaderCetakUlang(IPage page, String logoFile) {
        SysParam sysParam = FinancialApplication.getSysParam();
        Bitmap icon = getImageFromAssetsFile(logoFile);
        page.addLine().addUnit(icon, EAlign.CENTER);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.MERCH_EN).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR1).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR2).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_merchant_code) + sysParam.get(SysParam.MERCH_ID))
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_terminal_code_space) + sysParam.get(SysParam.TERMINAL_ID))
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        String temp = "";
        String temp2 = "";
        temp = String.format("%06d", transData.getOrigTransNo());
        temp2 = String.format("%06d", transData.getOrigBatchNo());

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_trans_no) + temp)
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_batch_num) + temp2)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        Long datetime = transData.getOrigDateTimeTrans();
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());
        temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4)
                + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_date) + temp)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
    }



    private void printHeaderCetakUlangOld(IPage page) {
        SysParam sysParam = FinancialApplication.getSysParam();
        Bitmap icon = getImageFromAssetsFile("bjb1.jpg");
        page.addLine().addUnit(icon, EAlign.CENTER);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.MERCH_EN).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR1).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(sysParam.get(SysParam.ADDR2).replace("  ", "").replace("\n", ""))
                        .setAlign(EAlign.CENTER)
                        .setFontSize(FONT_NORMAL)
                        .setTextStyle(Typeface.BOLD)
                        .setWeight(3.0f));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_merchant_code) + sysParam.get(SysParam.MERCH_ID))
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_terminal_code_space) + sysParam.get(SysParam.TERMINAL_ID))
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        String temp = "";
        String temp2 = "";
        temp = String.format("%06d", transData.getOrigTransNo());
        temp2 = String.format("%06d", transData.getOrigBatchNo());

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_trans_no) + temp)
                        .setWeight(7)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD))
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_batch_num) + temp2)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(12));

        Long datetime = transData.getOrigDateTimeTrans();
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());
        temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4)
                + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_en_date) + temp)
                        .setWeight(9)
                        .setFontSize(18)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
    }

    private String getModeCard(String temp) {
        if (enterMode == EnterMode.MANUAL) {
            temp += " M";
        } else if (enterMode == EnterMode.SWIPE) {
            temp += " S";
        } else if (enterMode == EnterMode.INSERT) {
            temp += " I";
        } else if (enterMode == EnterMode.CLSS_PBOC || enterMode == EnterMode.QPBOC) {
            temp += " C";
        } else if (enterMode == EnterMode.FALLBACK) {
            temp += " F";
        }
        return temp;
    }

    private String getAmount(String amt) {
        // add abdul 310321 info saldo amount dari transdata.getbalance
        try {
            long amount = Long.parseLong(amt);
            String temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);
            temp = currency.getName() + " " + temp;
            return temp;
        } catch (Exception e) {
            return "";
        }
    }

    public static String maskedNoRek(String accNo) {
        int cardLen = accNo.length();
        // 验证：16-20位数字
        /*if (cardLen < 13)
            return null;*/

        String maskCardNo = accNo;
        maskCardNo = accNo.substring(0, 3).concat(StringUtils.repeat("*", cardLen - 4)).concat(accNo.substring(cardLen - 3));

        return maskCardNo;
    }

    private void printBodyInfoSaldo(IPage page, String title) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(title)
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
        String temp = PanUtils.maskedCardNo(transType, transData.getPan());
        temp = getModeCard(temp);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_no_kartu) + " :")
                        .setWeight(5)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setWeight(10)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.detail_jml_saldo) + " :")
                        .setWeight(7)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(getAmount(transData.getBalance()))
                        .setWeight(8)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
    }

    private void printBukaRekening(IPage page) {

//        String data = "                                TRI                         PEKALONGAN22011994   089691007480    1234567890123456090421264637              T010";
//        String data = transData.getField48();
        String data = transData.getReprintData();

        String nama = data.substring(0, 35); //35
        nama = nama.trim();

        String tempat = data.substring(35, 70); //35
        tempat = StringUtils.deleteWhitespace(tempat);

        String tanggal = data.substring(70, 78); //8

        String no = data.substring(78, 93); //15
        no = StringUtils.deleteWhitespace(no);

        String nik = data.substring(93, 113); //20
        nik = StringUtils.deleteWhitespace(nik);

        String id = data.substring(113, 125); //12

        try {
            Date date1;
            SimpleDateFormat sdf1 = new SimpleDateFormat("ddMMyyyy");
            date1 = sdf1.parse(tanggal);
            sdf1.applyPattern("dd-MM-yyyy");
            tanggal = sdf1.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        page.addLine().addUnit("PROSES AWAL PEMBUKAAN REKENING BSA", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        //nama
        page.addLine().addUnit("NAMA", FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
        //nik
        page.addLine().addUnit("NIK", FONT_NORMAL)
                .addUnit(nik, FONT_NORMAL, EAlign.RIGHT);
        //tempat
        page.addLine().addUnit("TEMPAT LAHIR", FONT_NORMAL)
                .addUnit(tempat, FONT_NORMAL, EAlign.RIGHT);
        //tgl
        page.addLine().addUnit("TANGGAL LAHIR", FONT_NORMAL)
                .addUnit(tanggal, FONT_NORMAL, EAlign.RIGHT);
        //hp
        page.addLine().addUnit("NO. HANDPHONE", FONT_NORMAL)
                .addUnit(no, FONT_NORMAL, EAlign.RIGHT);
        //jml setoran
//        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);
        String amt = transData.getAmount();

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        page.addLine().addUnit("JUMLAH SETORAN", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

        //id trans
        page.addLine().addUnit("ID TRANSAKSI", FONT_NORMAL)
                .addUnit(id, FONT_NORMAL, EAlign.RIGHT);

    }

    private void printbodyDJP(IPage page) {
        String data = transData.getReprintData();
        if (data.isEmpty()){
            page.addLine().addUnit("STATUS", FONT_NORMAL)
                    .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
        }else{

            String billingId = "";
            String nama = "";
            String ntb = "";
            String ntpn = "";
            String biayaAdmin = "";

            try {
                billingId = transData.getBillingId();
                nama = data.substring(15, 65).trim();
                ntb = transData.getRefNo();
                ntpn = data.substring(165, 181);


            } catch (Exception e) {
                e.printStackTrace();
            }

            page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

            page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                    .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NAMA", FONT_NORMAL)
                    .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NTB", FONT_NORMAL)
                    .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
            if (!StringUtils.isEmpty(ntpn)) {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit(ntpn, FONT_NORMAL, EAlign.RIGHT);
            } else {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("STATUS", FONT_NORMAL)
                        .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
            }

            String amt = transData.getAmount();

            String amount = currency.getName() + " " + FinancialApplication.getConvert()
                    .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

            page.addLine().addUnit("\n", FONT_SMALL);

            page.addLine().addUnit("NILAI", FONT_NORMAL)
                    .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

            biayaAdmin = transData.getField28();

            if (!TextUtils.isEmpty(biayaAdmin) && (!iscetakUlangTrans)){
                String fee = currency.getName() + " " + FinancialApplication.getConvert()
                        .amountMinUnitToMajor(biayaAdmin, currency.getCurrencyExponent(), true);

                page.addLine().addUnit("BIAYA ADMIN", FONT_NORMAL)
                        .addUnit(fee, FONT_NORMAL, EAlign.RIGHT);
            }

            String F110 = transData.getField110();
            if(F110 != null && F110.isEmpty() == Boolean.FALSE){
                transData.setField110(F110);
                printDynamicText(page,F110);
            }

        }

    }

    private String tambah(String a, String b) {
        if (TextUtils.isEmpty(a)) a = "0";
        if (TextUtils.isEmpty(b)) b = "0";
        return String.valueOf(Long.parseLong(a) + Long.parseLong(b));
    }

    private String cnvrt(String a) {
        if (TextUtils.isEmpty(a)) a = "0";
        return currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(a, currency.getCurrencyExponent(), true);
    }

    private void printbodyEsamsat(IPage page) {
        String noRek = transData.getAccNo().trim();
        String data = transData.getReprintData().trim();
        transData.setSamsatKodeBayar(data);
        String kodeBayar = data.substring(0, 16);
        String nama = data.substring(84, 109).trim();
        String noPol = data.substring(149, 158);
        String jenis = data.substring(167, 182).trim();
        String tahun = data.substring(227, 231);
        String jumlahBayar = data.substring(343, 355);
        String biayaAdmin = transData.getField28();
        //String total = "0";
        String total = tambah(jumlahBayar, biayaAdmin);

        biayaAdmin = cnvrt(biayaAdmin);
        jumlahBayar = cnvrt(jumlahBayar);
        total = cnvrt(total);

        page.addLine().addUnit("Pembayaran PKB/SWDKLLJ/BBNKB/PNBP ", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.detail_no_rek_tujuan), FONT_NORMAL)
                .addUnit(noRek, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.esamsat_kode_bayar), FONT_NORMAL)
                .addUnit(kodeBayar, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.esamsat_nama_pemilik), FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.esamsat_no_pol), FONT_NORMAL)
                .addUnit(noPol, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.esamsat_jenis_kendaraan), FONT_NORMAL)
                .addUnit(jenis, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.esamsat_tahun_pembuatan), FONT_NORMAL)
                .addUnit(tahun, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.detail_jumlah_bayar), FONT_NORMAL)
                .addUnit(jumlahBayar, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.detail_admin), FONT_NORMAL)
                .addUnit(biayaAdmin, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit(FinancialApplication.getAppContext().getString(R.string.trans_total), FONT_NORMAL)
                .addUnit(total, FONT_NORMAL, EAlign.RIGHT);


    }



    private void printDynamicText(IPage page, String text) {
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        if(text.contains("|")) {

            String[] informations = text.split("\\|");
            for (int i = 0; i < informations.length; i++) {
                String theInfos = informations[i].toString();
                if (theInfos.contains(":")) {
                    String[] infos = theInfos.split("\\:");
                    page.addLine()
                            .addUnit(page.createUnit()
                                    .setText(infos[0].toString())
                                    .setAlign(EAlign.LEFT)
                                    //.setTextStyle(Typeface.BOLD)
                                    .setFontSize(FONT_NORMAL))
                            .addUnit(page.createUnit()
                                    .setText(infos[1].toString())
                                    //.setTextStyle(Typeface.BOLD)
                                    .setFontSize(FONT_NORMAL)
                                    .setAlign(EAlign.RIGHT));
                } else {
                    page.addLine()
                            .addUnit(page.createUnit()
                                    .setText(theInfos)
                                    .setAlign(EAlign.CENTER)
                                    //.setTextStyle(Typeface.BOLD)
                                    .setFontSize(FONT_NORMAL));
                }


            }
        }else {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(text)
                            .setAlign(EAlign.LEFT)
                            //.setTextStyle(Typeface.BOLD)
                            .setFontSize(FONT_NORMAL));

        }



    }







    private void printBodyPascabayarTimeouut(IPage page) {
        //transData.setReprintData(kodeBayar+"#"+productId+"#"+fee+"#"+basePrice+"#"+productDesc);
        String status = "";
        String[] data = transData.getReprintData().split("#");

        page.addLine().addUnit("PDAM ID", FONT_NORMAL)
                .addUnit(data[0], FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("KODE PRODUK", FONT_NORMAL)
                .addUnit(data[1], FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("TIPE PRODUK", FONT_NORMAL)
                .addUnit("PASCABAYAR", FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("KETERANGAN", FONT_NORMAL)
                .addUnit(data[4], FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("SERIAL NUMBER", FONT_NORMAL)
                .addUnit("-", FONT_NORMAL, EAlign.RIGHT);

        if (transData.getPrintTimeout().equalsIgnoreCase("y")) {
            status = "PENDING";
        } else {
            status = "BERHASIL";
        }

        page.addLine().addUnit("STATUS", FONT_NORMAL)
                .addUnit(status, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("HARGA", FONT_NORMAL)
                .addUnit(cnvrt(data[3]), FONT_NORMAL, EAlign.RIGHT);

        /*page.addLine().addUnit("BIAYA ADMIN", FONT_NORMAL)
                .addUnit(cnvrt(data[2]), FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("TOTAL", FONT_NORMAL)
                .addUnit(cnvrt(tambah(data[2], data[3])), FONT_NORMAL, EAlign.RIGHT);*/

    }

    private void printBodyPascabayar(IPage page) {
        String status = "";
        String data = transData.getReprintData();
        String[] record = data.split("\\|");
        for (int i = 0; i < record.length; i++) {

            if(record[i].contains(":")){
                String[] column = record[i].split(":");
                page.addLine().addUnit(column[0].trim(), FONT_NORMAL)
                        .addUnit(column[1].trim(), FONT_NORMAL, EAlign.RIGHT);
            }else {
                page.addLine().addUnit(record[i].toString(), FONT_NORMAL);
            }

        }
        try {
            if (transData.getPrintTimeout().equalsIgnoreCase("y")) {
                status = "PENDING";
            } else {
                status = "BERHASIL";
            }
        } catch (Exception e) {
            status = "PENDING";
        }

        if (!StringUtils.isEmpty(status)){
            page.addLine().addUnit("STATUS", FONT_NORMAL)
                    .addUnit(status, FONT_NORMAL, EAlign.RIGHT);

            if(status.equals("BERHASIL")){
                String F110 = transData.getField110();
                if(F110 != null && F110.isEmpty() == Boolean.FALSE){
                    transData.setField110(F110);
                    printDynamicText(page,F110);
                }
            }

        }




    }

    private void printbodyTimeoutDJP(IPage page) {
        //String data = transData.getField48();
        String data = transData.getReprintData();

        String billingId = "";
        String nama = "";
        String ntb = "";

        try {
            billingId = transData.getBillingId();
            nama = data.substring(15, 65).trim();
            ntb = transData.getRefNo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NAMA", FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTB", FONT_NORMAL)
                .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTPN", FONT_NORMAL)
                .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("STATUS", FONT_NORMAL)
                .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);


        String amt = transData.getAmount().substring(0, 10);

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        page.addLine().addUnit("\n", FONT_SMALL);

        page.addLine().addUnit("NILAI", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);


    }

    private void printbodyDJBC(IPage page) {
        String data = transData.getReprintData();
        if (data.isEmpty()){
            page.addLine().addUnit("STATUS", FONT_NORMAL)
                    .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
        }else{
            //        String a = "PDI TRESNO, PT.                                   013104757623000     09005175                        2012-11-07070600012-11-07070600";

            /*Nama Wajib Bayar An..50     0,50
            ID Wajib Bayar An..20       50,70
            Jenis Dokumen An..2         70,72
            Nomor Dokumen An..30        72, 102
            Tanggal Dokumen An..10      102, 112
            Kode KPPBC An..6            112, 118
            NTPN An..16                 118, 134*/

            String billingId = "";
            String nama = "";
            String ntb = "";
            String ntpn = "";
            String biayaAdmin = "";

            try {
                billingId = transData.getBillingId();
                nama = data.substring(0, 50).trim();
                ntb = transData.getRefNo();
                ntpn = data.substring(118, 134);

            } catch (Exception e) {
                e.printStackTrace();
            }

            page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

            page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                    .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NAMA", FONT_NORMAL)
                    .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NTB", FONT_NORMAL)
                    .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
            if (!StringUtils.isEmpty(ntpn)) {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit(ntpn, FONT_NORMAL, EAlign.RIGHT);
            } else {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("STATUS", FONT_NORMAL)
                        .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
            }

            String amt = transData.getAmount();

            String amount = currency.getName() + " " + FinancialApplication.getConvert()
                    .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

            page.addLine().addUnit("\n", FONT_SMALL);

            page.addLine().addUnit("NILAI", FONT_NORMAL)
                    .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

            biayaAdmin = transData.getField28();

            if (!TextUtils.isEmpty(biayaAdmin) && (!iscetakUlangTrans)){
                String fee = currency.getName() + " " + FinancialApplication.getConvert()
                        .amountMinUnitToMajor(biayaAdmin, currency.getCurrencyExponent(), true);

                page.addLine().addUnit("BIAYA ADMIN", FONT_NORMAL)
                        .addUnit(fee, FONT_NORMAL, EAlign.RIGHT);
            }


            //additional text
            String F110 = transData.getField110();
            if(F110 != null && F110.isEmpty() == Boolean.FALSE){
                transData.setField110(F110);
                printDynamicText(page,F110);
            }

        }

    }

    private void printbodyTimeoutDJBC(IPage page) {
//        String data = transData.getField48();
        String data = transData.getReprintData();
        String billingId = transData.getBillingId();
        String nama = data.substring(0, 50).trim();
        String ntb = transData.getRefNo();

        page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NAMA", FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTB", FONT_NORMAL)
                .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTPN", FONT_NORMAL)
                .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("STATUS", FONT_NORMAL)
                .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);


        String amt = transData.getAmount().substring(0, 10);

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        page.addLine().addUnit("\n", FONT_SMALL);

        page.addLine().addUnit("NILAI", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);


    }

    private void printbodyDJA(IPage page) {
        String data = transData.getReprintData();
        if (data.isEmpty()){
            page.addLine().addUnit("STATUS", FONT_NORMAL)
                    .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
        }else{
            /*Bill ID An..15
            Nama Wajib Bayar An..50 --65
            K/L An..3               --68
            Unit Eselon I An..2     --70
            Kode Satker An..6       --76
            NTPN An..16             --92*/

            /*String billingId = data.substring(0, 15);
            String nama = data.substring(15, 65).trim();
            String kl = data.substring(65, 68).trim();
            String ntb = transData.getRefNo();
            String ntpn = data.substring(76, 92);*/

            String billingId = "", nama = "", kl = "", eselon = "", ntb = "", ntpn = "", biayaAdmin = "";

            try {
                billingId = transData.getBillingId();
                nama = data.substring(0, 50).trim();
                kl = data.substring(50, 53).trim();
                eselon = data.substring(53, 55).trim();
                ntb = transData.getRefNo();
                ntpn = data.substring(61, 77);
            }catch (Exception e){

            }

            page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

            page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                    .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NAMA", FONT_NORMAL)
                    .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("KL/ES", FONT_NORMAL)
                    .addUnit(kl + "/" + eselon, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("NTB", FONT_NORMAL)
                    .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
            if (!StringUtils.isEmpty(ntpn)) {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit(ntpn, FONT_NORMAL, EAlign.RIGHT);
            } else {
                page.addLine().addUnit("NTPN", FONT_NORMAL)
                        .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
                page.addLine().addUnit("STATUS", FONT_NORMAL)
                        .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
            }


            String amt = transData.getAmount();

            String amount = currency.getName() + " " + FinancialApplication.getConvert()
                    .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

            page.addLine().addUnit("\n", FONT_SMALL);

            page.addLine().addUnit("NILAI", FONT_NORMAL)
                    .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

            biayaAdmin = transData.getField28();

            if (!TextUtils.isEmpty(biayaAdmin) && (!iscetakUlangTrans)){
                String fee = currency.getName() + " " + FinancialApplication.getConvert()
                        .amountMinUnitToMajor(biayaAdmin, currency.getCurrencyExponent(), true);

                page.addLine().addUnit("BIAYA ADMIN", FONT_NORMAL)
                        .addUnit(fee, FONT_NORMAL, EAlign.RIGHT);
            }

            String F110 = transData.getField110();
            if(F110 != null && F110.isEmpty() == Boolean.FALSE){
                transData.setField110(F110);
                printDynamicText(page,F110);
            }



        }

    }

    private void printbodyTimeoutDJA(IPage page) {
//        String data = transData.getField48();
        String data = transData.getReprintData();
        String billingId = transData.getBillingId();
        String nama = data.substring(0, 50).trim();
        String kl = data.substring(50, 53).trim();
        String eselon = data.substring(53, 55).trim();
        String ntb = transData.getRefNo();

        page.addLine().addUnit("BUKTI PENERIMAAN NEGARA", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit("ID BILLING", FONT_NORMAL)
                .addUnit(billingId, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NAMA", FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("KL/ES", FONT_NORMAL)
                .addUnit(kl + "/" + eselon, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTB", FONT_NORMAL)
                .addUnit(ntb, FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("NTPN", FONT_NORMAL)
                .addUnit("-", FONT_NORMAL, EAlign.RIGHT);
        page.addLine().addUnit("STATUS", FONT_NORMAL)
                .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);

        String amt = transData.getAmount().substring(0, 10);

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        page.addLine().addUnit("\n", FONT_SMALL);

        page.addLine().addUnit("NILAI", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);


    }

    private void reprintMPN(IPage page) {
        iscetakUlangTrans = true;

        String pref = transData.getBillingId().substring(0, 1);
        switch (pref) {
            case "0":
            case "1":
            case "2":
            case "3":

                /*if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJP(page);
                    } else {
                        printbodyDJP(page);
                    }
                } else {
                    printbodyDJP(page);
                }*/
                printbodyDJP(page);
                break;
            case "4":
            case "5":
            case "6":
                /*if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJBC(page);
                    } else {
                        printbodyDJBC(page);
                    }
                } else {
                    printbodyDJBC(page);
                }*/

                printbodyDJBC(page);
                break;
            case "7":
            case "8":
            case "9":
                /*if (!TextUtils.isEmpty(transData.getPrintTimeout())) {
                    if (transData.getPrintTimeout().equals("y")) {
                        printbodyTimeoutDJA(page);
                    } else {
                        printbodyDJA(page);
                    }
                } else {
                    printbodyDJA(page);
                }*/
                printbodyDJA(page);
                break;
        }

    }

    private void printBatalRekening(IPage page) {

        //String data = "090421264753                               TRIA                         PEKALONGAN22011994   089691007480    1234567890123456              T010";
//        String data = transData.getField48();
        String data = transData.getReprintData();

        String nama = data.substring(12, 47); //35
        //nama = StringUtils.deleteWhitespace(nama);
        nama = nama.trim();
        String tempat = data.substring(47, 82); //35
        tempat = StringUtils.deleteWhitespace(tempat);
        String tanggal = data.substring(82, 90); //8
        String no = data.substring(90, 105); //15
        no = StringUtils.deleteWhitespace(no);
        String nik = data.substring(105, 125); //20
        nik = StringUtils.deleteWhitespace(nik);

        try {
            Date date1;
            SimpleDateFormat sdf1 = new SimpleDateFormat("ddMMyyyy");
            date1 = sdf1.parse(tanggal);
            sdf1.applyPattern("dd-MM-yyyy");
            tanggal = sdf1.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        page.addLine().addUnit("PEMBATALAN REKENING BSA", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        //nama
        page.addLine().addUnit("NAMA", FONT_NORMAL)
                .addUnit(nama, FONT_NORMAL, EAlign.RIGHT);
        //nik
        page.addLine().addUnit("NIK", FONT_NORMAL)
                .addUnit(nik, FONT_NORMAL, EAlign.RIGHT);
        //tempat
        page.addLine().addUnit("TEMPAT LAHIR", FONT_NORMAL)
                .addUnit(tempat, FONT_NORMAL, EAlign.RIGHT);
        //tgl
        page.addLine().addUnit("TANGGAL LAHIR", FONT_NORMAL)
                .addUnit(tanggal, FONT_NORMAL, EAlign.RIGHT);
        //hp
        page.addLine().addUnit("NO. HANDPHONE", FONT_NORMAL)
                .addUnit(no, FONT_NORMAL, EAlign.RIGHT);
        //jml setoran
//        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);
        String amt = transData.getAmount();

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        page.addLine().addUnit("JUMLAH SETORAN", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

    }


    private void printBodyTransfer(IPage page) {

        //String data = "ALISHA DIPHDAN                                NUNUNG MASNUAH                ";
//        String data = transData.getField48();
        String data = transData.getReprintData();
        String namaPenerima = data.substring(0, 30).trim();
        String reffno = data.substring(30, 46).trim();
        String namaPengirim = data.substring(46, 76).trim();

        String noRekPengirim = transData.getAccNo().trim();
        noRekPengirim = Fox.Masking(noRekPengirim, 1, "", 3, noRekPengirim.length() - 3, "x");
        String noRekTujuan = transData.getField103();
        noRekTujuan = Fox.Masking(noRekTujuan, 1, "", 3, noRekTujuan.length() - 3, "x");
        String bankTujuan = transData.getDestBank();

//        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);
        String amt = transData.getAmount();

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);


        /*String biayaAdmin = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(transData.getField28()),
                        currency.getCurrencyExponent(), true);*/
//        long total = Long.parseLong(amt) + Long.parseLong(transData.getField28());

        String biayaAdmin = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(transData.getFeeTotalAmount()),
                        currency.getCurrencyExponent(), true);


        long total = Long.parseLong(amt) + Long.parseLong(transData.getFeeTotalAmount());

        String totaltrf = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(total),
                        currency.getCurrencyExponent(), true);

        page.addLine().addUnit("TRANSFER ANTAR BANK", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit("Rekening Asal", FONT_NORMAL)
                .addUnit(noRekPengirim, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Nama", FONT_NORMAL)
                .addUnit(namaPengirim, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Rekening Tujuan", FONT_NORMAL)
                .addUnit(noRekTujuan, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Nama", FONT_NORMAL)
                .addUnit(namaPenerima, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Bank Tujuan", FONT_NORMAL)
                .addUnit(bankTujuan, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Jumlah Transfer", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

        String sShowAdminFee = FinancialApplication.getSysParam().get(SysParam.SHOW_ADMIN_FEE);

        //set default = true
        boolean showAdminFee = true;
        //if there is am admin fee configured, then follow it from database.
        if(sShowAdminFee != null){
            showAdminFee = sShowAdminFee.equals("1") == true ? true : false;
        }

        //dont display if fee is 0
        if(transData.getField28() != null && Integer.parseInt(transData.getField28()) != 0 && showAdminFee == true) {
             page.addLine().addUnit("Biaya Admin", FONT_NORMAL)
                    .addUnit(biayaAdmin, FONT_NORMAL, EAlign.RIGHT);
        }

        page.addLine().addUnit("Total", FONT_NORMAL)
                .addUnit(totaltrf, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("No. Referensi", FONT_NORMAL)
                .addUnit(reffno, FONT_NORMAL, EAlign.RIGHT);


        String F110 = transData.getField110();
        if(F110 != null && F110.isEmpty() == Boolean.FALSE){
            transData.setField110(F110);
            printDynamicText(page,F110);
        }

        //------------------------------------------------------------------------------------------
    }

    private void printBodyOverbooking(IPage page) {

        //String data = "AHMAD JAYANI DRS                   SULAEMAN                           ";
//        String data = transData.getField48();
        String data = transData.getReprintData();
        String namaPenerima = data.substring(0, 35).trim();
        String reffno = transData.getRefNo();
        String namaPengirim = data.substring(35, 70).trim();

        String noRekPengirim = transData.getAccNo().trim();
        noRekPengirim = Fox.Masking(noRekPengirim, 1, "", 3, noRekPengirim.length() - 3, "x");
        String noRekTujuan = transData.getField103();
        noRekTujuan = Fox.Masking(noRekTujuan, 1, "", 3, noRekTujuan.length() - 3, "x");
        String amt = transData.getAmount();

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);


//        String biayaAdmin = currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(transData.getField28()), currency.getCurrencyExponent(), true);
        String biayaAdmin = currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(transData.getFeeTotalAmount()), currency.getCurrencyExponent(), true);

        long total = Long.parseLong(amt) + Long.parseLong(transData.getFeeTotalAmount());

        String totaltrf = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(total),
                        currency.getCurrencyExponent(), true);

        page.addLine().addUnit("PEMINDAHBUKUAN", FONT_SMALL, EAlign.LEFT);
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        page.addLine().addUnit("Rekening Asal", FONT_NORMAL)
                .addUnit(noRekPengirim, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Nama", FONT_NORMAL)
                .addUnit(namaPengirim, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Rekening Tujuan", FONT_NORMAL)
                .addUnit(noRekTujuan, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Nama", FONT_NORMAL)
                .addUnit(namaPenerima, FONT_NORMAL, EAlign.RIGHT);

        /*page.addLine().addUnit("Bank Tujuan", FONT_NORMAL)
                .addUnit(bankTujuan, FONT_NORMAL, EAlign.RIGHT);*/

        page.addLine().addUnit("Jumlah Transfer", FONT_NORMAL)
                .addUnit(amount, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Biaya Admin", FONT_NORMAL)
                .addUnit(biayaAdmin, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("Total", FONT_NORMAL)
                .addUnit(totaltrf, FONT_NORMAL, EAlign.RIGHT);

        page.addLine().addUnit("No. Referensi", FONT_NORMAL)
                .addUnit(reffno, FONT_NORMAL, EAlign.RIGHT);

        //------------------------------------------------------------------------------------------


        String F110 = transData.getField110();
        if(F110 != null && F110.isEmpty() == Boolean.FALSE){
            transData.setField110(F110);
            printDynamicText(page,F110);
        }


    }

    private void printBodyTarikTunai(IPage page, String title) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(title)
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
        String temp = PanUtils.maskedCardNo(transType, transData.getPan());
        temp = getModeCard(temp);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_no_kartu) + " : ")
                        .setWeight(5)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setWeight(10)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.detail_jml_tarik) + " : ")
                        .setWeight(9)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(getAmount(transData.getAmount()))
                        .setWeight(7)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.detail_saldo) + " : ")
                        .setWeight(8)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(getAmount(transData.getBalance()))
                        .setWeight(7)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
    }

    private void printBodySetorTunai(IPage page) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(getEnTransType(ETransType.SETOR_TUNAI))
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));
        //String temp = PanUtils.maskedCardNo(transType, transData.getAccNo());
        String temp = maskedNoRek(transData.getAccNo());
        //temp = getModeCard(temp);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_no_rek) + " : ")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.detail_nm_penerima) + " : ")
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("")
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
//                        .setText(transData.getField48().replace("  ", ""))
                        .setText(transData.getReprintData().replace("  ", ""))
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.detail_jml_setor) + " : ")
                        .setWeight(8)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(getAmount(transData.getAmount()))
                        .setWeight(7)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
    }

    private void printBodyPulsaData(IPage page) {
        String phone = "";
        String pcode = "";
        String tprod = "";
        String oper = "";
        String keter = "";
        String pname = "";
        String namaPelanggan = "";
        String resp48 = "";
        try {
            String[] f47 = transData.getField47().split("#"); // di 47 ada data pulsa data

            phone = f47[1];

            pcode = f47[2];

            //fase 2, produk kode ngambil dari host
            resp48 = transData.getField48().trim();
            if (!TextUtils.isEmpty(resp48)){
                String[] splited = resp48.split("\\s+");
                pcode = splited[1];
                Log.i("teg", "pcode : "+pcode);
            }

            /*if (pcode.contains("-")) {
                String[] temp = pcode.split("-");
                pcode = temp[1];
            }*/

            tprod = f47[3];
            if (tprod.equals("pulse")) {
                tprod = "Pulsa";
            }
            oper = f47[4];
            keter = f47[5];
            pname = f47[6];

            namaPelanggan = transData.getField59();

        } catch (Exception e) {
            e.printStackTrace();
        }
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("No Tujuan")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(phone)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        if (!StringUtils.isEmpty(namaPelanggan)) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Nama")
                            .setAlign(EAlign.LEFT)
                            .setTextStyle(Typeface.BOLD)
                            .setFontSize(FONT_NORMAL))
                    .addUnit(page.createUnit()
                            .setText(namaPelanggan)
                            .setTextStyle(Typeface.BOLD)
                            .setFontSize(FONT_NORMAL)
                            .setAlign(EAlign.RIGHT));
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Kode Produk")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(pcode)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Type Produk")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText("Prabayar")
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Operator")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(oper)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Keterangan")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(keter)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        String sts = "";
        try {
            //Sandy : previously was commented here, dunno why?
            if (transData.getPrintTimeout().equals("Y")) {
                sts = "Pending";
            } else {
                sts = "Success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            sts = "Success";
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Status")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(sts)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        String sn = "";
        try {
            sn = Fox.Hex2Txt(transData.getField61());
        } catch (Exception e) {
            e.printStackTrace();
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("SN")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(sn)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Harga")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(getAmount(transData.getSellPrice()))
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));



        String F110 = transData.getField110();
        if(F110 != null && F110.isEmpty() == Boolean.FALSE){
            transData.setField110(F110);
            printDynamicText(page,F110);
        }




    }

    private void printBodyPulsaDataNew(IPage page) {

        String dataPrinting = transData.getReprintData();
        String[] data = dataPrinting.split("#");
        String nama = "";
        String status = "";
        //String nama = transData.getField59().trim();
        String oper = data[0];
        String phone = data[1];
        String kodeProduk = data[2];
        if (kodeProduk.contains("-")) {
            String[] temp = kodeProduk.split("-");
            kodeProduk = temp[1];
        }
        String amount = data[3];
        String fee = data[4];
        String total = data[5];
        String desc = data[6];
        status = data[7];

        if (transData.getReprintData().equals("y")) {
            status = "pending";
        }

        if (data.length > 8) {
            nama = data[8];
        }

        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_NORMAL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("No Tujuan")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(phone)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        if (!StringUtils.isEmpty(nama)) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Nama")
                            .setAlign(EAlign.LEFT)
                            .setTextStyle(Typeface.BOLD)
                            .setFontSize(FONT_NORMAL))
                    .addUnit(page.createUnit()
                            .setText(nama.trim().toUpperCase())
                            .setTextStyle(Typeface.BOLD)
                            .setFontSize(FONT_NORMAL)
                            .setAlign(EAlign.RIGHT));
            page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        }


        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Kode Produk")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(kodeProduk)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Type Produk")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText("Prabayar")
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Operator")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(oper)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Keterangan")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(desc)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Status")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(status)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        String sn = "";
        try {
            sn = Fox.Hex2Txt(transData.getField61());
        } catch (Exception e) {
            e.printStackTrace();
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("SN")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(sn)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Harga")
                        .setAlign(EAlign.LEFT)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(amount)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));
    }

    private void printFooter(IPage page) {
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("TRANSAKSI BERHASIL")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("RESI INI MERUPAKAN BUKTI YANG SAH")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("RAHASIAKAN PIN ANDA")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        //Sandy : do not display if the transaction is PLN
        if(isPLN == false){
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("BJB CALL 14049")
                            .setAlign(EAlign.CENTER)
                            .setFontSize(22)
                            .setTextStyle(Typeface.BOLD));
        }

        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
    }

    private void printFooterPulsaData(IPage page) {
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("TERIMA KASIH ATAS KEPERCAYAAN ANDA")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
    }

    private void printFooterTimeout(IPage page) {
        page.addLine().addUnit(page.createUnit()
                .setText("_________________________________________________________________")
                .setFontSize(12)
                .setTextStyle(Typeface.BOLD)
                .setAlign(EAlign.CENTER));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("TRANSAKSI SEDANG DALAM PROSES")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("MOHON DAPAT DISIMPAN")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("RESI INI ADALAH")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("BUKTI PEMBAYARAN YANG SAH")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));

        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
    }

    private void printBodyMiniStatement(IPage page) {

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("MINISTATEMENT")
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit("Nomor Rekening", FONT_NORMAL, 1.3f)
                .addUnit(": " + transData.getAccNo(), FONT_NORMAL, EAlign.RIGHT, 1.5f);

        page.addLine().addUnit(" ", FONT_SMALL);

        String bit47 = transData.getField47();
        String[] date = new String[bit47.length() / 55];
        String[] sign = new String[bit47.length() / 55];
        String[] amount = new String[bit47.length() / 55];
        String[] desc = new String[bit47.length() / 55];

        int lendata = bit47.length() / 55;
        int panjangsatudata = 0;

        for (int i = 0; i < lendata; i++) {
            date[i] = Fox.Substr(bit47, 1 + panjangsatudata, 8);
            sign[i] = Fox.Substr(bit47, 10 + panjangsatudata, 1);
            amount[i] = Fox.Substr(bit47, 12 + panjangsatudata, 18).replace(" ", "");
            desc[i] = Fox.Substr(bit47, 31 + panjangsatudata, 25).replace("  ", "");

            String fullamt;
            if (sign[i].equals("D")) {
                fullamt = "- Rp" + amount[i];

                /*if (desc[i].contains("TARIK") || desc[i].contains("POTONGAN") || desc[i].contains("TRANSFER")){
                    fullamt = "- Rp"+amount[i];
                }else {
                    fullamt = "Rp"+amount[i];
                }*/

            } else {
                fullamt = "+ Rp" + amount[i];
            }

            page.addLine()
                    .addUnit(desc[i] + "(" + sign[i] + ")", FONT_SMALL);

            page.addLine()
                    .addUnit(date[i], FONT_SMALL)
                    .addUnit(fullamt, FONT_SMALL, EAlign.RIGHT, 1.3f);

            page.addLine().addUnit(" ", FONT_SMALL);

            panjangsatudata += 55;

        }
    }

    private void printBodyChangePin(IPage page) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("UBAH PIN")
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        String temp = PanUtils.maskedCardNo(transType, transData.getPan());
        temp = getModeCard(temp);

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_no_kartu) + " : ")
                        .setWeight(5)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setWeight(10)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        page.addLine()
                .addUnit("PIN Nasabah telah berhasil diubah", FONT_NORMAL);
    }

    private void printBodyPBB(IPage page) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("PBB-P2")
                        .setAlign(EAlign.LEFT)
                        .setFontSize(FONT_BIG)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_BIG));

        String temp = PanUtils.maskedCardNo(transType, transData.getPan());
        temp = getModeCard(temp);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(FinancialApplication.getAppContext().getString(R.string.receipt_no_kartu) + " : ")
                        .setWeight(5)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setWeight(10)
                        .setTextStyle(Typeface.BOLD)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        String bit61 = Fox.Hex2Txt(transData.getField61());
        String norek, kodebayar, jumlahbayar, denda, total, diskon, tahunPajak,namaWP,
                lokasi, kelurahan, kecamatan,provinsi,LT,LB, jatuhTempo;

        kodebayar = Fox.Substr(bit61, 1, 18);
        jumlahbayar = Fox.Substr(bit61, 230, 12);
        denda = Fox.Substr(bit61, 242, 12);
        total = Fox.Substr(bit61, 254, 12);
        diskon = Fox.Substr(bit61, 266, 12); //fase 2
        //additional (2022-02-07)
        tahunPajak = Fox.Substr(bit61, 19, 4);
        namaWP = Fox.Substr(bit61, 23, 35).trim();
        lokasi = Fox.Substr(bit61, 58, 35).trim();
        kelurahan = Fox.Substr(bit61, 93, 35).trim();
        kecamatan = Fox.Substr(bit61, 128, 35).trim();
        provinsi = Fox.Substr(bit61, 163, 35).trim();
        LT = Fox.Substr(bit61, 198, 12);
        LB = Fox.Substr(bit61, 210, 12);
        jatuhTempo = Fox.Substr(bit61, 222, 12);




        String biayaadmin = "";
        biayaadmin = transData.getField28();
        if (StringUtils.isEmpty(biayaadmin)){
            biayaadmin = "0";
        }

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String jmlBayarAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(jumlahbayar)), currency.getCurrencyExponent(), true);

        String biayaAdminAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(biayaadmin,
                        currency.getCurrencyExponent(), true);

        String dendaAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(denda)),
                        currency.getCurrencyExponent(), true);

        String diskonAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(diskon)),
                        currency.getCurrencyExponent(), true);

        String sLT = String.valueOf(Long.parseLong(LT));
        String sLB = String.valueOf(Long.parseLong(LB));




        long totalAmount = Long.parseLong(total);

        String totalBayar = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(totalAmount),
                        currency.getCurrencyExponent(), true);

        //kode bayar
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Kode Bayar")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(kodebayar)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //tahun pajak
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Tahun Pajak")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(tahunPajak)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));


        //Nama

        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Nama")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(namaWP)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));


        //Lokasi
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Lokasi")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(lokasi)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //Kelurahan
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Kelurahan")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(kelurahan)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));


        //Kecamatan
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Kecamatan")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(kecamatan)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //Provinsi
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Provinsi")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(provinsi)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));


        //Luas Tanah
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("LT")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(sLT)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //Luas Bangunan
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("LB")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(sLB)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //Jatuh Tempo
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Jatuh Tempo")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(jatuhTempo)
                        .setWeight(2)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));




        //jumlah bayar
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Jumlah Bayar")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(jmlBayarAmt)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //biaya adm
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Biaya Adm")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(biayaAdminAmt)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //denda
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Denda")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(dendaAmt)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //diskon
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Diskon")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(diskonAmt)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));

        //total
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("Total ")
                        .setFontSize(FONT_NORMAL))
                .addUnit(page.createUnit()
                        .setText(totalBayar)
                        .setFontSize(FONT_NORMAL)
                        .setAlign(EAlign.RIGHT));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_VERY_SMALL));

        if (!TextUtils.isEmpty(transData.getPrintTimeout())){
            if (transData.getPrintTimeout().equals("y")){
                page.addLine().addUnit("STATUS", FONT_NORMAL)
                        .addUnit("ON PROGRESS", FONT_NORMAL, EAlign.RIGHT);
            }else {
                //assuming it is success
                String F110 = transData.getField110();
                if(F110 != null && F110.isEmpty() == Boolean.FALSE){
                    transData.setField110(F110);
                    printDynamicText(page,F110);
                }

            }



        }







    }

    private String getBankName(String bankId) {
        String subBankId = bankId.substring(0, 4);
        String merchCode = FinancialApplication.getSysParam().get(SysParam.MERCH_MERCHCODE);
        String areaCode = FinancialApplication.getSysParam().get(SysParam.MERCH_AREACODE);
        if (merchCode != null && merchCode.length() > 0 && merchCode.equals(subBankId)) {
            return FinancialApplication.getAppContext().getString(R.string.commercial_bank);
        }

        for (int i = 0; i < bankNames.length; i++) {
            if (bankNames[i][0].equals(subBankId)) {
                if ("0000".equals(areaCode) || areaCode.endsWith(subBankId)) {
                    return bankNames[i][1];
                } else {
                    return bankNames[i][2];
                }
            }
        }
        return bankId;
    }

    private String getTerminalandAppVersion() {

        Map<ETermInfoKey, String> map = FinancialApplication.getDal().getSys().getTermInfo();

        return map.get(ETermInfoKey.MODEL) + " " + FinancialApplication.version;
    }

    private String getEnTransType(ETransType transType) {
        if (transType == ETransType.BALANCE_INQUIRY || transType == ETransType.BALANCE_INQUIRY_2) {
            return "INFORMASI SALDO";
        } else if (transType == ETransType.TARIK_TUNAI || transType == ETransType.TARIK_TUNAI_2) {
            return "PENARIKAN TUNAI";
        } else if (transType == ETransType.SETOR_TUNAI) {
            return "SETORAN TUNAI";
        } else if (transType == ETransType.CHANGE_PIN) {
            return "GANTI PIN";
        } else if (transType == ETransType.MINISTATEMENT) {
            return "MINISTATEMENT";
        } else {
            return null;
        }
    }

    private void printFooterPBB(IPage page) {
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("TRANSAKSI BERHASIL MOHON DISIMPAN")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("RESI INI MERUPAKAN BUKTI YANG SAH")
                        .setAlign(EAlign.CENTER)
                        .setFontSize(22)
                        .setTextStyle(Typeface.BOLD));
        page.addLine().addUnit(page.createUnit().setText(" ").setFontSize(FONT_SMALL));
    }

}
