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
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.pax.pay.utils.Controllers.PASCABAYAR;

public class InputPascabayarDataActivity extends BaseActivityWithTickForAction implements OnClickListener, OnItemClickListener {

    private TextView tvTitle, tvJudul, tvJudul2;
    private ImageView ivBack;

    private Button confirmBtn, btnCari;
    private EditText etKodeBayar;

    private String navTitle, kodeBayar;
    private boolean navBack;
    SpinnerDialog spinnerDialog;
    private int selectedItem = -1;
    private ProductData productData;
    private List<ProductData> listProduct;


    @Override
    protected int getLayoutId() {
        return R.layout.form_input_layout;
    }

    @Override
    protected void initViews() {
        ivBack = (ImageView) findViewById(R.id.header_back);
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        tvJudul = (TextView) findViewById(R.id.tv_judul);
        tvJudul2 = (TextView) findViewById(R.id.tv_judul2);
        tvJudul.setText("Input No. Telepon / Id Billing");
        tvJudul2.setText("Pembayaran");
        confirmBtn = (Button) findViewById(R.id.infos_confirm);
        confirmBtn.setEnabled(false);
        confirmBtn.setBackgroundResource(R.drawable.gray_button_background);
        confirmBtn.setOnClickListener(this);

        etKodeBayar = (EditText) findViewById(R.id.et_kode_bayar);
        btnCari = (Button) findViewById(R.id.btn_cari);
        btnCari.setBackgroundColor(Color.WHITE);

        listProduct = getAllPascabayarProductData();
        Log.d("teg", "size listProduct : " + listProduct.size());
        ArrayList<String> list = getPascabayarProductDataNameList();
        Collections.sort(list);

        spinnerDialog = new SpinnerDialog(InputPascabayarDataActivity.this, list,
                "Pilih Pembayaran");
        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                btnCari.setText(item);
                selectedItem = position;
                productData = getPascabayarProductDataByName(item);
                Log.d("teg", "onClick --> : " + productData.toString());
            }
        });
        btnCari.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });

    }

    //ambil ProductData.productDesc
    private ArrayList<String> getPascabayarProductDataNameList() {
        ArrayList<String> list = new ArrayList<String>();
        for (ProductData p : listProduct) {
            //list.add(p.getProductDescription());
            list.add(p.getProductName());
        }

        return list;
    }

    ProductData getPascabayarProductDataByDesc(String productDesc) {
        ProductData res = new ProductData();

        for (ProductData p : listProduct) {
            if (p.getProductDescription().equalsIgnoreCase(productDesc)) {
                res = p;
            }
        }
        return res;
    }

    ProductData getPascabayarProductDataByName(String productName) {
        ProductData res = new ProductData();

        for (ProductData p : listProduct) {
            if (p.getProductName().equalsIgnoreCase(productName)) {
                res = p;
            }
        }
        return res;
    }

    List<ProductData> getAllPascabayarProductData() {
        List<ProductData> list = new ArrayList<>();
        JSONObject dat;
        ProductData productData;
        String data = FinancialApplication.getSysParam().get("download");

        try {
            dat = new JSONObject(data);
            Log.d("teg", "panjang data : " + dat.length());
            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type");
                type = type.trim();
                if (type.equalsIgnoreCase(PASCABAYAR)) {

                    String productId = par.getString("productId");
                    String productName = par.getString("productName").trim();
                    String productDesc = par.getString("productDesc").trim();
                    String operator = par.getString("operator");
                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);
                    operator = operator.toUpperCase().trim();

                    productData = new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
//                    Log.i("teg", "-->" + productData.toString());
                    list.add(productData);
                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return list;
    }


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
                    ToastUtils.showMessage(InputPascabayarDataActivity.this, promptStr);
                    return;
                }
                //result = new ActionResult(TransResult.SUCC, productData);
                result = new ActionResult(TransResult.SUCC, new String[]{
                        kodeBayar,
                        productData.getProductId(),
                        productData.getProductDescription(),
                        productData.getFee(),
                        productData.getBasePrice(),
                        productData.getProductDescription(),
                        productData.getProductName(),
                        productData.getOperator()
                });

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

        kodeBayar = kodeBayar.trim();

        if (selectedItem == -1) {
            result = "Silahkan Pilih Pembayaran";
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
