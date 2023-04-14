/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv.clss;

import android.content.Context;
import android.os.ConditionVariable;
import android.util.Log;

import com.pax.abl.utils.TrackUtils;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.device.Device;
import com.pax.device.DeviceImplNeptune;
import com.pax.eemv.IClss;
import com.pax.eemv.IClssListener;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.TagsTable;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvBaseListenerImpl;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.utils.Utils;
import com.pax.settings.rate.LoadRate;
import com.pax.up.bjb.R;

import java.math.BigDecimal;

public class ClssListenerImpl extends EmvBaseListenerImpl implements IClssListener {

    private static final String TAG = "ClssListenerImpl";
    private IClss clss;

    private static final String MASTER_MCHIP = "A0000000041010";
    private static final String MASTER_MAESTRO = "A0000000043060";

    private boolean detect2ndTap = false;

    public ClssListenerImpl(Context context, IClss clss, TransData transData,
                            TransProcessListener listener) {
        super(context, FinancialApplication.getClss(), transData, listener);
        this.clss = clss;
    }

    @Override
    public int onCvmResult(ECvmResult result) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        intResult = 0;
        cv = new ConditionVariable();
        if (result == ECvmResult.ONLINE_PIN) {
            enterPin(true);
            cv.block(); // for the Offline pin case, block it for make sure the PIN activity is
            // ready, otherwise, may get the black screen.
        }

        return intResult;
    }

    @Override
    public void onComfirmCardInfo(String track1, String track2, String track3) throws EmvException {
        transData.setTrack1(track1);
        transData.setTrack2(track2);
        transData.setTrack3(track3);

        Log.i(TAG, "sandy.onComfirmCardInfo:" + transData.getTrack2());

        String pan = TrackUtils.getPan(track2);
        if (pan == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_DATA);
        }
        transData.setPan(pan);


        String expDate = TrackUtils.getExpDate(transData.getTrack2());
        transData.setExpDate(expDate);

        //PanSeqNo
        byte[] value = clss.getTlv(TagsTable.PAN_SEQ_NO);
        if (value != null) {
            String cardSerialNo = Utils.bcd2Str(value);
            transData.setCardSerialNo(cardSerialNo.substring(0, value.length * 2));
        }
    }


    @Override
    public EOnlineResult onOnlineProc(CTransResult result) {
        return onlineProc();
    }

    @Override
    public boolean onDetect2ndTap() {
        final ConditionVariable cv = new ConditionVariable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (transData.getEnterMode() == TransData.EnterMode.QPBOC) {
                    transProcessListener.onShowProgress(context.getString(R.string
                            .prompt_wave_card), 30);
                }
                try {
                    //tap card
                    ICardReaderHelper helper = FinancialApplication.getDal().getCardReaderHelper();
                    helper.polling(EReaderType.PICC, 30 * 1000);
                    helper.stopPolling();
                    detect2ndTap = true;
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                } finally {
                    transProcessListener.onHideProgress();
                    cv.open();
                }
            }
        }).start();
        cv.block();
        return detect2ndTap;
    }

    @Override
    public byte[] onUpdateKernelCfg(String aid) {
        if (MASTER_MCHIP.equals(aid)) {
            return new byte[]{(byte) 0x20};
        } else if (MASTER_MAESTRO.equals(aid)) {
            return new byte[]{(byte) 0xA0};
        }
        return null;
    }

    @Override
    public int onIssScrCon() {
        ApduSendL2 apduSendL2 = new ApduSendL2();
        ApduRespL2 apduRespL2 = new ApduRespL2();
        byte[] sendCommand = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00};
        System.arraycopy(sendCommand, 0, apduSendL2.command, 0, sendCommand.length);
        apduSendL2.lc = 14;
        String sendDataIn = "1PAY.SYS.DDF01";
        System.arraycopy(sendDataIn.getBytes(), 0, apduSendL2.dataIn, 0, sendDataIn.getBytes()
                .length);
        apduSendL2.le = 256;
        int ret = (int) DeviceImplNeptune.getInstance().iccCommand(apduSendL2, apduRespL2);
        if (ret != RetCode.EMV_OK)
            return ret;

        if (apduRespL2.swa != (byte) 0x90 || apduRespL2.swb != 0x00)
            return RetCode.EMV_RSP_ERR;

        apduSendL2 = new ApduSendL2();
        apduRespL2 = new ApduRespL2();
        System.arraycopy(sendCommand, 0, apduSendL2.command, 0, sendCommand.length);
        apduSendL2.lc = 14;
        System.arraycopy(transData.getAid().getBytes(), 0, apduSendL2.dataIn, 0, transData.getAid
                ().getBytes().length);
        apduSendL2.le = 256;
        ret = (int) DeviceImplNeptune.getInstance().iccCommand(apduSendL2, apduRespL2);
        if (ret != RetCode.EMV_OK)
            return ret;

        if (apduRespL2.swa != (byte) 0x90 || apduRespL2.swb != 0x00)
            return RetCode.EMV_RSP_ERR;

        return RetCode.EMV_OK;
    }

    @Override
    public void onPromptRemoveCard() {
        final ConditionVariable cv = new ConditionVariable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Device.removeCard(new Device.RemoveCardListener() {
                    @Override
                    public void onShowMsg(PollingResult result) {
                        if (transProcessListener != null) {
                            transProcessListener.onHideProgress();
                            transProcessListener.onShowNormalMessageWithConfirm("Please Remove " +
                                            "Card",
                                    Constants.SUCCESS_DIALOG_SHOW_TIME);
                        }
                    }
                });
                cv.open();
            }
        }).start();
        cv.block();
    }


    @Override
    public long exchangeRate(String cardCode) {
        String transCode = FinancialApplication.getSysParam().getCurrency().getCode();
        int currencyExponent = FinancialApplication.getSysParam().getCurrency().getDecimals();
        long amount = Long.parseLong(transData.getAmount());
        if (cardCode.equals(transCode)) {
            return 0;
        }
        LoadRate loadRate = LoadRate.readRate(transCode, cardCode);
        if (loadRate == null) {
            return 0;
        }
        int rate = Integer.parseInt(loadRate.getRate().substring(1, 8));
        int rateExponent = Integer.parseInt(loadRate.getRate().substring(0, 1));

        BigDecimal calAmount = BigDecimal.valueOf((double)amount/Math.pow(10,currencyExponent));
        BigDecimal calRate = BigDecimal.valueOf((double)rate/Math.pow(10, rateExponent));
        double tempAmount = calAmount.multiply(calRate).setScale(currencyExponent,BigDecimal
                .ROUND_HALF_UP).doubleValue()*Math.pow(10,currencyExponent);
        long foreignAmount = BigDecimal.valueOf(tempAmount).longValue();
        transData.setCardCurrencyCode(cardCode);
        transData.setCurrencyRate(loadRate.getRate());
        transData.setForeignAmount(String.valueOf(foreignAmount));
        return foreignAmount;

    }
}

