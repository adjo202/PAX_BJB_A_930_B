/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-3 10:27
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.ResponseCode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

/**
 * Created by liliang on 2017/8/3.
 */

public class ActionVoidCoupon extends AAction {

    public static final String TAG = "ActionVoidCoupon";

    private TransData mTransData;

    public ActionVoidCoupon(TransData data) {
        this(data, null);
    }

    public ActionVoidCoupon(TransData data, ActionStartListener listener) {
        super(listener);
        this.mTransData = data;

    }

    @Override
    protected void process() {
        new CouponVoidTask().execute();
    }

    class CouponVoidTask extends AsyncTask<Void, Void, Integer> {
        private Context mContext = TransContext.getInstance().getCurrentContext();
        private TransProcessListener mProcessListener;

        @Override
        protected void onPreExecute() {
            if (mProcessListener == null) {
                mProcessListener = new TransProcessListenerImpl(mContext);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int result = voidSale(mProcessListener);
            if (result != TransResult.SUCC) {
                return result;
            }


            if(!mTransData.getResponseCode().equals("00")){
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(mTransData.getResponseCode());
                Device.beepErr();
                mProcessListener.onShowErrMessageWithConfirm(
                        mContext.getString(R.string.emv_err_code) + responseCode.getCode()
                                + mContext.getString(R.string.emv_err_info) + responseCode.getMessage(),
                        Constants.FAILED_DIALOG_SHOW_TIME);

                return TransResult.ERR_ABORTED;
            }


            voidCoupon(mProcessListener);

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mProcessListener != null) {
                mProcessListener.onHideProgress();
            }
            mProcessListener = null;
            mContext = null;
            setResult(new ActionResult(result, null));
        }
    }

    private int voidSale(TransProcessListener listener) {

        int result = Transmit.getInstance().transmit(mTransData, listener);
        if (result != TransResult.SUCC) {
            return result;
        }

        //Sandy : handle here if void if fail
        if(!mTransData.getResponseCode().equals("00")){
            return -1;
        }

        long total      = Long.parseLong(mTransData.getActualPayAmount());
        long discount   = Long.parseLong(mTransData.getDiscountAmount());
        total = total + discount;

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode)) {
            mTransData.setAmount(String.valueOf(total).substring(0, String.valueOf(total).length() - 2));
            mTransData.setActualPayAmount(mTransData.getActualPayAmount().substring(0, mTransData.getActualPayAmount().length() - 2));
            mTransData.setDiscountAmount(mTransData.getDiscountAmount().substring(0, mTransData.getDiscountAmount().length() - 2));
        }else{
            mTransData.setAmount(String.valueOf(total));
        }


        //Log.d(TAG,"Sandy.ActionVoidCoupon.voidSale " + mTransData.getActualPayAmount());

        mTransData.saveTrans();

        return result;
    }

    private void voidCoupon(TransProcessListener listener) {
        mTransData.setHeader("609100321301");
        mTransData.setTransType(ETransType.COUPON_VERIFY_VOID.toString());
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode)){
            mTransData.setAmount(String.format("%s00",mTransData.getAmount()));
        }

        int result = Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        if (result != TransResult.SUCC) {
            Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        }


    }

}
