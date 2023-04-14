package com.pax.pay.trans.action.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.QRView;
import com.pax.view.dialog.DispExDeviceDialog;


/**
 * sandy@indopay.com
 * 2019-06-28
 */


public class DispQRActivity extends BaseActivityWithTickForAction implements OnClickListener {


    private QRView mQRView;
    private RelativeLayout writeUserName = null;
    //private Button clearBtn;
    private Button confirmBtn;
    private ImageView backBtn;


    private String qr;
    private String amount;

    private boolean processing = false;


/*
    private View.OnKeyListener onkeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                confirmBtn.performClick();
                return true;
            }
            return false;
        }
    };
*/
    @Override
    protected int getLayoutId() {
        return R.layout.activity_disp_qr_layout;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
        qr = bundle.getString(EUIParamKeys.CONTENT.toString());
    }

    @Override
    protected void initViews() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        TextView headerText = (TextView) findViewById(R.id.header_title);
        //headerText.setText(R.string.signature);
        headerText.setText("QR Code");

        backBtn = (ImageView) findViewById(R.id.header_back);
        backBtn.setVisibility(View.VISIBLE);

        TextView amountLabel = (TextView) findViewById(R.id.trans_amount_label);
        amountLabel.setText(String.format("%s ",currency.getName()));


        TextView amountText = (TextView) findViewById(R.id.trans_amount_tv);
        amount = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(Long.parseLong(amount)),
                currency.getCurrencyExponent(), true);
        amountText.setText(amount);

        writeUserName = (RelativeLayout) findViewById(R.id.writeUserNameSpace);
        //clearBtn = (Button) findViewById(R.id.clear_btn);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.requestFocus();
        confirmBtn.setText("Inquiry");


        //Display QR
        mQRView = new QRView(DispQRActivity.this);
        mQRView.setText(qr);
        writeUserName.addView(mQRView);
        //mQRView.setOnKeyListener(onkeyListener);




    }

    @Override
    protected void setListeners() {
        confirmBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

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
        // Do nothing
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
            case R.id.confirm_btn:
                clearProcessFlag();
                finish(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                break;
        }

    }

    protected void setProcessFlag() {
        processing = true;
    }

    protected void clearProcessFlag() {
        processing = false;
    }

    protected boolean isProcessing() {
        return processing;
    }


}
