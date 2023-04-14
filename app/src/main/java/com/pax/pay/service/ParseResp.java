package com.pax.pay.service;

import android.util.Log;

import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.service.ParseReq.RequestData;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseResp {
    public static final String TAG = "ParseReq";
    private static ParseResp parseResp;

    private ParseResp() {

    }

    public synchronized static ParseResp getInstance() {
        if (parseResp == null) {
            parseResp = new ParseResp();
        }

        return parseResp;
    }

    public String parse(ActionResult result) {
        try {
            String temp;
            JSONObject json = new JSONObject();

            if (result == null) {
                json.put(ServiceConstant.RSP_CODE, TransResult.ERR_HOST_REJECT);
                return json.toString();
            }

            RequestData requestData = ParseReq.getInstance().getRequestData();
            if (requestData != null) {
                temp = requestData.getAppId();
                if (temp != null && temp.length() != 0) {
                    json.put(ServiceConstant.APP_ID, temp);
                }
            }

            // 应答码
            json.put(ServiceConstant.RSP_CODE, result.getRet());

            if (result.getRet() != TransResult.SUCC) {
                temp = (String) result.getData();

                // 错误应答信息
                if (temp != null && temp.length() != 0) {
                    json.put(ServiceConstant.RSP_MSG, (String) result.getData());
                }
                return json.toString();
            }

            if (result.getData() == null) {
                return json.toString();
            }

            if (result.getData() instanceof CardInformation) {
                CardInformation cardInfo = (CardInformation) result.getData();

                if (cardInfo != null) {
                    json.put(ServiceConstant.CARD_NO, cardInfo.getPan());
                }

                return json.toString();
            }

            // 商户名称
            json.put(ServiceConstant.MERCH_NAME, FinancialApplication.getSysParam().get(SysParam.MERCH_CN));
            // 商户编号
            json.put(ServiceConstant.MERCH_ID, FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
            // 终端编号
            json.put(ServiceConstant.TERM_ID, FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));

            TransData transData = (TransData) result.getData();

            // 卡号
            temp = transData.getPan();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.CARD_NO, PanUtils.maskedCardNo(ETransType.valueOf(transData.getTransType()), transData.getPan()));
            }

            // 凭证号
            temp = String.valueOf(transData.getTransNo());
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.VOUCHER_NO, String.format("%06d", transData.getTransNo()));
            }

            // 批次号
            temp = String.valueOf(transData.getBatchNo());
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.BATCH_NO, String.format("%06d", transData.getBatchNo()));
            }

            // 发卡行号
            temp = transData.getIsserCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ISSER_CODE, temp);
            }

            // 收单行号
            temp = transData.getAcqCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ACQ_CODE, temp);
            }

            // 授权码
            temp = transData.getAuthCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.AUTH_NO, temp);
            }

            // 参考号
            temp = transData.getRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.REF_NO, temp);
            }

            // 交易时间
            temp = transData.getTime();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.TRANS_TIME, temp);
            }

            // 交易日期
            temp = transData.getDate();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.TRANS_DATE, temp);
            }

            // 交易金额
            temp = transData.getAmount();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.TRANS_AMOUNT, temp);
            }

            // 原授权码
            temp = transData.getOrigAuthCode();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ORIG_AUTH_NO, temp);
            }

            // 原凭证号
            temp = String.valueOf(transData.getOrigTransNo());
            if (temp != null && temp.length() != 0 && !temp.equals("0")) {
                json.put(ServiceConstant.ORIG_VOUCHER_NO, String.format("%06d", transData.getOrigTransNo()));
            }

            // 原参考号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ORIG_REF_NO, temp);
            }

            // 支付码号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.C2B, temp);
            }

            // 付款凭证号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.C2B_VOUCHER_NO, temp);
            }

            // 原付款凭证号
            temp = transData.getOrigRefNo();
            if (temp != null && temp.length() != 0) {
                json.put(ServiceConstant.ORIG_C2B_VOUCHER_NO, temp);
            }

            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

}
