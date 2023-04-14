package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.poslib.ThreadPoolManager;
import com.pax.settings.SysParam;

public class PosDownloadProduct extends BaseTrans {

    public PosDownloadProduct(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.TOTAL_PRODUCT_PULSA_DATA, transListener);
    }


    @Override
    protected void bindStateOnAction() {
        /*
        ThreadPoolManager.getInstance().execute(new Runnable() {
             @Override
             public void run() {

                 TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                 int ret = TransOnline.posDownloadProduct(transProcessListenerImpl);
                 if (ret != TransResult.SUCC) {
                     transProcessListenerImpl.onHideProgress();
                     transEnd(new ActionResult(ret, null));
                     return;
                 }

                 transProcessListenerImpl.onHideProgress();
                 transEnd(new ActionResult(ret, null));
                 return;

             }

        });
         */

        new Thread(new Runnable() {
            @Override
            public void run() {
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = TransOnline.posDownloadProduct(transProcessListenerImpl);
                if (ret != TransResult.SUCC) {
                    transProcessListenerImpl.onHideProgress();
                    transEnd(new ActionResult(ret, null));
                    return;
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
