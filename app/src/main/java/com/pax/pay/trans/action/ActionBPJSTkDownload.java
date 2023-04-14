/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-9 3:4
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.action;

import android.content.Context;
import android.os.Handler;

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
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

/**
 * Created by sandy on 2022/03/22.
 */

public class ActionBPJSTkDownload extends AAction {

    private TransProcessListenerImpl listenerImpl;
    private Context context;
    private Handler handler;
    private String transType;

    public ActionBPJSTkDownload(ActionStartListener listener) {
        super(listener);
        this.handler = new Handler();
    }

    public void setParam(Context context, String transType) {
        this.context = context;
        this.transType = transType;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                listenerImpl = new TransProcessListenerImpl(context);
                int ret = onlineProcess();
                if (listenerImpl != null) {
                    listenerImpl.onHideProgress();
                }

                if (ret == TransResult.SUCC) {
                    Device.beepOk();
                } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT
                        && listenerImpl != null) {
                    // ERR_ABORTED AND ERR_HOST_REJECT 之前已提示错误信息，此处不需要再提示
                    listenerImpl.onShowErrMessageWithConfirm(TransResult.getMessage(context,ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);

                }
                exit();



            }
        }).start();
    }



    private int onlineProcess(){
        int ret = -1;
        if(ETransType.DOWNLOAD_LOCATION_DATA_BPJS_TK.toString().equals(transType)){
            ret = TransOnline.posDownloadBpjsTkProductLocation(listenerImpl);
        }else if (ETransType.DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK.toString().equals(transType)) {
            ret = TransOnline.posDownloadBpjsTkProductBranchOffice(listenerImpl);
        }else if (ETransType.DOWNLOAD_DISTRICT_DATA_BPJS_TK.toString().equals(transType)) {
            ret = TransOnline.posDownloadBpjsTkDistrict(listenerImpl);
        }

         return ret;
    }

    private void exit() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                ModemCommunicate.getInstance().onClose();
            }
        });
    }


}
