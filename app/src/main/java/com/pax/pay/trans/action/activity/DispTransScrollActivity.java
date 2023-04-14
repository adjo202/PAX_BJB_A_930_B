package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.Utils;
import com.pax.pay.utils.ViewUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;

public class DispTransScrollActivity extends BaseActivityWithTickForAction implements View.OnClickListener {

    private TextView tvTitle, tvnoAcc;
    private ImageView ivBack;
    private Button btnConfirm;

    private String navTitle, noAcc;
    private boolean navBack;
    private LinearLayout llDetailContainer;

    private ArrayList<String> leftColumns = new ArrayList<String>();
    private ArrayList<String> rightColumns = new ArrayList<String>();

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
        noAcc = getIntent().getStringExtra("noacc");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trans_detail_scroll;
    }

    @Override
    protected void initViews() {
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        tvnoAcc = (TextView) findViewById(R.id.noacc);
        tvnoAcc.setText("No Rekening : "+noAcc);

        llDetailContainer = (LinearLayout) findViewById(R.id.detail_layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;

        if (Utils.isScreenOrientationPortrait(this)) {
            for (int i = 0; i < leftColumns.size(); i++) {
                RelativeLayout layer = ViewUtils.genSingleLineLayoutBlack(DispTransScrollActivity.this, leftColumns.get(i),
                        rightColumns.get(i));
                llDetailContainer.addView(layer, params);
            }
        } else { //Modified by Steven 2017-4-13 14:26:53
            /*
            ListView listview = (ListView) findViewById(R.id.query_listview);
            List<HashMap<String, Object>> transList = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < leftColumns.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("transType", leftColumns.get(i));
                map.put("transValue", rightColumns.get(i));

                transList.add(map);
            }

            DetailListAdapter transDetailListAdapter = new DetailListAdapter(DispTransDetailActivity.this, transList);
            listview.setFocusable(false);
            listview.setAdapter(transDetailListAdapter);
            */
        }
        btnConfirm = (Button) findViewById(R.id.confirm_btn);
    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        btnConfirm.setOnClickListener(this);
        btnConfirm.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    ActionResult result = new ActionResult(TransResult.SUCC, null);
                    finish(result);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {

        ActionResult result = null;
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case R.id.confirm_btn:
                result = new ActionResult(TransResult.SUCC, null);
                finish(result);

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

    }

}

