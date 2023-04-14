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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.AccountData;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pax.pay.trans.FundTransferTrans.arr;


public class InputDataOverbookingActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle;
    private ImageView ivBack;

    private Button confirmBtn;
    private Spinner spinner;
    private EditText etNoRekening, etNominal, etReffNo;

    private String navTitle, noRekening, nominal, reffNo, bankName;
    private boolean navBack;
    private boolean mEditing;
    private String strPre = "";

    Currency currency;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_overbooking_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        confirmBtn = (Button) findViewById(R.id.infos_confirm);
        confirmBtn.setOnClickListener(this);

        spinner = (Spinner) findViewById(R.id.spinner1);
        etNoRekening = (EditText) findViewById(R.id.et_nomor_rekening);
        etNominal = (EditText) findViewById(R.id.et_nominal);
        //etNominal.setHint(FinancialApplication.getSysParam().getCurrency().getFormat());
        etReffNo = (EditText) findViewById(R.id.et_reffnum);
        currency = FinancialApplication.getSysParam().getCurrency();

        List<String> list = listSpinner();
        Collections.sort(list);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_style, list);
//                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


    }

    private List<String> listSpinner() {
        List<String> list = new ArrayList<String>();
        list.add("123456789");
        list.add("987654321");

        /*for (int i = 0; i < arr.length; i++) {
            list.add(arr[i][0]);
        }*/
        return list;
    }

    private String getDisplayAmount(String digits) {
        final String PATTERN = "###,###";
        String displayAmount = "";
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        Log.d("teg", "curr : "+currency.toString());

        /*if (digits.length() > 9) {
            ToastUtils.showMessage(InputDataTransferctivity.this, "");
            return strPre;
        }*/

        if (digits == null || digits.length() == 0) {
            return currency.getFormat();
        }

        try {

            DecimalFormat decimalFormat = new DecimalFormat(PATTERN);
            displayAmount = decimalFormat.format(Long.valueOf(digits));

        } catch (Exception e) {
            Log.e("teg", "Exception", e);
            displayAmount = currency.getFormat();
        }

        return displayAmount;
    }


    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }

        etNoRekening.addTextChangedListener(watcher);
        etReffNo.addTextChangedListener(watcher);
        /*etNominal.addTextChangedListener(new EnterAmountTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                changeConfirmButtonStatus(s);
            }

        });*/

        etNominal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("teg", "s : "+s.toString());
                try {
                    if (null != s.toString()) {
                        if (!mEditing) {
                            mEditing = true;
                            /*String replaceable = String.format("[%s,.\\s]", currency.getName());
                            String cleanString = s.toString().replaceAll(replaceable, "");*/
                            String digits = s.toString().replace(".", "").trim().replaceAll("[^(0-9)]", "");
                            Log.d("teg", "digits "+digits);
                            String displayAmount = "";
                            displayAmount = getDisplayAmount(digits);
                            Log.d("teg", "displayAmount "+displayAmount);
                            try {
                                s.replace(0, s.length(), displayAmount);

                                etNominal.setText(displayAmount);
                                etNominal.setSelection(displayAmount.length());
                            } catch (NumberFormatException nfe) {
                                s.clear();
                            }
                            mEditing = false;
                        }

                        /*etNominal.removeTextChangedListener(this);

                        String replaceable = String.format("[%s,.\\s]", currency.getName());
                        String cleanString = s.toString().replaceAll(replaceable, "");

                        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                                .amountMinUnitToMajor(cleanString, currency.getCurrencyExponent(), true);

                        etNominal.setText(amount);
                        etNominal.setSelection(amount.length());
                        etNominal.addTextChangedListener(this);
                        changeConfirmButtonStatus(s);*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

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
                try {
                    String promptStr = proses();
                    if (!TextUtils.isEmpty(promptStr)) {
                        ToastUtils.showMessage(InputDataOverbookingActivity.this, promptStr);
                        return;
                    }

                    Log.d("teg", "spinner : " + spinner.getSelectedItem().toString());
                    finish(new ActionResult(TransResult.SUCC,  new String[]{ noRekening, nominal, reffNo}) );
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
        if(spinner == null || spinner.getSelectedItem() ==null ) {
            result = "Bank Tujuan Tidak Boleh Kosong";
            return result;
        }

        noRekening = etNoRekening.getText().toString();
        if (TextUtils.isEmpty(noRekening)) {
            result = "No. Rekening Tidak Boleh Kosong";
            etNoRekening.setText("");
            return result;
        }

        if (noRekening.length()<10){
            result = "Format No. Rekening Salah";
            etNoRekening.setText("");
        }

        nominal = etNominal.getText().toString();
        String replaceable = String.format("[%s,.\\s]", currency.getName());
        nominal = nominal.replaceAll(replaceable, "");
        if (TextUtils.isEmpty(nominal)) {
            result = "Nominal Tidak Boleh Kosong";
            etNominal.setText("0");
            return result;
        }

        /*if (!TextUtils.isEmpty(nominal)) {
            if (Long.parseLong(nominal) < 10000) {
                result = "Amount Error, Please Check!";
                etNominal.setText("0");
                return result;
            }
        }*/

        reffNo = etReffNo.getText().toString();
        /*if (TextUtils.isEmpty(reffNo)) {
            result = "Refference No Can Not Be Empty";
            etReffNo.setText("");
            return result;
        }*/

        /*bankName = spinner.getSelectedItem().toString();
        if (TextUtils.isEmpty(bankName)) {
            result = "Bank Name No Can Not Be Empty";
            return result;
        }*/

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
        /*if (content != null && content.length() > 0) {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.button_click_background);
        } else {
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
        finish(result);
    }

    /*private void setEdtListener(final CustomEditText editText) {
        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String promptStr = proses();
                    if (promptStr != null) {
                        if (v.getTag().equals(promptStr)) {
                            ToastUtils.showMessage(InputDataTransferctivity.this, promptStr);

                        }
                        return true;
                    }

                    ActionResult result = new ActionResult(TransResult.SUCC, new String[]{bankName, noRekening, nominal, reffNo});
                    finish(result);
                }
                return false;
            }
        });
    }*/
}
