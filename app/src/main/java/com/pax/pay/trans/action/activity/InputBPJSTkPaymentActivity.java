package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.BPJSTkData;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import java.util.ArrayList;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


@SuppressLint("SimpleDateFormat")
public class InputBPJSTkPaymentActivity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "InputBPJSTkPaymentActivity";

    private Button confirmBtn;
    private EditText et_nik;
    private String  navTitle,nik;
    private int selectedMonthProgram = -1;
    private Button month_program;
    SpinnerDialog spinnerDialog;
    private LinearLayout combo_month_container;
    private boolean isDisplayCombobox;

    private int INDEX_FOR_NAME = 1;
    private int INDEX_FOR_CODE = 0;



    String month_programs[][] = {
            {"1" , "1 Bulan"},
            {"2" , "2 Bulan"},
            {"3" , "3 Bulan"},
            {"6" , "6 Bulan"},
            {"12" , "12 Bulan"}
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_bpjs_tk_payment;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        isDisplayCombobox = getIntent().getBooleanExtra(EUIParamKeys.INPUT_TYPE_1.toString(), false);
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        String pNIK = FinancialApplication.getSysParam().get(SysParam.BPJS_INQUIRY_REQUEST_DATA);

        confirmBtn              = (Button) findViewById(R.id.lanjutkanBtn);
        et_nik                  = (EditText) findViewById(R.id.et_nik);
        if(pNIK != null){
            et_nik.setText(pNIK);
            et_nik.setEnabled(false);
        }else
            et_nik.setEnabled(true);


        combo_month_container   = (LinearLayout) findViewById(R.id.combo_month_container);
        month_program           = (Button) findViewById(R.id.month_program);
        month_program.setBackgroundColor(Color.WHITE);


        if(isDisplayCombobox)
            combo_month_container.setVisibility(View.VISIBLE);
        else
            combo_month_container.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        spinnerDialog = new SpinnerDialog(InputBPJSTkPaymentActivity.this, list_month_programs(),
                "Bulan Program");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                month_program.setText(item);
                selectedMonthProgram = position;
            }
        });

    }



    private ArrayList<String> list_month_programs() {

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < month_programs.length; i++) {
            list.add(month_programs[i][1]);
        }
        return list;
    }

    @Override
    protected void setListeners() {
        et_nik.addTextChangedListener(watcher);
        confirmBtn.setOnClickListener(this);
        //bulan program
        month_program.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                spinnerDialog.showSpinerDialog();
            }
        });


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
                        ToastUtils.showMessage(InputBPJSTkPaymentActivity.this, promptStr);
                        return;
                    }

                    if(isDisplayCombobox == false){
                        selectedMonthProgram = 0;
                        //store Counter Form for further screen
                        FinancialApplication.getSysParam().set(SysParam.BPJS_INQUIRY_COUNTER_FORM,"1");
                    }else
                        FinancialApplication.getSysParam().set(SysParam.BPJS_INQUIRY_COUNTER_FORM,"2");


                    String sMonthProgram            =  month_programs[selectedMonthProgram][INDEX_FOR_NAME];
                    String sMonthProgramCode        =  month_programs[selectedMonthProgram][INDEX_FOR_CODE];


                    BPJSTkData bpjsData             =  new BPJSTkData(nik,sMonthProgram,sMonthProgramCode);
                    finish(new ActionResult(TransResult.SUCC, bpjsData));

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
        if (nik.length()!=16) {
            result = "Masukkan 16 Digit NIK";
            return result;
        }

        //Sandy : this rule is applicable only for displaying combobox
        if(isDisplayCombobox && selectedMonthProgram == -1){
                result = "Silahkan Pilih Bulan Program";
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









}








