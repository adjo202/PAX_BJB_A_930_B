package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.ScanCodeUtils;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

import java.text.SimpleDateFormat;


@SuppressLint("SimpleDateFormat")
public class InputTransData1Activity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "InputTransData1Activity";

    private SimpleStyleKeyboardUtil keyboardUtil;
    private TextView headerText;
    private TextView promptText;

    private ImageView backBtn;
    private Button confirmBtn;

    private String prompt;
    private String navTitle;

    private EInputType inputType;

    private boolean isVoidLastTrans;
    private boolean isAuthZero;
    private boolean isSupportScan;

    private int maxLen;
    private int minLen;

    private CustomEditText mEditText;
    private Button sysKeyBoard;
    private Button btnScanner;

    private RelativeLayout mTipLayout;

    private int state; //tri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
        showSysKeyboard();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_auth;
    }

    @Override
    protected void loadParam() {
        prompt = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_1.toString());
        maxLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        isVoidLastTrans = getIntent().getBooleanExtra(EUIParamKeys.VOID_LAST_TRANS_UI.toString(), false);
        isAuthZero = getIntent().getBooleanExtra(EUIParamKeys.INPUT_AUTH_ZERO.toString(), true);
        isSupportScan = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_SCAN.toString(), false);
        state = getIntent().getIntExtra("state", 0);
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        backBtn = (ImageView) findViewById(R.id.header_back);

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);

        promptText = (TextView) findViewById(R.id.prompt_amount);
        promptText.setText(prompt);

        mEditText = (CustomEditText) findViewById(R.id.auth_amount);
        mEditText.setInputType(InputType.TYPE_NULL);
        mEditText.setIMEEnabled(false, true);
        mEditText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    infoConfirm();
                }
                return false;
            }
        });

        confirmBtn = (Button) findViewById(R.id.info_confirm);

        if (state == 1) {
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        }

        mTipLayout = (RelativeLayout) findViewById(R.id.void_last_prompt);
        if (!isVoidLastTrans) {
            mTipLayout.setVisibility(View.INVISIBLE);
        }

        btnScanner = (Button) findViewById(R.id.start_void_scanner);
        if (isSupportScan) {
            btnScanner.setVisibility(View.VISIBLE);
            btnScanner.setFocusable(false);
        }

        sysKeyBoard = (Button) findViewById(R.id.edit_system_keyboard);
    }

    private void setEditText() {
        switch (inputType) {
            case AMOUNT:
                setEditTextAmount();
                break;
            case DATE:
                setEditTextDate();
                break;
            case NUM:
                setEditTextNum();
                break;
            case ALPHNUM:
                setEditTextAlphnum();
                break;
            case TEXT:
                setEditTextText();
                break;
            default:
                break;
        }
    }

    // 金额
    private void setEditTextAmount() {
        mTipLayout.setVisibility(View.INVISIBLE);
        mEditText.setHint(FinancialApplication.getSysParam().getCurrency().getFormat());
        mEditText.requestFocusAndTouch(keyboardUtil);
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                confirmBtnChange();
            }

        });
    }

    // 数字
    private void setEditTextNum() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        mEditText.requestFocusAndTouch(keyboardUtil);
        if (minLen == 0 || isVoidLastTrans) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    confirmBtnChange();
                }
            });
        }

    }

    // 日期
    private void setEditTextDate() {
        mTipLayout.setVisibility(View.INVISIBLE);
        mEditText.setHint(getString(R.string.date_default));
        mEditText.requestFocusAndTouch(keyboardUtil);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
    }

    // 数字加字母
    private void setEditTextAlphnum() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        mEditText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
        mEditText.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                confirmBtnChange();
            }
        });
    }

    // 所有输入形式
    private void setEditTextText() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        mEditText.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        btnScanner.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnScanner.setEnabled(false);
                tickTimerStop(); // 扫码时暂停计时
                ScanCodeUtils.getInstance().start(InputTransData1Activity.this, handler);
            }
        });

    }

    @Override
    public void onClick(View v) {

        ActionResult result = null;
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case R.id.info_confirm:
                infoConfirm();
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
            finish(result);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 输入数值检查
     */
    private String process() {
        String content = mEditText.getText().toString().trim();
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        if (TextUtils.isEmpty(content) || content.equals(currency.getFormat())) {
            return null;
        }

        switch (inputType) {
            case DATE:
                if (content.length() != 4) {
                    return null;
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
                try {
                    dateFormat.setLenient(false); //宽大的
                    dateFormat.parse(content); //解析
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return null;
                }
                break;
            case NUM:
                if (content.length() < minLen || content.length() > maxLen) {
                    return null;
                }
                if (isAuthZero) {
                    int flag = maxLen - content.length();
                    for (int i = 0; i < flag && isAuthZero; i++) {
                        content = "0" + content;
                    }
                }
                break;
            case ALPHNUM:
                if (content.length() < minLen || content.length() > maxLen) {
                    return null;
                }
                if (isAuthZero) {
                    int flag = maxLen - content.length();
                    for (int i = 0; i < flag; i++) {
                        content = content + " ";
                    }
                }
                break;
            case AMOUNT:
                break;
            default:
                break;
        }

        return content;
    }

    private void infoConfirm() {
        String content = process();
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        switch (inputType) {
            case NUM:
            case ALPHNUM:
                if (minLen == 0 ||
                        isVoidLastTrans && mEditText.getText().toString().trim().isEmpty()) {
                    break;
                }

                if (TextUtils.isEmpty(content)) {
                    ToastUtils.showMessage(InputTransData1Activity.this, getString(R.string.please_input_again));
                    mEditText.requestFocusAndTouch(keyboardUtil);
                }

                break;
            case AMOUNT:
                if (state != 1) {
                    if (TextUtils.isEmpty(content) || content.equals(currency.getFormat())) {
                        ToastUtils.showMessage(InputTransData1Activity.this, getString(R.string.please_input_again));
                        mEditText.requestFocusAndTouch(keyboardUtil);
                    }
                }
                break;
            default:
                break;
        }
        Log.d(TAG, "Sandy.infoConfirm=" + content);
        ActionResult result = new ActionResult(TransResult.SUCC, content);
        finish(result);

    }

    private void confirmBtnChange() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String content = mEditText.getText().toString();
        if (content != null && content.length() > 0 && !content.equals(currency.getFormat())) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            if (state==1){
                confirmBtn.setEnabled(true);
            }else {
                confirmBtn.setEnabled(false);
                confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
            }

        }
    }

    // 显示调用系统键盘按键
    private void showSysKeyboard() {
        if (inputType == EInputType.TEXT || inputType == EInputType.ALPHNUM) {
            sysKeyBoard.setVisibility(View.VISIBLE);
            sysKeyBoard.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //Central system API to the overall input method framework (IMF) architecture, which arbitrates interaction between applications and the current input method
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // explicitly request that the current input method's soft input area be shown to the user, if needed.
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                }
            });

        }

    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case ScanCodeUtils.SCAN_CODE_END:
                btnScanner.setEnabled(true);
                tickTimerStart();
                String content = ScanCodeUtils.getInstance().getQrCode();
                ActionResult result = new ActionResult(TransResult.SUCC, content);
                finish(result);
                break;
            default:
                break;
        }

    }

}