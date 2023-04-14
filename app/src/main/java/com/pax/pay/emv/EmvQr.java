/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-7 4:54
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.emv;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;

import com.pax.eemv.utils.Tools;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObj;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;

import java.lang.reflect.Field;

/**
 * Created by liliang on 2017/8/7.
 */

public class EmvQr {

    public static final String TAG = "EmvQr";

    public static final String AID_DEBIT_APP = "A000000333010101";
    public static final String AID_CREDIT_APP = "A000000333010102";
    public static final String AID_QCREDIT_APP = "A000000333010103";

    private String mAid;
    private String mTrackData;
    private String mPan;
    private String mExpDate;
    private String mCardSeqNum;
    private String mCouponNum;
    private String mIccData;

    private EmvQr() {

    }

    public static EmvQr decodeEmvQrB64(String emvQr) {
        if (emvQr == null) {
            return null;
        }

        byte[] bytes = Base64.decode(emvQr, Base64.DEFAULT);
        if (bytes == null) {
            return null;
        }
        Log.d(TAG, Tools.bcd2Str(bytes));

        return decodeEmvQrBcd(bytes);
    }

    public static EmvQr decodeEmvQrBcd(byte[] emvQr) {
        if (emvQr == null || emvQr.length == 0) {
            return null;
        }

        try {
            ITlv tlv = FinancialApplication.getPacker().getTlv();
            ITlvDataObjList objList = tlv.unpack(emvQr);
            if (objList == null) {
                return null;
            }

            byte[] bytes = objList.getValueByTag(0x61);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            objList = tlv.unpack(bytes);

            EmvQr emvQrObj = new EmvQr();
            bytes = objList.getValueByTag(0x4F);
            emvQrObj.mAid = FinancialApplication.getConvert().bcdToStr(bytes);

            bytes = objList.getValueByTag(0x57);
            emvQrObj.mTrackData = FinancialApplication.getConvert().bcdToStr(bytes);
            emvQrObj.mPan = emvQrObj.mTrackData.split("D")[0];
            int index = emvQrObj.mTrackData.indexOf('D') + 1;
            emvQrObj.mExpDate = emvQrObj.mTrackData.substring(index, index + 4);

            bytes = objList.getValueByTag(0x5F34);
            emvQrObj.mCardSeqNum = FinancialApplication.getConvert().bcdToStr(bytes);

            bytes = objList.getValueByTag(0x9F60);
            if (bytes != null && bytes.length > 0) {
                String coupon = new String(bytes);
                if (!TextUtils.isEmpty(coupon)) {
                    emvQrObj.mCouponNum = coupon.substring(4);
                }
            }

            bytes = objList.getValueByTag(0x63);
            emvQrObj.mIccData = createIccData(bytes);

            return emvQrObj;

        } catch (TlvException | IllegalArgumentException e) {
            Log.e(TAG, "Failed to decode EMV QR.", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode EMV QR.", e);
        }

        return null;
    }

    private static String createIccData(byte[] iccBytes) {
        if (iccBytes == null) {
            throw new IllegalArgumentException("iccBytes is null.");
        }

        ArrayMap<Integer, String> map = new ArrayMap<>(9);
        map.put(0x9F37, "12345678");
        map.put(0x95, "0000008000");
        map.put(0x9A, "171123");
        map.put(0x9C, "00");
        map.put(0x9F02, "000000000001");
        map.put(0x5F2A, "0360");
        map.put(0x9F1A, "0360");
        map.put(0x9F03, "000000000000");
        map.put(0x9F33, "E0F1C8");

        ITlv tlv = FinancialApplication.getPacker().getTlv();
        ITlvDataObjList iccDataList;
        try {
            iccDataList = tlv.unpack(iccBytes);
            for (Integer tag : map.keySet()) {
                if (iccDataList.getByTag(tag) == null) {
                    ITlvDataObj obj = tlv.createTlvDataObject();
                    obj.setTag(tag);
                    byte[] value = FinancialApplication.getConvert().strToBcd(map.get(tag),
                            IConvert.EPaddingPosition.PADDING_RIGHT);
                    obj.setValue(value);
                    iccDataList.addDataObj(obj);
                }
            }

            byte[] bytes = tlv.pack(iccDataList);
            return FinancialApplication.getConvert().bcdToStr(bytes);
        } catch (TlvException e) {
            Log.e(TAG, "Failed to decode icc data from EMV QR.", e);
        }

        return null;
    }

    public boolean isUpiAid() {
        if (mAid == null) {
            return false;
        }

        switch (mAid) {
            case AID_DEBIT_APP:
            case AID_CREDIT_APP:
            case AID_QCREDIT_APP:
                return true;
            default:
                return false;
        }
    }

    public String getAid() {
        return mAid;
    }

    public String getTrackData() {
        return mTrackData;
    }

    public String getPan() {
        return mPan;
    }

    public String getExpireDate() {
        return mExpDate;
    }

    public String getCouponNum() {
        return mCouponNum;
    }

    public boolean isSupportUplan() {
        return mCouponNum != null;
    }

    public String getCardSeqNum() {
        return mCardSeqNum;
    }

    public String getIccData() {
        return mIccData;
    }

    @Override
    public String toString() {
        Field[] fields = EmvQr.class.getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder();
        for (Field field : fields) {
            try {
                Object value = field.get(this);
                stringBuilder.append("[" + field.getName() + ":" + value + "]\n");
                Log.d(TAG, "[" + field.getName() + ":" + value + "]");
            } catch (IllegalAccessException e) {
                Log.e(TAG, "", e);
            }
        }
        return stringBuilder.toString();
    }
}
