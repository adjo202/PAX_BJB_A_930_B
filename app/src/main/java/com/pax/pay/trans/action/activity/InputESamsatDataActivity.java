package com.pax.pay.trans.action.activity;

import android.graphics.Color;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.Collections;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class InputESamsatDataActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private Button confirmBtn, btnCari;
    private EditText etKodeBayar;

    private String navTitle, kodeBayar, provinsi, merchantCode;
    private boolean navBack;
    SpinnerDialog spinnerDialog;
    private int selectedItem = -1;


    @Override
    protected int getLayoutId() {
        return R.layout.form_input_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        confirmBtn = (Button) findViewById(R.id.infos_confirm);
        confirmBtn.setEnabled(false);
        confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        confirmBtn.setOnClickListener(this);

        etKodeBayar = (EditText) findViewById(R.id.et_kode_bayar);
        btnCari = (Button) findViewById(R.id.btn_cari);
        btnCari.setBackgroundColor(Color.WHITE);

        ArrayList<String> list = getList();
        Collections.sort(list);

        spinnerDialog = new SpinnerDialog(InputESamsatDataActivity.this, list,
                "Pilih Provinsi");
        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                provinsi = item;
                btnCari.setText(item);
                selectedItem = position;
                merchantCode = getMerchantCode(item);
            }
        });
        btnCari.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });

    }

    String getMerchantCode(String s){
        for (int i=0;i<arr.length;i++){
            if (arr[i][0].equalsIgnoreCase(s)){
                return new String(arr[i][1]);
            }
        }
        return "";
    }

    private ArrayList<String> getList(){
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i][0]);
        }
        return list;
    }

    static String arr[][] = {
            {"E-Samsat Jawa Barat", "PKB"},
            {"E-Samsat Banten", "PKC"},
            {"E-Samsat Kepulauan Riau", "PKE"}
    };


    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        etKodeBayar.addTextChangedListener(watcher);
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
                String promptStr = proses();
                if (!TextUtils.isEmpty(promptStr)) {
                    ToastUtils.showMessage(InputESamsatDataActivity.this, promptStr);
                    return;
                }
                Log.d("teg", "on confirm + " + kodeBayar + " - " + provinsi+" - "+merchantCode);
                result = new ActionResult(TransResult.SUCC, new String[]{
                        kodeBayar.toUpperCase(),
                        provinsi.toUpperCase(),
                        merchantCode.toUpperCase()});

                finish(result);
                break;
            default:
                break;
        }

    }

    private String proses() {
        String result = "";
        kodeBayar = etKodeBayar.getText().toString();
        if (TextUtils.isEmpty(kodeBayar)) {
            result = "Kode Bayar Tidak Boleh Kosong";
            etKodeBayar.setText("");
            return result;
        }

        if (selectedItem == -1) {
            result = "Silahkan Pilih Provinsi";
            return result;
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


    private void changeConfirmButtonStatus(Editable s) {
        String content = s.toString();
        if (content != null && content.length() > 0) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        }
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

}
