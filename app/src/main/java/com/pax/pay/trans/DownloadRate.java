package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.settings.SysParam;

/**
 * Created by yanglj on 2017-03-31.
 */

public class DownloadRate extends BaseTrans {
    public DownloadRate(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.RATE_DOWN, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                if (FinancialApplication.getSysParam().get(SysParam.OTHTC_SUPP_EXRATE).equals(SysParam.Constant.NO)) {
                    transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                    return;
                }
                int ret = TransOnline.rateDl(transProcessListenerImpl);
                transProcessListenerImpl.onHideProgress();
                if (ret != TransResult.SUCC) {
                    transEnd(new ActionResult(ret, null));
                    return;
                }
                transEnd(new ActionResult(TransResult.SUCC, null));
                return;
            }
        }).start();
    }

    @Override
    public void onActionResult(String state, ActionResult result) {

    }
}
