package com.pax.pay.trans.model;

import android.util.Log;

import com.pax.eemv.enums.ETransResult;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObj;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public enum ReceiptElements {
    /**
     * 交易通用信息
     */
    TAG_FF00 { // 商户名称

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x00 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            byte[] value = new byte[40];
            try {
                byte[] temp = FinancialApplication.getSysParam().get(SysParam.MERCH_CN).getBytes("GBK");

                if (temp.length > 40) {
                    System.arraycopy(temp, 0, value, 0, 40);
                } else {
                    value = temp;
                }

                return value;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "", e);
            }
            return null;
        }
    },

    TAG_FF01 { // 交易类型

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x01 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String transName = "";
            ETransType transType = ETransType.valueOf(transData.getTransType());
            try {
                switch (transType) {
                    case SALE:
                        transName = "消费";
                        break;
                    case EC_SALE:
                        transName = "电子现金消费";
                        break;
                    case VOID:
                        transName = "消费撤销";
                        break;
                    case AUTH:
                        transName = "预授权";
                        break;
                    case AUTHVOID:
                        transName = "预授权撤销";
                        break;
                    case AUTHCM:
                        transName = "预授权完成（请求）";
                        break;
                    case AUTHCMVOID:
                        transName = "预授权完成撤销";
                        break;
                    case AUTH_SETTLEMENT:
                        transName = "预授权完成（通知）";
                        break;
                    case REFUND:
                        transName = "退货";
                        break;
                    case INSTAL_SALE:
                        transName = "分期消费";
                        break;
                    case INSTAL_VOID:
                        transName = "分期撤销";
                        break;
                    case EC_REFUND:
                        transName = "电子现金退货";
                        break;
                    case EC_LOAD:
                        transName = "电子现金指定账户圈存";
                        break;
                    case OFFLINE_TRANS_SEND:
                        transName = "电子现金非指定账户圈存";
                        break;
                    case EC_CASH_LOAD:
                        transName = "电子现金现金充值";
                        break;
                    case EC_CASH_LOAD_VOID:
                        transName = "电子现金充值撤销";
                        break;
                    case OFFLINE_SETTLE:
                        transName = "离线结算";
                        break;
                    case SETTLE_ADJUST_TIP:
                    case SETTLE_ADJUST:
                        transName = "结算调整";
                        break;
                    default:
                        transName = "未知交易";
                }

                return transName.getBytes("GBK");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "", e);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "", e);
            }
            return null;
        }
    },
    TAG_FF02 {// 操作员号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x02 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String operId = TransContext.getInstance().getOperID();
            return FinancialApplication.getConvert().strToBcd(operId, EPaddingPosition.PADDING_LEFT);
        }
    },
    TAG_FF03 {// 收单机构

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x03 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String acqCode = transData.getAcqCode();
            if (acqCode != null && acqCode.length() > 0) {
                acqCode += "           ";
                return acqCode.substring(0, 11).getBytes();
            }
            return null;
        }
    },
    TAG_FF04 {// 发卡机构

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x04 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String isserCode = transData.getIsserCode();
            if (isserCode != null && isserCode.length() > 0) {
                isserCode += "           ";
                return isserCode.substring(0, 11).getBytes();
            }
            return null;
        }
    },
    TAG_FF05 {// 有效期

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x05 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String expDate = transData.getExpDate();
            if (expDate != null && expDate.length() > 0) {
                return FinancialApplication.getConvert().strToBcd(expDate, EPaddingPosition.PADDING_LEFT);
            }

            return null;
        }
    },
    TAG_FF06 {// 日期时间

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x06 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String date = transData.getDate();
            String time = transData.getTime();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            String dateTime = String.valueOf(year) + date + time;
            return FinancialApplication.getConvert().strToBcd(dateTime, EPaddingPosition.PADDING_LEFT);
        }
    },
    TAG_FF07 {// 授权码

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x07 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String authCode = transData.getAuthCode();
            if (authCode == null || authCode.length() == 0) {
                return null;
            }
            if (authCode != null && authCode.length() > 0) {
                return authCode.getBytes();
            }
            return null;
        }
    },
    TAG_FF08 {// 小费金额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x08 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String tipAmount = transData.getTipAmount();
            if (tipAmount != null && tipAmount.length() > 0) {
                return FinancialApplication.getConvert().strToBcd(String.format("%012d", Long.parseLong(tipAmount)),
                        EPaddingPosition.PADDING_LEFT);
            }
            return null;
        }
    },
    TAG_FF09 { // 卡组织

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x09 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.AUTH_SETTLEMENT || transType == ETransType.EC_REFUND
                    || transType == ETransType.REFUND || transType == ETransType.OFFLINE_SETTLE
                    || transType == ETransType.SETTLE_ADJUST || transType == ETransType.SETTLE_ADJUST_TIP) {
                String interOrgCode = transData.getInterOrgCode();
                if (interOrgCode != null && interOrgCode.length() > 0) {
                    return interOrgCode.getBytes();
                }
                return "   ".getBytes();
            }
            return null;
        }
    },
    TAG_FF0A { // 交易币种

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x0A };
        }

        @Override
        public byte[] getValue(TransData transData) {
            Currency currency = FinancialApplication.getSysParam().getCurrency();
            return currency.getCode().getBytes();
        }
    },

    TAG_FF0B {// 持卡人手机号（不足位前补0）

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x0B };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },

    /**
     * IC卡有关信息
     */

    TAG_FF20 {// 应用标签

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x20 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String label = transData.getEmvAppLabel();
            if (label != null && label.length() > 0) {
                label += "0000000000000000";
                return label.substring(0, 16).getBytes();
            }
            return null;
        }
    },
    TAG_FF21 {// 应用名称

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x21 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String appName = transData.getEmvAppName();
            if (appName != null && appName.length() > 0) {
                appName += "0000000000000000";
                return appName.substring(0, 16).getBytes();
            }
            return null;
        }
    },
    TAG_FF22 {// 应用标识

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x22 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String aid = transData.getAid();
            if (aid != null && aid.length() > 0) {
                return FinancialApplication.getConvert().strToBcd(aid, EPaddingPosition.PADDING_LEFT);
            }
            return null;
        }
    },
    TAG_FF23 { // IC卡交易证书

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x23 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            String temp = "";
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.SALE || transType == ETransType.AUTH || transType == ETransType.EC_LOAD) {
                temp = transData.getArqc();
                if (temp != null && temp.length() > 0) {
                    temp += "0000000000000000";
                    return FinancialApplication.getConvert().strToBcd(temp.substring(0, 16), EPaddingPosition.PADDING_LEFT);
                }

            }
            return null;
        }
    },
    TAG_FF24 {// 充值后卡片余额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x24 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            if (transData.getTransType().equals(ETransType.EC_LOAD.toString())) {
                String balance = transData.getBalance();
                if (balance != null && balance.length() > 0) {
                    return FinancialApplication.getConvert().strToBcd(balance, EPaddingPosition.PADDING_LEFT);
                }
            }

            return null;
        }
    },
    TAG_FF25 {// 转入卡卡号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x25 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            if (ETransType.valueOf(transData.getTransType()) == ETransType.EC_TRANSFER_LOAD) {
                String pan = transData.getTransferPan();
                return pan.getBytes();
            }

            return null;
        }
    },
    TAG_FF26 { // 不可预知数

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x26 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            String transName = transData.getTransType();
            byte result = transData.getEmvResult();

            if ((transName.equals(ETransType.EC_SALE.toString()) || transName.equals(ETransType.SALE.toString()))
                    && result == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                ITlv tlv = FinancialApplication.getPacker().getTlv();
                byte[] emvValue = FinancialApplication.getConvert().strToBcd(transData.getSendIccData(),
                        EPaddingPosition.PADDING_LEFT);
                ITlvDataObjList list = tlv.createTlvDataObjectList();
                try {
                    list = tlv.unpack(emvValue);
                } catch (TlvException e) {
                    Log.e(TAG, "", e);
                }
                ITlvDataObj obj = list.getByTag(0x9F37);
                byte[] value = obj.getValue();
                if (value != null && value.length > 0) {
                    String temp = FinancialApplication.getConvert().bcdToStr(value);
                    temp += "00000000";
                    return FinancialApplication.getConvert().strToBcd(temp.substring(0, 8), EPaddingPosition.PADDING_LEFT);
                }
            }
            return null;
        }
    },
    TAG_FF27 {// 应用交互特征

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x27 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            String temp = "";
            String transName = transData.getTransType();
            byte result = transData.getEmvResult();
            if (transName.equals(ETransType.EC_SALE.toString())
                    && result == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                ITlv tlv = FinancialApplication.getPacker().getTlv();
                byte[] emvValue = FinancialApplication.getConvert().strToBcd(transData.getSendIccData(),
                        EPaddingPosition.PADDING_LEFT);
                ITlvDataObjList list = tlv.createTlvDataObjectList();
                try {
                    list = tlv.unpack(emvValue);
                } catch (TlvException e) {
                    Log.e(TAG, "", e);
                }
                ITlvDataObj obj = list.getByTag(0x82);
                byte[] value = obj.getValue();
                if (value != null && value.length > 0) {
                    temp = FinancialApplication.getConvert().bcdToStr(value);
                    temp += "0000";
                    return FinancialApplication.getConvert().strToBcd(temp.substring(0, 4), EPaddingPosition.PADDING_LEFT);
                }
            }
            return null;
        }
    },
    TAG_FF28 { // 终端验证结果

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x28 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            String transName = transData.getTransType();
            byte emvResult = transData.getEmvResult();
            if (transName.equals(ETransType.EC_SALE.toString())
                    && emvResult == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                String temp = transData.getTvr();
                if (temp != null && temp.length() > 0) {
                    temp += "0000000000";
                    return FinancialApplication.getConvert().strToBcd(temp.substring(0, 10), EPaddingPosition.PADDING_LEFT);
                }
            }

            return null;
        }
    },
    TAG_FF29 { // 交易状态信息

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x29 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String transName = transData.getTransType();
            byte emvResult = transData.getEmvResult();
            if (transName.equals(ETransType.SALE.toString())
                    && emvResult == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                String temp = transData.getTsi();
                if (temp != null && temp.length() > 0) {
                    return temp.getBytes();
                }
            }
            return null;
        }
    },
    TAG_FF2A {// 应用交易计数器

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x2A };
        }

        @Override
        public byte[] getValue(TransData transData) {

            String transName = transData.getTransType();
            byte emvResult = transData.getEmvResult();
            if (transName.equals(ETransType.EC_SALE.toString())
                    && emvResult == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                String temp = transData.getAtc();
                if (temp != null && temp.length() > 0) {
                    temp += "0000";
                    return FinancialApplication.getConvert().strToBcd(temp.substring(0, 4), EPaddingPosition.PADDING_LEFT);
                }
            }
            return null;
        }
    },
    TAG_FF2B {// 发卡应用数据

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x2B };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String transName = transData.getTransType();
            byte result = transData.getEmvResult();
            if (transName.equals(ETransType.EC_SALE.toString())
                    && result == (byte) ETransResult.OFFLINE_APPROVED.ordinal()) {
                ITlv tlv = FinancialApplication.getPacker().getTlv();

                byte[] emvValue = FinancialApplication.getConvert().strToBcd(transData.getSendIccData(),
                        EPaddingPosition.PADDING_RIGHT);
                ITlvDataObjList list = tlv.createTlvDataObjectList();
                try {
                    list = tlv.unpack(emvValue);
                } catch (TlvException e) {
                    Log.e(TAG, "", e);
                }
                ITlvDataObj obj = list.getByTag(0x9F10);
                byte[] value = obj.getValue();
                if (value != null && value.length > 0) {
                    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, };
                    System.arraycopy(value, 0, data, 0, value.length);
                    return data;
                }
            }
            return null;
        }
    },

    TAG_FF30 {// 应用标签

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x30 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String label = transData.getEmvAppLabel();
            if (label != null && label.length() > 0) {
                return label.getBytes();
            }
            return null;
        }
    },

    TAG_FF31 { // 应用名称

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x31 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String appName = transData.getEmvAppName();
            if (appName != null && appName.length() > 0) {
                return appName.getBytes();
            }
            return null;
        }
    },

    /**
     * 创新业务信息
     */
    TAG_FF40 { // 扣持卡人金额备注信息

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x40 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            try {
                String issuerResp = transData.getIssuerResp();
                String centerResp = transData.getCenterResp();
                String recvBankResp = transData.getRecvBankResp();
                String temp = "";

                if (issuerResp != null && issuerResp.length() > 0) {
                    temp += issuerResp;
                }
                if (centerResp != null && centerResp.length() > 0) {
                    temp += centerResp;
                }
                if (recvBankResp != null && recvBankResp.length() > 0) {
                    temp += recvBankResp;
                }
                if (temp != null && temp.length() > 0) {
                    return temp.getBytes("GBK");
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "", e);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "", e);
            }
            return null;
        }
    },
    TAG_FF41 { // 分期付款期数

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x41 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },

    TAG_FF42 {// 分期付款首期金额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x42 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF43 { // 分期付款还款币种

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x43 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF44 { // 持卡人手续费

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x44 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF45 { // 商品代码

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x45 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF46 { // 兑换积分数

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x46 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF47 { // 积分余额数

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x47 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF48 { // 自付金额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x48 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF49 {// 承兑金额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x49 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF4A {// 可用金额

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x4A };
        }

        @Override
        public byte[] getValue(TransData transData) {
            if (!transData.getTransType().equals(ETransType.EC_LOAD.toString())) {
                String balance = transData.getBalance();
                if (balance != null && balance.length() > 0 && Long.parseLong(balance) > 0) {
                    return FinancialApplication.getConvert().strToBcd(balance, EPaddingPosition.PADDING_LEFT);
                }
            }

            return null;
        }
    },

    TAG_FF4B { // 手机号码

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x4B };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    /**
     * 原交易信息
     */
    TAG_FF60 { // 原凭证号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x60 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String origTransNo = String.format("%06d", transData.getOrigTransNo());
            if (origTransNo == null || origTransNo.length() == 0) {
                origTransNo = "000000";
            }
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.EC_REFUND || transType == ETransType.VOID || transType == ETransType.AUTHCMVOID
                    || transType == ETransType.EC_CASH_LOAD_VOID || transType == ETransType.AUTHVOID) {
                return FinancialApplication.getConvert().strToBcd(origTransNo, EPaddingPosition.PADDING_LEFT);
            }
            return null;
        }
    },
    TAG_FF61 {// 原批次号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x61 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String origBatchNo = String.format("%06d", transData.getOrigBatchNo());
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.EC_REFUND) {
                return FinancialApplication.getConvert().strToBcd(origBatchNo, EPaddingPosition.PADDING_LEFT);
            }
            return null;
        }
    },
    TAG_FF62 {// 原参考号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x62 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String origRefNo = transData.getOrigRefNo();
            if (origRefNo == null || origRefNo.length() == 0) {
                origRefNo = "000000000000";
            }
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.REFUND) {
                return FinancialApplication.getConvert().strToBcd(origRefNo, EPaddingPosition.PADDING_LEFT);
            }
            return null;
        }
    },
    TAG_FF63 {// 原交易日期

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x63 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String origDate = transData.getOrigDate();
            if (origDate == null || origDate.length() == 0) {
                origDate = "0000";
            }
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.REFUND || transType == ETransType.EC_REFUND) {
                return FinancialApplication.getConvert().strToBcd(origDate, EPaddingPosition.PADDING_LEFT);
            }

            return null;
        }
    },
    TAG_FF64 { // 原授权码

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, 0x64 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            String origAuth = transData.getOrigAuthCode();
            if (origAuth == null || origAuth.length() == 0) {
                origAuth = "000000";
            }
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType == ETransType.AUTHVOID || transType == ETransType.AUTHCM
                    || transType == ETransType.AUTHCMVOID) {
                return origAuth.getBytes();
            }
            return null;
        }
    },

    TAG_FF65 { // 原终端号

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x65 };
        }

        @Override
        public byte[] getValue(TransData transData) {

            if (transData.getTransType().equals(ETransType.EC_REFUND.toString())) {
                String origTermID = transData.getOrigTermID();
                if (origTermID != null && origTermID.length() > 0) {
                    return origTermID.getBytes();
                }
            }
            return null;
        }
    },

    TAG_FF70 { // 打印联数(位数不足前补0)

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xFF, 0x70 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            int receiptNum = 0;
            String temp = FinancialApplication.getSysParam().get(SysParam.PRINT_VOUCHER_NUM);
            if (temp != null)
                receiptNum = Integer.parseInt(temp);
            if (receiptNum < 1 || receiptNum > 3) // 打印联数只能为1-3
                receiptNum = 2;
            return FinancialApplication.getConvert().strToBcd(String.format("%02d", receiptNum),
                    EPaddingPosition.PADDING_LEFT);
        }

    },
    /**
     * 保留信息
     */
    TAG_FF80 { // 发卡方保留域

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, (byte) 0x80 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF81 {// 中国银联保留域

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, (byte) 0x81 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },
    TAG_FF82 {// 受理机构保留域

        @Override
        public byte[] getTag() {
            return new byte[] { (byte) 0xff, (byte) 0x82 };
        }

        @Override
        public byte[] getValue(TransData transData) {
            return null;
        }
    },

    ;

    public static final String TAG = "ReceiptElements";

    public abstract byte[] getTag();

    public abstract byte[] getValue(TransData transData);
}
