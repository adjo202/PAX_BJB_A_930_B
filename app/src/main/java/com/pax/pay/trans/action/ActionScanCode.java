package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.dal.IScanner;
import com.pax.dal.IScanner.IScanListener;
import com.pax.device.Device;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.ScanCodeActivity;

public class ActionScanCode extends AAction {

    private Context context;
    private String title;
    private String amount;
    private boolean isShow = false;
    private String qrCode = null;
    private IScanner scanner = null;

    /**
     * 
     * @param context
     * @param title
     * @param amount
     */
    public void setParam(Context context, String title, String amount) {
        this.context = context;
        this.title = title;
        this.amount = amount;
        this.isShow = true;

    }

    public ActionScanCode(ActionStartListener listener) {
        super(listener);
    }

    @Override
    protected void process() {

        if (isShow) {
            Intent intent = new Intent(context, ScanCodeActivity.class);
            intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
            intent.putExtra(EUIParamKeys.NAV_BACK.toString(), true);
            intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
            context.startActivity(intent);
        } else {

            scanner = Device.getScanner();
            scanner.close(); // 系统扫码崩溃之后，再调用掉不起来

            scanner.open();
            scanner.start(new IScanListener() {
                @Override
                public void onCancel() {
                }

                @Override
                public void onFinish() {
                    if (scanner != null) {
                        scanner.close();
                    }
                    if (qrCode != null && qrCode.length() > 0) {
                        setResult(new ActionResult(TransResult.SUCC, qrCode));
                    } else {
                        setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                }

                @Override
                public void onRead(String content) {
                    qrCode = content;
                }
            });

        }
    }

}
