/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-9 3:4
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
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

/**
 * Created by liliang on 2017/8/9.
 */

public class ActionCouponSale extends AAction {

    public static final String TAG = "ActionCouponSale";

    private TransData mTransData;

    public ActionCouponSale(TransData data) {
        this(data, null);
        Log.d(TAG,"sandy.Coupon.ActionCouponSale.1");
    }

    public ActionCouponSale(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;
        Log.d(TAG,"sandy.Coupon.ActionCouponSale.2");
    }

    @Override
    protected void process() {
        Log.d(TAG,"sandy.Coupon.process");
        new CouponSaleTask().execute();
    }

    class CouponSaleTask extends AsyncTask<Void, Void, Integer> {

        private Context mContext = TransContext.getInstance().getCurrentContext();
        private TransProcessListener mProcessListener;

        @Override
        protected void onPreExecute() {
            Log.d(TAG,"sandy.Coupon.onPreExecute");
            if (mProcessListener == null) {
                mProcessListener = new TransProcessListenerImpl(mContext);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG,"sandy.Coupon.doInBackground");

            int result = couponVerify(mProcessListener);
            if (result != TransResult.SUCC) {
                return result;
            }

            result = couponSale(mProcessListener);
            return result;

        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG,"sandy.Coupon.onPostExecute");
            if (mProcessListener != null) {
                mProcessListener.onHideProgress();
            }
            mProcessListener = null;
            mContext = null;
            setResult(new ActionResult(result, null));
        }
    }

    private int couponVerify(TransProcessListener listener) {

        mTransData.setTransType(ETransType.COUPON_VERIFY.toString());
        Log.d(TAG,"sandy.Coupon.couponVerify");
        int result = Transmit.getInstance().transmit(mTransData, true, true, true, listener);
        if (result != TransResult.SUCC) {
            return result;
        }

        byte[] bytes = FinancialApplication.getConvert().strToBcd(mTransData.getField62(),IConvert.EPaddingPosition.PADDING_LEFT);
        String discountInfo = new String(bytes);
        String discountAmount = String.valueOf(Long.parseLong(discountInfo.substring(42, 54)));
        String actualAmount = String.valueOf(Long.parseLong(discountInfo.substring(28, 40)));
        Log.d(TAG,"sandy.Coupon.actualAmount:" + actualAmount);
        Log.d(TAG,"sandy.Coupon.discountAmount:" + discountAmount);
        mTransData.setActualPayAmount(actualAmount);
        mTransData.setDiscountAmount(discountAmount);

        //sandy
        //hold the value
        mTransData.setOrigCouponDateTimeTrans(mTransData.getDateTimeTrans());
        mTransData.setOrigCouponRefNo(mTransData.getRefNo());

        //then it should be new value
        mTransData.setDate(Device.getDate().substring(4));
        mTransData.setTime(Device.getTime());
        mTransData.setDateTimeTrans(Long.parseLong(String.format("%s%s",mTransData.getDate(),mTransData.getTime())));
        mTransData.setOrigEnterMode(mTransData.getEnterMode());
        mTransData.setOrigHasPin(mTransData.getHasPin());



        return result;
    }

    private int couponSale(TransProcessListener listener) {
        Log.d(TAG,"sandy.Coupon.couponSale");
        mTransData.setTransType(ETransType.COUPON_SALE.toString());

        int result = Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        if (result != TransResult.SUCC) {
            return result;
        }

        try {
            long total = Long.parseLong(mTransData.getActualPayAmount());
            long discount = Long.parseLong(mTransData.getDiscountAmount());
            total = total + discount;
            mTransData.setAmount(String.valueOf(total));
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        mTransData.saveTrans();
        return result;
    }
}
