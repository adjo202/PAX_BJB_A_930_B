package com.pax.pay.service;

import android.util.Log;

import com.pax.pay.trans.TransResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class ParseReq {
    public static final String TAG = "ParseReq";

    private JSONObject json;
    private RequestData requestData;
    
    private static ParseReq parseReq;

    private ParseReq() {

    }

    public static synchronized ParseReq getInstance() {
        if (parseReq == null) {
            parseReq = new ParseReq();
        }

        return parseReq;
    }

    public int check(JSONObject json) {
        this.json = json;
        String transType = "";
        int ret = -1;
        requestData = new RequestData();

        ret = checkTransType();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        ret = checkAppID();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        transType = requestData.getTransType();

        // 消费/预授权
        if (transType.equals(ServiceConstant.TRANS_SALE) || transType.equals(ServiceConstant.TRANS_AUTH)
                || transType.equals(ServiceConstant.TRANS_QR_SALE)) {
            ret = checkTransAmount1();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 预授权完成请求/预授权完成通知/预授权撤销/
        } else if (transType.equals(ServiceConstant.TRANS_AUTH_CM) || transType.equals(ServiceConstant.TRANS_AUTH_ADV)
                || transType.equals(ServiceConstant.TRANS_AUTH_VOID)) {
            ret = checkTransAmount2();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            ret = checkOrigAuthNo();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            ret = checkOrigDate();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 退货
        } else if (transType.equals(ServiceConstant.TRANS_REFUND)) {
            ret = checkTransAmount2();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            ret = checkOriRefNo();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            ret = checkOrigDate();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 撤销/预授权完成请求撤销
        } else if (transType.equals(ServiceConstant.TRANS_VOID) || transType.equals(ServiceConstant.TRANS_AUTH_CM_VOID)) {
            ret = checkVoucherNo();
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 签退/结算/重打最后一笔/重打任意笔/打印交易明细/打印交易汇总/重打结算单/获取卡号/终端参数设置/交易查询/余额查询
        } else if (transType.equals(ServiceConstant.TRANS_LOGOFF) || transType.equals(ServiceConstant.TRANS_SETTLE)
                || transType.equals(ServiceConstant.TRANS_PRN_LAST) || transType.equals(ServiceConstant.TRANS_PRN_ANY)
                || transType.equals(ServiceConstant.TRANS_PRN_DETAIL) || transType.equals(ServiceConstant.TRANS_QUERY)
                || transType.equals(ServiceConstant.TRANS_BALANCE) || transType.equals(ServiceConstant.TRANS_PRN_TOTAL)
                || transType.equals(ServiceConstant.TRANS_PRN_LAST_BATCH)
                || transType.equals(ServiceConstant.TRANS_GET_CARD_NO)
                || transType.equals(ServiceConstant.TRANS_SETTING)) {

            return TransResult.SUCC;
            // 签到
        } else if (transType.equals(ServiceConstant.TRANS_LOGON)) {
            ret = checkOperId();
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } else if (transType.equals(ServiceConstant.PRN_BITMAP)) {
            ret = checkBitmap();
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } else if (transType.equals(ServiceConstant.TRANS_QR_VOID)) {
            ret = checkOrigc2bVoucher();
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } else if (transType.equals(ServiceConstant.TRANS_QR_REFUND)) {
            ret = checkOrigc2bVoucher();
            if (ret != TransResult.SUCC) {
                return ret;
            }
            ret = checkTransAmount2();
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } else {
            return TransResult.ERR_PARAM;

        }

        return TransResult.SUCC;

    }

    public RequestData getRequestData() {
        return requestData;
    }

    /**
     * 检查交易类型
     * 
     * @return
     */
    private int checkTransType() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_TYPE);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }

        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        requestData.setTransType(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查操作员号
     * 
     * @return
     */
    private int checkOperId() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.OPER_ID);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }
        if (temp == null || temp.length() != 2) {
            return TransResult.ERR_PARAM;
        }

        try {
            Integer.parseInt(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }

        requestData.setOperId(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查交易金额(不存在或者格式错都返回ERR_PARAM)
     * 
     * @return
     */
    private int checkTransAmount1() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_AMOUNT);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }

        // 交易金额
        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        long amount = 0;
        try {
            amount = Long.parseLong(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        if (amount == 0) {
            return TransResult.ERR_PARAM;
        }
        requestData.setTransAmount(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查交易金额(不存在返回SUCC,格式错返回ERR_PARAM)
     * 
     * @return
     */
    private int checkTransAmount2() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.TRANS_AMOUNT);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        long amount = 0;
        try {
            amount = Long.parseLong(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        if (amount == 0) {
            return TransResult.ERR_PARAM;
        }
        requestData.setTransAmount(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查应用ID
     * 
     * @return
     */
    private int checkAppID() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.APP_ID);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }
        if (temp == null || temp.length() == 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setAppId(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查原交易日期
     * 
     * @return
     */
    private int checkOrigDate() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_DATE);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() != 4) {
            return TransResult.ERR_PARAM;
        }

        // 检查合法性
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }
        requestData.setOrigDate(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查原授权码
     * 
     * @return
     */
    private int checkOrigAuthNo() {
        StringBuilder temp;
        try {
            temp = new StringBuilder(json.getString(ServiceConstant.ORIG_AUTH_NO));
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 6) {
            return TransResult.ERR_PARAM;
        }

        if (temp.length() < 6) {
            int flag = 6 - temp.length();
            for (int i = 0; i < flag; i++) {
                temp.insert(0, '0');
            }
        }
        requestData.setOrigAuthNo(temp.toString());
        return TransResult.SUCC;
    }

    /**
     * 检查图片
     * 
     * @return
     */
    private int checkBitmap() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.PRN_BMP);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PARAM;
        }

        if (temp == null || temp.length() < 1) {
            return TransResult.ERR_PARAM;
        }

        requestData.setBitmap(temp);
        return TransResult.SUCC;
    }

    /**
     * 检查凭证号
     * 
     * @return
     */
    private int checkVoucherNo() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.VOUCHER_NO);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 6) {
            return TransResult.ERR_PARAM;
        }

        long voucherNo = -1;
        try {
            voucherNo = Long.parseLong(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        if (voucherNo < 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setVoucherNo(String.format("%06d", voucherNo));
        return TransResult.SUCC;
    }

    /**
     * 检查原参考号
     * 
     * @return
     */
    private int checkOriRefNo() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_REF_NO);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() > 12) {
            return TransResult.ERR_PARAM;
        }

        long refNo = -1;
        try {
            refNo = Long.parseLong(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        if (refNo < 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setOriRefNo(String.format("%012d", refNo));
        return TransResult.SUCC;
    }

    /**
     * 检查原付款凭证号(扫码)
     * 
     * @return
     */
    private int checkOrigc2bVoucher() {
        String temp;
        try {
            temp = json.getString(ServiceConstant.ORIG_C2B_VOUCHER_NO);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            return TransResult.SUCC;
        }

        if (temp == null || temp.length() == 0) {
            return TransResult.SUCC;
        }

        if (temp.length() < 12 || temp.length() > 20) {
            return TransResult.ERR_PARAM;
        }

        long oric2bVoucher = -1;
        try {
            oric2bVoucher = Long.parseLong(temp);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        if (oric2bVoucher < 0) {
            return TransResult.ERR_PARAM;
        }

        requestData.setOrigC2bVoucher(temp);
        return TransResult.SUCC;
    }

    public static class RequestData {
        String appId;
        String appName;
        String transType;
        String transAmount;
        String operId;
        String origDate;
        String origAuthNo;
        String bitmap;
        String voucherNo;
        String oriRefNo;
        String origC2bVoucher;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getTransType() {
            return transType;
        }

        public void setTransType(String transType) {
            this.transType = transType;
        }

        public String getTransAmount() {
            return transAmount;
        }

        public void setTransAmount(String transAmount) {
            this.transAmount = transAmount;
        }

        public String getOperId() {
            return operId;
        }

        public void setOperId(String operId) {
            this.operId = operId;
        }

        public String getOrigDate() {
            return origDate;
        }

        public void setOrigDate(String origDate) {
            this.origDate = origDate;
        }

        public String getOrigAuthNo() {
            return origAuthNo;
        }

        public void setOrigAuthNo(String origAuthNo) {
            this.origAuthNo = origAuthNo;
        }

        public String getBitmap() {
            return bitmap;
        }

        public void setBitmap(String bitmap) {
            this.bitmap = bitmap;
        }

        public String getVoucherNo() {
            return voucherNo;
        }

        public void setVoucherNo(String voucherNo) {
            this.voucherNo = voucherNo;
        }

        public String getOriRefNo() {
            return oriRefNo;
        }

        public void setOriRefNo(String oriRefNo) {
            this.oriRefNo = oriRefNo;
        }

        public String getOrigC2bVoucher() {
            return origC2bVoucher;
        }

        public void setOrigC2bVoucher(String origC2bVoucher) {
            this.origC2bVoucher = origC2bVoucher;
        }
    }
}
