package com.pax.pay.trans.action;

import android.content.Context;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

public class ActionTransOnline extends AAction {

    private TransProcessListenerImpl transProcessListenerImpl;
    private Context context;
    private TransData transData;

    public ActionTransOnline(ActionStartListener listener) {
        super(listener);
    }

    public ActionTransOnline(TransData data) {
        super(null);
        this.transData = data;
    }

    public void setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = Transmit.getInstance().transmit(transData, transProcessListenerImpl);
                transProcessListenerImpl.onHideProgress();
                transProcessListenerImpl = null;
                setResult(new ActionResult(ret, null));
                context = null;
            }
        }).start();
    }

}