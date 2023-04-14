package com.pax.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransResult;
import com.pax.view.dialog.DispSingleLineMsgDialog;

public class ActionDispSingleLineMsg extends AAction {

    private Handler handler;
    private Context context;

    private String prompt;
    private String content;
    private int ticktime;
    private DispSingleLineMsgDialog dialog;

    public ActionDispSingleLineMsg(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, Handler handler, String title, String prompt, String content, int ticktime) {
        this.context = context;
        this.handler = handler;
        this.prompt = prompt;
        this.content = content;
        this.ticktime = ticktime;
    }

    @Override
    protected void process() {

        handler.post(new Runnable() {

            @Override
            public void run() {
                dialog = new DispSingleLineMsgDialog(context, prompt, content, ticktime);
                dialog.setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                                ((DispSingleLineMsgDialog) dialog).tickTimerStop();
                                dialog.dismiss();
                            }
                            return true;
                        }
                        return false;

                    }
                });
                dialog.setCancelable(false);
                dialog.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        ActionResult result = new ActionResult(TransResult.SUCC, null);
                        setResult(result);
                    }
                });
                dialog.show();

            }
        });

    }

}
