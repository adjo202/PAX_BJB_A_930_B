package com.pax.pay.trans.action.activity;

import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.DetailsActivity;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.up.bjb.R;

public class DispRekBSAActivity extends BaseActivityWithTickForAction implements View.OnClickListener {

    private String prompt1;
    private String prompt2;
    private int maxLen1;
    private int minLen1;
    private int maxLen2;
    private int minLen2;
    private Button confirm;
    private String navTitle;
    private Button btn1, btn2;

    private TextView headerText;
    private String res = "0";


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1:
                res = "1";
                break;
            case R.id.btn2:
                res = "2";
                break;
            case R.id.infos_confirm:
                ActionResult result = new ActionResult(TransResult.SUCC, null);
                finish(result);
                break;
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_disp_rek_bsa;
    }

    @Override
    protected void initViews() {

        confirm = (Button) findViewById(R.id.infos_confirm);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        confirm.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        maxLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_1.toString(), 6);
        minLen1 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_1.toString(), 0);
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        maxLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN_2.toString(), 6);
        minLen2 = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN_2.toString(), 0);

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AAction action = TransContext.getInstance().getCurrentAction();
            ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
            action.setResult(result);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
