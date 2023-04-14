package com.pax.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.abl.utils.PanUtils.EPanMode;
import com.pax.dal.IPed;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.RSAPinKey;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.EnterPinActivity;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.view.dialog.CustomAlertDialog;

public class ActionEnterPin extends AAction {
    public static final String TAG = "ActionEnterPin";

    /**
     * 脱机pin时返回的结果
     *
     * @author Steven.W
     *
     */
    public static class OfflinePinResult {
        // SW1 SW2
        byte[] respOut;
        int ret;

        public byte[] getRespOut() {
            return respOut;
        }

        public void setRespOut(byte[] respOut) {
            this.respOut = respOut;
        }

        public int getRet() {
            return ret;
        }

        public void setRet(int ret) {
            this.ret = ret;
        }
    }

    public ActionEnterPin(ActionStartListener listener) {
        super(listener);
    }

    private static final byte ICC_SLOT = 0x00;
    private static final String OFFLINE_EXP_PIN_LEN = "0,4,5,6,7,8,9,10,11,12";
    private static final int SHOW_ERR_MSG = 1;
    private Context context;

    private String title;
    private String pan;
    private String header;
    private String subheader;
    private String amount;
    private RSAPinKey rsaPinKey;

    private boolean isSupportBypass;
    private EEnterPinType enterPinType;
    private Handler locHandler;
    private String pedUiType;
    private String pinPadType;
    private int enterMode;

    public void setParam(Context context, String title, String pan, boolean supportBypass, String header,
            String subHeader, String amount, EEnterPinType enterPinType, int enterMode) {
        this.context = context;
        this.title = title;
        this.pan = pan;
        this.isSupportBypass = supportBypass;
        this.header = header;
        this.subheader = subHeader;
        this.amount = amount;
        this.enterPinType = enterPinType;
        this.enterMode = enterMode;
        locHandler = createHandler();
        pedUiType = FinancialApplication.getSysParam().get(SysParam.INTERNAL_PED_UI_STYLE);
        pinPadType = FinancialApplication.getSysParam().get(SysParam.EX_PINPAD);
    }

    /**
     * 设置脱机密文pin的rsaPinKey
     *
     * @param rsaPinKey
     */
    public void setRSAPinKey(RSAPinKey rsaPinKey) {
        this.rsaPinKey = rsaPinKey;
    }

    public static enum EEnterPinType {
        ONLINE_PIN, // 联机pin
        OFFLINE_PLAIN_PIN, // 脱机明文pin
        OFFLINE_CIPHER_PIN, // 脱机密文pin
    }

    @Override
    protected void process() {
        IPed ped = Device.getPed();
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        Log.d(TAG, "Sandy.ActionEnterPin.process is called!");

        if (pinPadType.equals(SysParam.Constant.PAD_INTERNAL) && pedUiType != null
                &&  pedUiType.equals(SysParam.Constant.PAD_INTERNAL_UI_DEFAULT)) {
            try {
                if (amount!=null) {
                    ped.setAmount(FinancialApplication.getConvert().amountMinUnitToMajor(amount,
                            currency.getCurrencyExponent(), true));
                }
                ped.showInputBox(true, header);

            } catch (PedDevException e) {
                Log.e(TAG, "", e);
            }

            switch (enterPinType) {
                case ONLINE_PIN:
                    enterOnlinePin();
                    break;
                case OFFLINE_PLAIN_PIN:
                    enterOfflinePlainPin();
                    break;
                case OFFLINE_CIPHER_PIN:
                    enterOfflineCipherPin();
                    break;

                default:
                    break;
            }
        } else {
            try {
                ped.setAmount("");
                ped.showInputBox(false, "");
            } catch (PedDevException e) {
                Log.e(TAG, "", e);
            }
            Intent intent = new Intent(context, EnterPinActivity.class);
            intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
            intent.putExtra(EUIParamKeys.PROMPT_1.toString(), header);
            intent.putExtra(EUIParamKeys.PROMPT_2.toString(), subheader);
            intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
            intent.putExtra(EUIParamKeys.ENTERPINTYPE.toString(), enterPinType);
            intent.putExtra(EUIParamKeys.PANBLOCK.toString(), PanUtils.getPanBlock(pan, EPanMode.X9_8_WITH_PAN));
            intent.putExtra(EUIParamKeys.SUPPORTBYPASS.toString(), isSupportBypass);
            if (rsaPinKey != null) {
                intent.putExtra(EUIParamKeys.RSA_PIN_KEY_MODULUSLEN.toString(), rsaPinKey.getModulusLen());
                intent.putExtra(EUIParamKeys.RSA_PIN_KEY_MODULUS.toString(), rsaPinKey.getModulus());
                intent.putExtra(EUIParamKeys.RSA_PIN_KEY_EXPONENT.toString(), rsaPinKey.getExponent());
                intent.putExtra(EUIParamKeys.RSA_PIN_KEY_ICC_RANDAM.toString(), rsaPinKey.getIccRandom());
            }
            context.startActivity(intent);
        }
    }

    private void enterOnlinePin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IPed ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
                    ped.setIntervalTime(1, 1);
                    byte[] pindata;
                    /*
                    if (enterMode == EnterMode.SWIPE || enterMode == EnterMode.FALLBACK) {     // 刷卡 //FallBack
                        pindata = Device.getPinBlock(PanUtils.getPanBlock(pan, EPanMode.X9_8_WITH_PAN),
                                isSupportBypass);
                    }else{
                        pindata = Device.getPinBlock(PanUtils.getPanBlock(pan, EPanMode.X9_8_WITH_PAN),
                                isSupportBypass);
                    }
                     */

                    pindata = Device.getPinBlock(PanUtils.getPanBlock(pan, EPanMode.X9_8_WITH_PAN),isSupportBypass);

                    if (pindata == null || pindata.length == 0)
                        setResult(new ActionResult(TransResult.SUCC, null));
                    else {
                        setResult(new ActionResult(TransResult.SUCC, FinancialApplication.getConvert().bcdToStr(pindata)));
                    }
                } catch (final PedDevException e) {
                    Log.e(TAG, "", e);
                    Message msg = new Message();
                    msg.what = SHOW_ERR_MSG;
                    msg.obj = e.getErrMsg();
                    locHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void enterOfflineCipherPin() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                IPed ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
                try {
                    ped.setIntervalTime(1, 1);
                    byte[] resp = ped.verifyCipherPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, rsaPinKey, (byte) 0x00, 60 * 1000);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    setResult(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(e.getErrCode());
                    setResult(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                }
            }
        }).start();
    }

    private void enterOfflinePlainPin() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                IPed ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
                try {
                    ped.setIntervalTime(1, 1);
                    byte[] resp = ped.verifyPlainPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, (byte) 0x00, 60 * 1000);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    setResult(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(e.getErrCode());
                    setResult(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                }

            }
        }).start();
    }

    // 创建handler
    private Handler createHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_ERR_MSG:
                        Device.beepErr();
                        CustomAlertDialog promptDialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE);
                        promptDialog.setTimeout(3);
                        promptDialog.setContentText((String) msg.obj);
                        promptDialog.show();
                        promptDialog.showConfirmButton(true);
                        promptDialog.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface arg0) {
                                setResult(new ActionResult(TransResult.ERR_ABORTED, null));
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
    public void setResult(ActionResult result) {
        if (pinPadType.equals(SysParam.Constant.PAD_INTERNAL) && pedUiType != null
                && pedUiType.equals(SysParam.Constant.PAD_INTERNAL_UI_DEFAULT)) {
            // do nothing
        } else {
            ActivityStack.getInstance().pop();
            TransContext.getInstance().setCurrentContext(context);
        }
        super.setResult(result);
    }

}
