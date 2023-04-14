package com.pax.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pax.dal.IPed;
import com.pax.dal.IPed.IPedInputPinListener;
import com.pax.dal.entity.EKeyCode;
import com.pax.dal.entity.EPedType;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.Utils;
import com.pax.up.bjb.R;

public class InputPwdDialog extends Dialog {
    public static final String TAG = "InputPwdDialog";

    private String title; // 标题
    private String prompt; // 提示信息
    private Context context;

    private Handler handler;

    private CustomAlertDialog promptDialog = null;
    private TextView titleTv;
    private TextView subtitleTv;
    private EditText pwdEdt;
    private TextView pwdTv;
    private int maxLength;

    private SimpleStyleKeyboardUtil keyboardUtil;
    private FrameLayout mFrameLayout;

    private OnPwdListener listener;

    public InputPwdDialog(Context context, Handler handler, int length, String title, String prompt) {
        this(context, R.style.popup_dialog);
        this.handler = handler;
        this.maxLength = length;
        this.title = title;
        this.prompt = prompt;
        this.context = context;
    }

    /**
     * 输联机密码时调用次构造方法
     *
     * @param context
     * @param handler
     * @param title
     * @param prompt
     */
    public InputPwdDialog(Context context, Handler handler, String title, String prompt) {
        super(context, R.style.popup_dialog);
        this.handler = handler;
        this.title = title;
        this.prompt = prompt;
        this.context = context;
    }

    public InputPwdDialog(Context context, int theme) {
        super(context, theme);

    }

    public interface OnPwdListener {
        public void onSucc(String data);

        public void onErr();
    }

    public void setPwdListener(OnPwdListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View convertView = getLayoutInflater().inflate(R.layout.activity_inner_pwd_layout, null);
        setContentView(convertView);
        if (Utils.isScreenOrientationPortrait(context)) {
            getWindow().setGravity(Gravity.BOTTOM); // 显示在底部
            getWindow().getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = 880;
            getWindow().setAttributes(lp);
        }
        initViews(convertView);

    }

    private void initViews(View view) {
        keyboardUtil = new SimpleStyleKeyboardUtil(context,view);
        titleTv = (TextView) view.findViewById(R.id.prompt_title);
        titleTv.setText(title);

        subtitleTv = (TextView) view.findViewById(R.id.prompt_no_pwd);
        if (prompt != null) {
            subtitleTv.setText(prompt);
        } else {
            subtitleTv.setVisibility(View.INVISIBLE);
        }

        pwdTv = (TextView) view.findViewById(R.id.pwd_input_text);
        pwdTv.setVisibility(View.GONE);
        pwdEdt = (EditText) view.findViewById(R.id.pwd_input_et);
        pwdEdt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pwdEdt.getWindowToken(), 0);
        pwdEdt.setInputType(InputType.TYPE_NULL);
        pwdEdt.setFocusable(true);
        pwdEdt.setTransformationMethod(new WordReplacement());

        pwdEdt.setOnTouchListener(new KeyboardTouchListener(keyboardUtil, SimpleStyleKeyboardUtil.TYPE_PAXSTYLE));
        pwdEdt.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String content = pwdEdt.getText().toString().trim();
                    if (listener != null) {
                        listener.onSucc(content);
                    }
                }
                return false;
            }
        });
        keyboardUtil.showKeyBoardLayout(pwdEdt, SimpleStyleKeyboardUtil.TYPE_PAXSTYLE);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.fl_trans_softkeyboard);
    }

    public void setContentText(final String content) {
        if (handler != null) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    if (pwdTv != null) {
                        pwdTv.setText(content);
                        pwdTv.setTextSize(50f);
                    }
                }
            });
        }

    }

    public String getContentText() {
        StringBuilder buffer = new StringBuilder();
        if (pwdTv != null) {
            buffer.append(pwdTv.getText().toString());
        }
        return buffer.toString();
    }

    public void inputOfflinePin() {

        pwdTv.setVisibility(View.VISIBLE);
        pwdEdt.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.INVISIBLE);
        keyboardUtil.hideKeyBoardForBack();

        IPed ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
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
                    temp = getContentText();
                    temp += "*";
                }
                setContentText(temp);
            }
        });
        try {
            ped.setIntervalTime(1, 1);
        } catch (PedDevException e) {
            Log.e(TAG, "", e);
        }
    }

    public void inputOnlinePin(final String panBlock, final boolean supportBypass) {

        pwdTv.setVisibility(View.VISIBLE);
        pwdEdt.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.INVISIBLE);
        keyboardUtil.hideKeyBoardForBack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IPed ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
                    ped.setIntervalTime(1, 1);
                    ped.setInputPinListener(new IPedInputPinListener() {

                        @Override
                        public void onKeyEvent(final EKeyCode arg0){
                            String temp = "";
                            if (arg0 == EKeyCode.KEY_CLEAR) {
                                temp = "";
                            } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                                // do nothing
                                return;
                            } else {
                                temp = getContentText();
                                temp += "*";
                            }
                            setContentText(temp);
                        }
                    });
                    // 有时密码框出不来， 有时键盘出不来， 目前真没找到更好的方法处理， 暂时用延时试试看， 也不一定起作用
                    // 后面那个大神有更好的方式再改正吧
                    SystemClock.sleep(200);
                    byte[] pindata = Device.getPinBlock(panBlock, supportBypass);
                    if (listener != null) {
                        if (pindata == null || pindata.length == 0)
                            listener.onSucc(null);
                        else {
                            listener.onSucc(FinancialApplication.getConvert().bcdToStr(pindata));
                        }
                    }
                } catch (final PedDevException e) {
                    Log.e(TAG, "", e);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Device.beepErr();
                            promptDialog = new CustomAlertDialog(getContext(), CustomAlertDialog.ERROR_TYPE);
                            promptDialog.setTimeout(3);
                            promptDialog.setContentText(e.getErrMsg());
                            promptDialog.show();
                            promptDialog.showConfirmButton(true);
                            promptDialog.setOnDismissListener(new OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    if (promptDialog != null)
                                        promptDialog.dismiss();
                                    if (listener != null) {
                                        listener.onErr();
                                    }
                                }
                            });
                        }
                    });

                } finally {
                    try {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                dismiss();
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        }).start();
    }

    class WordReplacement extends ReplacementTransformationMethod {

        private String word;

        @Override
        protected char[] getOriginal() {
            // 循环ASCII值 字符串形式累加到String
            for (char i = 0; i < 256; i++) {
                word += String.valueOf(i);
            }
            // strWord转换为字符形式的数组
            return word.toCharArray();
        }

        @Override
        protected char[] getReplacement() {
            char[] charReplacement = new char[255];
            // 输入的字符在ASCII范围内，将其转换为*
            for (int i = 0; i < 255; i++) {
                charReplacement[i] = '*';
            }

            return charReplacement;
        }
    }

}
