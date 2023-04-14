package com.pax.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.view.dialog.InputPwdDialog;
import com.pax.view.dialog.InputPwdDialog.OnPwdListener;

public class ActionInputPasword extends AAction {

    private Handler handler;
    private Context context;
    private int maxLen;
    private String title;
    private String subTitle;

    public ActionInputPasword(ActionStartListener listener) {
        super(listener);
    }

    public ActionInputPasword(Handler handler, int maxLen, String title, String subTitle) {
        super(null);
        this.handler = handler;
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
    }

    public void setParam(Context context, Handler handler, int maxLen, String title, String subTitle) {
        this.context = context;
        this.handler = handler;
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new InputPwdRunnalbe());
    }

    class InputPwdRunnalbe implements Runnable {
        private InputPwdDialog dialog = null;

        @Override
        public void run() {
            dialog = new InputPwdDialog(context, handler, maxLen, title, subTitle);
            dialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
                            setResult(result);
                            dialog.dismiss();
                        }
                        return true;
                    }
                    return false;

                }
            });
            dialog.setCancelable(false);
            dialog.setPwdListener(new OnPwdListener() {
                @Override
                public void onSucc(String data) {
                    ActionResult result = new ActionResult(TransResult.SUCC, data);
                    setResult(result);
                    dialog.dismiss();
                    dialog = null;
                }

                @Override
                public void onErr() {
                    ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
                    setResult(result);
                    dialog.dismiss();
                    dialog = null;
                }
            });
            dialog.show();
        }
    }

}
