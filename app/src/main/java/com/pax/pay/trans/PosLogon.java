package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.settings.SysParam;

public class PosLogon extends BaseTrans {

    public PosLogon(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.LOGON, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = TransOnline.posLogon(transProcessListenerImpl);
                Log.d("SANDY","Sandy=bindStateOnAction called!" +ret );
                if (ret != TransResult.SUCC) {
                    transProcessListenerImpl.onHideProgress();
                    transEnd(new ActionResult(ret, null));
                    return;
                }

                ret = TransOnline.downLoadCheck(true, true, transProcessListenerImpl);
                if (ret != TransResult.SUCC) {
                    transProcessListenerImpl.onHideProgress();
                    transEnd(new ActionResult(ret, null));
                    return;
                }

                if (SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam.OTHTC_SUPP_EXRATE)) &&
                    SysParam.Constant.NO.equals(FinancialApplication.getSysParam().get(SysParam.OTHTC_FORCE_ONLINE)) &&
                    SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam.QUICK_PASS_TRANS_SWITCH))) {
                        ret = TransOnline.rateDl(transProcessListenerImpl);
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
