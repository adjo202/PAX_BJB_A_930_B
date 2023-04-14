package com.pax.pay.emv;

import android.content.Context;
import android.os.ConditionVariable;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.TrackUtils;
import com.pax.device.Device;
import com.pax.eemv.IEmvBase;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.Online;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.ResponseCode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;


/**
 * Created by wangyq on 2017/7/27.
 */

public class EmvBaseListenerImpl {
    private static final String TAG = "EmvBaseListenerImpl";
    protected Context context;
    protected TransData transData;
    protected TransProcessListener transProcessListener;
    protected ConditionVariable cv;
    protected int intResult;
    private IEmvBase emvBase;

    protected EmvBaseListenerImpl(Context context, IEmvBase emvBase, TransData transData,
                                  TransProcessListener listener) {
        this.context = context;
        this.transData = transData;
        this.emvBase = emvBase;
        this.transProcessListener = listener;
    }

    protected EOnlineResult onlineProc() {
        IConvert convert = FinancialApplication.getConvert();
        try {
            ETransType transType = ETransType.valueOf(transData.getTransType());
            // read ARQC
            byte[] arqc = emvBase.getTlv(0x9f26);
            if (arqc != null && arqc.length > 0) {
                transData.setArqc(convert.bcdToStr(arqc));
            }
            // //IT26卡 纯电子现金 “消费” 消费金额超过余额时转联机
            byte[] aid = emvBase.getTlv(0x4f);
            if (aid != null && aid.length > 0) {
                // If the transaction is not electronic cash deposit-designated account,
                // electronic cash deposit-non-designated account, electronic cash cash recharge,
                // electronic cash cash recharge cancellation and other electronic cash, it will be terminated
                if (transType != ETransType.EC_CASH_LOAD && transType != ETransType.EC_LOAD
                        && transType != ETransType.EC_TRANSFER_LOAD && transType != ETransType
                        .EC_CASH_LOAD_VOID) {
                    boolean ret = FinancialApplication.getGl().getUtils().isByteArrayValueSame(aid, 0,
                            new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x06}, 0,
                            8);
                    if (ret) {
                        //纯电子现金无法联机
                        if (transProcessListener != null) {

                            if (transType == ETransType.AUTH || transType == ETransType.AUTH_SETTLEMENT
                                    || transType == ETransType.AUTHCM || transType == ETransType.AUTHCMVOID
                                    || transType == ETransType.AUTHVOID) {
                                transProcessListener.onShowErrMessageWithConfirm(
                                        context.getString(R.string
                                                .emv_err_auth_trans_can_not_use_pure_card),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            } else {
                                transProcessListener.onShowErrMessageWithConfirm(
                                        context.getString(R.string
                                                .emv_err_pure_card_can_not_online),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }

                        }
                        return EOnlineResult.ABORT;
                    }
                }
            }

            // 如果上一笔联机失败， 下笔脱机交易强制联机
            if (transType == ETransType.EC_SALE) {
                transType = ETransType.SALE;
                transData.setTransType(transType.toString());
            }


            Transmit.getInstance().sendScriptResult(transProcessListener);

            int sendReversal = Transmit.getInstance().sendReversal(transProcessListener);
            if (sendReversal == TransResult.ERR_MAC) {
                transProcessListener.onShowErrMessageWithConfirm(
                        context.getString(R.string.err_mac),
                        Constants.FAILED_DIALOG_SHOW_TIME);
                return EOnlineResult.ABORT;
            }
            if (sendReversal != TransResult.SUCC) {
                return EOnlineResult.ABORT;
            }

            if (FinancialApplication.getSysParam().get(SysParam.OFFLINETC_UPLOAD_TYPE).equals(SysParam
                    .Constant.OFFLINETC_UPLOAD_NEXT))
                Transmit.getInstance().sendOfflineTrans(transProcessListener, true);

            // 生成联机的55域数据
            byte[] f55 = EmvTags.getF55(emvBase, transType, false, false);
            byte[] f55Dup = EmvTags.getF55(emvBase, transType, true, false);
            //String sf55 = convert.bcdToStr(f55);

            transData.setSendIccData(convert.bcdToStr(f55));
            Log.i("abdul", "seticcdata = " + transData.getSendIccData());
            if (f55Dup != null && f55Dup.length > 0) {
                transData.setDupIccData(convert.bcdToStr(f55Dup));
            }

            // 非指定账户圈存
            if (transType == ETransType.EC_TRANSFER_LOAD) {

                byte[] track2 = emvBase.getTlv(0x57);
                String strTrack2 = convert.bcdToStr(track2);
                strTrack2 = strTrack2.split("F")[0];
                // 卡号
                String pan = TrackUtils.getPan(strTrack2);
                transData.setTransferPan(pan);

                // 获取卡片序列号
                byte[] cardSeq = emvBase.getTlv(0x5f34);
                if (cardSeq != null && cardSeq.length > 0) {
                    String temp = convert.bcdToStr(cardSeq);
                    transData.setCardSerialNo(temp.substring(0, 2));
                }
            } else {
                Component.saveCardInfoAndCardSeq(transData);
            }

            // 联机通讯
            int commResult = 0;
            int ret = -1;
            if (transProcessListener != null) {
                transProcessListener.onUpdateProgressTitle(transType.getTransName());
            }

            if (transType == ETransType.COUPON_VERIFY) {

                ret = Online.getInstance().online(transData, transProcessListener);
                if (ret == TransResult.SUCC) {
                    //transData.setHeader("603200321301");
                    transData.setTransType(ETransType.COUPON_SALE.toString());
                    transType = ETransType.COUPON_SALE;

                    byte[] bytes = convert.strToBcd(transData.getField62(), IConvert.EPaddingPosition.PADDING_LEFT);
                    String discountInfo = new String(bytes);

                    String discountAmount = String.valueOf(Long.parseLong(discountInfo.substring(42, 54)));
                    String actualAmount = String.valueOf(Long.parseLong(discountInfo.substring(28, 40)));

                    transData.setActualPayAmount(actualAmount);
                    transData.setDiscountAmount(discountAmount);

                    //sandy
                    if (!transData.getResponseCode().equals("00")) {
                        ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
                        Log.d("teg", "4");
                        TransData.deleteDupRecord();
                        Device.beepErr();
                        transProcessListener.onShowErrMessageWithConfirm(
                                context.getString(R.string.emv_err_code) + responseCode.getCode()
                                        + context.getString(R.string.emv_err_info) + responseCode.getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                        return EOnlineResult.DENIAL;
                    }

                    //Log.d(TAG,"sandy.Coupon.origDateTimeTrans.1 " + transData.getOrigDateTimeTrans());
                    //sandy
                    //hold the value
                    transData.setOrigCouponDateTimeTrans(transData.getDateTimeTrans());
                    transData.setOrigCouponRefNo(transData.getRefNo());

                    //then it should be new value
                    transData.setDate(Device.getDate().substring(4));
                    transData.setTime(Device.getTime());
                    transData.setDateTimeTrans(Long.parseLong(String.format("%s%s", transData.getDate(), transData.getTime())));
                    transData.setOrigEnterMode(transData.getEnterMode());
                    transData.setOrigHasPin(transData.getHasPin());


                } else {
                    if (transProcessListener != null) {
                        transProcessListener.onShowErrMessageWithConfirm(TransResult.getMessage(context, ret), Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                    return EOnlineResult.ABORT;
                }


            }


            ret = Online.getInstance().online(transData, transProcessListener);
            if (ret == TransResult.SUCC) {
                if (transType == ETransType.COUPON_SALE) {
                    try {
                        long total = Long.parseLong(transData.getActualPayAmount());
                        long discount = Long.parseLong(transData.getDiscountAmount());
                        total = total + discount;
                        transData.setAmount(String.valueOf(total));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                        .getResponseCode());
                transData.setResponseMsg(responseCode.getMessage());
                if ("A".equals(responseCode.getCategory())) {
                    commResult = 1;
                    if (!"00".equals(transData.getResponseCode())) {
                        if (transProcessListener != null) {
                            //transProcessListener.onShowErrMessageWithConfirm(responseCode
                            transProcessListener.onShowErrMessage(responseCode
                                            .getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                } else { // 联机拒绝
                    commResult = 2;
                }

                //tri add
                //rc 95 Unmatch price
                if (transData.getResponseCode().equals("95")) {
                    switch (transType) {
                        case OVERBOOKING_PULSA_DATA:
                        case PASCABAYAR_OVERBOOKING:
                        case PDAM_OVERBOOKING:

                            if (transProcessListener != null) {
                                //transProcessListener.onShowErrMessageWithConfirm(responseCode
                                transProcessListener.onShowErrMessage(responseCode
                                                .getMessage(),
                                        Constants.FAILED_DIALOG_SHOW_TIME);
                            }

                            TransData.deleteDupRecord();
                            return EOnlineResult.APPROVE;
                    }

                }


                //gak dipake, spek fase 2
                /*if (transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69")){
                    switch (transType){
                        case PBB_PAY:
                        case DIRJEN_ANGGARAN:
                        case DIRJEN_PAJAK:
                        case DIRJEN_BEA_CUKAI:
                            return EOnlineResult.APPROVE;

                    }
                }*/

                //Sandy : as per BJB, skip to check emv
                if (transType == ETransType.ACCOUNT_LIST) { // add abdul tarik tunai
                    if ("00".equals(transData.getResponseCode())) {
                        return EOnlineResult.APPROVE;
                    } else {
                        if (!"00".equals(transData.getResponseCode())) {
                            if (transProcessListener != null) {
                                //transProcessListener.onShowErrMessageWithConfirm(responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                                transProcessListener.onShowErrMessage(responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                            }
                        }
                    }
                }

                //tri, fase 2 : nampilin resp message dari host f120
                if (!"00".equals(transData.getResponseCode())) {
                    if (transProcessListener != null) {

                        // Sandy : if transfer antar bank and got response 68 then just approved it and
                        // display the receipt (11/03/2022)
                        if((transType == ETransType.TRANSFER || transType == ETransType.TRANSFER_2) &&
                                transData.getResponseCode().equals("68")){
                                return EOnlineResult.APPROVE;
                        }


                        String msg = "";
                        msg = transData.getField120();
                        if (isDisplayErrFromHost(transType, transData.getResponseCode()) && (!StringUtils.isEmpty(msg))) {

                            transProcessListener.onShowErrMessage(
                                    FinancialApplication.getAppContext().getString(R.string.emv_err_code) + transData.getResponseCode()
                                            + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                            + msg, Constants.FAILED_DIALOG_SHOW_TIME);
                            return EOnlineResult.ABORT;
                        } else {
                            transProcessListener.onShowErrMessage(
                                    FinancialApplication.getAppContext().getString(R.string.emv_err_code) + transData.getResponseCode()
                                            + FinancialApplication.getAppContext().getString(R.string.emv_err_info)
                                            + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                            return EOnlineResult.ABORT;

                        }




                    }
                }
                //end


            } else {
                /*if (transProcessListener != null) {
                    transProcessListener.onShowErrMessageWithConfirm(TransResult.getMessage
                                    (context, ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }*/
                if (ret == TransResult.ERR_RECV) {

                    //tri
                    switch (transType) {
                        case PBB_PAY:
                        case DIRJEN_ANGGARAN:
                        case DIRJEN_PAJAK:
                        case DIRJEN_BEA_CUKAI:
                            transData.setReason(TransData.REASON_NO_RECV);
                            return EOnlineResult.APPROVE;
                        /*case OVERBOOKING_PULSA_DATA:
                        case PASCABAYAR_OVERBOOKING:
                        case PDAM_OVERBOOKING:*/
                    }

                    if (transType.isDupSend()) {
                        //kd add for Auto reversal
                        if (transType != ETransType.ACCOUNT_LIST) {
                            Log.d("teg", "sendAutoReversal");
                            /*transData.setDupTransNo(transData.getTransNo());
                            Log.d("teg", "EMV DupTransNo 1 : "+transData.getDupTransNo()+" - "+"TransNo : "+transData.getTransNo());*/
                            transProcessListener.onShowErrMessage("Timeout", 2);
                            transProcessListener.onShowErrMessage("Auto Reversal \n Please wait", 2);
                            int res = new Transmit().sendAutoReversal(transProcessListener); //1 ERR_RECV
                            /*if (res == 0) {
                                transData.setPrintTimeout("NP");
                                return EOnlineResult.APPROVE; //tri
                            }*/
                        }
                    }

                }
                return EOnlineResult.ABORT;
            }


            String rspF55 = transData.getRecvIccData();
            ITlv tlv = FinancialApplication.getPacker().getTlv();
            if (rspF55 != null && rspF55.length() > 0) {
                // 设置授权数据
                byte[] resp55 = convert.strToBcd(rspF55, IConvert.EPaddingPosition.PADDING_LEFT);
                ITlvDataObjList list = tlv.unpack(resp55);

                byte[] value91 = list.getValueByTag(0x91);
                if (value91 != null && value91.length > 0) {
                    try {
                        emvBase.setTlv(0x91, value91);
                    } catch (EmvException e) {
                        Log.e(TAG, "", e);
                    }
                }
                // 设置脚本 71
                byte[] value71 = list.getValueByTag(0x71);
                if (value71 != null && value71.length > 0) {
                    try {
                        emvBase.setTlv(0x71, value71);
                    } catch (EmvException e) {
                        Log.e(TAG, "", e);
                    }
                }

                // 设置脚本 72
                byte[] value72 = list.getValueByTag(0x72);
                if (value72 != null && value72.length > 0) {
                    try {
                        emvBase.setTlv(0x72, value72);
                    } catch (EmvException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }

            if (commResult != 1) {
                Log.d("teg", "5");
                TransData.deleteDupRecord();
                Device.beepErr();
                transProcessListener.onShowErrMessageWithConfirm(
                        context.getString(R.string.emv_err_code) + transData.getResponseCode()
                                + context.getString(R.string.emv_err_info) + transData
                                .getResponseMsg(),
                        Constants.FAILED_DIALOG_SHOW_TIME);
                return EOnlineResult.DENIAL;
            }
            // 设置授权码
            String authCode = transData.getAuthCode();
            if (authCode != null && authCode.length() > 0) {
                try {
                    emvBase.setTlv(0x89, authCode.getBytes());
                } catch (EmvException e) {
                    Log.e(TAG, "", e);
                }
            }
            try {
                emvBase.setTlv(0x8A, "00".getBytes());
            } catch (EmvException e) {
                Log.e(TAG, "", e);
            }
            return EOnlineResult.APPROVE;

        } catch (TlvException | IndexOutOfBoundsException e) {
            Log.d("teg", "sendAutoReversal emv error");
            Log.e(TAG, "", e);
            Device.beepErr();
            transProcessListener.onShowErrMessage("Bit55 EMV Error", 2);
            transProcessListener.onShowErrMessage("Auto Reversal \n Please wait", 2);
            new Transmit().sendAutoReversal(transProcessListener); //2 TlvException
        } finally {
            if (transProcessListener != null) {
                transProcessListener.onHideProgress();
            }
        }

        return EOnlineResult.FAILED;
    }

    boolean isDisplayErrFromHost(ETransType transType, String retCode) {

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

    protected void enterPin(boolean isOnlinePin) {
        Log.d(TAG, "Sandy.EmvBaseListenerImpl.enterPin is called! " + isOnlinePin);
        if (!isOnlinePin) {
            return;
        }
        final String header = context.getString(R.string.prompt_bankcard_pwd);
        final String subheader = context.getString(R.string.prompt_no_password);

        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                String pan = "";
                if (ETransType.valueOf(transData.getTransType()) == ETransType.EC_TRANSFER_LOAD) {
                    pan = transData.getPan();

                } else {

                    byte[] track2 = emvBase.getTlv(0x57);
                    String strTrack2 = FinancialApplication.getConvert().bcdToStr(track2);
                    strTrack2 = strTrack2.split("F")[0];
                    pan = strTrack2.split("D")[0];

                }
                ((ActionEnterPin) action).setParam(context, ETransType.valueOf(transData
                                .getTransType())
                                .getTransName(), pan, true, header, subheader, transData
                                .getAmount(),
                        EEnterPinType.ONLINE_PIN, transData.getEnterMode());

            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() == TransResult.SUCC) {
                    String data = (String) result.getData();
                    if (!TextUtils.isEmpty(data)) {
                        transData.setHasPin(true);
                        transData.setPin(data);
                        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                    } else {
                        intResult = EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement();
                    }
                } else {
                    intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                }

                if (cv != null) {
                    cv.open();
                }
            }
        });
        actionEnterPin.execute();

    }
}
