package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.ScanCodeUtils;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat")
public class InputTransData2Activity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "InputEcRefundDataAct";

    private SimpleStyleKeyboardUtil keyboardUtil;
    private TextView headerText;
    private ImageView backBtn;

    private TextView promptNum;
    private CustomEditText mEditNum;
    private TextView promptExtraNum;
    private CustomEditText mEditExtraNum;

    private String prompt1;
    private String prompt2;
    private EInputType inputType1;
    private EInputType inputType2;
    private int maxLen1;
    private int minLen1;
    private int maxLen2;
    private int minLen2;
    private Button btnScanner;
    private Button confirm;
    private Button sysKeyBoard1;
    private Button sysKeyBoard2;
    private String navTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
        setEtraEditText();
        showSysKeyboard();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_info;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType1 = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_1.toString());
        maxLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        inputType2 = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE_2.toString());
        maxLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_2.toString(), 6);
        minLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_2.toString(), 0);
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        backBtn = (ImageView) findViewById(R.id.header_back);

        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);

        promptNum = (TextView) findViewById(R.id.prompt_num);
        promptNum.setText(prompt1);
        promptExtraNum = (TextView) findViewById(R.id.prompt_extranum);
        promptExtraNum.setText(prompt2);

        mEditNum = (CustomEditText) findViewById(R.id.prompt_edit_num);
        mEditNum.setIMEEnabled(false, true);
        mEditNum.setFocusable(true);
        mEditNum.requestFocusAndTouch(keyboardUtil);
        mEditNum.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    infoConfirm();
                    return true;
                }
                return false;
            }
        });

        mEditExtraNum = (CustomEditText) findViewById(R.id.prompt_edit_extranum);
        mEditExtraNum.setIMEEnabled(false, true);
        mEditExtraNum.setFocusable(true);
        mEditExtraNum.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    infoConfirm();
                    return true;
                }
                return false;
            }
        });

        btnScanner = (Button) findViewById(R.id.start_scanner);

        confirm = (Button) findViewById(R.id.infos_confirm);
        confirm.setEnabled(false);

        sysKeyBoard1 = (Button) findViewById(R.id.edit_system_keyboard);
        sysKeyBoard2 = (Button) findViewById(R.id.edit_system_extra_keyboard);
    }

    private void setEditText() {
        switch (inputType1) {
            case DATE:
                setEditTextDate(mEditNum);
                break;
            case NUM:
                setEditTextNum(mEditNum, maxLen1);
                break;
            case ALPHNUM:
                setEditTextAlphnum(mEditNum, maxLen1);
                break;
            case TEXT:
                setEditTextText(mEditNum, maxLen1);
                break;
            default:
                break;
        }
    }

    private void setEtraEditText() {
        switch (inputType2) {
            case DATE:
                setEditTextDate(mEditExtraNum);
                break;
            case NUM:
                setEditTextNum(mEditExtraNum, maxLen2);
                break;
            case ALPHNUM:
                setEditTextAlphnum(mEditExtraNum, maxLen2);
                break;
            case TEXT:
                setEditTextText(mEditExtraNum, maxLen2);
                break;
            default:
                break;
        }
    }

    // 数字
    private void setEditTextNum(CustomEditText editText, int len) {
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(len) });
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
    }

    // 日期
    private void setEditTextDate(CustomEditText editText) {
        SpannableString ss = new SpannableString(getString(R.string.date_default));
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(getResources().getDimensionPixelOffset(R.dimen.font_size_large),
                false);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss)); // 一定要进行转换,否则属性会消失
        editText.setHintTextColor(getResources().getColor(R.color.textedit_hint));
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
    }

    // 数字加字母
    private void setEditTextAlphnum(CustomEditText editText, int len) {
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(len) });
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });

    }

    // 所有输入形式
    private void setEditTextText(CustomEditText editText, int len) {
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(len) });
    }

    // 显示调用系统键盘按键
    private void showSysKeyboard() {
        if (inputType1 == EInputType.TEXT || inputType1 == EInputType.ALPHNUM) {
            sysKeyBoard1.setVisibility(View.VISIBLE);
            sysKeyBoard2.setVisibility(View.INVISIBLE);
            setkeyboardListener(mEditNum, sysKeyBoard1);

            if (inputType2 == EInputType.TEXT || inputType2 == EInputType.ALPHNUM) {
                sysKeyBoard2.setVisibility(View.VISIBLE);
                setkeyboardListener(mEditExtraNum, sysKeyBoard2);
            }

        } else if (inputType2 == EInputType.TEXT || inputType2 == EInputType.ALPHNUM) {
            sysKeyBoard1.setVisibility(View.INVISIBLE);
            sysKeyBoard2.setVisibility(View.VISIBLE);
            setkeyboardListener(mEditExtraNum, sysKeyBoard2);
        }

    }

    private void setkeyboardListener(final CustomEditText editText, Button keyBoard) {
        keyBoard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        btnScanner.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                btnScanner.setEnabled(false);
                tickTimerStop(); // 扫码时暂停计时
                ScanCodeUtils.getInstance().start(InputTransData2Activity.this, handler);
            }
        });

        // 输入框数值监听
        mEditNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                confirmBtnChange();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable e) {
                // Do nothing
            }
        });

        mEditExtraNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                confirmBtnChange();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // Do nothing
            }
        });

        // 输入框触摸监听
        mEditNum.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        mEditExtraNum.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ActionResult result = null;

        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case R.id.infos_confirm:
                infoConfirm();
                break;
            default:
                break;
        }

    }

    private void confirmBtnChange() {
        String content = process(mEditNum, inputType1, maxLen1, minLen1);
        String extraContent = process(mEditExtraNum, inputType2, maxLen2, minLen2);
        if (content != null || extraContent != null) {
            confirm.setEnabled(true);
            confirm.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirm.setEnabled(false);
            confirm.setBackgroundResource(R.drawable.gray_button_background);
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
    private String process(CustomEditText editText, EInputType inputType, int maxLen, int minLen) {
        String content = editText.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            return null;
        }

        switch (inputType) {
            case DATE:
                if (content.length() != 4) {
                    return "";
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
                try {
                    dateFormat.setLenient(false);
                    dateFormat.parse(content);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return "";
                }
                return content;
            case NUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    return content;
                } else {
                    return "";
                }
            case ALPHNUM:
                if (content.length() >= minLen && content.length() <= maxLen) {
                    if (content.length() < maxLen) {
                        int flag = maxLen1 - content.length();
                        for (int i = 0; i < flag; i++) {
                            content = content + " ";
                        }
                    }
                } else {
                    return "";
                }
                break;
            default:
                break;
        }
        return content;
    }

    private void infoConfirm() {
        String content = process(mEditNum, inputType1, maxLen1, minLen1);
        if (content == null) {
            mEditNum.setText("");
            mEditNum.requestFocusAndTouch(keyboardUtil);
            return;
        }
        if (content.length() == 0) {
            ToastUtils.showMessage(InputTransData2Activity.this, getString(R.string.please_input_again));
            mEditNum.requestFocusAndTouch(keyboardUtil);
            return;
        }

        String extraContent = process(mEditExtraNum, inputType2, maxLen2, minLen2);
        if (extraContent == null) {
            mEditExtraNum.requestFocusAndTouch(keyboardUtil);

            return;
        }
        if (extraContent.length() == 0) {
            ToastUtils.showMessage(InputTransData2Activity.this, getString(R.string.prompt_card_date_err));
            mEditExtraNum.requestFocusAndTouch(keyboardUtil);
            return;
        }

        if (content != null && content.length() != 0 && extraContent != null && extraContent.length() != 0) {
            ActionResult result = new ActionResult(TransResult.SUCC, new String[] { content, extraContent });
            finish(result);
        }
    }

    /**
     * 解析扫码结果
     * 
     * @param result
     */
    private void parseJsonResult(final String result) {

        try {
            JSONArray resultArray = new JSONArray(result);
            JSONObject resultObj = resultArray.optJSONObject(0);

            String code = "";
            try {
                code = resultObj.getString("authCode");
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }

            String date = "";
            try {
                date = resultObj.getString("date");
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }

            String refNo = "";
            try {
                refNo = resultObj.getString("refNo");
            } catch (JSONException e) {
                Log.e(TAG, "", e);
            }

            // 设置第一个框
            switch (inputType1) {
                case NUM:
                    mEditNum.setText(refNo);
                    break;
                case DATE:
                    mEditNum.setText(date);
                    break;
                case ALPHNUM:
                    mEditNum.setText(code);
                    break;
                default:
                    break;
            }

            // 设置第二个框
            switch (inputType2) {
                case NUM:
                    mEditExtraNum.setText(refNo);
                    break;
                case DATE:
                    mEditExtraNum.setText(date);
                    break;
                case ALPHNUM:
                    mEditExtraNum.setText(code);
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case ScanCodeUtils.SCAN_CODE_END:
                btnScanner.setEnabled(true);
                tickTimerStart();
                String content = ScanCodeUtils.getInstance().getQrCode();
                if (!TextUtils.isEmpty(content)) {
                    parseJsonResult(content);
                }
                break;
            default:
                break;
        }

    }
}
