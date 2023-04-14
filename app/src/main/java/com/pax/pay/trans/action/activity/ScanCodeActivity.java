package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ScanCodeUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

public class ScanCodeActivity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "ScanCodeActivity";

    private String amount;
    private String navTitle;

    private boolean navBack;

    private Button btnScanConfirm; // 扫码确认按钮
    private ImageView ivBack; // 返回按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_code_activity;
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        TextView tvAmount = (TextView) findViewById(R.id.amount_txt_ec);
        tvAmount.setText(amount);

        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);

        btnScanConfirm = (Button) findViewById(R.id.ok_scan_btn);

    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }
        btnScanConfirm.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        // 显示金额
        try {
            amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
            if (amount != null && amount.length() > 0) {
                amount = FinancialApplication.getConvert().amountMinUnitToMajor(amount,
                        currency.getCurrencyExponent(), true);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            amount = null;
        }

    }

    @Override
    public void onClick(View v) {
        ActionResult result = null;
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case R.id.ok_scan_btn:
                btnScanConfirm.setEnabled(false);
                tickTimerStop(); // 扫码时暂停计时
                ScanCodeUtils.getInstance().start(ScanCodeActivity.this, handler);
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

    @Override
    protected void handleMsg(Message msg) {

        switch (msg.what) {
            case ScanCodeUtils.SCAN_CODE_END:
                String qrCode = ScanCodeUtils.getInstance().getQrCode();
                if (qrCode != null && qrCode.length() > 0) {
                    finish(new ActionResult(TransResult.SUCC, qrCode));
                } else {
                    finish(new ActionResult(TransResult.ERR_ABORTED, null));
                }
                break;
            default:
                break;
        }

    }

}
