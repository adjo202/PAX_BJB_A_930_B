package com.pax.pay.operator;

import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

/**
 * 操作员改密
 * 
 * @author Steven.W
 * 
 */
public class OperChgPwdActivity extends BaseActivity implements OnClickListener {
    private TextView tvTitle;
    private ImageView ivBack;

    private SimpleStyleKeyboardUtil keyboardUtil;
    private CustomEditText edtOperId;
    private CustomEditText edtPwd;
    private CustomEditText edtPwdConfirm;

    private Button btnConfirm;
    private String navTitle;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.modify_pwd_layout;
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        edtOperId = (CustomEditText) findViewById(R.id.oper_id);
        edtPwd = (CustomEditText) findViewById(R.id.new_pwd);
        edtPwdConfirm = (CustomEditText) findViewById(R.id.re_new_pwd);

        setEditListener(edtOperId);
        setEditListener(edtPwd);
        setEditListener(edtPwdConfirm);

        btnConfirm = (Button) findViewById(R.id.oper_confirm);

    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        edtOperId.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtPwd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtPwdConfirm.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
    }

    @Override
    protected void handleMsg(Message msg) {
        // do nothing
    }

    @Override
    public void onClick(View v) {
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();
        switch (v.getId()) {
            case R.id.header_back:
                finish();
                break;
            case R.id.oper_confirm:
                if (updatePwd()) {
                    ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.passwd_revise_succ));
                    finish();
                    return;
                }
                quickClickProtection.stop();
                break;
            default:
                quickClickProtection.stop();
                break;
        }
    }

    private boolean updatePwd() {
        if (!checkOperId()) {
            return false;
        }
        if (!checkPwd()) {
            return false;
        }
        new Operator(edtOperId.getText().toString(), edtPwd.getText().toString()).update();

        return true;

    }

    // 操作员号检查， 00， 99 不允许修改
    private boolean checkOperId() {
        String operId = edtOperId.getText().toString();
        if (TextUtils.isEmpty(operId)) {
            edtOperId.requestFocusAndTouch(keyboardUtil);
            edtOperId.setText("");
            return false;
        }

        if (operId.length() != 2) {
            ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.oper_is_not_exist));
            edtOperId.requestFocusAndTouch(keyboardUtil);
            edtOperId.setText("");
            return false;
        }

        if (operId.equals(SysParam.OPER_SYS) || operId.equals(SysParam.OPER_MANAGE)) {
            edtOperId.requestFocusAndTouch(keyboardUtil);
            edtOperId.setText("");
            edtPwd.setText("");
            edtPwdConfirm.setText("");
            return false;
        }

        if (Operator.find(operId) == null) {
            ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.oper_is_not_exist));
            edtOperId.requestFocusAndTouch(keyboardUtil);
            edtOperId.setText("");
            edtPwd.setText("");
            edtPwdConfirm.setText("");
            return false;
        }

        return true;

    }

    private boolean checkPwd() {

        String operPwd = edtPwd.getText().toString();
        if (TextUtils.isEmpty(operPwd)) {
            edtPwd.requestFocusAndTouch(keyboardUtil);
            edtPwd.setText("");
            return false;
        }

        if (operPwd != null && operPwd.length() != 4) {
            ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.please_enter_four_num));
            edtPwd.requestFocusAndTouch(keyboardUtil);
            edtPwd.setText("");
            return false;
        }

        String pwdConfirm = edtPwdConfirm.getText().toString();
        if (TextUtils.isEmpty(pwdConfirm)) {
            edtPwdConfirm.requestFocusAndTouch(keyboardUtil);
            edtPwdConfirm.setText("");
            return false;
        }

        if (!pwdConfirm.equals(edtPwd.getText().toString())) {
            ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.psw_is_not_equal));
            edtPwdConfirm.requestFocusAndTouch(keyboardUtil);
            edtPwdConfirm.setText("");
            return false;
        }

        return true;
    }

    private void setEditListener(EditText editText) {
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (updatePwd()) {
                        ToastUtils.showMessage(OperChgPwdActivity.this, getString(R.string.passwd_revise_succ));
                        finish();
                        return true;
                    }
                    quickClickProtection.stop();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (keyboardUtil.getKeyboardShowState()) {// 键盘是否visible
                if (!keyboardUtil.keyboardViewIsExisted()) {// 键盘是否在屏幕上占有空间
                    return super.onKeyDown(keyCode, event);
                }
                keyboardUtil.hideAllKeyBoard();
            } else {
                return super.onKeyDown(keyCode, event);
            }

            return false;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
