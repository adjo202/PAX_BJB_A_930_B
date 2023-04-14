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
import com.pax.pay.trans.action.ActionInputBPJSTkRegister;
import com.pax.pay.trans.action.ActionInputBPJSTkVerification;
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
import com.pax.pay.trans.model.BPJSTkData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class BPJSTkPendaftaranTrans extends BaseTrans {

    private static final String TAG = "BPJSTkPendaftaranTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;
    //private String cancelationContent;
    private boolean isFreePin;
    private boolean isSupportBypass = true;
    private int trans;

    private LinkedHashMap<String, String> mapData = new LinkedHashMap<>();
    private LinkedHashMap<String, String> mapDataAfterReg = new LinkedHashMap<>();



    public BPJSTkPendaftaranTrans(Context context, Handler handler, String amount, boolean isFreePin, ETransType tipeTransaksi, int trans,
                                  TransEndListener transListener) {
        super(context, handler, tipeTransaksi, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
        transType = tipeTransaksi;
        this.trans = trans;
    }



    protected enum State {
        INPUT_BILLING,
        INPUT_VERIFICATION,
        CHECK_CARD,
        CHECK_CARD_NORMAL,
        ENTER_TIP,
        ENTER_PIN,
        PRE_ONLINE,                 //0. Verification to BPJS
        ONLINE,                     //1. Registration to BPJS
        OVERBOOKING,                //2. Overbooking to BJB
        ONLINE_BPJS_PAYMENT,        //3. Payment to BPJS
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        DETAIL_TRANSAKSI_AFTER_REGISTER,
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
        searchCardAction.setTitle(context.getString(R.string.detail_bpjs_pendaftaran));
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
        ActionInputBPJSTkVerification inputVerification = new ActionInputBPJSTkVerification(handler,
                com.pax.pay.trans.action.ActionInputBPJSTkVerification.INFO_TYPE_SALE, null, trans);
        //inputVerification.setTitle(transType.getTransName());
        inputVerification.setTitle(context.getString(R.string.detail_bpjs_pendaftaran));

        inputVerification.setInfoTypeSale(context.getString(R.string.prompt_input_bpjs_tk_nik),
                com.pax.pay.trans.action.ActionInputBPJSTkVerification.EInputType.NUM, 15, false);
        bind(State.INPUT_VERIFICATION.toString(), inputVerification);

        ActionInputBPJSTkRegister inputId = new ActionInputBPJSTkRegister(handler,
                com.pax.pay.trans.action.ActionInputBPJSTkRegister.INFO_TYPE_SALE, null, transData);
        inputId.setTitle(context.getString(R.string.detail_bpjs_pendaftaran));
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_bpjs_tk_nik), com.pax.pay.trans.action.ActionInputBPJSTkRegister.EInputType.NUM, 15, false);
        bind(State.INPUT_BILLING.toString(), inputId);


        // Input Tip amount when supporting TIP, add by richard 20170411
        /*
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind(State.ENTER_TIP.toString(), tipAmountAction);
        */

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


        ActionEmvProcess emvProcessAction2 = new ActionEmvProcess(handler, transData);
        bind(State.OVERBOOKING.toString(), emvProcessAction2);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        //verification
        ActionTransOnline transOnlineAction0 = new ActionTransOnline(transData);
        bind(State.PRE_ONLINE.toString(), transOnlineAction0);

        // registration
        ActionTransOnline transOnlineAction1 = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction1);

        // payment
        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind(State.ONLINE_BPJS_PAYMENT.toString(), transOnlineAction2);


        //Sandy : Deprecated, no need to preview locally
        /*
        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.detail_bpjs_pendaftaran), mapData);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);
        */

        ActionDispTransDetailVertical dispTransDetailAfterReg = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.detail_bpjs_pendaftaran), mapDataAfterReg);
                TransContext.getInstance().setCurrentAction(action);
            }
        });

        dispTransDetailAfterReg.setInletType(2);
        bind(State.DETAIL_TRANSAKSI_AFTER_REGISTER.toString(), dispTransDetailAfterReg);

        // signature action
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
            case INPUT_BILLING:
                afterInput(result);
                break;
            case INPUT_VERIFICATION:
                afterVerification(result);
                break;
            case PRE_ONLINE:
                afterPreOnline(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case CHECK_CARD_NORMAL:
                onCheckCardNormal(result);
                break;
            /*
                case ENTER_TIP:
                onEnterTip(result);
                break;
             */
            case ENTER_PIN:
                onEnterPin(result);
                break;
            case ONLINE:
                onAfterRegistration(result);
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
            case DETAIL_TRANSAKSI_AFTER_REGISTER:
                int ret = result.getRet();
                if (ret == TransResult.SUCC) {
                    String success = (String) result.getData();
                    if(success.contains("back"))
                        gotoState(State.INPUT_BILLING.toString());
                    else
                        onDetailDisplayAfterRegister(result);
                }
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

    private JSONObject getF48() {

        String F48 = transData.getField48();
        try {
            return new JSONObject(F48);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Sandy : Determine if electronic signature or printing is required
    protected void toSignOrPrint() {

        if (transData.getHasPin()) {
            transData.setSignFree(true);
            gotoState(State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }

    }



    protected void afterVerification(ActionResult result) {
        BPJSTkData bpjsTkData = ((BPJSTkData) result.getData());
        transData.setTransType(ETransType.BPJS_TK_VERIFICATION.toString());
        JSONObject json = new JSONObject();
        try {
            json.put("nik",             bpjsTkData.getNik());
            json.put("nama",            bpjsTkData.getCustomerName());
            json.put("nomorHandphone",  bpjsTkData.getHp());
            json.put("tanggalLahir",    bpjsTkData.getBirthDate());
            transData.setField48(json.toString());
            gotoState(State.PRE_ONLINE.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    protected void afterPreOnline(ActionResult result) {
        gotoState(State.INPUT_BILLING.toString());

    }

     protected void afterInput(ActionResult result) {
        BPJSTkData bpjsTkData = ((BPJSTkData) result.getData());

        //displaying on screen
        mapData.put("NIK",              bpjsTkData.getNik());
        mapData.put("Nama",             bpjsTkData.getCustomerName());
        mapData.put("Alamat",           bpjsTkData.getAddress());
        mapData.put("Email",            bpjsTkData.getEmail());
        mapData.put("Tanggal Lahir",    bpjsTkData.getBirthDate());
        mapData.put("Jenis Pekerjaan",  bpjsTkData.getJobType());

        if(bpjsTkData.getJobType2() != null)
            mapData.put("Jenis Pekerjaan 2",  bpjsTkData.getJobType2());

        mapData.put("Nomor Handphone",  bpjsTkData.getHp());
        mapData.put("Upah",             bpjsTkData.getFormattedSalary());
        mapData.put("Program",          bpjsTkData.getFormatProgram());
        mapData.put("Periode Iuran",    bpjsTkData.getMonthProgram());
        mapData.put("Lokasi Pekerjaan", bpjsTkData.getJobLocation());
        mapData.put("Lokasi BPJS",      bpjsTkData.getBPJSLocation());
        mapData.put("Jam Awal",         bpjsTkData.getStartTime());
        mapData.put("Jam Akhir",        bpjsTkData.getEndTime());

        try{
            //Sandy : for sending to BPJS API
            JSONObject json = new JSONObject();
            json.put("nik",             bpjsTkData.getNik());
            json.put("nomorHandphone",  bpjsTkData.getHp());
            json.put("nama",            bpjsTkData.getCustomerName());
            json.put("email",           bpjsTkData.getEmail());
            json.put("tanggalLahir",    bpjsTkData.getBirthDate());
            json.put("jenisPekerjaan",  bpjsTkData.getJobTypeCode());
            if(bpjsTkData.getJobTypeCode2() != null)
                json.put("jenisPekerjaan2",  bpjsTkData.getJobTypeCode2());
            json.put("alamat",          bpjsTkData.getAddress());
            json.put("lokasiBPJS",      bpjsTkData.getBPJSLocationCode());
            json.put("lokasiPekerjaan", bpjsTkData.getJobLocationCode());
            json.put("jamAwal",         bpjsTkData.getStartTime());
            json.put("jamAkhir",        bpjsTkData.getEndTime());
            json.put("program",         bpjsTkData.getFormatProgramCode());
            json.put("salary",          bpjsTkData.getSalary());
            json.put("periode",         bpjsTkData.getMonthProgramCode());
            transData.setField48(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

         //Sandy : no need to preview data DETAIL_TRANSAKSI will be deprecated
         //gotoState(State.DETAIL_TRANSAKSI.toString());
         transData.setTransType(ETransType.BPJS_TK_PENDAFTARAN.toString());
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
        gotoState(State.INPUT_VERIFICATION.toString());

    }


    protected void onAfterOverbooking(ActionResult result) {
        String f48 = FinancialApplication.getSysParam().get(SysParam.BPJS_PAYMENT_REQUEST_DATA);
        ETransResult transResult = (ETransResult) result.getData();
        transData.setTransType(ETransType.BPJS_TK_PEMBAYARAN.toString());
        transData.setField48(f48);
        gotoState(State.ONLINE_BPJS_PAYMENT.toString());

    }




    protected void onAfterRegistration(ActionResult result) {
        String F63 = transData.getField63();
        //for printing
        //transData.setReprintData(F63);
        String[] F63s = F63.split("\\|");

        for(String row : F63s){
            if(row.contains(":")){
                String[] informations = row.split(":");
                mapDataAfterReg.put(informations[0],informations[1]);
            }
        }
        //payment info need to be
        String f48 = transData.getField48();
        FinancialApplication.getSysParam().set(SysParam.BPJS_PAYMENT_REQUEST_DATA,f48);
        gotoState(State.DETAIL_TRANSAKSI_AFTER_REGISTER.toString());

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

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

            //Sandy : in order to change into BPJS TK Pembayaran, we should change the transType
            transData.setTransType(ETransType.BPJS_TK_PENDAFTARAN.toString());

            //Sandy : delete the Duplicate record if its already succeed
            transData.setPrintTimeout("N");
            transData.saveTrans();
            TransData.deleteDupRecord();
            gotoState(State.PRINT_TICKET.toString());
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

    protected void onDetailDisplay(ActionResult result) {
        //transData.setAmount();
        transData.setTransType(ETransType.BPJS_TK_PENDAFTARAN.toString());
        gotoState(State.ONLINE.toString());
    }

    protected void onDetailDisplayAfterRegister(ActionResult result){

        JSONObject info = getF48();
        try{

            String nik = info.getString("nik");
            String bpjsProductCode = "BPJSTKREGISTER";

            //amount and fee is already got from its server
            transData.setPhoneNo(nik);
            transData.setField47(String.format("111111111#22222#%s", bpjsProductCode));
            transData.setFeeTotalAmount(transData.getField28());
            transData.setTransType(ETransType.BPJS_OVERBOOKING.toString());
            gotoState(State.OVERBOOKING.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }




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
