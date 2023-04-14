package com.pax.pay.trans.action.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.Utils;
import com.pax.pay.utils.ViewUtils;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.util.ArrayList;


public class DispTransDetailUsing2ButtonActivity extends BaseActivityWithTickForAction implements OnClickListener {

    private TextView tvTitle;
    private ImageView ivBack;
    private Button btnConfirm,btnCancel;

    private String navTitle;
    private boolean navBack;
    private LinearLayout llDetailContainer;

    private ArrayList<String> leftColumns = new ArrayList<String>();
    private ArrayList<String> rightColumns = new ArrayList<String>();
    private String content;
    private CustomAlertDialog confirmDialog;


    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        content = getIntent().getStringExtra(EUIParamKeys.CONTENT.toString());
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trans_detail_with_cancel_scroll_new;
    }

    @Override
    protected void initViews() {
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        llDetailContainer = (LinearLayout) findViewById(R.id.detail_layout);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;

        if (Utils.isScreenOrientationPortrait(this)) {
            for (int i = 0; i < leftColumns.size(); i++) {
                //RelativeLayout layer = ViewUtils.genSingleLineLayoutNew(DispTransDetailNewActivity.this, leftColumns.get(i), rightColumns.get(i));
                String label = leftColumns.get(i);
                String value = rightColumns.get(i);

                LinearLayout layer = ViewUtils.genSingleLineLayoutNew(DispTransDetailUsing2ButtonActivity.this, leftColumns.get(i), rightColumns.get(i));
                //Sandy : disable admin fee if the value is Rp 0 (zero)
                if(i == 5 && value.toString().trim().toLowerCase().contains("rp 0"))
                    layer.setVisibility(View.GONE);
                else
                    layer.setVisibility(View.VISIBLE);

                layer.setBackgroundResource(R.drawable.edit_frame);
                layer.setPadding(5,5,5,5);
                llDetailContainer.addView(layer, params);
            }
        }

        btnConfirm  = (Button) findViewById(R.id.confirm_btn);
        btnCancel   = (Button) findViewById(R.id.cancel_btn);
    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        btnConfirm.setOnClickListener(this);
        btnConfirm.setOnKeyListener(new OnKeyListener() {

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

        btnCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        ActionResult result = null;
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.SUCC, "back1");
                finish(result);
                break;
            case R.id.cancel_btn:
                result = new ActionResult(TransResult.SUCC, "back2");
                finish(result);
                break;
            case R.id.confirm_btn:

                if (confirmDialog != null) {
                    confirmDialog.dismiss();
                }
                confirmDialog = new CustomAlertDialog(this, CustomAlertDialog.NORMAL_TYPE);
                confirmDialog.show();
                confirmDialog.setTitleText(navTitle);
                confirmDialog.setContentText("Do you want to proceed this transaction?");
                confirmDialog.setCancelable(false);
                confirmDialog.setCanceledOnTouchOutside(false);
                confirmDialog.showCancelButton(true);
                confirmDialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();

                    }
                });
                confirmDialog.showConfirmButton(true);
                confirmDialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        finish(new ActionResult(TransResult.SUCC, "Ok"));
                    }
                });
                confirmDialog.show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
        finish(result);
    }
}
