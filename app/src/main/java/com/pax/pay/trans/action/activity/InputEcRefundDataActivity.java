package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

import java.text.SimpleDateFormat;

@SuppressLint({ "ClickableViewAccessibility", "SimpleDateFormat" })
public class InputEcRefundDataActivity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "InputEcRefundDataAct";

    private ImageView ivBack;

    private CustomEditText edtOrigTermId;// 原交易终端号
    private CustomEditText edtOrigBatchNo;// 原交易批次号
    private CustomEditText edtOrigTransNo;// 原交易流水号
    private CustomEditText edtOrigTransDate;// 原交易日期

    private Button btnCofirm;
    private Button sysKeyBoard;

    private String origTermId;
    private String origBatchNo;
    private String origTransNo;
    private String origDate;

    private String navTitle;
    private SimpleStyleKeyboardUtil keyboardUtil;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_ec_refund_data_layout;
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);

        ivBack = (ImageView) findViewById(R.id.header_back);

        edtOrigTermId = (CustomEditText) findViewById(R.id.ec_orig_term_id);
        edtOrigTermId.setIMEEnabled(false, true);
        edtOrigTermId.setTag(getString(R.string.prompt_err_term_id));
        edtOrigBatchNo = (CustomEditText) findViewById(R.id.ec_orig_batch_no);
        edtOrigBatchNo.setIMEEnabled(false, true);
        edtOrigBatchNo.setTag(getString(R.string.prompt_err_batch_no));
        edtOrigTransNo = (CustomEditText) findViewById(R.id.ec_orig_trans_no);
        edtOrigTransNo.setIMEEnabled(false, true);
        edtOrigTransNo.setTag(getString(R.string.prompt_err_trans_no));
        edtOrigTransDate = (CustomEditText) findViewById(R.id.ec_orig_date);
        edtOrigTransDate.setIMEEnabled(false, true);
        edtOrigTransDate.setTag(getString(R.string.prompt_card_date_err));

        btnCofirm = (Button) findViewById(R.id.infos_confirm);
        btnCofirm.setOnClickListener(this);

        sysKeyBoard = (Button) findViewById(R.id.edit_system_keyboard);
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            changeConfirmButtonStatus(s);
        }
    };

    private void changeConfirmButtonStatus(Editable s) {
        String content = s.toString();
        if (content != null && content.length() > 0) {
            btnCofirm.setEnabled(true);
            btnCofirm.setBackgroundResource(R.drawable.button_click_background);
        } else {
            btnCofirm.setEnabled(false);
            btnCofirm.setBackgroundResource(R.drawable.gray_button_background);
        }
    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        edtOrigTermId.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtOrigTermId.addTextChangedListener(watcher);

        edtOrigBatchNo.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtOrigBatchNo.addTextChangedListener(watcher);

        edtOrigTransNo.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtOrigTransNo.addTextChangedListener(watcher);

        edtOrigTransDate.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtOrigTransDate.addTextChangedListener(watcher);

        btnCofirm.setOnClickListener(this);

        setEdtListener(edtOrigTermId);
        setEdtListener(edtOrigBatchNo);
        setEdtListener(edtOrigTransNo);
        setEdtListener(edtOrigTransDate);

        setEditText_alphnum(edtOrigTermId);
        showSysKeyboard(edtOrigTermId);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    // 数字加字母
    private void setEditText_alphnum(EditText editText) {
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                char[] numberChars = "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
                return numberChars;
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
    }

    // 显示调用系统键盘按键
    private void showSysKeyboard(final EditText editText) {
        sysKeyBoard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });

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

    @Override
    public void onClick(View v) {
        ActionResult result = null;
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case R.id.infos_confirm:
                String promptStr = process();
                quickClickProtection.stop();
                if (promptStr != null) {
                    ToastUtils.showMessage(InputEcRefundDataActivity.this, promptStr);
                    return;
                }

                result = new ActionResult(TransResult.SUCC, new String[] { origTermId, origBatchNo, origTransNo,
                        origDate });
                finish(result);

                break;

            default:
                quickClickProtection.stop();
                break;
        }
    }

    private String process() {

        String result = "";

        // 原交易终端号
        origTermId = edtOrigTermId.getText().toString();
        if (origTermId == null || origTermId.length() != 8) {
            result = getString(R.string.prompt_err_term_id);
            edtOrigTermId.setText("");
            edtOrigTermId.requestFocusAndTouch(keyboardUtil);
            return result;
        }

        // 原交易批次号
        origBatchNo = edtOrigBatchNo.getText().toString();
        if (origBatchNo == null || origBatchNo.length() == 0) {
            result = getString(R.string.prompt_err_batch_no);
            edtOrigBatchNo.setText("");
            edtOrigBatchNo.requestFocusAndTouch(keyboardUtil);
            return result;
        }
        origBatchNo = String.format("%06d", Long.parseLong(origBatchNo));

        // 原交易流水号
        origTransNo = edtOrigTransNo.getText().toString();
        if (origTransNo == null || origTransNo.length() == 0) {
            result = getString(R.string.prompt_err_trans_no);
            edtOrigTransNo.setText("");
            edtOrigTransNo.requestFocusAndTouch(keyboardUtil);
            return result;
        }
        origTransNo = String.format("%06d", Long.parseLong(origTransNo));

        // 原交易日期
        origDate = edtOrigTransDate.getText().toString();
        if (origDate.length() != 4) {
            edtOrigTransDate.setText("");
            edtOrigTransDate.requestFocusAndTouch(keyboardUtil);
            return getString(R.string.prompt_card_date_err);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(origDate);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            result = getString(R.string.prompt_card_date_err);
            edtOrigTransDate.setText("");
            edtOrigTransDate.requestFocusAndTouch(keyboardUtil);
            return result;
        }
        return null;

    }

    private void setEdtListener(final CustomEditText editText) {
        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String promptStr = process();
                    if (promptStr != null) {
                        if (v.getTag().equals(promptStr)) {
                            ToastUtils.showMessage(InputEcRefundDataActivity.this, promptStr);

                        }
                        return true;
                    }

                    ActionResult result = new ActionResult(TransResult.SUCC, new String[] { origTermId, origBatchNo,
                            origTransNo, origDate });
                    finish(result);
                }
                return false;
            }
        });
    }
}
