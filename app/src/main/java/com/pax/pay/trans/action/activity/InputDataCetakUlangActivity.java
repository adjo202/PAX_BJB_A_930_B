package com.pax.pay.trans.action.activity;

import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pax.pay.trans.FundTransferTrans.arr;


public class InputDataCetakUlangActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private Button confirmBtn;
    private EditText etKodeBilling, etNtb;

    private String navTitle, kodeBilling = "", ntb = "";
    private boolean navBack;
    private boolean mEditing;

    Currency currency;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_cetak_ulang_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        confirmBtn = (Button) findViewById(R.id.infos_confirm);
        confirmBtn.setOnClickListener(this);

        etKodeBilling = (EditText) findViewById(R.id.et_kode_billing);
        etNtb = (EditText) findViewById(R.id.et_ntb);

        currency = FinancialApplication.getSysParam().getCurrency();

    }


    private String getDisplayAmount(String digits) {
        final String PATTERN = "###,###";
        String displayAmount = "";
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        Log.d("teg", "curr : "+currency.toString());

        /*if (digits.length() > 9) {
            ToastUtils.showMessage(InputDataTransferctivity.this, "");
            return strPre;
        }*/

        if (digits == null || digits.length() == 0) {
            return currency.getFormat();
        }

        try {

            DecimalFormat decimalFormat = new DecimalFormat(PATTERN);
            displayAmount = decimalFormat.format(Long.valueOf(digits));

        } catch (Exception e) {
            Log.e("teg", "Exception", e);
            displayAmount = currency.getFormat();
        }

        return displayAmount;
    }


    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        etKodeBilling.addTextChangedListener(watcher);
        etNtb.addTextChangedListener(watcher);

    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
    }

    @Override
    protected void handleMsg(Message msg) {

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
                try {
                    String promptStr = proses();
                    if (!TextUtils.isEmpty(promptStr)) {
                        ToastUtils.showMessage(InputDataCetakUlangActivity.this, promptStr);
                        return;
                    }
                    result = new ActionResult(TransResult.SUCC, new String[]{kodeBilling, ntb});

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

        kodeBilling = etKodeBilling.getText().toString();
        if (TextUtils.isEmpty(kodeBilling)) {
            result = "Kode Billing Tidak Boleh Kosong";
            etKodeBilling.setText("");
            return result;
        }

        if (kodeBilling.length()<15){
            result = "Kode Billing Kurang Dari 15 Digit";
        }

        ntb = etNtb.getText().toString();
        if (TextUtils.isEmpty(ntb)) {
            result = "NTB Tidak Boleh Kosong";
            etNtb.setText("");
            return result;
        }

        if (ntb.length()<12){
            result = "NTB Kurang Dari 12 Digit";
        }

        return result;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        confirmBtn.setEnabled(true);
        confirmBtn.setBackgroundResource(R.drawable.button_click_background);

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

    private void setOnKeyListener(View view) {
        view.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                /*if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER
                        && confirmBtn.isEnabled()) {
                    ActionResult result = new ActionResult(TransResult.SUCC, option);
                    AppLog.i("teg", "datas : "+datas);
                    finish(result);
                    return true;
                }*/
                return false;
            }
        });
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
        /*if (content != null && content.length() > 0) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
        finish(result);
    }

}
