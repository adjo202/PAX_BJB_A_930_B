package com.pax.pay.operator;

import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

/**
 * 修改主管密码
 * 
 * @author Steven.W
 * 
 */
public class ManagerChgPwdActivity extends BaseActivity implements OnClickListener {
    private ImageView ivBack;

    private SimpleStyleKeyboardUtil keyboardUtil;
    private CustomEditText edtNewPwd;
    private CustomEditText edtNewPwdConfirm;

    private Button btnConfirm;
    private String navTitle;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.manager_chg_pwd_layout;
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        edtNewPwd = (CustomEditText) findViewById(R.id.new_pwd);
        edtNewPwdConfirm = (CustomEditText) findViewById(R.id.re_new_pwd);

        setEditListener(edtNewPwd);
        setEditListener(edtNewPwdConfirm);

        btnConfirm = (Button) findViewById(R.id.oper_confirm);

    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        edtNewPwd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));
        edtNewPwdConfirm.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

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
                quickClickProtection.stop();
                if (updatePwd()) {
                    ToastUtils.showMessage(ManagerChgPwdActivity.this, getString(R.string.passwd_revise_succ));
                    finish();
                }
                break;
            default:
                quickClickProtection.stop();
                break;
        }

    }

    private boolean updatePwd() {

        // 检查newPwd合法性
        String newPWD = edtNewPwd.getText().toString();
        if (newPWD == null || "".equals(newPWD)) {
            edtNewPwd.requestFocusAndTouch(keyboardUtil);
            edtNewPwd.setText("");
            return false;
        }

        if (newPWD != null && newPWD.length() != 6) {
            ToastUtils.showMessage(ManagerChgPwdActivity.this, getString(R.string.please_enter_six_num));
            edtNewPwd.requestFocusAndTouch(keyboardUtil);
            edtNewPwd.setText("");
            return false;
        }

        // 检查newPwdConfirm合法性
        String newAgainPWD = edtNewPwdConfirm.getText().toString();

        if (newAgainPWD == null || "".equals(newAgainPWD)) {
            edtNewPwdConfirm.requestFocusAndTouch(keyboardUtil);
            edtNewPwdConfirm.setText("");
            return false;
        }

        // 比较两次输入是否相同
        if (!newAgainPWD.equals(newPWD)) {
            ToastUtils.showMessage(ManagerChgPwdActivity.this, getString(R.string.psw_is_not_equal));
            edtNewPwdConfirm.requestFocusAndTouch(keyboardUtil);
            edtNewPwdConfirm.setText("");
            return false;
        }
        String pasword = edtNewPwd.getText().toString().trim();
        FinancialApplication.getSysParam().set(SysParam.SEC_MNGPWD, pasword);
        return true;

    }

    private void setEditListener(EditText editText) {
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (updatePwd()) {
                        ToastUtils.showMessage(ManagerChgPwdActivity.this, getString(R.string.passwd_revise_succ));
                        quickClickProtection.stop();
                        finish();
                    } else {
                        quickClickProtection.stop();
                    }
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
