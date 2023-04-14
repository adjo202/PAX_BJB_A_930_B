/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-8-24
 * Module Author: wangyq
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action.function;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.device.Device;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.ModemCommunicate;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;


public class ActionOnlineParamProcess extends AAction {
    private TransProcessListenerImpl listenerImpl;
    private Context context;
    private Handler handler;
    private String transType;

    public ActionOnlineParamProcess(ActionStartListener listener) {
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




    private int startOnlineProcNew() {
        int ret;
        if (ETransType.ECHO.toString().equals(transType)) {
            ret = TransOnline.echo(listenerImpl);
        }else{
                ret = TransOnline.downloadParam(listenerImpl);
                if(ret != TransResult.SUCC)
                    return ret;
                ret = TransOnline.emvAidDl(listenerImpl);
                if(ret != TransResult.SUCC)
                    return ret;
                ret = TransOnline.emvCapkDl(listenerImpl);
                if(ret != TransResult.SUCC)
                    return ret;
            }

        return ret;
    }




    private int startOnlineProc() {
        int ret;
        if (ETransType.DOWNLOAD_PARAM.toString().equals(transType)) {
            ret = TransOnline.downloadParam(listenerImpl);
        } else if (ETransType.ECHO.toString().equals(transType)) {
            ret = TransOnline.echo(listenerImpl);
        } else if (ETransType.EMV_MON_PARAM.toString().equals(transType)) {
            ret = TransOnline.emvAidDl(listenerImpl);
        } else if (ETransType.EMV_MON_CA.toString().equals(transType)) {
            ret = TransOnline.emvCapkDl(listenerImpl);
        } else {
            ret = TransOnline.posStatusSubmission(listenerImpl);
        }
        return ret;
    }
}
