package com.pax.pay.trans.action;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintTrans;
import com.pax.settings.SysParam;

public class ActionPrintTransReceipt extends AAction {

    private Context context;
    private Handler handler;
    private TransData transData;

    public ActionPrintTransReceipt(ActionStartListener listener) {
        super(listener);
    }

    public ActionPrintTransReceipt(TransData data, Handler handler) {
        super(null);
        this.handler = handler;
        this.transData = data;
    }

    public void setParam(Context context, Handler handler, TransData transData) {
        this.context = context;
        this.handler = handler;
        this.transData = transData;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        new Thread(new Runnable() {

            @Override
            public void run() {
                ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
                PrintListenerImpl listener = new PrintListenerImpl(context, handler);
                //sandy
                receiptPrintTrans.print(transData, false, listener);
                setResult(new ActionResult(TransResult.SUCC, transData));
                context = null;
            }
        }).start();
    }

}
