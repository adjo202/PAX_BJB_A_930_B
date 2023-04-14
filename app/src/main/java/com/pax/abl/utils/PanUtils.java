package com.pax.abl.utils;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.settings.SysParam;

import org.apache.commons.lang.StringUtils;

public class PanUtils {
    public enum EPanMode {
        X9_8_WITH_PAN,
        X9_8_NO_PAN,
    }

    /**
     * 按各个银行要求处理卡号
     * 
     * @param pan
     * @param mode
     * @return
     */
    public static String getPanBlock(String pan, EPanMode mode) {
        String panBlock = null;
        if (pan == null || pan.length() < 13 || pan.length() > 19) {
            return null;
        }
        switch (mode) {
            case X9_8_WITH_PAN:
                panBlock = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);
                break;
            case X9_8_NO_PAN:
                panBlock = "0000000000000000";
                break;

            default:
                break;
        }

        return panBlock;
    }

    /**
     * 空格分隔卡号
     * 
     * @param cardNo
     * @return
     */
    public static String separateWithSpace(String cardNo) {
        if (cardNo == null)
            return null;

        String temp = "";
        int total = cardNo.length() / 4;
        for (int i = 0; i < total; i++) {
            temp += cardNo.substring(i * 4, i * 4 + 4);
            if (i != (total - 1)) {
                temp += " ";
            }
        }
        if (total * 4 < cardNo.length()) {
            temp += " " + cardNo.substring(total * 4, cardNo.length());
        }

        return temp;
    }

    /**
     * 前6后4， 其他显示“*”
     * 
     * @param cardNo
     * @return
     */
    public static String maskedCardNo(ETransType transType, String cardNo) {
        int cardLen = cardNo.length();
        // 验证：16-20位数字
        if (cardLen < 13)
            return null;

        String maskCardNo = cardNo;
        boolean maskFlag = false;

        if(FinancialApplication.getSysParam().get(SysParam.SALE_MASK_CARD_NUMBER).equals(SysParam.Constant.YES) &&
           transType != ETransType.AUTH && transType != ETransType.MOTO_AUTH &&
           transType != ETransType.OFFLINE_SETTLE && transType != ETransType.SETTLE_ADJUST) {
            maskFlag = true;
        }else if(FinancialApplication.getSysParam().get(SysParam.PREAUTH_MASK_CARD_NUMBER).equals(SysParam.Constant.YES) &&
                (transType == ETransType.AUTH || transType == ETransType.MOTO_AUTH)) {
            maskFlag = true;
        }

        if(maskFlag)
            maskCardNo = cardNo.substring(0, 6).concat(StringUtils.repeat("*", cardLen-10)).concat(cardNo.substring(cardLen-4));

        return maskCardNo;
    }

    /**
     * added by Steven.T  2017-6-7 11:23:23
     * 前6后4， 其他显示“*”
     * @param cardNo
     * @return
     */
    /*
    public static String maskedCardNo(String cardNo) {
        char[] tempNum = cardNo.toCharArray();
        int cardLength = tempNum.length;
        // 验证：16-20位数字
        if (cardLength < 13)
            return null;

        for (int i = 0; i < cardLength; i++) {
            if ((i + 1 > 6) && (i < cardLength - 4)) {
                tempNum[i] = '*';
            }
        }
        return new String(tempNum);
    }*/

    public static String maskedCardNo(String cardNo) {
        return maskedString(cardNo,13);
    }

    public static String maskedString(String str, int minLengh) {
        char[] tempNum = str.toCharArray();
        int cardLength = tempNum.length;
        if (cardLength < minLengh)
            return null;

        for (int i = 0; i < cardLength; i++) {
            if ((i + 1 > 6) && (i < cardLength - 4)) {
                tempNum[i] = '*';
            }
        }
        return new String(tempNum);
    }


}
