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
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionChooseAccountList;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputPDAMData;
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

import static com.pax.pay.utils.Controllers.PDAM;
import static com.pax.pay.utils.Controllers.PULSA;


public class PDAMTrans extends BaseTrans {

    private static final String TAG = "PDAMTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;
    String f48 = "";
    ProductData productData;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    private LinkedHashMap<String, String> mapData = new LinkedHashMap<>();

    public PDAMTrans(Context context, Handler handler,boolean isFreePin,TransEndListener transListener) {
        super(context, handler, ETransType.PDAM_INQUIRY, transListener);
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        ActionInputPDAMData enterInfosAction = new ActionInputPDAMData(handler, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_pdam));
        bind(State.INPUT_DATA.toString(), enterInfosAction);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(context.getString(R.string.trans_pdam));
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


        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_pdam),
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

        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind(State.SEND_PURCHASE.toString(), transOnlineAction2);

        ActionTransOnline transOnlineAction3 = new ActionTransOnline(transData);
        bind(State.SEND_OVERBOOKING.toString(), transOnlineAction3);

        ActionTransOnline transOnlineAction4 = new ActionTransOnline(transData);
        bind(State.SEND_INQUIRY.toString(), transOnlineAction4);

        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(), transData, transType.getTransName(), "Pilih Rekening");
        bind(State.ACCOUNT_LIST.toString(), accountListAction);

        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetail) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_detail), prepareDisp(transData.getField63()));
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

    }

    protected enum State {
        //DISP,
        //ENTER_AMOUNT,
        INPUT_DATA,
        CHECK_CARD,
        ENTER_PIN,
        ONLINE,
        SEND_PURCHASE,      //1
        SEND_OVERBOOKING,
        SEND_INQUIRY,       //3
        ACCOUNT_LIST,
        EMV_PROC,
        OVERBOOKING,        //2
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        SIGNATURE,
        PRINT_TICKET
    }

    private String cnvrt(String a) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        if (TextUtils.isEmpty(a)) a = "0";
        return currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(a, currency.getCurrencyExponent(), true);
    }

    private LinkedHashMap<String, String> prepareDisp(String data) {
        LinkedHashMap<String, String> mapData = new LinkedHashMap<>();
        //String data = "PDAMIDPEL    : 222222222222|NAMA         : LIM SUI SIAN|BLN          : SEP18|JML          : 1 BLN|TAG          : 138479|ADMIN        : 2500|TOTAL        : 140979|";
        String[] record = data.split("\\|");

        for (int i = 0; i < record.length; i++) {
            String[] column = record[i].split(":");
            mapData.put(column[0].trim(), column[1].trim());
            /*
            if (column[0].trim().equalsIgnoreCase("TAG") ||
                    column[0].trim().equalsIgnoreCase("ADMIN") ||
                    column[0].trim().equalsIgnoreCase("TOTAL")) {

                mapData.put(column[0].trim(), cnvrt(column[1].trim()));
            } else {
                mapData.put(column[0].trim(), column[1].trim());
            }*/
        }

        return mapData;
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

        if ((state != State.SEND_INQUIRY )) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {

            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
//                onCheckCard2(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                onOnline(result);
                break;
            case SEND_PURCHASE:
                if (result.getRet() == TransResult.SUCC) {
                    if (!StringUtils.isEmpty(transData.getField63())) { //cek field 63 ada isinya gk
                        transData.setReprintData(transData.getField63());
                        transData.setFeeTotalAmount(transData.getField28()); //bit 28 overbooking ngambil dari respon bit 28 payment
                        f48 = transData.getField48().trim();
                        copyData();
                        //transData.setTransNo(transData.getTransNo() + 1);
                        gotoState(State.DETAIL_TRANSAKSI.toString());
                    } else {
                        transEnd(new ActionResult(TransResult.ERR_INVALID_RESPONSE_DATA, null));
                    }
                } else if (result.getRet() == TransResult.ERR_RECV) {
                    transData.setPrintTimeout("y");
                    gotoState(State.PRINT_TICKET.toString());

                } else {
                    transEnd(result);
                }
                break;
            case OVERBOOKING:
                //onOverbooking(result);
                /*if (transData.getResponseCode().equals("95")) {
                    copyData();
                    updatePrice();
                } else {
                    copyData();
                    onOverbooking(result);
                }*/

                if (!TextUtils.isEmpty(transData.getResponseCode())){
                    if (transData.getResponseCode().equals("95")) {
                        copyData();
                        updatePrice();
                    }else{
                        copyData();
                        onOverbooking(result);
                    }
                }else {
                    copyData();
                    onOverbooking(result);
                }
                break;
            case SEND_OVERBOOKING:
                if (result.getRet() == TransResult.SUCC) {
                    transData.setFeeTotalAmount(transData.getField28()); //simpen fee jika overbooking sukses saja
                    transData.setAmount(transData.getAmount().substring(0,10));

                    transData.saveTrans();
                    //transData.setTransNo(transData.getTransNo() + 1);
                    transData.setTransType(ETransType.PDAM_INQUIRY.toString());
                    gotoState(State.SEND_INQUIRY.toString());
                } else {

                    transEnd(result);
                }
                break;
            case SEND_INQUIRY:
                if (result.getRet() == TransResult.SUCC) {
                    if (!TextUtils.isEmpty(transData.getPrintTimeout())){
                        if (transData.getPrintTimeout().equalsIgnoreCase("NP")){
                            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                        }else {
                            transData.setReprintData(transData.getField63());
                            transData.setPrintTimeout("N");
                            transData.setAmount(transData.getAmount().substring(0,10));
                            copyData();
                            //transData.saveTrans();
                            transData.updateTrans();
                            gotoState(State.PRINT_TICKET.toString());
                        }
                    }else {
                        transData.setReprintData(transData.getField63());
                        transData.setPrintTimeout("N");
                        transData.setAmount(transData.getAmount().substring(0,10));
                        copyData();
                        //transData.saveTrans();
                        transData.updateTrans();
                        gotoState(State.PRINT_TICKET.toString());
                    }

                } else if (result.getRet() == TransResult.ERR_RECV) {
                    transData.setPrintTimeout("Y");
                    copyData();
                    transData.updateTrans();
                    gotoState(State.PRINT_TICKET.toString());
                } else {
                    transEnd(result);
                }
                break;
            case EMV_PROC: // EMV follow-up processing
                onEmvProc(result);
//                afterEmv(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI:
                if (result.getRet()==TransResult.ERR_ABORTED){
                    transEnd(result);
                }else {
                    transData.setTransType(ETransType.PDAM_OVERBOOKING.toString());
                    //gotoState(State.SEND_OVERBOOKING.toString());
                    gotoState(State.OVERBOOKING.toString());
                }
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case INPUT_DATA:
                onInputData(result);
                break;

            case ACCOUNT_LIST:
                //toSignOrPrint();
                String accNo = ((String) result.getData().toString());
                String[] no = transData.getAccNo().split("#");
                String[] type = transData.getAccType().split("#");
                int i = no.length;
                int a = type.length;
                for (int n = 0; n < no.length; n++) {
                    if (no[n].equals(accNo)) {
                        transData.setAccNo(no[n]);
                        transData.setAccType(type[n]);
                        transData.setTransNo(transData.getTransNo() + 1);
                        transData.setTransType(ETransType.E_SAMSAT_INQUIRY.toString());
                        gotoState(State.INPUT_DATA.toString());
                        //gotoState(State.ONLINE2.toString()); //buat test
                    }
                }

                break;

            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    private void copyData(){
        String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                + Component.getPaddedString(productData.getProductId(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);

        transData.setField47(dt48+ "#" +                     //0
                transData.getPhoneNo() + "#" +              //1
                productData.getProductId()+ "#" +           //2
                "PDAM" + "#" +                              //3
                productData.getOperator()+ "#" +            //4
                productData.getProductDescription()+ "#" +  //5
                productData.getProductName()                //6
        );
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
            //int res = Controllers.updateProductDataById(PDAM, productData);
            int res = Controllers.updateProductDataById(productData);
            Log.d("teg", "res " + res);
        }

        transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
    }

    protected void onEnterAmount(ActionResult result) {
        String amount = "";
        if (result.getData() != null) {
            amount = ((String) result.getData()).replace(",", "");
        } else {
            amount = "0";
        }

        ActionSearchCardCustom action = (ActionSearchCardCustom) getAction(State.CHECK_CARD.toString());
        action.setAmount(amount);

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode)) {
            transData.setAmount(String.format("%s00", amount));
        } else {
            transData.setAmount(amount);
        }
        gotoState(State.INPUT_DATA.toString());
    }

    private String paddingKiri(String s, int n) {
        return StringUtils.leftPad(s, n, " ");
    }


    //kodeBayar, productData.getProductId(), kode
    protected void onInputData(ActionResult result) {
        String[] res = (String[]) result.getData();
        String kodeBayar = res[0];      //ex : 222222222222
        String productId = res[1];      //ex : 7c70ac66b
        String productCode = res[2];    //ex : PDAMKOBDG --> gak dipake
        String fee = res[3];
        String basePrice= res[4];
        String productDesc = res[5];
        String prodName = res[6];
        String operator = res[7];

        productData = new ProductData();
        productData.setProductId(productId);
        transData.setProduct_code(productId);
        productData.setProductDescription(productDesc);
        productData.setBasePrice(basePrice);
        productData.setFee(fee);
        productData.setProductName(prodName);
        productData.setOperator(operator);

        //transData.setReprintData(kodeBayar+"#"+productId+"#"+fee+"#"+basePrice+"#"+productDesc);

        transData.setFeeTotalAmount(fee);
        //transData.setField47("0"+"#"+"0"+"#"+productCode);
        transData.setField47("0" + "#" + kodeBayar + "#" + productId); //bit 56 overbooking //ngikutin flow pulsa
        transData.setPhoneNo(kodeBayar); //ngikutin
        transData.setField48(genBit48(kodeBayar, productId));
        transData.setTransType(ETransType.PDAM_PURCHASE.toString());
        gotoState(State.SEND_PURCHASE.toString());
    }

    private String genBit48(String s1, String s2) {
        if (StringUtils.isEmpty(s1)) {
            s1 = "";
        }

        if (StringUtils.isEmpty(s2)) {
            s2 = "";
        }

        String dt48 = Component.getPaddedString(s1, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                + Component.getPaddedString(s2, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);

        return dt48;
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
        transData.updateTrans();
    }


    protected void onCheckCard(ActionResult result) {
        //transData.setTransType(ETransType.ACCOUNT_LIST.toString());

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
            } /*else {
                //input tip
                gotoState(State.ENTER_TIP.toString());
            }*/
        } else { // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
        }
    }


    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        //gotoState(State.ONLINE.toString());
        gotoState(State.INPUT_DATA.toString());
    }

    protected void onOnline(ActionResult result) {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

        gotoState(State.ACCOUNT_LIST.toString());
    }


    protected void onEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {


            if (transResult == ETransResult.ONLINE_APPROVED) {
                toSignOrPrint();
                return;
            }

        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void onOverbooking(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {

            transData.setFeeTotalAmount(transData.getField28()); //simpen fee jika overbooking sukses saja
            transData.setAmount(transData.getAmount().substring(0,10));
            transData.saveTrans();
            //transData.setTransNo(transData.getTransNo() + 1);
            transData.setTransType(ETransType.PDAM_INQUIRY.toString());
            gotoState(State.SEND_INQUIRY.toString());

        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
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
            toSignOrPrint();
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
