package com.pax.pay.trans.transmit;

import android.text.TextUtils;
import android.util.Log;

import com.pax.device.Device;
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ResponseCode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;

public class Transmit {
    public static Transmit transmit;

    public Transmit() {

    }

    public static Transmit getInstance() {
        if (transmit == null) {
            transmit = new Transmit();
        }

        return transmit;
    }


    public int transmit(TransData transData, TransProcessListener listener) {
        return transmit(transData, true, true, true, listener);
    }

    public int transmit(TransData transData, boolean sendScript, boolean sendDup, boolean
            sendOffline, TransProcessListener listener) {
        int ret = 0;
        ETransType transType = ETransType.valueOf(transData.getTransType());
        String retCode = "";

        // 处理脚本
        if (transType.isScriptSend() && sendScript) {
            sendScriptResult(listener);
        }

        // 处理冲正
        if (transType.isDupSend() && sendDup) {
            int dupRet = sendReversal(listener);
            if (dupRet != TransResult.SUCC) {
                return dupRet;
            }
        }

        if (sendOffline && FinancialApplication.getSysParam().get(SysParam.OFFLINETC_UPLOAD_TYPE).equals
                (SysParam.Constant.OFFLINETC_UPLOAD_NEXT)) {
            sendOfflineTrans(listener, true);
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(transType.getTransName());
        }

        int i = 0;
        // 只有平台返回密码错时， 才会下次循环
        for (i = 0; i < 3; i++) {
            if (i != 0) {
                // 输入密码
                if (listener != null) {
                    ret = listener.onInputOnlinePin(transData);
                    if (ret != 0) {
                        return TransResult.ERR_ABORTED;
                    }
                } else {
                    return TransResult.ERR_HOST_REJECT;
                }
                transData.setTransNo(Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TRANS_NO)));
            }
            if (listener != null) {
                listener.onUpdateProgressTitle(transType.getTransName());
            }

            //sending a data
            ret = Online.getInstance().online(transData, listener);
            if (ret == TransResult.SUCC) {
                //String retCode = transData.getResponseCode();
                retCode = transData.getResponseCode();
                Log.d("teg", "retCode : " + retCode);
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(retCode);
                if ("A".equals(responseCode.getCategory())) {
                    // 有缺陷的交易成功
                    if (!"00".equals(retCode)) {
                        Device.beepErr();
                        if (listener != null) {

                            //tri
                            if (transType == ETransType.PURCHASE_PULSA_DATA ||
                                    transType == ETransType.INQ_PULSA_DATA ||
                                    transType == ETransType.PASCABAYAR_PURCHASE ||
                                    transType == ETransType.PASCABAYAR_INQUIRY ||
                                    transType == ETransType.PDAM_PURCHASE ||
                                    transType == ETransType.PDAM_INQUIRY) {

                                if ("01".equals(retCode)) {
                                    String prompt = transData.getField63().trim();
                                    if (!StringUtils.isEmpty(prompt)) {
                                        //listener.onShowErrMessageWithConfirm(retCode + "\n" + prompt,
                                        listener.onShowErrMessage(retCode + "\n" + prompt,
                                                Constants.FAILED_DIALOG_SHOW_TIME);
                                    } else {
                                        //listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                        listener.onShowErrMessage(responseCode.getMessage(),
                                                Constants.FAILED_DIALOG_SHOW_TIME);
                                    }
                                } else {
//                                    listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                    listener.onShowErrMessage(responseCode.getMessage(),
                                            Constants.FAILED_DIALOG_SHOW_TIME);
                                }


                            } else {
                                //listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                listener.onShowErrMessage(responseCode.getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }

                        }
                    }
                    return TransResult.SUCC;
                } else {

                    //prabayar
                    if (transType == ETransType.PURCHASE_PULSA_DATA || transType == ETransType.INQ_PULSA_DATA) {

                        String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                                + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                        transData.setField48(dt48);
                        transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                                transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                                transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                                "#" + transData.getField63());

                        if (!TextUtils.isEmpty(transData.getAmount()) && transData.getAmount().length()<11){
                            transData.setAmount(transData.getAmount() + "00");
                        }

                        transData.setTransType(ETransType.OVERBOOKING_PULSA_DATA.toString());
                        Device.beepErr();
                        transData.saveDup();

                        //tri
                        if ("01".equals(retCode)) {
                            String prompt = transData.getField63().trim();
                            if (!StringUtils.isEmpty(prompt)) {
                                listener.onShowErrMessage(retCode + "\n" + prompt,
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                listener.onShowErrMessage(responseCode.getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }
                            listener.onShowErrMessage("Auto Reversal \n Please wait", 2);
                            sendAutoReversal(listener); //3 PURCHASE_PULSA_DATA INQ_PULSA_DATA

                            return TransResult.ERR_ABORTED;
                        }

                        //pascabayar
                    } else if (transType == ETransType.PASCABAYAR_INQUIRY) {
                        Log.d("teg", "-----------------pascabayar--------------------");
                        String bit48 = "bit48";
                        String billId = transData.getPhoneNo();
                        String productCode = transData.getProduct_code();
                        transData.setField47(bit48 + "#" + billId + "#" + productCode); //dipake f56 overbooking
                        Log.d("teg", "bit 47 " + transData.getField47());
                        Log.d("teg", "-----------------pascabayar--------------------");

                        String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                                + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                        transData.setField48(dt48);

                        transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                                transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                                transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                                "#" + transData.getField63());

                        if (!TextUtils.isEmpty(transData.getAmount()) && transData.getAmount().length()<11){
                            transData.setAmount(transData.getAmount() + "00");
                        }

                        transData.setTransType(ETransType.PASCABAYAR_OVERBOOKING.toString());
                        Device.beepErr();
                        transData.saveDup();

                        //tri
                        if ("01".equals(retCode)) {
                            String prompt = transData.getField63().trim();
                            if (!StringUtils.isEmpty(prompt)) {
                                listener.onShowErrMessage(retCode + "\n" + prompt,
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                listener.onShowErrMessage(responseCode.getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }
                            listener.onShowErrMessage("Auto Reversal \n Please wait", 2);

                            //sendAutoReversal(listener, transData.getTransNo()-1); //4 PASCABAYAR_INQUIRY
                            sendAutoReversal(listener); //4 PASCABAYAR_INQUIRY

                            return TransResult.ERR_ABORTED;
                        }

                        //pdam pascabayar
                    }  else if (transType == ETransType.PDAM_INQUIRY) {

                        String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                                + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                        transData.setField48(dt48);
                        transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                                transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                                transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                                "#" + transData.getField63());

                        if (!TextUtils.isEmpty(transData.getAmount()) && transData.getAmount().length()<11){
                            transData.setAmount(transData.getAmount() + "00");
                        }

                        transData.setTransType(ETransType.PDAM_OVERBOOKING.toString());
                        Device.beepErr();
                        transData.saveDup();

                        //tri
                        if ("01".equals(retCode)) {
                            String prompt = transData.getField63().trim();
                            if (!StringUtils.isEmpty(prompt)) {
                                listener.onShowErrMessage(retCode + "\n" + prompt,
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                listener.onShowErrMessage(
                                        retCode + "\n" +responseCode.getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }

                            listener.onShowErrMessage("Auto Reversal \n Please wait", 2);

                            //sendAutoReversal(listener, transData.getTransNo()-1); //5 PDAM_INQUIRY
                            sendAutoReversal(listener); //5 PDAM_INQUIRY

                            return TransResult.ERR_ABORTED;
                        }
                    }else if (transType == ETransType.BPJS_TK_PEMBAYARAN ) {

                        transData.setTransType(ETransType.BPJS_OVERBOOKING.toString());
                        Device.beepErr();
                        transData.saveDup();

                        //just follow what SWI has been done before
                        if ("01".equals(retCode)) {
                            String prompt = transData.getField63().trim();
                            if (!StringUtils.isEmpty(prompt)) {
                                listener.onShowErrMessage(retCode + "\n" + prompt,
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                listener.onShowErrMessage(responseCode.getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }
                            listener.onShowErrMessage("Auto Reversal \n Please wait", 2);
                            sendAutoReversal(listener);

                            return TransResult.ERR_ABORTED;
                        }

                        //pascabayar
                    }

                    Log.d("teg", "0");

                    TransData.deleteDupRecord();
                    if ("55".equals(retCode)) {
                        if (listener != null) {
                            Device.beepErr();
//                            listener.onShowErrMessageWithConfirm(
                            listener.onShowErrMessage(
                                    retCode + "\n" +responseCode.getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        continue;
                    }

                    if (listener != null) {
                        Device.beepErr();

                        //tri
                        if (!"00".equals(transData.getResponseCode())) {
                            String msg = "";
                            msg = transData.getField120();
                            if (isDisplayErrFromHost(transType, retCode) && (!StringUtils.isEmpty(msg))) {

                                listener.onShowErrMessage(
                                        FinancialApplication.getAppContext().getString(R.string.emv_err_code) + retCode
                                                + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                                + msg, Constants.FAILED_DIALOG_SHOW_TIME);
                                return TransResult.ERR_ABORTED;
                            }else {

                                if (isPPOBTransAndRc01(transType, retCode)){
                                    String err = "";
                                    err = transData.getField63();

                                    listener.onShowErrMessage(
                                            FinancialApplication.getAppContext().getString(R.string.emv_err_code) + retCode
                                                    + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                                    + err, Constants.FAILED_DIALOG_SHOW_TIME);
                                    return TransResult.ERR_ABORTED;

                                }else {
//                                    listener.onShowErrMessageWithConfirm(
                                    listener.onShowErrMessage(
                                            FinancialApplication.getAppContext().getString(R.string.emv_err_code) + retCode
                                                    + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                                    + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                                    return TransResult.ERR_ABORTED;
                                }
                            }
                        }
                        //end

                        //asli
                        /*if (transData.getTransType().equals(ETransType.QR_INQUIRY.toString()) == Boolean.FALSE) {
                            listener.onShowErrMessageWithConfirm(
                                    FinancialApplication.getAppContext().getString(R.string.emv_err_code) + retCode
                                            + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                            + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                        }*/

                    }
                    return TransResult.ERR_HOST_REJECT;
                }
            } else if (ret == TransResult.ERR_RECV) {
                // abdul add
                if (transType == ETransType.INQ_PULSA_DATA ||
                        transType == ETransType.PURCHASE_PULSA_DATA ||
                        transType == ETransType.PASCABAYAR_INQUIRY ||
                        transType == ETransType.PASCABAYAR_PURCHASE ||
                        transType == ETransType.PDAM_INQUIRY ||
                        transType == ETransType.PDAM_PURCHASE

                ) {

                    return TransResult.ERR_RECV;
                }

                if (transType == ETransType.OVERBOOKING_PULSA_DATA ||
                        transType == ETransType.PASCABAYAR_OVERBOOKING ||
                        transType == ETransType.PDAM_OVERBOOKING ||
                        transType == ETransType.BPJS_OVERBOOKING
                ) {
                    String dt48 = Component.getPaddedString(transData.getPhoneNo(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                            + Component.getPaddedString(transData.getProduct_code(), 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);
                    transData.setField48(dt48);
                    transData.setField47(transData.getField48() + "#" + transData.getPhoneNo() + "#" +
                            transData.getProduct_code() + "#" + transData.getTypeProduct() + "#" +
                            transData.getOperator() + "#" + transData.getKeterangan() + "#" + transData.getProduct_name() +
                            "#" + transData.getField63());
                }

                boolean isDupSend = transType.isDupSend();

                //dia add
                //Sandy : i dont want to modify this, as there will be impacted to the others
                if (isDupSend) {
                    listener.onShowErrMessage("Timeout", 2);
                    listener.onShowErrMessage("Auto Reversal \n Please wait", 2);
                    sendAutoReversal(listener); //6 ERR_RECV
                }

                //Sandy : Just add this one for auto reversal
                if (transType == ETransType.BPJS_TK_PEMBAYARAN ) {
                    listener.onShowErrMessage("Timeout", 2);
                    listener.onShowErrMessage("Auto Reversal \n Please wait", 2);
                    sendAutoReversal(listener);
                }


            }

            break;
        }
        if (i == 3) {
            return TransResult.ERR_ABORTED;
        }
        return ret;
    }

    boolean isPPOBTransAndRc01(ETransType transType, String retCode){
        if (retCode.equals("01")) {
            switch (transType) {
                case PASCABAYAR_PURCHASE:
                case PASCABAYAR_INQUIRY:
                case PDAM_PURCHASE:
                case PDAM_INQUIRY:
                case PURCHASE_PULSA_DATA:
                case INQ_PULSA_DATA:
                case BPJS_TK_VERIFICATION:
                case BPJS_TK_PENDAFTARAN:
                case BPJS_TK_INQUIRY:
                    //case BPJS_TK_PEMBAYARAN: //send it to BPJSPendaftaranTrans.java
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }


    //MPN, PBB, E-SAMSAt
    boolean isDisplayErrFromHost(ETransType transType, String retCode) {
        return true;
    }

    //deprecated
    @Deprecated
    boolean isDisplayErrFromHost1(ETransType transType, String retCode) {

        if (TextUtils.isEmpty(retCode)) return false;

        String[] retcodeMPN = {"01", "02", "03", "04", "14", "27", "30", "31", "32", "51", "75", "76", "83", "88", "90", "91", "92", "97", "98", "99"};
        String[] retcodePBB = {"54", "55", "76", "85", "88"};
        String[] retcodeEsamsatInquiry = {"01", "02", "03", "04", "05", "06", "09", "10", "11", "12", "13", "14", "54", "55", "56", "76"};
        String[] retcodeEsamsat = {"21", "22", "23", "24", "97", "98", "99"};

        switch (transType) {
            case DIRJEN_PAJAK_INQUIRY:
            case DIRJEN_PAJAK:
            case DIRJEN_BEA_CUKAI_INQUIRY:
            case DIRJEN_BEA_CUKAI:
            case DIRJEN_ANGGARAN_INQUIRY:
            case DIRJEN_ANGGARAN:
            case CETAK_ULANG:
                for (int i = 0; i < retcodeMPN.length; i++) {
                    if (retCode.equals(retcodeMPN[i])) {
                        return true;
                    }
                }
                break;

            case PBB_INQ:
            case PBB_PAY:
                for (int i = 0; i < retcodePBB.length; i++) {
                    if (retCode.equals(retcodePBB[i])) {
                        return true;
                    }
                }
                break;

            case E_SAMSAT_INQUIRY:
                for (int i = 0; i < retcodeEsamsatInquiry.length; i++) {
                    if (retCode.equals(retcodeEsamsatInquiry[i])) {
                        return true;
                    }
                }
                break;
            case E_SAMSAT:
                for (int i = 0; i < retcodeEsamsat.length; i++) {
                    if (retCode.equals(retcodeEsamsat[i])) {
                        return true;
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * 脚本结果上送
     *
     * @return
     */
    public int sendScriptResult(TransProcessListener listener) {
        //TransData scriptTransData = TransData.readScript();
        //Sandy : disable the Issuer Script Uploader
        TransData scriptTransData = null;
        if (scriptTransData == null) {
            return TransResult.SUCC;
        }

        scriptTransData.setOrigTransType(scriptTransData.getTransType());
        scriptTransData.setTransType(ETransType.IC_SCR_SEND.toString());

        // 流水号+1
        Component.incTransNo();
        Component.transInit(scriptTransData);

        int retry = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.RESEND_TIMES));
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.IC_SCR_SEND.getTransName());
        }

        int ret = 0;
        for (int i = 0; i < (retry + 1); i++) {
            ret = Online.getInstance().online(scriptTransData, listener);
            if (ret == TransResult.SUCC) {
                break;
            }
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_PACK || ret == TransResult.ERR_SEND) {
                if (listener != null) {
//                    listener.onShowErrMessageWithConfirm(
                    listener.onShowErrMessage(
                            TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }
                return TransResult.ERR_ABORTED;
            }
            continue;
        }

        TransData.deleteScript();
        return ret;
    }

    /**
     * 冲正处理
     *
     * @return
     */
    public int sendReversal(TransProcessListener listener) {
        TransData dupTransData = TransData.readDupRecord();
        if (dupTransData == null) {
            return TransResult.SUCC;
        }

        long transNo = dupTransData.getTransNo();
        String dupReason = dupTransData.getReason();

        ETransType transType = ETransType.valueOf(dupTransData.getTransType());
        if (transType == ETransType.AUTHVOID
                || transType == ETransType.AUTHCMVOID
                || transType == ETransType.AUTHCM
                || transType == ETransType.VOID) {
            dupTransData.setOrigAuthCode(dupTransData.getOrigAuthCode());
        } else {
            dupTransData.setOrigAuthCode(dupTransData.getAuthCode());
        }
        dupTransData.setIsReversal(true);
        Component.transInit(dupTransData);

        dupTransData.setTransNo(transNo);
        dupTransData.setReason(dupReason);

        if (listener != null) {
            listener.onUpdateProgressTitle(FinancialApplication.getAppContext().getString(R.string
                    .reverse_prompt));
        }

        int ret = 0;
        if (transType == ETransType.COUPON_SALE || transType == ETransType.COUPON_VERIFY) {
            ret = couponSaleReversal(dupTransData, listener);
        } else {
            ret = normalReversal(dupTransData, listener);
        }

        //if success then delete it
        if (ret == TransResult.SUCC) {
            Log.d("teg", "1");
            TransData.deleteDupRecord();
        } else {
            if (listener != null) {
//                listener.onShowErrMessageWithConfirm(
                listener.onShowErrMessage(
                        FinancialApplication.getAppContext().getString(R.string.reverse_err),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }

            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReason(TransData.REASON_NO_RECV);
            }
            TransData.updateDupRecord(dupTransData);
        }

        return ret;
    }

    public int sendAutoReversal(TransProcessListener listener) {
        TransData dupTransData = TransData.readDupRecord();
        if (dupTransData == null) {
            return TransResult.SUCC;
        }

        long transNo = dupTransData.getTransNo();
        String dupReason = dupTransData.getReason();

        ETransType transType = ETransType.valueOf(dupTransData.getTransType());
        if (transType == ETransType.AUTHVOID
                || transType == ETransType.AUTHCMVOID
                || transType == ETransType.AUTHCM
                || transType == ETransType.VOID) {
            dupTransData.setOrigAuthCode(dupTransData.getOrigAuthCode());
        } else {
            dupTransData.setOrigAuthCode(dupTransData.getAuthCode());
        }
        dupTransData.setIsReversal(true);
        Component.transInit(dupTransData);

        dupTransData.setTransNo(transNo);
        dupTransData.setReason(dupReason);

        if (listener != null) {
            listener.onUpdateProgressTitle(FinancialApplication.getAppContext().getString(R.string
                    .reverse_prompt));
        }

        int ret = 0;
        ret = autoReversal(dupTransData, listener);

        if (ret == TransResult.SUCC) {
            Log.d("teg", "2x");
            TransData.deleteDupRecord();
            return ret;
        } else {
            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReason(TransData.REASON_NO_RECV);
                TransData.updateDupRecord(dupTransData);
            }

            if (listener != null) {
//                listener.onShowErrMessageWithConfirm(
                listener.onShowErrMessage(
                        FinancialApplication.getAppContext().getString(R.string.reverse_err),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }

        return ret;
    }

    //tri
    public int sendAutoReversal(TransProcessListener listener, long trnsNo) {
        TransData dupTransData = TransData.readDupRecord();
        if (dupTransData == null) {
            return TransResult.SUCC;
        }

        long transNo = trnsNo;
        String dupReason = dupTransData.getReason();

        ETransType transType = ETransType.valueOf(dupTransData.getTransType());
        if (transType == ETransType.AUTHVOID
                || transType == ETransType.AUTHCMVOID
                || transType == ETransType.AUTHCM
                || transType == ETransType.VOID) {
            dupTransData.setOrigAuthCode(dupTransData.getOrigAuthCode());
        } else {
            dupTransData.setOrigAuthCode(dupTransData.getAuthCode());
        }
        dupTransData.setIsReversal(true);
        Component.transInit(dupTransData);

        dupTransData.setTransNo(transNo);
        dupTransData.setReason(dupReason);

        if (listener != null) {
            listener.onUpdateProgressTitle(FinancialApplication.getAppContext().getString(R.string
                    .reverse_prompt));
        }

        int ret = 0;
        ret = autoReversal(dupTransData, listener);

        if (ret == TransResult.SUCC) {
            Log.d("teg", "2x");
            TransData.deleteDupRecord();
            return ret;
        } else {
            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReason(TransData.REASON_NO_RECV);
                TransData.updateDupRecord(dupTransData);
            }

            if (listener != null) {
//                listener.onShowErrMessageWithConfirm(
                listener.onShowErrMessage(
                        FinancialApplication.getAppContext().getString(R.string.reverse_err),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }

        return ret;
    }

    private int autoReversal(TransData dupTransData, TransProcessListener listener) {
        //int retry = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.REVERSL_CTRL));
        int retry = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_DTIMES)); //tri, SysParam.PTAG_MODEM_DTIMES --> dari download param tag 13
        Log.d("teg", "retry : " + retry);
        Log.d("teg", "time [bit12] : " + dupTransData.getTime());
        int ret = 0;
        for (int i = 0; i < retry; i++) {

            ret = Online.getInstance().online(dupTransData, listener);
            if (ret == TransResult.SUCC) {
                String retCode = dupTransData.getResponseCode();
                Log.d("teg", "autoReversal");

                if ("00".equals(retCode) || "12".equals(retCode) || "25".equals(retCode)) {
                    Log.d("teg", "3");
                    TransData.deleteDupRecord();
                    return TransResult.SUCC;
                }
                dupTransData.setReason(TransData.REASON_OTHERS);
                if (i == (retry - 1)) {
                    return TransResult.ERR_HOST_REJECT;
                } else {
                    continue;
                }

            } else if (ret == TransResult.ERR_MAC) {

                return TransResult.ERR_MAC;
            }
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_PACK || ret == TransResult.ERR_SEND) {
                if (listener != null) {
                    listener.onShowErrMessageWithConfirm(
                            TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }

                return TransResult.ERR_ABORTED;
            }

            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReason(TransData.REASON_NO_RECV);
            }
            Log.d("teg", "ret reversal " + ret);
            continue;
        }

        return ret;
    }

    private int normalReversal(TransData dupTransData, TransProcessListener listener) {
        int retry = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.REVERSL_CTRL));
        int ret = 0;
        for (int i = 0; i < 1; i++) {
            ret = Online.getInstance().online(dupTransData, listener);
            if (ret == TransResult.SUCC) {
                String retCode = dupTransData.getResponseCode();
                // If the charging is received with a response code of 12 or 25,
                // it should default to successful charging.
                if ("00".equals(retCode) || "12".equals(retCode) || "25".equals(retCode)) {
                    return TransResult.SUCC;
                }
                dupTransData.setReason(TransData.REASON_OTHERS);
                //denny add
                return TransResult.ERR_HOST_REJECT;
                /*if (i == (retry-1)){

                }else {
                    continue;
                }*/
            } else if (ret == TransResult.ERR_MAC) {
                // 冲正如果mac错,则跳出循环,下一次联机交易还是要进行冲正
                return TransResult.ERR_MAC;
            }

            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_PACK || ret == TransResult.ERR_SEND) {
                if (listener != null) {
                    listener.onShowErrMessageWithConfirm(
                            TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }

                return TransResult.ERR_ABORTED;
            }

            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReason(TransData.REASON_NO_RECV);
            }
            continue;
        }

        return ret;
    }

    private int couponSaleReversal(TransData data, TransProcessListener listener) {
        int result = 0;
        int retry = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.REVERSL_CTRL));
        ETransType transType = ETransType.valueOf(data.getTransType());
        for (int i = 0; i < retry; i++) {
            if (transType == ETransType.COUPON_SALE) {
                result = reverseCouponSale(data, listener);
                result = processReversalResult(result, listener);
                if (result == TransResult.ERR_RECV) {
                    data.setReason(TransData.REASON_NO_RECV);
                    continue;
                }
                if (result != TransResult.SUCC) {
                    return result;
                }

                String retCode = data.getResponseCode();
                if (!"00".equals(retCode) && !"12".equals(retCode) && !"25".equals(retCode)) {
                    data.setReason(TransData.REASON_OTHERS);
                    continue;
                }

                long total = Long.parseLong(data.getActualPayAmount());
                long discount = Long.parseLong(data.getDiscountAmount());
                total = total + discount;
                data.setAmount(String.format("%012d", total));
                data.setTransType(ETransType.COUPON_VERIFY.toString());
                TransData.updateDupRecord(data);
            }


            data.setHeader("609100321301");
            result = reverseCouponVerify(data, listener);
            result = processReversalResult(result, listener);
            if (result == TransResult.ERR_RECV) {
                data.setReason(TransData.REASON_NO_RECV);
                continue;
            }
            if (result != TransResult.SUCC) {
                return result;
            }

            String retCode = data.getResponseCode();
            if (!"00".equals(retCode) && !"12".equals(retCode) && !"25".equals(retCode)) {
                data.setReason(TransData.REASON_OTHERS);
                continue;
            }

            break;
        }

        return result;
    }

    private int reverseCouponSale(TransData data, TransProcessListener listener) {
        return Online.getInstance().online(data, listener);
    }

    private int reverseCouponVerify(TransData data, TransProcessListener listener) {
        return Online.getInstance().online(data, listener);
    }

    private int processReversalResult(int result, TransProcessListener listener) {
        if (result == TransResult.ERR_CONNECT || result == TransResult.ERR_PACK
                || result == TransResult.ERR_SEND) {
            if (listener != null) {
                //listener.onShowErrMessageWithConfirm(
                listener.onShowErrMessage(
                        TransResult.getMessage(FinancialApplication.getAppContext(), result),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }

            return TransResult.ERR_ABORTED;
        }

        return result;
    }

    /**
     * 脱机交易上送
     *
     * @param isOnline 是否为在下笔联机交易到来之前上送
     * @return
     */
    public int sendOfflineTrans(TransProcessListener listener, boolean isOnline) {

        int ret = TransOnline.offlineTransSend(listener, isOnline);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }
        if (ret != TransResult.SUCC) {
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(
                        TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }
        return ret;
    }

    /**
     * 联机交易电子签名上送
     *
     * @return
     */
    public int sendOnlineSignature(long currTransNo, TransProcessListener listener) {
        int ret = TransOnline.onlineSignatureSend(currTransNo, listener);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }
        if (ret != TransResult.SUCC) {
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(
                        TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }

        return ret;
    }

    /**
     * 离线类交易电子签名上送
     *
     * @return
     */
    public int sendOfflineSignature(TransProcessListener listener) {
        int ret = TransOnline.offlineSignatureSend(listener);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }
        if (ret != TransResult.SUCC) {
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(
                        TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }

        return ret;
    }

    /**
     * 重新上送失败签名
     *
     * @return
     */
    public int resendErrSignature(TransProcessListener listener) {

        int ret = TransOnline.errorSignatureResend(listener);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }
        if (ret != TransResult.SUCC) {
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(
                        TransResult.getMessage(TransContext.getInstance().getCurrentContext(), ret),
                        Constants.FAILED_DIALOG_SHOW_TIME);
            }
        }
        return ret;
    }
}
