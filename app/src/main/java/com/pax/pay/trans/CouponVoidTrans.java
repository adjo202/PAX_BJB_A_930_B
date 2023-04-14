/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-7-24 4:59
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionVoidCoupon;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ResponseCode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

/**
 * Created by liliang on 2017/7/24.
 */

public class CouponVoidTrans extends BaseTrans {
    public static final String TAG = "CouponVoidTrans";

    protected TransData origTransData;
    protected TransData origCouponTransData;
    private String origTransNo;
    private boolean isNeedFindOrigTrans = true;
    private boolean isNeedInputTransNo = true;

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        VOID_COUPON,
        SIGNATURE,
        PRINT_TICKET
    }


    public CouponVoidTrans(Context context, Handler handler, TransData origTransData,
    TransEndListener transListener) {
        super(context, handler, ETransType.COUPON_SALE_VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
   }
    public CouponVoidTrans(Context context, Handler handler, String origTransNo, TransEndListener transListener) {
        super(context, handler, ETransType.VOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    public CouponVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.COUPON_SALE_VOID, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno),
                ActionInputTransData.EInputType.NUM, 6, true);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // Confirm trans info
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler,
                context.getString(R.string.trans_void));
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);

        // Void coupon
        ActionVoidCoupon voidCouponAction = new ActionVoidCoupon(transData);
        bind(State.VOID_COUPON.toString(), voidCouponAction);

        // Signature action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                //sandy
                String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
                if(SysParam.Constant.YES.equals(isIndopayMode)){
                    transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));
                    if(transData.getActualPayAmount().length() >= 2)
                        transData.setActualPayAmount(transData.getActualPayAmount().substring(0,transData.getActualPayAmount().length()-2));

                    if(transData.getDiscountAmount().length() >= 2)
                        transData.setDiscountAmount(transData.getDiscountAmount().substring(0,transData.getDiscountAmount().length()-2));
                }

                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // Print action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.INPUT_PWD.toString());
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        if (state != State.SIGNATURE && state != State.VOID_COUPON) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_PWD:
                onInputPwd(result);
                break;
            case ENTER_TRANSNO:
                onEnterTransNo(result);
                break;
            case TRANS_DETAIL:
                gotoState(State.VOID_COUPON.toString());
                break;
            case VOID_COUPON:
                onVoidTrans(result);
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

    protected void onInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (data != null &&
                !data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }

        if (isNeedInputTransNo) {
            gotoState(SaleVoidTrans.State.ENTER_TRANSNO.toString());
        } else {
            if (isNeedFindOrigTrans) {
                validateOrigTransData(origTransNo);
            } else {
                transData.setEnterMode(TransData.EnterMode.MANUAL);
                transData.setPin("");
                transData.setHasPin(false);
                copyOrigTransData();
                gotoState(State.VOID_COUPON.toString());
            }
        }



    }

    protected void onEnterTransNo(ActionResult result) {
        String transNo = (String) result.getData();
        if (validateOrigTransData(transNo)) {
            transData.setEnterMode(TransData.EnterMode.MANUAL);
            transData.setPin("");
            transData.setHasPin(false);
            copyOrigTransData();

            ActionDispTransDetail action = (ActionDispTransDetail) getAction(State.TRANS_DETAIL.toString());
            action.setTransData(origTransData);
            gotoState(State.TRANS_DETAIL.toString());
        }
    }

    private boolean validateOrigTransData(String origTransNo) {
        long transNo;
        try {
            transNo = Long.parseLong(origTransNo);
        } catch (NumberFormatException e) {
            Log.e(TAG, null, e);
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return false;
        }

        origTransData = TransData.readTrans(transNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return false;
        }



        String trType = origTransData.getTransType();
        // 非优惠券交易不能撤销
        if (!trType.equals(ETransType.COUPON_SALE.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return false;
        }

        String trStatus = origTransData.getTransState();
        // 已撤销交易，不能重复撤销/已调整交易不可撤销
        if (TransData.ETransStatus.VOID.toString().equals(trStatus)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return false;
        }
        if (TransData.ETransStatus.ADJUST.toString().equals(trStatus)) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return false;
        }

        return true;
    }

    // 设置原交易记录
    protected void copyOrigTransData() {
        //sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        if(SysParam.Constant.YES.equals(isIndopayMode)){
            transData.setAmount(String.format("%s00",origTransData.getAmount()));
            transData.setActualPayAmount(String.format("%s00",origTransData.getActualPayAmount()));
            transData.setDiscountAmount(String.format("%s00",origTransData.getDiscountAmount()));
        }
        else{
            transData.setAmount(origTransData.getAmount());
            transData.setActualPayAmount(origTransData.getActualPayAmount());
            transData.setDiscountAmount(origTransData.getDiscountAmount());
        }

            /**
        Log.d(TAG,"Sandy.CouponVoidTrans.copyOrigTransData.amount " + transData.getAmount());
        Log.d(TAG,"Sandy.CouponVoidTrans.copyOrigTransData.actual " + transData.getActualPayAmount());
        Log.d(TAG,"Sandy.CouponVoidTrans.copyOrigTransData.discount " + transData.getDiscountAmount());
        **/

        transData.setOrigCouponRefNo(origTransData.getOrigCouponRefNo());
        transData.setOrigEnterMode(origTransData.getOrigEnterMode());
        transData.setOrigHasPin(origTransData.getOrigHasPin());
        transData.setDateTimeTrans(origTransData.getDateTimeTrans());
        transData.setOrigCouponDateTimeTrans(origTransData.getOrigCouponDateTimeTrans());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setCouponNo(origTransData.getCouponNo());
    }

    protected void onSignature(ActionResult result) {
        //Sandy :  it was a bug!!!!!
        transData.setActualPayAmount(origTransData.getActualPayAmount());
        transData.setDiscountAmount(origTransData.getDiscountAmount());
        transData.setAmount(origTransData.getAmount());
        /**
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.trans.actual " + transData.getActualPayAmount());
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.oritrans.actual " + origTransData.getActualPayAmount());
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.trans.discount " + transData.getDiscountAmount());
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.oritrans.discount " + origTransData.getDiscountAmount());
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.trans.amount " + transData.getAmount());
        Log.d(TAG,"Sandy.ActionVoidCoupon.onSignature.oritrans.discount " + origTransData.getAmount());
        **/


        // 保存签名数据
            byte[] signData = (byte[]) result.getData();
            if (signData != null && signData.length > 0) {
                transData.setSignData(signData);
                transData.updateTrans();
            }
            gotoState(State.PRINT_TICKET.toString());

    }


    private void onVoidTrans(ActionResult result){
        if(transData.getResponseCode().equals("00")){
            origTransData.setTransState(TransData.ETransStatus.VOID.toString());
            origTransData.updateTrans();
            gotoState(State.SIGNATURE.toString());
       }else{
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
       }

     }




}
