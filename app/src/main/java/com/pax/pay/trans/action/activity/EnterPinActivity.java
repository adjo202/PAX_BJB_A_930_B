package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.dal.IPed;
import com.pax.dal.IPed.IPedInputPinListener;
import com.pax.dal.entity.EKeyCode;
import com.pax.dal.entity.RSAPinKey;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionEnterPin.OfflinePinResult;
import com.pax.pay.utils.AppLog;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

@SuppressLint("SimpleDateFormat")
public class EnterPinActivity extends BaseActivityWithTickForAction {
    private static final String TAG = "EnterPinActivity";

    private TextView promptTv1;
    private TextView promptTv2;
    private TextView amountTv;
    private TextView pwdTv;

    private String title;
    private String panBlock;
    private String prompt2;
    private String prompt1;
    private String amount;

    private ImageView imageView;
    private ImageView exPinpadView;
    private LinearLayout amountLayout;
    private CustomAlertDialog promptDialog;

    private boolean supportBypass;
    private boolean isFirstStart = true;// 判断界面是否第一次加载

    private EEnterPinType enterPinType;
    private RSAPinKey rsaPinKey;

    private static final byte ICC_SLOT = 0x00;
    public static final String OFFLINE_EXP_PIN_LEN = "0,4,5,6,7,8,9,10,11,12";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 界面不需要超时， 超时有输密码接口口控制
        tickTimerStop();
    }

    // 当页面加载完成之后再执行弹出键盘的动作
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.d(TAG, "Sandy.EnterPinActivity.onWindowFocusChanged is called!");
        if (hasFocus && isFirstStart) {
            if (enterPinType == EEnterPinType.ONLINE_PIN) {
                enterOnlinePin(panBlock, supportBypass);
            } else if (enterPinType == EEnterPinType.OFFLINE_CIPHER_PIN) {
                enterOfflineCipherPin();
            } else if (enterPinType == EEnterPinType.OFFLINE_PLAIN_PIN) {
                enterOfflinePlainPin();
            }
            isFirstStart = false;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_pwd;
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        amount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        
        Intent intent = getIntent();
        enterPinType = (EEnterPinType) getIntent().getSerializableExtra(EUIParamKeys.ENTERPINTYPE.toString());
        if (enterPinType == EEnterPinType.ONLINE_PIN) {
            panBlock = intent.getStringExtra(EUIParamKeys.PANBLOCK.toString());
            supportBypass = intent.getBooleanExtra(EUIParamKeys.SUPPORTBYPASS.toString(), false);
        } else {
            rsaPinKey = new RSAPinKey();
            rsaPinKey.setModulusLen(intent.getIntExtra(EUIParamKeys.RSA_PIN_KEY_MODULUSLEN.toString(), -1));
            rsaPinKey.setModulus(intent.getByteArrayExtra(EUIParamKeys.RSA_PIN_KEY_MODULUS.toString()));
            rsaPinKey.setExponent(intent.getByteArrayExtra(EUIParamKeys.RSA_PIN_KEY_EXPONENT.toString()));
            rsaPinKey.setIccRandom(intent.getByteArrayExtra(EUIParamKeys.RSA_PIN_KEY_ICC_RANDAM.toString()));
        }
    }

    @Override
    protected void initViews() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        imageView = (ImageView) findViewById(R.id.header_back);
        imageView.setVisibility(View.INVISIBLE);

        TextView titleTv = (TextView) findViewById(R.id.header_title);
        titleTv.setText(title);

        amountTv = (TextView) findViewById(R.id.amount_txt);
        amountLayout = (LinearLayout) findViewById(R.id.trans_amount_layout);
        if (amount != null && amount.length() != 0) {
            amount = FinancialApplication.getConvert().amountMinUnitToMajor(amount,
                    currency.getCurrencyExponent(), true);
            amountTv.setText(amount);
        } else {
            amountLayout.setVisibility(View.GONE);
        }

        promptTv1 = (TextView) findViewById(R.id.prompt_title);
        promptTv1.setText(prompt1);

        promptTv2 = (TextView) findViewById(R.id.prompt_no_pwd);
        if (prompt2 != null && supportBypass) {//需要输入密码则不显示
            promptTv2.setText(prompt2);
        } else {
            promptTv2.setVisibility(View.INVISIBLE);
        }

        pwdTv = (TextView) findViewById(R.id.pwd_input_text);

        // 外置密码键盘时的UI显示
        // 不支持外置密码键盘 Modified by Steven 2017-4-13 14:42:10
        /* */
        if (!Constant.PAD_INTERNAL.equals(FinancialApplication.getSysParam().get(SysParam.EX_PINPAD))) {
            promptTv1.setVisibility(View.GONE);
            pwdTv.setVisibility(View.GONE);

            promptTv2.setText(getString(R.string.prompt_external_ped));
            promptTv2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelOffset(R.dimen.font_size_small));

            exPinpadView = (ImageView) findViewById(R.id.prompt_ex_pinpad);
            exPinpadView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setContentText(final String content) {
        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    if (pwdTv != null) {
                        pwdTv.setText(content);
                        pwdTv.setTextSize(30f);
                    }
                }
            });
        }

    }

    private void enterOnlinePin(final String panBlock, final boolean supportBypass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IPed ped = Device.getPed();
                try {
                    ped.clearScreen();
                    Currency currency = FinancialApplication.getSysParam().getCurrency();
                    ped.showStr((byte) 0x00, (byte) 0x00, currency.getName() + ":" +
                            (amount != null ? amount : ""));
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                }
                // 外置密码键盘没有这个接口
                try {
                    ped.setIntervalTime(1, 1);
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                }
                try {
                    ped.setInputPinListener(new IPedInputPinListener(){

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });

                    byte[] pindata = Device.getPinBlock(panBlock, supportBypass);
                    if (pindata == null || pindata.length == 0)
                        finish(new ActionResult(TransResult.SUCC, null));
                    else {
                        finish(new ActionResult(TransResult.SUCC, FinancialApplication.getConvert().bcdToStr(pindata)));
                    }
                } catch (final PedDevException e) {
                    Log.e(TAG, "", e);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Device.beepErr();
                            promptDialog = new CustomAlertDialog(EnterPinActivity.this, CustomAlertDialog.ERROR_TYPE);
                            promptDialog.setTimeout(3);
                            promptDialog.setContentText(e.getErrMsg());
                            promptDialog.show();
                            promptDialog.showConfirmButton(true);
                            promptDialog.setOnDismissListener(new OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    finish(new ActionResult(TransResult.ERR_ABORTED, null));
                                }
                            });
                        }
                    });

                }
            }
        }).start();
    }

    public void enterOfflineCipherPin() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                IPed ped = Device.getPed();
                try {
                    ped.setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });
                    ped.setIntervalTime(1, 1);
                    byte[] resp = ped.verifyCipherPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, rsaPinKey, (byte) 0x00, 60 * 1000);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    finish(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(e.getErrCode());
                    finish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                }
            }
        }).start();
    }

    public void enterOfflinePlainPin() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                IPed ped = Device.getPed();
                try {
                    ped.setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0) {
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = pwdTv.getText().toString();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });
                    ped.setIntervalTime(1, 1);
                    byte[] resp = ped.verifyPlainPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, (byte) 0x00, 60 * 1000);
                    if (resp == null || resp.length == 0) {
                        AppLog.i(TAG, "verifyPlainPin resp = null or len = 0");
                    } else {
                        AppLog.i(TAG, "verifyPlainPin resp = " + FinancialApplication.getConvert().bcdToStr(resp));
                    }
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                    offlinePinResult.setRespOut(resp);
                    finish(new ActionResult(TransResult.SUCC, offlinePinResult));
                } catch (PedDevException e) {
                    Log.e(TAG, "", e);
                    OfflinePinResult offlinePinResult = new OfflinePinResult();
                    offlinePinResult.setRet(e.getErrCode());
                    finish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
                }

            }
        }).start();
    }
}