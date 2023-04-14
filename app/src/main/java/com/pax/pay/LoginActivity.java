package com.pax.pay;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;

import com.pax.device.Device;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.menu.OperMenuActivity;
import com.pax.pay.operator.Operator;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SettingsActivity;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;
import com.pax.view.dialog.DialogUtils;

public class LoginActivity extends BaseActivityWithTickForAction implements OnClickListener {

    public static final String OPER_ID = "operID"; // 操作员KEY

    private SimpleStyleKeyboardUtil keyboardUtil;
    private CustomEditText edtOperId;
    private CustomEditText edtOperPwd;
    private Button btnConfirm;


    private static final int REQUEST_CODE = 1;
    private static final String TAG = "LoginActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimerStop();
        setPermission();
        //Device.getSIMInfo();
    }


    private void setPermission(){
        int phonePermissionCheck            = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int fineLocationPermissionCheck     = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermissionCheck   = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        String [] permissionSet = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };


        if (    fineLocationPermissionCheck != PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck != PackageManager.PERMISSION_GRANTED &&
                phonePermissionCheck != PackageManager.PERMISSION_GRANTED
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionSet,108);
            }

            return;
        }
    }







    @Override
    protected void onResume() {
        super.onResume();
        setEdiEmpty();
        // KeyBoardUtils.show(LoginActivity.this, flKeyboardContainer);
    }

    @Override
    protected void loadParam() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_layout;
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        edtOperId = (CustomEditText) findViewById(R.id.oper_id_edt);

        edtOperId.setIMEEnabled(false, true);
        edtOperId.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

                if (arg1 == KeyEvent.KEYCODE_ENTER && arg2.getAction() == KeyEvent.ACTION_UP) {
                    process();
                    return true;
                }
                return false;
            }
        });
        edtOperPwd = (CustomEditText) findViewById(R.id.oper_pwd_edt);
        edtOperPwd.setIMEEnabled(false, true);
        edtOperPwd.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_ENTER && arg2.getAction() == KeyEvent.ACTION_UP) {
                    process();
                    return true;
                }
                return false;
            }
        });
        btnConfirm = (Button) findViewById(R.id.oper_confirm);

    }

    @Override
    protected void setListeners() {
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
                        ToastUtils.showMessage(LoginActivity.this, getString(R.string.oper_is_not_exist));
                        edtOperId.setText("");
                        edtOperId.requestFocusAndTouch(keyboardUtil);
                        return;
                    }
                    edtOperPwd.requestFocusAndTouch(keyboardUtil);
                }
            }
        });

        edtOperPwd.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        edtOperPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

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

            }
        });

        btnConfirm.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 退出当前应用
//            DialogUtils.showExitAppDialog(LoginActivity.this);
            DialogUtils.showExitAppDialog(LoginActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void handleMsg(Message msg) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oper_confirm:
                process();
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
                ActivityStack.getInstance().pop();
            } else {
                setPwdEditEmpty();
                ToastUtils.showMessage(LoginActivity.this, getString(R.string.oper_pwd_is_err));
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
                ActivityStack.getInstance().pop();
            } else {
                setPwdEditEmpty();
                ToastUtils.showMessage(LoginActivity.this, getString(R.string.oper_pwd_is_err));
            }
            return;
        }

        // 操作员登录
        Operator operator = Operator.find(operId);
        if (operator == null) {
            setEdiEmpty();
            ToastUtils.showMessage(LoginActivity.this, getString(R.string.oper_is_not_exist));
            return;
        }
        if (!operator.getPd().equals(operPwd)) {
            setPwdEditEmpty();
            ToastUtils.showMessage(LoginActivity.this, getString(R.string.oper_pwd_is_err));
            return;
        }

        FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS, Controller.Constant.YES);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(OPER_ID, operId);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        ActivityStack.getInstance().pop();
    }

    private void setEdiEmpty() {
        edtOperId.setText("");
        edtOperPwd.setText("");
        edtOperId.requestFocusAndTouch(keyboardUtil);
    }

    private void setPwdEditEmpty() {
        edtOperPwd.setText("");
        edtOperPwd.setFocusable(true);
        edtOperPwd.requestFocusAndTouch(keyboardUtil);
    }

}
