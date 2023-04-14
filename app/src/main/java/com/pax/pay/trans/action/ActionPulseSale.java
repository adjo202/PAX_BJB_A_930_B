package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.ModemCommunicate;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

public class ActionPulseSale extends AAction {

    private TransProcessListenerImpl listenerImpl;
    private Context context;
    private Handler handler;
    private String transType;

    public ActionPulseSale(ActionStartListener listener) {
        super(listener);
        this.handler = new Handler();
    }

    public void setParam(Context context, String transType) {
        this.context = context;
        this.transType = transType;
    }

    @Override
    protected void process() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                listenerImpl = new TransProcessListenerImpl(context);

                int ret = startOnlineProc();

                if (listenerImpl != null) {
                    listenerImpl.onHideProgress();
                }

                if (ret == TransResult.SUCC) {
                    Device.beepOk();
                } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT
                        && listenerImpl != null) {
                    // ERR_ABORTED AND ERR_HOST_REJECT 之前已提示错误信息，此处不需要再提示
                    listenerImpl.onShowErrMessageWithConfirm(TransResult.getMessage(context,
                            ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);

                }
                exit();
            }
        }).start();

    }

    private void exit() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ModemCommunicate.getInstance().onClose();
            }
        });
    }

    private int startOnlineProc() {
        int ret = -1;
        if (ETransType.ECHO.toString().equals(transType)) {
            ret = TransOnline.echo(listenerImpl);
        }
        return ret;
    }




}
