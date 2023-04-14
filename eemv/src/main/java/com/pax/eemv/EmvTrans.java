package com.pax.eemv;

import android.util.Log;

import com.pax.eemv.entity.Amounts;
import com.pax.eemv.entity.CandList;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class EmvTrans {
    private IEmvListener listener;
    public static final String TAG = "EmvTrans";

    EmvTrans() {
        //do nothing
    }

    void setEmvListener(IEmvListener listener) {
        this.listener = listener;
    }

    public Amount getAmount() {
        Amount amt = new Amount();
        if (listener != null) {
            Amounts emvAmounts = listener.onGetAmounts();
            try {
                amt.setAmount(emvAmounts.getTransAmount());
                amt.setCashBackAmt(emvAmounts.getCashBackAmount());
            } catch (EmvException e) {
                Log.w("EmvTrans", "", e);
            }
        }
        return amt;
    }

    int waitAppSelect(int tryCnt, CandList[] appList) throws EmvException {
        if (listener != null) {
            List<CandList> candLists = new ArrayList<>();
            candLists.addAll(Arrays.asList(appList));
            return listener.onWaitAppSelect(tryCnt < 1, candLists);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    int confirmCardNo(String pan) throws EmvException {
        if (listener != null) {
            return listener.onConfirmCardNo(pan);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    int cardHolderPwd(boolean bOnlinePin, int leftTimes, byte[] pinData) throws EmvException {
        Log.d(TAG, "Sandy.EmvTrans.cardHolderPwd is called! " + bOnlinePin);
        if (listener != null) {
            return listener.onCardHolderPwd(bOnlinePin, leftTimes, pinData);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    EOnlineResult onlineProc() throws EmvException {
        if (listener != null) {
            return listener.onOnlineProc();
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    public boolean chkExceptionFile() {
        return listener != null && listener.onChkExceptionFile();
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.EmvTrans
 * JD-Core Version:    0.6.0
 */