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
import com.pax.pay.trans.action.ActionInputBPJSTkPayment;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSearchCardCustom;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.BPJSTkData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class BPJSTkPembayaranTrans extends BaseTrans {

    private static final String TAG = "BPJSTkPembayaranTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;
    private boolean isFreePin;
    private boolean isSupportBypass = true;
    private int trans;

    private LinkedHashMap<String, String> mapData = new LinkedHashMap<>();
    private LinkedHashMap<String, String> mapDataAfterReg = new LinkedHashMap<>();



    public BPJSTkPembayaranTrans(Context context, Handler handler, String amount, boolean isFreePin, ETransType tipeTransaksi, int trans,
                                 TransEndListener transListener) {
        super(context, handler, tipeTransaksi, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
        transType = tipeTransaksi;
        this.trans = trans;
    }



    protected enum State {
        INPUT_DATA,
        INPUT_DATA_WITH_COMBO,
        CHECK_CARD,
        CHECK_CARD_NORMAL,
        ENTER_TIP,
        ENTER_PIN,
        ONLINE,                     //1. Inquiry to BPJS
        OVERBOOKING,                //2. Overbooking to BJB
        ONLINE_BPJS_PAYMENT,        //3. Payment to BPJS
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        DETAIL_TRANSAKSI_AFTER_INQUIRY,
        SIGNATURE,
        PRINT_TICKET
    }




    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        //searchCardAction.setTitle(transType.getTransName());
        searchCardAction.setTitle(context.getString(R.string.detail_bpjs_pembayaran));

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

        //first screen after deep a card
        ActionInputBPJSTkPayment inputId = new ActionInputBPJSTkPayment(handler, null, trans);
        inputId.setTitle(context.getString(R.string.detail_bpjs_pembayaran));
        inputId.hideCombobox();
        bind(State.INPUT_DATA.toString(), inputId);

        //second step for displaying combobox
        ActionInputBPJSTkPayment inputId2 = new ActionInputBPJSTkPayment(handler,  null, trans);
        inputId2.setTitle(context.getString(R.string.detail_bpjs_pembayaran));
        inputId2.displayCombobox();
        bind(State.INPUT_DATA_WITH_COMBO.toString(), inputId2);

        // Input Tip amount when supporting TIP, add by richard 20170411
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind(State.ENTER_TIP.toString(), tipAmountAction);

        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
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


        ActionEmvProcess emvProcessAction2 = new ActionEmvProcess(handler, transData);
        bind(State.OVERBOOKING.toString(), emvProcessAction2);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        // 联机action
        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind(State.ONLINE_BPJS_PAYMENT.toString(), transOnlineAction2);


        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        //transType.getTransName(),
                        context.getString(R.string.detail_bpjs_pembayaran),
                        mapData);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);


        ActionDispTransDetailVertical dispTransDetailAfterReg = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        //transType.getTransName()
                        context.getString(R.string.detail_bpjs_pembayaran),
                        mapDataAfterReg);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI_AFTER_INQUIRY.toString(), dispTransDetailAfterReg);




        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.CLSS_PREPROC.toString());
    }


    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
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
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                //Sandy : delete the reversal
                //please go through at com.pax.pay.emv.EmvBaseListenerImpl
                //the reversal deletion should be modify at that file
                if(!transData.getResponseCode().equals("00")){
                    TransData.deleteDupRecord();
                }
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_DATA:
            case INPUT_DATA_WITH_COMBO:
                afterInput(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: //
                onAfterInquiry(result);
                break;
            case ONLINE_BPJS_PAYMENT:
                onAfterPayment(result);
                break;
            case EMV_PROC: // EMV follow-up processing

                if (transData.getReason().equals(TransData.REASON_NO_RECV)){
                    transData.setPrintTimeout("y");
                    toSignOrPrint();
                }else {
                    afterEmvNormal(result);
                }

                break;
            case OVERBOOKING:
                onAfterOverbooking(result);
                break;
            case CLSS_PREPROC:
                FinancialApplication.getSysParam().set(SysParam.BPJS_INQUIRY_REQUEST_DATA,null);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI_AFTER_INQUIRY:
                onDetailDisplayAfterInquiry(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    private JSONObject getF48() {

        String F48 = transData.getField48();
        try {
            return new JSONObject(F48);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
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

    }


    protected void afterInput(ActionResult result) {
        BPJSTkData bpjsTkData = ((BPJSTkData) result.getData());


        mapData.put("NIK",bpjsTkData.getNik());
        mapData.put("Periode Iuran",bpjsTkData.getMonthProgram());

        try{
            JSONObject json = new JSONObject();
            json.put("nik",             bpjsTkData.getNik());
            json.put("periode",         bpjsTkData.getMonthProgramCode());
            transData.setField48(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        transData.setTransType(ETransType.BPJS_TK_INQUIRY.toString());
        gotoState(State.ONLINE.toString());

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


        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.INPUT_DATA.toString());
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
        gotoState(State.INPUT_DATA.toString());
    }


    protected void onAfterOverbooking(ActionResult result) {

        transData.setTransType(ETransType.BPJS_TK_PEMBAYARAN.toString());
        gotoState(State.ONLINE_BPJS_PAYMENT.toString());

    }



    protected void onAfterInquiry(ActionResult result) {

        try {

            JSONObject F48 = getF48();
            int inquiryCounterForm  = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.BPJS_INQUIRY_COUNTER_FORM));
            String f48NIK           = F48.getString("nik");

            String F63 = transData.getField63();
            String[] F63s = F63.split("\\|");

                for(String row : F63s){
                if(row.contains(":")){
                    String[] informations = row.split(":");
                    mapDataAfterReg.put(informations[0],informations[1]);
                }else
                    mapDataAfterReg.put("",row);
                }

            //Sandy : if there is kodeIuran,
            //means there is no outstanding need to be pay
            if(inquiryCounterForm == 1){
                if(F48.has("kodeIuran")){
                    gotoState(State.DETAIL_TRANSAKSI_AFTER_INQUIRY.toString());
                }else{
                    FinancialApplication.getSysParam().set(SysParam.BPJS_INQUIRY_REQUEST_DATA,f48NIK);
                    gotoState(State.INPUT_DATA_WITH_COMBO.toString());
                }
            }else {
                gotoState(State.DETAIL_TRANSAKSI_AFTER_INQUIRY.toString());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    protected void onAfterPayment(ActionResult result){

            if (!transData.getResponseCode().equals("00")) {
                Device.beepErr();
                transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                return;
            }

            String F63 = transData.getField63();
            //for printing
            transData.setReprintData(F63);

            //Sandy : delete the Duplicate record if its already succeed
            transData.setPrintTimeout("N");

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

            transData.saveTrans();
            TransData.deleteDupRecord();
            gotoState(State.PRINT_TICKET.toString());
    }

    private int validate() {
        int err = 0;
        String f48 = transData.getField48();
        if (TextUtils.isEmpty(f48)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
            return err;
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

                transData.setFeeTotalAmount(transData.getField28());

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

    @Deprecated
    protected void onDetailDisplay(ActionResult result) {
        transData.setTransType(ETransType.BPJS_TK_INQUIRY.toString());
        gotoState(State.ONLINE.toString());
    }

    protected void onDetailDisplayAfterInquiry(ActionResult result){

        JSONObject info = getF48();
        try{

            String nik = info.getString("nik");
            String bpjsProductCode = "BPJSTKPAYMENT";
            //amount and fee is already got from its server
            transData.setPhoneNo(nik); //this should be nik and kode_iuran
            transData.setField47(String.format("111111111#22222#%s", bpjsProductCode));
            transData.setFeeTotalAmount(transData.getField28());
            transData.setTransType(ETransType.BPJS_OVERBOOKING.toString());
            gotoState(State.OVERBOOKING.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }



    /*
    protected void onSignature(ActionResult result) {

        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }*/

}
