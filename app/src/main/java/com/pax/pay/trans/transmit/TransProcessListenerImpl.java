package com.pax.pay.trans.transmit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.abl.mac.EMac;
import com.pax.dal.IPed;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.gl.convert.IConvert;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.util.Arrays;

public class TransProcessListenerImpl implements TransProcessListener {
    public static final String TAG = "TransProcListenerImpl";

    // 显示消息
    private static final int ID_SHOW_MSG = 1;
    // 隐藏消息dialog
    private static final int ID_HIDE = 2;
    // 显示错误信息确定框
    private static final int ID_SHOW_ERR_CONFIRM = 3;
    // 显示正确信息确定框
    private static final int ID_SHOW_NORMAL_CONFIRM = 4;
    private static final int ID_SHOW_ERROR = 5;
    // 显示的消息
    private String processMessage;
    // 显示的超时
    private int processTimeOut;

    // 确认的消息
    private String confirmMessage;
    // 显示的超时
    private int confirmTimeOut;
    // 显示的title
    private String processTitle;
    // 用于onInputOnlinePin返回结果
    private int result;

    private Context context;
    private CustomAlertDialog dialog;
    private Handler locHandler;
    private IConvert convert = FinancialApplication.getConvert();
    private ConditionVariable cv; //Class that implements the condition variable locking paradigm
    // 是否显示消息
    private boolean isShowMessage;

    public TransProcessListenerImpl(Context context) {
        this.context = context;
        this.isShowMessage = true;
        locHandler = createHandler();
    }

    public TransProcessListenerImpl(Context context, boolean isShowMessage) {
        this.context = context;
        this.isShowMessage = isShowMessage;
        locHandler = createHandler();
    }

    @Override
    public void onShowProgress(final String message, final int timeOut) {

        if (!isShowMessage) {
            return;
        }
        this.processMessage = message;
        this.processTimeOut = timeOut;
        locHandler.sendEmptyMessage(ID_SHOW_MSG); //Sends a Message containing only the what value
    }

    private int onShowMessageWithConfirm(final String message, final int timeout, final int alertType) {
        if (!isShowMessage) {
            return 0;
        }
        onHideProgress();
        this.confirmMessage = message;
        this.confirmTimeOut = timeout;
        cv = new ConditionVariable();
        if(alertType == CustomAlertDialog.ERROR_TYPE) {
            locHandler.sendEmptyMessage(ID_SHOW_ERR_CONFIRM);
        }else{
            locHandler.sendEmptyMessage(ID_SHOW_NORMAL_CONFIRM);
        }
        cv.block();  //Block the current thread until the condition is opened.
        return 0;
    }

    @Override
    public int onShowErrMessageWithConfirm(final String message, final int timeout) {
        return onShowMessageWithConfirm(message, timeout, CustomAlertDialog.ERROR_TYPE);
    }

    @Override
    public int onShowNormalMessageWithConfirm(String message, int timeout) {
        return onShowMessageWithConfirm(message, timeout, CustomAlertDialog.NORMAL_TYPE);
    }

    @Override
    public byte[] onCalcMac(byte[] data) {
        IPed ped = Device.getPed();
        return EMac.CUP.getMac(ped, Constants.INDEX_TAK, data);
    }

    @Override
    public byte[] onEncTrack(byte[] track) {
        byte[] block = null;
        String trackStr;
        int len = track.length;
        int isDouble = 0;      
        if (len % 2 > 0) {
            isDouble =1;
            trackStr = new String(track) + "0";
        } else {
            trackStr = new String(track);
        }
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        byte[] trackData = new byte[8];
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) {
            trackData = new byte[16];
        }
        Arrays.fill(trackData, (byte)0xff); 
        byte[] bTrack = convert.strToBcd(trackStr, EPaddingPosition.PADDING_LEFT);
        if ( bTrack.length  - 1 < trackData.length){
        	  System.arraycopy (bTrack, 0, trackData, 0, bTrack.length  - 1);

        }else{
            System.arraycopy(bTrack, bTrack.length - trackData.length - 1, trackData, 0, trackData.length);

        }

        try {
            block = Device.calcDes(trackData);

            if ( bTrack.length  - 1 < trackData.length){
                byte[] data = new byte[trackData.length +1];
                System.arraycopy (block, 0, data, 0, trackData.length );
                System.arraycopy (bTrack, bTrack.length-1, data, trackData.length, 1 );

                if(isDouble==1){
                    return convert.bcdToStr(data).substring(0, convert.bcdToStr(data).length()-1).getBytes();

                }
                return convert.bcdToStr(data).getBytes();
            }else{
                System.arraycopy(block, 0, bTrack, bTrack.length - block.length - 1, block.length);
                return convert.bcdToStr(bTrack).substring(0, len).getBytes();
            }
        } catch (PedDevException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    @Override
    public void onHideProgress() {
        locHandler.sendEmptyMessage(ID_HIDE);
    }


    @Override
    public void onUpdateProgressTitle(String title) {
        if (!isShowMessage) {
            return;
        }

        this.processTitle = title;
    }

    @Override
    public int onInputOnlinePin(final TransData transData) {
        cv = new ConditionVariable();
        result = 0;
        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(context,
                        ETransType.valueOf(transData.getTransType()).getTransName(), transData.getPan(), true,
                        context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());

            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult actionResult) {
                int ret = actionResult.getRet();
                if (ret == TransResult.SUCC) {
                    String data = (String) actionResult.getData();
                    transData.setPin(data);
                    if (data != null && data.length() > 0) {
                        transData.setHasPin(true);
                    } else {
                        transData.setHasPin(false);
                    }
                    result = 0;
                    cv.open();  //Open the condition, and release all threads that are blocked
                } else {
                    result = -1;
                    cv.open();
                }
            }
        });
        actionEnterPin.execute();

        cv.block();  //Block the current thread until the condition is opened or until timeout milliseconds have passed
        TransContext.getInstance().setCurrentContext(context);

        return result;
    }

    // 创建handler
    private Handler createHandler() {                //Use the provided queue instead of the default one.
        return new Handler(Looper.getMainLooper()) { //Returns the application's main looper, which lives in the main thread of the application
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ID_SHOW_MSG:
                        if (dialog == null) {
                            dialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);
                            dialog.show();
                            dialog.setCancelable(false);
                        }
                        dialog.setTimeout(processTimeOut);
                        dialog.setTitleText(processTitle);
                        dialog.setContentText(processMessage);
                        break;
                    case ID_HIDE:

                        if (dialog != null) {
                            SystemClock.sleep(200);
                            dialog.dismiss();  //Dismiss this dialog, removing it from the screen
                            dialog = null;
                        }
                        break;
                    case ID_SHOW_ERR_CONFIRM:
                        CustomAlertDialog efmDialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE,
                                confirmTimeOut);
                        efmDialog.setContentText(confirmMessage);
                        efmDialog.show();
                        efmDialog.showConfirmButton(true);
                        efmDialog.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface arg0) {
                                cv.open(); //Open the condition, and release all threads that are blocked
                                efmDialog.dismiss();
                            }
                        });
                        break;
                    case ID_SHOW_NORMAL_CONFIRM:
                        CustomAlertDialog nfmDialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE_TIMEOUT,
                                confirmTimeOut);
                        nfmDialog.setContentText(confirmMessage);
                        nfmDialog.show();
                        nfmDialog.showConfirmButton(true);
                        nfmDialog.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface arg0) {
                                cv.open(); //Open the condition, and release all threads that are blocked
                            }
                        });
                        break;
                    case ID_SHOW_ERROR:
                        CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, confirmTimeOut);
                        dialog.setContentText(confirmMessage);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                return keyCode == KeyEvent.KEYCODE_BACK;
                            }
                        });
                        dialog.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface arg0) {
                                cv.open();
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public int onShowErrMessage(final String message, final int timeout) {
        if (!isShowMessage) {
            return 0;
        }
        onHideProgress();
        this.confirmMessage = message;
        this.confirmTimeOut = timeout;
        cv = new ConditionVariable();
        locHandler.sendEmptyMessage(ID_SHOW_ERROR);
        cv.block();  //Block the current thread until the condition is opened.
        return 0;
    }
}
