/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:22
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispTransDetailVertical;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputKodeBilling;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSearchCardCustom;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.pax.pay.trans.action.ActionInputKodeBilling.EInputType.NUM;
import static com.pax.pay.trans.action.ActionInputKodeBilling.INFO_TYPE_SALE;

public class MpnTrans extends BaseTrans {

    private static final String TAG = "MpnG2Trans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;
    private int trans;

    private LinkedHashMap<String, String> mapData = new LinkedHashMap<>();

    /*public MpnTrans(Context context, Handler handler, String amount, boolean isFreePin,
                    TransEndListener transListener) {
        super(context, handler, ETransType.DIRJEN_PAJAK_INQUIRY, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
    }*/

    //nambah trans tipe
    public MpnTrans(Context context, Handler handler, String amount, boolean isFreePin, ETransType tipeTransaksi, int trans,
                    TransEndListener transListener) {
        super(context, handler, tipeTransaksi, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
        transType = tipeTransaksi;
        this.trans = trans;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(transType.getTransName());
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }
        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS : ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction2 = new ActionSearchCard(null);
        searchCardAction2.setMode(searchCardMode);
        searchCardAction2.setUiType(searchCardMode == SearchMode.TAP ? ESearchCardUIType.QUICKPASS : ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD_NORMAL.toString(), searchCardAction2);

        /*ActionInputTransData inputId = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputId.setTitle(transType.getTransName());
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_billing), EInputType.NUM, 15,  false);
        bind(State.INPUT_BILLING.toString(), inputId);*/

        ActionInputKodeBilling inputId = new ActionInputKodeBilling(handler, INFO_TYPE_SALE, null, trans);
        inputId.setTitle(transType.getTransName());
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_billing), NUM, 15, false);
        bind(State.INPUT_BILLING.toString(), inputId);

        ActionInputTransData input2 = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        input2.setTitle(transType.getTransName());
        input2.setInfoTypeSale(context.getString(R.string.prompt_input_ntb), EInputType.NUM, 12, false);
        bind(State.INPUT_NTB.toString(), input2);

        // Input Tip amount when supporting TIP, add by richard 20170411
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind(State.ENTER_TIP.toString(), tipAmountAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_mpn_g2),
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        transType.getTransName(), mapData);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);

        // 签名action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.CLSS_PREPROC.toString());
//        gotoState(State.INPUT_BILLING.toString());

    }

    protected enum State {
        INPUT_BILLING,
        INPUT_NTB,
        CHECK_CARD,
        CHECK_CARD_NORMAL,
        ENTER_TIP,
        ENTER_PIN,
        ONLINE,         //1. inquiry
        EMV_PROC,       //2. payment
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        SIGNATURE,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(FinancialApplication.getEmv(), transType, true, false);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55Dup));
            }
            //fall back treatment
            if (transData.getIsFallback()) {
                ActionSearchCard action = (ActionSearchCard) getAction(State.CHECK_CARD.toString());
                action.setMode(SearchMode.SWIPE);
                action.setUiType(ESearchCardUIType.DEFAULT);
                gotoState(State.CHECK_CARD.toString());
                return;
            }
        }
        if ((state != State.SIGNATURE) || (state != State.DETAIL_TRANSAKSI) || (state != State.EMV_PROC)) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_NTB:
                afterInputNtb(result);
                break;
            case INPUT_BILLING:
                afterInput(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case CHECK_CARD_NORMAL:
                onCheckCardNormal(result);
                break;
            case ENTER_TIP:  //add by richard 20170412. input tip in sale transaction
                onEnterTip(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                onOnline(result);
                break;
            case EMV_PROC: // EMV follow-up processing

                if (transData.getReason().equals(TransData.REASON_NO_RECV)){
                    transData.setPrintTimeout("y");
                    toSignOrPrint();
                }else {
                    afterEmvNormal(result);
                }

                /*if (trans==4){
                    afterEmvNormal(result);
                }else {
//                    afterEmv(result);
                    afterEmvNormal(result);
                }*/
                break;
            case CLSS_PREPROC:
                if (trans == 4) {
                    gotoState(State.CHECK_CARD_NORMAL.toString());
                } else {
                    gotoState(State.CHECK_CARD.toString());
                }
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI:
                onDetailDisplay(result);
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    private void parsingf48() {

        transData.setReprintData(transData.getField48());
        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);
        String biayaAdmin = transData.getField28();

        Currency currency = FinancialApplication.getSysParam().getCurrency();

        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);


        if (transData.getTransType().equals(ETransType.DIRJEN_PAJAK_INQUIRY.toString())) {
            parsingDJP();
        } else if (transData.getTransType().equals(ETransType.DIRJEN_BEA_CUKAI_INQUIRY.toString())) {
            parsingDJBC();
        } else if (transData.getTransType().equals(ETransType.DIRJEN_ANGGARAN_INQUIRY.toString())) {
            parsingDJA();
        }

        mapData.put(context.getString(R.string.detail_jumlah_bayar), amount);

        if (!TextUtils.isEmpty(biayaAdmin)){
            String fee = currency.getName() + " " + FinancialApplication.getConvert()
                    .amountMinUnitToMajor(biayaAdmin, currency.getCurrencyExponent(), true);

            mapData.put(context.getString(R.string.detail_admin), fee);
        }
    }

    private void parsingDJBC() {

        String data = transData.getField48();
        //String data = "PDI TRESNO, PT.                                   013104757623000     09005175                        2012-11-07070600";
        /*Nama Wajib Bayar An..50
        ID Wajib Bayar An..20
        Jenis Dokumen An..2
        Nomor Dokumen An..30
        Tanggal Dokumen An..10
        Kode KPPBC An..6*/

        String nama = data.substring(0, 50).trim();
        String id = data.substring(50, 70).trim();
        String jenisDoc = data.substring(70, 72);
        String noDoc = data.substring(72, 102);
        String tgl = data.substring(102, 112);
        String kppbc = data.substring(112, 118);

        mapData.put(context.getString(R.string.detail_kode_billing), transData.getBillingId());
        mapData.put(context.getString(R.string.detail_nama), nama);
        mapData.put(context.getString(R.string.detail_jenis_dok), jenisDoc);
        mapData.put(context.getString(R.string.detail_tgl_dok), tgl);
        mapData.put(context.getString(R.string.detail_kppbc), kppbc);
        //mapData.put(context.getString(R.string.detail_jumlah_bayar), amount);

    }

    private void parsingDJA() {

        String data = transData.getField48();
        //String data = "PEMEGANG KAS DINAS BINA MARGA                     01303409257";
        /*Nama Wajib Bayar An..50
        K/L An..3
        Unit Eselon I An..2
        Kode Satker An..6*/

        String nama = data.substring(0, 50).trim();
        String kl = data.substring(50, 53);
        String eselon = data.substring(53, 55);
        String satker = data.substring(55, 61);

        mapData.put(context.getString(R.string.detail_kode_billing), transData.getBillingId());
        mapData.put(context.getString(R.string.detail_nama), nama);
        mapData.put(context.getString(R.string.detail_lembaga), kl);
        mapData.put(context.getString(R.string.detail_eselon), eselon);
        mapData.put(context.getString(R.string.detail_satker), satker);

    }

    private void parsingDJP() {
        /*len respon bit48 djp = 165
        String data = "262075104045000MARGI L'ARD                                       JL CILINCING LAMA NO.3,JAKARTA UTARA              41121110011112013000000000000000                  ";*/
        String data = transData.getField48();
        Log.d("teg", "resp bit48 : " + data.length());

        try {
            String npwp = data.substring(0, 15);
            String nama = data.substring(15, 65).trim();
            String alamat = data.substring(65, 115).trim();
            String akun = data.substring(115, 121);
            String jenisSetoran = data.substring(121, 124);
            String masaPajak = data.substring(124, 132);
            String noSK = data.substring(132, 147);
            String nop = data.substring(147, 165);

            Date date1;
            SimpleDateFormat sdf1 = new SimpleDateFormat("ddMMyyyy");
            date1 = sdf1.parse(masaPajak);
            sdf1.applyPattern("dd-MM-yyyy");
            masaPajak = sdf1.format(date1);

            mapData.put(context.getString(R.string.detail_kode_billing), transData.getBillingId());
            mapData.put(context.getString(R.string.detail_kode_npwp), npwp);
            mapData.put(context.getString(R.string.detail_nama), nama);
            mapData.put(context.getString(R.string.detail_masa_pajak), masaPajak);
            mapData.put(context.getString(R.string.detail_nop), nop);
            mapData.put(context.getString(R.string.detail_jenis_setoran), jenisSetoran);
            //mapData.put(context.getString(R.string.detail_jumlah_bayar), amount);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {

        if (transData.getHasPin()) {
            transData.setSignFree(true);
            gotoState(State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }

        /*transData.setFeeTotalAmount(transData.getField28());
        transData.updateTrans();*/
    }


    protected void afterInputNtb(ActionResult result) {
        String ntb = ((String) result.getData());
        transData.setNtb(ntb);
        gotoState(State.ONLINE.toString());
    }

    protected void afterInput(ActionResult result) {
        String billingId = ((String) result.getData());
        transData.setBillingId(billingId);

        if (trans == 4) {
            gotoState(State.INPUT_NTB.toString());
        } else {
            gotoState(State.ONLINE.toString());
        }

    }

    protected void onCheckCardNormal(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if (mode == SearchMode.INSERT) {
                gotoState(State.EMV_PROC.toString());
            } else {
                gotoState(State.ENTER_PIN.toString());
            }
        } else {
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理

            //gotoState(State.ACCOUNT_LIST.toString());

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.INPUT_BILLING.toString());
                return;
            }
            // 输密码
            gotoState(State.ENTER_PIN.toString());
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void onCheckCard(ActionResult result) {

        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) {
                if (mode == SearchMode.INSERT) {
                    //gotoState( State.EMV_PROC.toString());
                    transData.setPan(cardInfo.getPan());
                    transData.setTrack2(cardInfo.getTrack2());
                    gotoState(State.ENTER_PIN.toString());
                } else {

                    gotoState(State.ENTER_PIN.toString());
                }
            } else {
                //input tip
                gotoState(State.ENTER_TIP.toString());
            }
        } else { // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void onEnterTip(ActionResult result) {
        //get enter mode
        int enterMode = transData.getEnterMode();
        //save tip amount
        String tipAmount = ((String) result.getData()).replace(",", "");
        long longTipAmount = Long.parseLong(tipAmount);
        long longAmount = Long.parseLong(transData.getAmount());

        if ((longTipAmount * 100) > (longAmount * Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TIP_RATE)))) {
            Device.beepErr();
            ToastUtils.showMessage(context, context.getString(R.string.prompt_amount_over_limit));
            gotoState(State.ENTER_TIP.toString());
            return;
        } else {
            transData.setTipAmount(tipAmount);
            transData.setAmount(String.valueOf(longAmount + longTipAmount));
        }
        if (enterMode == EnterMode.INSERT) {
            gotoState(State.EMV_PROC.toString());
        } else {
            gotoState(State.ENTER_PIN.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState(State.INPUT_BILLING.toString());
    }


    protected void onOnline(ActionResult result) {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        int er = validate();
        if (er != 0) {
            transEnd(new ActionResult(er, null));
            return;
        } else {
            parsingf48();
            transData.setTransNo(transData.getTransNo() + 1);
            gotoState(State.DETAIL_TRANSAKSI.toString());
        }

    }

    private int validate() {
        int err = 0;
        String f48 = transData.getField48();
        int len = f48.length();

        if (TextUtils.isEmpty(f48)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
            return err;
        }

        if (transData.getTransType().equals(ETransType.DIRJEN_PAJAK_INQUIRY.toString())) {
            if (len < 165) {
                err = TransResult.ERR_INVALID_RESPONSE_DATA;
            }
        } else if (transData.getTransType().equals(ETransType.DIRJEN_BEA_CUKAI_INQUIRY.toString())) {
            if (len < 118) {
                err = TransResult.ERR_INVALID_RESPONSE_DATA;
            }
        } else if (transData.getTransType().equals(ETransType.DIRJEN_ANGGARAN_INQUIRY.toString())) {
            if (len < 61) {
                err = TransResult.ERR_INVALID_RESPONSE_DATA;
            }
        }

        return err;
    }

    protected void afterEmvNormal(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理

            //di EmvBaseListenerImpl kalo timeout return approve
            if (!transData.getResponseCode().equals("00")) {
                transData.setPrintTimeout("y");
                toSignOrPrint();
            } else {
                String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
                if (SysParam.Constant.YES.equals(isIndopayMode))
                    transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

                transData.setReprintData(transData.getField48());
                transData.setFeeTotalAmount(transData.getField28());

                //gak dipake, spek fase 2
            /*if (transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69")){
                transData.setPrintTimeout("y");
            }else {
                transData.setPrintTimeout("n");
            }*/

                transData.saveTrans();

                toSignOrPrint();
            }

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            // 输密码
            gotoState(State.ENTER_PIN.toString());
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssProcess(ActionResult result) {
        CTransResult transResult = (CTransResult) result.getData();
        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED ||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(), transData);
        // 写交易记录
        transData.saveTrans();

        if (transResult.getCvmResult() == ECvmResult.SIG) {
            //do signature after online
            transData.setSignFree(false);
            transData.setPinFree(true);
        } else {
            transData.setSignFree(true);
            transData.setPinFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult.getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setIsOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
            gotoState(State.ONLINE.toString());
        }
    }

    protected void onDetailDisplay(ActionResult result) {
        // 电子现金交易无需签名
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
        } else {
            transData.setSignFree(false);
        }

        if (transData.getTransType().equals(ETransType.DIRJEN_PAJAK_INQUIRY.toString())) {
            transData.setTransType(ETransType.DIRJEN_PAJAK.toString());
        } else if (transData.getTransType().equals(ETransType.DIRJEN_BEA_CUKAI_INQUIRY.toString())) {
            transData.setTransType(ETransType.DIRJEN_BEA_CUKAI.toString());
        } else if (transData.getTransType().equals(ETransType.DIRJEN_ANGGARAN_INQUIRY.toString())) {
            transData.setTransType(ETransType.DIRJEN_ANGGARAN.toString());
        }

        gotoState(State.EMV_PROC.toString());
    }

    protected void onSignature(ActionResult result) {

        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

}
