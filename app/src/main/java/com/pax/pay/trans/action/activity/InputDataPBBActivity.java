package com.pax.pay.trans.action.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class InputDataPBBActivity extends BaseActivityWithTickForAction implements View.OnClickListener {

    private Button confirmBtn;
    private EditText et_nop, et_tahun;

    private String navTitle, nop, tahun, pemda;
    private int selectedItem = -1;
    private Button svPemda;
    SpinnerDialog spinnerDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_pbb;
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
//        confirmBtn.setOnClickListener(this);

        et_nop = (EditText) findViewById(R.id.et_nop);
        et_tahun = (EditText) findViewById(R.id.et_tahun);
        svPemda = (Button) findViewById(R.id.sv_pemda);
        svPemda.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        spinnerDialog = new SpinnerDialog(InputDataPBBActivity.this, list_pemda(),
                "Daftar Kab/Kota");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
//                Toast.makeText(InputDataPBBActivity.this, item + "  " + position + "", Toast.LENGTH_SHORT).show();
                svPemda.setText(item);
                selectedItem = position;
            }
        });

        svPemda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });
    }

    private ArrayList<String> list_pemda() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < kode_pemda.length; i++) {
            list.add(kode_pemda[i][1]);
        }
        return list;
    }


    @Override
    protected void setListeners() {

        et_nop.addTextChangedListener(watcher);
        et_tahun.addTextChangedListener(watcher);
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
                        ToastUtils.showMessage(InputDataPBBActivity.this, promptStr);
                        return;
                    }
                    String pemda = kode_pemda[selectedItem][0];

                    result = new ActionResult(TransResult.SUCC, new String[] {nop, tahun, pemda});

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

        nop = et_nop.getText().toString();
        if (TextUtils.isEmpty(nop)) {
            result = "NOP Tidak Boleh Kosong";
            return result;
        }
        if (nop.length()<18) {
            result = "Masukkan 18 Digit NOP";
            return result;
        }

        tahun = et_tahun.getText().toString();
        if (TextUtils.isEmpty(tahun)) {
            result = "Tahun Tidak Boleh Kosong";
            return result;
        }

        String year = Device.getDate().substring(0,4);
        if (Integer.parseInt(tahun) <= 1900 || Integer.parseInt(tahun) > Integer.parseInt(year)){
            result = "Silahkan Masukkan Tahun Kembali";
            return result;
        }

        if (selectedItem == -1) {
            result = "Silahkan Pilih Kode Pemda";
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

    public static String kode_pemda[][] = {
            {"0002" , "PEMKOT CIREBON"},
            {"0003" , "PEMKAB KARAWANG"},
            {"0004" , "PEMKAB CIAMIS"},
            {"0006" , "PEMKOT SUKABUMI"},
            {"0007" , "PEMKOT SERANG"},
            {"0008" , "PEMKAB SUBANG"},
            {"0009" , "PEMKAB INDRAMAYU"},
            {"0010" , "PEMKOT BEKASI"},
            {"0011" , "PEMKAB SUMEDANG"},
            {"0012" , "PEMKOT TANGERANG"},
            {"0013" , "PEMKOT BOGOR"},

            //added at v.2.0.7
            {"0014" , "PEMKOT CIANJUR"},

            {"0015" , "PEMKAB KUNINGAN"},
            {"0016" , "PEMKAB MAJALENGKA"},
            {"0017" , "PEMKAB GARUT"},
            {"0018" , "PEMKAB PURWAKARTA"},
            {"0019" , "PEMKAB LEBAK"},
            {"0021" , "PEMKAB PANDEGLANG"},
            {"0022" , "PEMKAB BANDUNG"},
            {"0023" , "PEMKOT CIMAHI"},
            {"0025" , "PEMKOT DEPOK"},
            {"0026" , "PEMKAB CIKARANG"},
            {"0027" , "PEMKOT BANDUNG"},
            {"0028" , "PEMKOT CILEGON"},
            {"0031" , "PEMKAB CIREBON"},
            {"0036" , "PEMKOT BANJAR"},
            {"0048" , "PEMKAB BOGOR"},
            {"0052" , "PEMKAB SUKABUMI"},
            {"0075" , "PEMKAB BANDUNG BARAT"},
            {"0084" , "PEMKAB PANGANDARAN"},
            {"0098" , "PEMKAB TASIKMALAYA"},
            {"0182" , "PEMKOT TANGERANG SELATAN"},
            {"1005" , "PEMKOT TASIKMALAYA"},
            {"1007" , "PEMKAB SERANG"},
            {"1012" , "PEMKAB TANGERANG"},
            //added at v.2.0.7
            {"0241" , "PEMKOT BATAM"},
            {"0281" , "PEMKOT PEKANBARU"},
            {"0391" , "PEMKOT PALEMBANG"},
            {"0372" , "PEMKOT SURAKARTA (SOLO)"}



    };


}


