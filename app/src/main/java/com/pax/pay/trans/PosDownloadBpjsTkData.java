package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;

@Deprecated
public class PosDownloadBpjsTkData extends BaseTrans {

    private ETransType transType;

    public PosDownloadBpjsTkData(Context context, Handler handler, ETransType transType, TransEndListener transListener) {
        super(context, handler, transType, transListener);
        this.transType = transType;
    }

    @Override
    protected void bindStateOnAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = -1;

                //download location data
                if(transType == ETransType.DOWNLOAD_LOCATION_DATA_BPJS_TK){
                    ret = TransOnline.posDownloadBpjsTkProductLocation(transProcessListenerImpl);
                    if (ret != TransResult.SUCC) {
                        transProcessListenerImpl.onHideProgress();
                        transEnd(new ActionResult(ret, null));
                        return;
                    }
                }

                //download branch office data
                if(transType == ETransType.DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK) {
                    ret = TransOnline.posDownloadBpjsTkProductBranchOffice(transProcessListenerImpl);
                    if (ret != TransResult.SUCC) {
                        transProcessListenerImpl.onHideProgress();
                        transEnd(new ActionResult(ret, null));
                        return;
                    }
                }

                //download district data
                if(transType == ETransType.DOWNLOAD_DISTRICT_DATA_BPJS_TK) {
                    ret = TransOnline.posDownloadBpjsTkDistrict(transProcessListenerImpl);
                    if (ret != TransResult.SUCC) {
                        transProcessListenerImpl.onHideProgress();
                        transEnd(new ActionResult(ret, null));
                        return;
                    }
                }




                transProcessListenerImpl.onHideProgress();
                transEnd(new ActionResult(ret, null));
                return;

            }
        }).start();
    }

    @Override
    public void onActionResult(String state, ActionResult result) {
        //Do nothing
    }

}
