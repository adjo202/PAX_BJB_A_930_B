package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;

public class PosLogout extends BaseTrans {

    public PosLogout(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.LOGOUT, transListener);
    }

    @Override
    protected void bindStateOnAction() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                TransProcessListener transProcessListener = new TransProcessListenerImpl(context);
                //手动签退时，增加是否还有交易流水的判断 Added by Steven.T 2017-6-12 11:36:44
                if (TransData.getTransCount() > 0) {
                    transEnd(new ActionResult(TransResult.ERR_HAVE_TRANS, null));
                    return;
                }

                TransOnline.posLogout(transProcessListener);

                // 终端签退,发送报文无论是否成功，都改变POS终端登录状态和操作员登录状态
                FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller
                        .Constant.NO);
                FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS, Controller
                        .Constant.NO);
                transEnd(new ActionResult(TransResult.SUCC, null));
                return;

            }
        }).start();

    }

    @Override
    public void onActionResult(String state, ActionResult result) {
        //Do nothing
    }

}
