package com.pax.pay.trans.action.activity;

import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.AccountData;
import com.pax.pay.trans.model.OptionModel;
import com.pax.up.bjb.R;
import com.pax.view.OptionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectAccountActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private TextView tvPrompt;
    private GridView mGridView;

    private Button confirmBtn;

    private String navTitle;
    private boolean navBack;
    private String prompt1;

    private OptionsAdapter mAdapter;

    private ArrayList<AccountData> mNameList = new ArrayList<AccountData>();
    private OptionModel option;

    private List<OptionModel> data = new ArrayList<OptionModel>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_account_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);

        tvPrompt = (TextView) findViewById(R.id.prompt_select);
        tvPrompt.setText(prompt1);

        mGridView = (GridView) findViewById(R.id.grid_select);
        data = getData(mNameList);

        mAdapter = new OptionsAdapter(this, data);
        mGridView.setAdapter(mAdapter);

        confirmBtn = (Button) findViewById(R.id.info_confirm);
        confirmBtn.setEnabled(false);

    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        mGridView.setOnItemClickListener(this);
        confirmBtn.setOnClickListener(this);

        setOnKeyListener(confirmBtn);
        setOnKeyListener(mGridView);

    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        mNameList = (ArrayList<AccountData>) getIntent().getSerializableExtra(EUIParamKeys.CONTENT.toString());
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
            case R.id.info_confirm:
                result = new ActionResult(TransResult.SUCC, option.getObject());
                finish(result);
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.changeState(position);
        confirmBtn.setEnabled(true);
        confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        option = data.get(position);
    }

    private List<OptionModel> getData(ArrayList<AccountData> list) {
        List<OptionModel> models = new ArrayList<OptionModel>();
        for (int i = 0; i < list.size(); i++) {
            models.add(new OptionModel(String.format("%02d", i), list.get(i).getAccountNumber(), list.get(i)));
        }
        return models;
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
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER
                        && confirmBtn.isEnabled()) {
                    ActionResult result = new ActionResult(TransResult.SUCC, option);
                    finish(result);
                    return true;
                }
                return false;
            }
        });
    }
}
