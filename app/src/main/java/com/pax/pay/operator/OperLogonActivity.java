package com.pax.pay.operator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.menu.OperMenuActivity;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SettingsActivity;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

public class OperLogonActivity extends BaseActivity implements OnClickListener {
    private TextView tvTitle; // 标题
    private ImageView ivBack; // 返回按钮
    private CustomEditText edtOperId; // 员工号输入框
    private CustomEditText edtOperPwd; // 密码输入框

    private SimpleStyleKeyboardUtil keyboardUtil;
    private Button btnConfirm; // 确定按钮

    private String navTitle;
    private boolean navBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d("OperLogonActivity","Sandy=OperLogonActivity:" );
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEdiEmpty();
    }

    private void setEdiEmpty() {
        edtOperId.setText("");
        edtOperPwd.setText("");
        edtOperId.requestFocusAndTouch(keyboardUtil);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_oper_logon;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);
        edtOperId = (CustomEditText) findViewById(R.id.oper_no);
        edtOperId.setIMEEnabled(false, true);

        edtOperPwd = (CustomEditText) findViewById(R.id.oper_pwd);
        edtOperPwd.setIMEEnabled(false, true);

        btnConfirm = (Button) findViewById(R.id.oper_confirm);
        btnConfirm.setEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        edtOperId.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        edtOperId.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 2) {
                    if (s.toString().equals(SysParam.OPER_MANAGE) || s.toString().equals(SysParam.OPER_SYS)) {
                        edtOperPwd.requestFocusAndTouch(keyboardUtil);
                        return;
                    }
                    Operator oper = Operator.find(s.toString());
                    if (oper == null) {
                        ToastUtils.showMessage(OperLogonActivity.this, getString(R.string.oper_is_not_exist));
                        edtOperId.setText("");
                        edtOperId.requestFocusAndTouch(keyboardUtil);
                        return;
                    }
                    edtOperPwd.requestFocusAndTouch(keyboardUtil);
                    changeConfirmButtonStatus();
                }
            }
        });

        edtOperId.setOnKeyListener(new OnKeyListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    process();
                }
                return false;
            }
        });

        edtOperPwd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        edtOperPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String operId = edtOperId.getText().toString().trim();

                if (operId.equals(SysParam.OPER_SYS)) {
                    edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8) });
                } else if (operId.equals(SysParam.OPER_MANAGE)) {
                    edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
                } else {
                    edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                changeConfirmButtonStatus();
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
        });

        edtOperPwd.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    process();
                }
                return false;
            }
        });

        btnConfirm.setOnClickListener(this);
    }

    private void changeConfirmButtonStatus() {
        String operId = edtOperId.getText().toString();
        String operPwd = edtOperPwd.getText().toString();
        if (operId == null || operId.length() != 2 || operPwd == null || operPwd.length() == 0) {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackgroundResource(R.drawable.gray_button_background);
        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundResource(R.drawable.button_click_background);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oper_confirm:
                process();
                break;
            case R.id.header_back:
                finish();
                break;
            default:
                break;
        }

    }

    /**
     * 操作员号和密码检查
     */
    private void process() {
        String operId = edtOperId.getText().toString().trim();
        String operPwd = edtOperPwd.getText().toString().trim();
        if (operId == null || operId.length() == 0) {
            edtOperId.setFocusable(true);
            edtOperId.requestFocusAndTouch(keyboardUtil);
            return;
        }

        if (operId.equals(SysParam.OPER_SYS)) {
            edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8) });
        } else if (operId.equals(SysParam.OPER_MANAGE)) {
            edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        } else {
            edtOperPwd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
        }

        if (operPwd == null || operPwd.length() == 0) {
            edtOperPwd.setFocusable(true);
            edtOperPwd.requestFocusAndTouch(keyboardUtil);
            return;
        }

        // 系统管理员登录
        if (operId.equals(SysParam.OPER_SYS)) {

            if (operPwd.equals(FinancialApplication.getSysParam().get(SysParam.SEC_SYSPWD))) {
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.settings_title));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                ToastUtils.showMessage(OperLogonActivity.this, getString(R.string.oper_pwd_is_err));
            }
            return;
        }
        // 主管登录
        if (operId.equals(SysParam.OPER_MANAGE)) {
            if (operPwd.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.oper_manage));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                Intent intent = new Intent(this, OperMenuActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                ToastUtils.showMessage(OperLogonActivity.this, getString(R.string.oper_pwd_is_err));
            }
            return;
        }

        Operator operator = Operator.find(operId);
        if (operator == null) {
            edtOperId.setText("");
            edtOperPwd.setText("");
            edtOperId.requestFocusAndTouch(keyboardUtil);
            ToastUtils.showMessage(OperLogonActivity.this, getString(R.string.oper_is_not_exist));
            return;
        }

        if (!operator.getPd().equals(operPwd)) {
            edtOperPwd.setText("");
            edtOperPwd.setFocusable(true);
            edtOperPwd.requestFocusAndTouch(keyboardUtil);
            ToastUtils.showMessage(OperLogonActivity.this, getString(R.string.oper_pwd_is_err));
            return;
        }

        FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS, Controller.Constant.YES);

        if (operId.equals(SysParam.OPER_MANAGE) || operId.equals(SysParam.OPER_SYS)) {
            return;
        }
        TransContext.getInstance().setOperID(operId);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!navBack) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
