package com.pax.pay.trans.action;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.device.DeviceImplNeptune;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.device.DeviceManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvListenerImpl;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.up.bjb.R;

public class ActionEmvProcess extends AAction {

    public static final String TAG = "ActionEmvProcess";

    private Handler handler;
    private Context context;
    private TransData transData;

    public ActionEmvProcess(ActionStartListener listener) {
        super(listener);
    }

    public ActionEmvProcess(Handler handler, TransData data) {
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
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        this.context = TransContext.getInstance().getCurrentContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                TransProcessListener transProcessListener = new TransProcessListenerImpl(context);
                EmvListenerImpl emvListener = new EmvListenerImpl(context, FinancialApplication.getEmv(), handler, transData, transProcessListener);
                if (transData.getEnterMode() == EnterMode.INSERT
                        || transData.getTransferEnterMode() == EnterMode.INSERT) {
                    transProcessListener.onShowProgress(context.getString(R.string.process_please_wait), 0);
                }

                //Sandy : as per BJB, skip to check emv
                try {
                    Log.i(TAG, "sandy.getTrackData:" + transData.getTrack2());
                    EmvTransProcess emvTransProcess = EmvTransProcess.getInstance();
                    CTransResult result = emvTransProcess.transProcess(transData, emvListener);
                    transProcessListener.onHideProgress();
                    setResult(new ActionResult(TransResult.SUCC, result.getTransResult()));
                } catch (EmvException e) {

                    //Sandy : as per BJB, skip to check emv
                    if(ETransType.valueOf(transData.getTransType()) == ETransType.ACCOUNT_LIST){
                        if (e.getErrCode() != EEmvExceptions.EMV_ERR_USER_CANCEL.ordinal()) {
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.SUCC, null));
                        }
                        //setResult(new ActionResult(e.getErrCode()), null));
                    }


                    Log.e(TAG, "", e);
                    Device.beepErr();
                    if (e.getErrCode() != EEmvExceptions.EMV_ERR_UNKNOWN.ordinal()) {
                        if (e.getErrCode() == EEmvExceptions.EMV_ERR_FALL_BACK.ordinal()) {
                            transProcessListener.onShowErrMessageWithConfirm(
                                    //mContext.getString(R.string.err_card_unsupport_demotion),
                                    e.getErrMsg(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                            transData.setIsFallback(true);
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.FALL_BACK, null));
                        } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_USER_CANCEL.ordinal()) {
                            // Sandy : User cancels without prompting
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_PURE_EC_CARD_NOT_ONLINE.ordinal()) {// 纯电子现金卡不能联机
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.ERR_PURE_CARD_CAN_NOT_ONLINE, null));
                        } else {

                            transProcessListener.onShowErrMessageWithConfirm(e.getErrMsg(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                            transProcessListener.onHideProgress();
                            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        }
                    }else {
                        transProcessListener.onHideProgress();
                        setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                }
            }
        }).start();
    }
}
