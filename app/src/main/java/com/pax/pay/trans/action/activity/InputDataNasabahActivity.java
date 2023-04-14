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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

import java.text.SimpleDateFormat;

public class InputDataNasabahActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private Button confirmBtn;
    private EditText etNama, etNik, etTempatLahir, etTglLahir, etNohp, etAmount;

    private String navTitle, nama, noHp, nik, tempatLahir, tglLahir;
    private boolean navBack;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_nasabah_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        confirmBtn = (Button) findViewById(R.id.infos_confirm);
        confirmBtn.setOnClickListener(this);

        etNama = (EditText) findViewById(R.id.et_nama);
        etNohp = (EditText) findViewById(R.id.et_nomor_hp);
        etNik = (EditText) findViewById(R.id.et_nik);
        etTempatLahir = (EditText) findViewById(R.id.et_tempat_lahir);
        etTglLahir = (EditText) findViewById(R.id.et_tgl_lahir);

    }


    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        etNama.addTextChangedListener(watcher);
        etNik.addTextChangedListener(watcher);
        etTempatLahir.addTextChangedListener(watcher);
        etTglLahir.addTextChangedListener(watcher);
        etNohp.addTextChangedListener(watcher);


    }

    private void setEditText_alphnum(EditText editText) {
        editText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                char[] numberChars = "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890".toCharArray();
                return numberChars;
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
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
                    ToastUtils.showMessage(InputDataNasabahActivity.this, promptStr);
                    return;
                }
                Log.d("teg", "on confirm + "+nama+tempatLahir+tglLahir+noHp+nik);
                result = new ActionResult(TransResult.SUCC, new String[]{nama.toUpperCase(), tempatLahir.toUpperCase(), tglLahir, noHp, nik});

                finish(result);
                break;
            default:
                break;
        }

    }

    private String proses() {
        String result = "";
        nama = etNama.getText().toString();
        if (TextUtils.isEmpty(nama)) {
            result = "Nama Tidak Boleh Kosong";
            etNama.setText("");
            return result;
        }

        nik = etNik.getText().toString();
        if (nik.length() != 16) {
            result = "Format No. Identitas Salah";
            etNik.setText("");
            return result;
        }

        tempatLahir = etTempatLahir.getText().toString();
        if (TextUtils.isEmpty(tempatLahir)) {
            result = "Tempat Lahir Tidak Boleh Kosong";
            etTempatLahir.setText("");
            return result;
        }

        tglLahir = etTglLahir.getText().toString();
        if (tglLahir.length() != 8) {
            result = "Format Tanggal Lahir Salah";
            etTglLahir.setText("");
            return result;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(tglLahir);
        } catch (Exception e) {
            Log.e("teg", "", e);
            result = "Format Tanggal Lahir Salah";
            etTglLahir.setText("");
            return result;
        }

        noHp = etNohp.getText().toString();
        if (TextUtils.isEmpty(noHp)) {
            result = "No. Handphone Tidak Boleh Kosong";
            etNohp.setText("");
            return result;
        }

        if (noHp.length()<10){
            result = "Format No. Handphone Salah";
            etNohp.setText("");
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
        if (content != null && content.length() > 0) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        }
    }

    private void setEdtListener(final CustomEditText editText) {
        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String promptStr = proses();
                    if (promptStr != null) {
                        if (v.getTag().equals(promptStr)) {
                            ToastUtils.showMessage(InputDataNasabahActivity.this, promptStr);

                        }
                        return true;
                    }

                    ActionResult result = new ActionResult(TransResult.SUCC, new String[]{nama, nik,
                            tempatLahir, tglLahir, noHp});
                    finish(result);
                }
                return false;
            }
        });
    }


}
