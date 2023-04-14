package com.pax.pay.trans.transmit;

import com.pax.pay.trans.model.TransData;

public interface TransProcessListener {

    public void onShowProgress(String message, int timeout);

    public void onUpdateProgressTitle(String title);

    public void onHideProgress();

    public int onShowNormalMessageWithConfirm(String message, int timeout);

    public int onShowErrMessageWithConfirm(String message, int timeout);

    public int onShowErrMessage(String message, int timeout);

    public int onInputOnlinePin(TransData transData);

    public byte[] onCalcMac(byte[] data);

    public byte[] onEncTrack(byte[] track);
}