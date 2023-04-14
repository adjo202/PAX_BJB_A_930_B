package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.helper.MoneyTextWatcher;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.BPJSTkData;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


@SuppressLint("SimpleDateFormat")
public class InputBPJSTkVerificationActivity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "InputBPJSTkVerificationActivity";

    private Button confirmBtn;
    private EditText et_nik, et_nama_lengkap,et_tgl_lahir,et_hp;
    private String navTitle, nik,customerName,hp,tglLahir;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_bpjs_tk_verification;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);

        confirmBtn      = (Button) findViewById(R.id.lanjutkanBtn);

        et_nik          = (EditText) findViewById(R.id.et_nik);
        et_nik.requestFocus();

        et_nama_lengkap = (EditText) findViewById(R.id.et_nama_lengkap);
        et_hp           = (EditText) findViewById(R.id.et_hp);

        et_tgl_lahir = (EditText) findViewById(R.id.et_tgl_lahir);
        et_tgl_lahir.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }






    @Override
    protected void setListeners() {
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

                    //Sandy : disable temporary for testing purpose!
                    if (!TextUtils.isEmpty(promptStr)) {
                        ToastUtils.showMessage(InputBPJSTkVerificationActivity.this, promptStr);
                        return;
                    }


                    BPJSTkData bpjsData = new BPJSTkData(nik,customerName,tglLahir,hp);

                    result = new ActionResult(TransResult.SUCC, bpjsData);
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

        nik = et_nik.getText().toString();
        if (TextUtils.isEmpty(nik)) {
            result = "NIK Tidak Boleh Kosong";
            return result;
        }
        if (nik.length() != 16) {
            result = "Masukkan 16 Digit NIK";
            return result;
        }

        customerName = et_nama_lengkap.getText().toString();
        if (TextUtils.isEmpty(customerName)) {
            result = "Nama Tidak Boleh Kosong";
            return result;
        }


        hp = et_hp.getText().toString();
        if (TextUtils.isEmpty(hp)) {
            result = "Nomor HP Tidak Boleh Kosong";
            return result;
        }




        tglLahir = et_tgl_lahir.getText().toString();
       if (tglLahir.length() != 8) {
            result = "Format Tanggal Lahir Salah";
            et_tgl_lahir.setText("");
            return result;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(tglLahir);
        } catch (Exception e) {
            Log.e("teg", "", e);
            result = "Format Tanggal Lahir Salah";
            return result;
        }

        String tgl = tglLahir.substring(0,2);
        String bln = tglLahir.substring(2,4);
        String year = tglLahir.substring(4,8);
        String completeDate = String.format("%s-%s-%s",tgl,bln,year);
        tglLahir = completeDate;


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(new ActionResult(TransResult.ERR_ABORTED, null));

    }


}








