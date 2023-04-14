/*
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
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionDispTransDetailVertical;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.Controllers;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;

import static com.pax.pay.utils.Controllers.PULSA;

public class PulsaDataTransNew extends BaseTrans {

    private static final String TAG = "PulsaDataTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount, selPrice, phone, fee, kd_produk, produk_name, desc, operator, tipe;

    private boolean isFreePin;
    private boolean isSupportBypass = true;
    String x = "";

    public PulsaDataTransNew(Context context, Handler handler, String amount, String selPrice, String fee, String phone, String kd_product, String produk_name, String desc,
                          String operator, String tipePr, boolean isFreePin,
                          TransEndListener transListener) {
        super(context, handler, ETransType.OVERBOOKING_PULSA_DATA, transListener);
        //request Account list first
        this.amount = amount;
        this.selPrice = selPrice;
        this.fee = fee;
        this.phone = phone;
        this.kd_produk = kd_product;
        this.produk_name = produk_name;
        this.desc = desc;
        this.operator = operator;
        this.tipe = tipePr; //mode
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (amount != null && amount.length() > 0) {
            //Sandy :
            //modifying 2 digits (00) decimal point
            //Long amtl = Long.parseLong(amount) + Long.parseLong(fee);
            //amount = String.valueOf(amtl);
            if(SysParam.Constant.YES.equals(isIndopayMode)) {
                transData.setAmount(amount.replace(",", "")+"00");
            } else {
                transData.setAmount(amount.replace(",", ""));
            }
//            transData.setAmount(amount.replace(",", ""));
            transData.setSellPrice(selPrice.replace(",", ""));
        }

        if (phone != null && phone.length() > 0) {

            transData.setPhoneNo(phone);
            transData.setFeeTotalAmount(fee);
            transData.setProduct_code(kd_produk);
            transData.setProduct_name(produk_name);
            transData.setKeterangan(desc);
            transData.setOperator(operator);
            transData.setTypeProduct(tipe);

            if (transData.getProduct_code().contains("-")) {
                String[] temp = transData.getProduct_code().split("-");
                x = temp[0];
                String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                        + Component.getPaddedString(x, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                transData.setField48(dt48);

                transData.setField47(transData.getField48() + "#" + //222222222222           da121bb37
                        transData.getPhoneNo() + "#" +              //222222222222
                        transData.getProduct_code() + "#" +         //da121bb37-PLN20A
                        transData.getTypeProduct() + "#" +          //pln //mode
                        transData.getOperator() + "#" +             //PLN
                        transData.getKeterangan() + "#" +           //PLN 20000
                        transData.getProduct_name() + "#" +         //PLN 20000
                        transData.getField63());                    //NULL

                Log.d("teg", "47 [1] - "+transData.getField47());
            } else {
                String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                        + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                transData.setField48(dt48);
                transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                        transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                        transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                        "#" + transData.getField63());

                Log.d("teg", "47 [2] - "+transData.getField47());
            }
        }

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(null);
        searchCardAction.setTitle(context.getString(R.string.trans_prabayar));
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value
        if (amount != null && amount.length() > 0) {
            /*if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getSellPrice().substring(0, transData.getSellPrice().length() - 2));
            else
                searchCardAction.setAmount(transData.getSellPrice());*/
            searchCardAction.setAmount(transData.getSellPrice());
        }

        searchCardAction.setUiType(searchCardMode == SearchMode.TAP ? ESearchCardUIType.QUICKPASS: ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // Input Tip amount when supporting TIP, add by richard 20170411
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind( State.ENTER_TIP.toString(), tipAmountAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin( action -> {
            // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
            if (!isFreePin) {
                isSupportBypass = false;
            }
            ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_prabayar),
                    transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                    context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
        } );
        bind( State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind( State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind( State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind( State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind( State.ONLINE.toString(), transOnlineAction);

        ActionDispTransDetail confirmTrx = new ActionDispTransDetail( new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetail) action).setParam( getCurrentContext(), handler,
                        context.getString(R.string.trans_prabayar), prepareDisp());
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL.toString(), confirmTrx);

        /*ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_prabayar), prepareDisp());
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL.toString(), dispTransDetail);*/

        ActionTransOnline transPurchaseAction = new ActionTransOnline(transData);
        bind( State.ONLINE_PURCHASE.toString(), transPurchaseAction);

        ActionTransOnline transInqAction = new ActionTransOnline(transData);
        bind( State.ONLINE_INQ.toString(), transInqAction);

        // 签名action
        ActionSignature signatureAction = new ActionSignature( action -> ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                Component.genFeatureCode(transData)) );
        bind( State.SIGNATURE.toString(), signatureAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,handler);
        bind( State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 执行的第一action
        gotoState( State.CHECK_CARD.toString());

    }

    protected enum State {
        CHECK_CARD,
        ENTER_TIP,
        ENTER_INFO,
        ENTER_PIN,
        ONLINE, // overbooking pulsa data
        DETAIL, // tambahan detail setelah overbooking
        ONLINE_INQ, // inq
        ONLINE_PURCHASE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        SIGNATURE,
        PRINT_TICKET
    }

    private void updatePrice() {
        Log.d("teg", "updatePrice");
        String productId = "", basePrice = "", sellPrice = "", fee = "";
        String f56 = transData.getNtb(); //minjem variabel, isinya field 56 resp overbooking --> 081280808080|ca70d3281
        if (f56.contains("|")) {
            String[] temp = f56.split("\\|");
            productId = temp[1];

            basePrice = transData.getAmount().substring(0, 10);
            Long base = Long.parseLong(basePrice);
            basePrice = String.valueOf(base);

            fee = transData.getField28();
            Long feeL = Long.parseLong(fee);
            fee = String.valueOf(feeL);

            Log.d("teg", "updatePrice "+productId+fee+"-"+basePrice);

            ProductData productData = new ProductData(productId, basePrice+"00", sellPrice+"00", fee+"00");
            //int res = Controllers.updateProductDataById( productData);
            int res = Controllers.updateProductDataById(productData);
            Log.d("teg", "res " + res);
        }

        transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        Log.i("abdul", "result = " + result.getRet());
        State state = State.valueOf(currentState);
        Log.i("abdul", "cek respon code transtype " + transData.getTransType() + " = " + transData.getResponseCode());
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(FinancialApplication.getEmv(), transType, true, false);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55Dup));
            }
            //fall back treatment
            if(transData.getIsFallback()){
                ActionSearchCard action = (ActionSearchCard)getAction( State.CHECK_CARD.toString());
                action.setMode(SearchMode.SWIPE);
                action.setUiType(ESearchCardUIType.DEFAULT);
                gotoState( State.CHECK_CARD.toString());
                return;
            }
        }
        if (state != State.SIGNATURE || state != State.EMV_PROC) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            //dia add
            if (state!=State.ONLINE_INQ && state!=State.ONLINE_PURCHASE){
                if (ret != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
            }

            /*if (!currentState.equals( State.ONLINE_PURCHASE.toString() ) || !currentState.equals( State.ONLINE_INQ.toString() )) {

            }*/
        }

        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case ENTER_TIP:  //add by richard 20170412. input tip in sale transaction
                onEnterTip(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                // online overbook pulsa data
                break;
            case EMV_PROC: // EMV follow-up processing
                //onEmvProc(result);

                if (!TextUtils.isEmpty(transData.getResponseCode())){
                    if (transData.getResponseCode().equals("95")) {
                        updatePrice();
                    }else{
                        onEmvProc(result);
                    }
                }else {
                    onEmvProc(result);
                }

                /*if (transData.getResponseCode().equals("95")) {
                    updatePrice();
                } else {
                    onEmvProc(result);
                }*/
                break;
            case ONLINE_PURCHASE:
                transData.setPhoneNo(phone);
                transData.setFeeTotalAmount(fee);
                transData.setProduct_code(kd_produk);
                transData.setProduct_name(produk_name);
                transData.setKeterangan(desc);
                transData.setOperator(operator);
                transData.setTypeProduct(tipe);
                transData.setTransType(ETransType.INQ_PULSA_DATA.toString());

                /*String nama = "";
                if (!StringUtils.isEmpty(transData.getField59())){
                    nama = transData.getField59();
                }*/

                if (transData.getProduct_code().contains("-")) {
                    String[] temp = transData.getProduct_code().split("-");
                    x = temp[0];
                    String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                            + Component.getPaddedString(x, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);

                    transData.setField48(dt48);

                    transData.setField47(transData.getField48() + "#" +
                            transData.getPhoneNo() + "#" +
                            transData.getProduct_code() + "#" +
                            transData.getTypeProduct() + "#" +
                            transData.getOperator() + "#" +
                            transData.getKeterangan() + "#" +
                            transData.getProduct_name() + "#" +
                            transData.getField63());

                    Log.d("teg", "47 [3] - "+transData.getField47());
                } else {
                    String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                            + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);

                    transData.setField48(dt48);

                    transData.setField47(transData.getField48() + "#" +
                            transData.getPhoneNo() + "#" +
                            transData.getProduct_code() + "#" +
                            transData.getTypeProduct() + "#" +
                            transData.getOperator() + "#" +
                            transData.getKeterangan() + "#" +
                            transData.getProduct_name() + "#" +
                            transData.getField63());

                    Log.d("teg", "47 [4] - "+transData.getField47());
                }

                if (result.getRet() == TransResult.ERR_RECV) {
                    transData.setPrintTimeout("Y");
                    transData.updateTrans();
                    gotoState(State.PRINT_TICKET.toString());
                } else if (result.getRet() == TransResult.SUCC) {
                    transData.updateTrans();
                    //gotoState(State.ONLINE_INQ.toString());
                    Device.enableBackKey(false);
                    gotoState(State.DETAIL.toString());
                } else {
                    transEnd(result);
                }
                break;
            // tambahin detail confirm
            case DETAIL:
                Device.enableBackKey(true);
                gotoState(State.ONLINE_INQ.toString());
                break;
            case ONLINE_INQ:
                if (result.getRet() == TransResult.ERR_RECV) {
                    transData.setPrintTimeout("Y");
                    transData.updateTrans();
                    gotoState(State.PRINT_TICKET.toString());
                } else if (result.getRet() == TransResult.SUCC) {
                    transData.updateTrans();
                    toSignOrPrint();
                } else {
                    transEnd(result);
                }
                break;
            case CLSS_PREPROC:
                gotoState( State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
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

    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {

        transData.setFeeTotalAmount(transData.getField28());
        transData.setSignFree( transData.getHasPin() );
        transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                "#" + transData.getField63());
        transData.updateTrans();
        gotoState(State.PRINT_TICKET.toString());
    }

    protected void onCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if(FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) { //TODO mode 2
                if(mode == SearchMode.INSERT ){
                    gotoState( State.EMV_PROC.toString());
                } else {
                    gotoState( State.ENTER_PIN.toString());
                }
            }else {
                //input tip
                gotoState( State.ENTER_TIP.toString());
            }
        } else{ // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }
    }

    protected void onEnterTip(ActionResult result) {
        //get enter mode
        int enterMode = transData.getEnterMode();
        //save tip amount
        String tipAmount = ((String) result.getData()).replace(",", "");
        long longTipAmount = Long.parseLong(tipAmount);
        long longAmount = Long.parseLong(transData.getAmount());

        if ((longTipAmount * 100) > (longAmount * Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TIP_RATE)))){
            Device.beepErr();
            ToastUtils.showMessage(context, context.getString(R.string.prompt_amount_over_limit));
            gotoState( State.ENTER_TIP.toString());
            return;
        } else {
            transData.setTipAmount(tipAmount);
            transData.setAmount(String.valueOf(longAmount + longTipAmount));
        }
        if (enterMode == EnterMode.INSERT ) {
            gotoState( State.EMV_PROC.toString());
        } else {
            transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
            gotoState( State.ENTER_PIN.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState( State.ONLINE.toString());
    }

    protected void onEmvProc(ActionResult result) {
        // Determine whether the chip card transaction is a complete process or a simple process.
        // If it is a simple process, the next is online processing,
        // and the complete process is followed by a signature
        ETransResult transResult = (ETransResult) result.getData();

        // EMV complete process, offline approval or online approval
        // both enter the signature process
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {
            // Online approval/offline approval processing
            // Write transaction records
            Log.d(TAG,"Sandy.onEmvProc");

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

            //transData.setAmount(transData.getAmount().substring(0, 10)); //tri
            transData.setFeeTotalAmount(transData.getField28());

            transData.saveTrans();
            if (transResult == ETransResult.ONLINE_APPROVED) {
                // set ke purchase pulsa data
                transData.setTransType(ETransType.PURCHASE_PULSA_DATA.toString());
                gotoState(State.ONLINE_PURCHASE.toString());
                return;
            }
            //gotoState( State.BALANCE_DISP.toString());
            gotoState( State.ONLINE.toString());
        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                //gotoState( State.ONLINE.toString());
                // set ke purchase pulsa data
                transData.setTransType(ETransType.PURCHASE_PULSA_DATA.toString()); // set setor tunai
                gotoState(State.ONLINE_PURCHASE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState( State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState( State.ENTER_PIN.toString());
            }
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssProcess(ActionResult result) {
        Log.d(TAG,"Sandy=SaleTrans.afterClssProcess called!");
        CTransResult transResult = (CTransResult)result.getData();
        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(), transData);
        // 写交易记录

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
            toSignOrPrint();
        }
    }

    protected void onSignature(ActionResult result) {

        // 保存签名数据
        /*byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }*/
        gotoState( State.PRINT_TICKET.toString());
    }

    //tri

    private String tambah(String a, String b) {
        if (TextUtils.isEmpty(a)) a = "0";
        if (TextUtils.isEmpty(b)) b = "0";
        return String.valueOf(Long.parseLong(a) + Long.parseLong(b));
    }

    private String cnvrt(String a) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        if (TextUtils.isEmpty(a)) a = "0";
        return currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(a, currency.getCurrencyExponent(), true);
    }

    private LinkedHashMap<String, String> prepareDisp() {
        String nama = transData.getField59();

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        hashMap.put("PHONE NUMBER", phone);
        hashMap.put("OPERATOR", operator.toUpperCase());
        if (!StringUtils.isEmpty(nama)){
            hashMap.put("NAMA PELANGGAN", nama);
        }
        hashMap.put("PRODUCT NAME", produk_name.toUpperCase());
        hashMap.put("PRICE", cnvrt(amount));
        hashMap.put("ADMIN FEE", cnvrt(fee));
        hashMap.put("TOTAL", cnvrt(tambah(amount, fee)));
        String status = "BERHASIL";

        if (!StringUtils.isEmpty(transData.getPrintTimeout())) {
            if (transData.getPrintTimeout().equals("Y")) {
                status = "PENDING";
            }
        }
        hashMap.put("STATUS", status);



        return hashMap;
    }

}
