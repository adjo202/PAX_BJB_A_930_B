package com.pax.pay.trans.action.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.RedeemData;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class InputDataRedeemPointActivity extends BaseActivityWithTickForAction implements View.OnClickListener {

    private Button confirmBtn;
    private EditText et_voucher_code, et_id_billing;
    private String navTitle, billerId,voucherCode;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_redeem_point;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        confirmBtn = (Button) findViewById(R.id.lanjutkanBtn);
        et_voucher_code = (EditText) findViewById(R.id.et_voucher_code);
        et_id_billing = (EditText) findViewById(R.id.et_id_billing);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

    }


    @Override
    protected void setListeners() {

        et_voucher_code.addTextChangedListener(watcher);
        et_id_billing.addTextChangedListener(watcher);
        confirmBtn.setOnClickListener(this);

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        ActionResult result = null;
        switch (v.getId()) {
            case R.id.lanjutkanBtn:
                try {
                    String promptStr = proses();
                    if (!TextUtils.isEmpty(promptStr)) {
                        ToastUtils.showMessage(InputDataRedeemPointActivity.this, promptStr);
                        return;
                    }
                 
                    result = new ActionResult(TransResult.SUCC, new RedeemData(billerId,voucherCode));

                    finish(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    private String proses() {
        String result = "";

        billerId = et_id_billing.getText().toString();
        if (TextUtils.isEmpty(billerId)) {
            result = "Id Billing / Nomor telp tidak boleh kosong!";
            return result;
        }
        if (billerId.length() < 8) {
            result = "Minimal 8 Digit!";
            return result;
        }

        voucherCode = et_voucher_code.getText().toString();
        if (TextUtils.isEmpty(voucherCode)) {
            result = "Kode voucher tidak boleh kosong!";
            return result;
        }


        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish( new ActionResult(TransResult.ERR_ABORTED, null));
            return true;
        }

        return super.onKeyDown(keyCode, event);
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

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(new ActionResult(TransResult.ERR_ABORTED, null));

    }


}


