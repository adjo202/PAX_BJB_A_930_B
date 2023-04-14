package com.pax.pay.operator;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

/**
 * 添加操作员
 * 
 * @author Steven.W
 * 
 */
public class OperAddActivity extends BaseActivity implements OnClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private CustomEditText edtOperName;
    private CustomEditText edtOperId;
    private CustomEditText edtOperPwd;

    private Button btnConfirm;
    private Button sysKeyBoard;

    private String navTitle;
    private static final int MSG_SUCCESS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.add_oper_layout;
    }

    @Override
    protected void initViews() {

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        edtOperName = (CustomEditText) findViewById(R.id.new_oper_name);
        edtOperId = (CustomEditText) findViewById(R.id.new_oper_id);
        edtOperPwd = (CustomEditText) findViewById(R.id.new_oper_pwd);

        setEditListener(edtOperName);
        setEditListener(edtOperId);
        setEditListener(edtOperPwd);

        btnConfirm = (Button) findViewById(R.id.oper_confirm);

        //Added by Steven.T 2017-6-15 15:10:58
        btnConfirm.setEnabled(false);

        // 输入框数值监听
        edtOperId.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                confirmBtnChange();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable e) {

            }
        });

    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case MSG_SUCCESS:
                final CustomAlertDialog dialog = new CustomAlertDialog(OperAddActivity.this,
                        CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(getString(R.string.add_success_end));
                dialog.setContentText(getString(R.string.add_success_continue));
                dialog.show();
                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {

                        edtOperName.setText("");
                        edtOperId.setText("");
                        edtOperPwd.setText("");
                        dialog.dismiss();
                    }
                });

                break;

            default:
                break;
        }

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
                if (saveOper()) {
                    Message.obtain(handler, MSG_SUCCESS).sendToTarget();
                }
                quickClickProtection.stop();
                break;

            default:
                quickClickProtection.stop();
                break;
        }

    }

    private boolean saveOper() {
        // 检查操作员号
        String operId = edtOperId.getText().toString();

        if (operId == null || operId.length() != 2) {
            edtOperId.requestFocus();
            edtOperId.setText("");
            return false;
        }

        if (operId.equals("00") || operId.equals("99")) {
            edtOperId.requestFocus();
            edtOperId.setText("");
            ToastUtils.showMessage(OperAddActivity.this, getString(R.string.add_oper_not_allow));
            return false;
        }

        if (Operator.find(operId) != null) {
            edtOperId.requestFocus();
            edtOperId.setText("");
            edtOperPwd.setText("");
            ToastUtils.showMessage(OperAddActivity.this, getString(R.string.oper_exist));
            return false;
        }
        // 检查操作员密码
        String operPwd = edtOperPwd.getText().toString();

        if (operPwd == null || operPwd.length() != 4) {
            edtOperPwd.requestFocus();
            edtOperPwd.setText("");
            ToastUtils.showMessage(OperAddActivity.this, getString(R.string.please_enter_four_num));
            return false;
        }

        // 保存操作员
        String operName = edtOperName.getText().toString();
        boolean isSuccess = new Operator(operId, operPwd, operName).add();
        if (!isSuccess) {
            ToastUtils.showMessage(OperAddActivity.this, getString(R.string.add_failure_end));
            return false;
        }

        return true;
    }

    private void setEditListener(EditText editText) {

        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (saveOper()) {
                        Message.obtain(handler, MSG_SUCCESS).sendToTarget();
                    }
                    quickClickProtection.stop();
                    return true;
                }
                return false;
            }
        });

    }

    private void confirmBtnChange() {
        String content = process(edtOperId);
        if (content != null) {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundResource(R.drawable.button_click_background);
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackgroundResource(R.drawable.gray_button_background);
        }
    }
    /**
     * 输入数值检查
     */
    private String process(CustomEditText editText) {
        String content = editText.getText().toString().trim();

        if (content == null || content.length() == 0) {
            return null;
        }
        return content;
    }

}
