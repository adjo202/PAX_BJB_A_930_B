package com.pax.pay.pulse;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.PulsaDataTrans;
import com.pax.pay.trans.PulsaDataTransNew;
import com.pax.pay.trans.action.ActionPulseSale;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.ViewUtils;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class PulseDetailsActivity extends BaseActivity implements OnClickListener, OnItemOnClickListener {
    private static final String TAG = "PulseDetailsActivity";
    private TextView headerText;
    private ImageView backBtn;
    private LinearLayout mLayout;

    private ImageView printBtn;
    private Button confirmBtn;

    private ProductData productData;

    private String phone_number;
    private String mode;
    private String kodeProduk;

    //private MenuPopupWindow popupWindow;
    private String navTitle;
    private String total;
    private String price;
    private String adminFee;


    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<Object> values = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pulse_detail_layout;
    }

    public ATransaction.TransEndListener listener = result -> {
        handler.post(new Runnable() {

            @Override
            public void run() {
                /*hasDoTrans = false;// 重置交易标志位
                resetUI();*/
            }
        });
    };

    @Override
    protected void loadParam() {
        Bundle bundle   = getIntent().getExtras();
        navTitle        = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        productData     = (ProductData) bundle.getSerializable(EUIParamKeys.CONTENT.toString());
        phone_number    = getIntent().getStringExtra("phone_number");
        mode            = getIntent().getStringExtra("mode");
        kodeProduk      = getIntent().getStringExtra("kodeproduk");
        initialList();
    }

    @Override
    protected void initViews() {
        mLayout = (LinearLayout) findViewById(R.id.detail_layout);
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        backBtn = (ImageView) findViewById(R.id.header_back);
        printBtn = (ImageView) findViewById(R.id.print_btn);
        confirmBtn = (Button) findViewById(R.id.ok_btn);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;
        RelativeLayout layer = new RelativeLayout(PulseDetailsActivity.this);
        layer.setLayoutParams(params);

        for (int i = 0; i < titles.size(); i++) {
            layer = ViewUtils.genSingleLineLayout(PulseDetailsActivity.this, titles.get(i), values.get(i));
            mLayout.addView(layer, params);
        }

        /*if (mode.equalsIgnoreCase("pln") || mode.equalsIgnoreCase("others")){
            new PulsaDataTransNew(PulseDetailsActivity.this, handler,
                    productData.getBasePrice(),
                    productData.getSellPrice(),
                    productData.getFee(),
                    phone_number,
                    productData.getProductId()+"-"+kodeProduk, //kode produk
                    productData.getProductName(),
                    productData.getProductDescription(),
                    productData.getOperator(),
                    mode,
                    true,
                    listener).execute();
        }else {
            new PulsaDataTransNew(PulseDetailsActivity.this, handler,
                    productData.getBasePrice(),
                    productData.getSellPrice(),
                    productData.getFee(),
                    phone_number,
                    productData.getProductId(),
                    productData.getProductName(),
                    productData.getProductDescription(),
                    productData.getOperator(),
                    mode,
                    true,
                    listener).execute();
        }*/


        new PulsaDataTransNew(PulseDetailsActivity.this, handler,
                productData.getBasePrice(),
                productData.getSellPrice(),
                productData.getFee(),
                phone_number,
                productData.getProductId(),
                productData.getProductName(),
                productData.getProductDescription(),
                productData.getOperator(),
                mode,
                true,
                listener).execute();



        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                process();
                break;
            case R.id.header_back:
                finish();
                break;
            case R.id.print_btn:
                //popupWindow.show(v);
                break;
            default:
                break;
        }
    }


    private void process() {
        String message = "Are you sure want to process?";
        showConfirmationDialog(message);
    }


    private void showConfirmationDialog(String msg) {
        CustomAlertDialog dialog = new CustomAlertDialog(PulseDetailsActivity.this, CustomAlertDialog.NORMAL_TYPE);
        dialog.setContentText(msg);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
        dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                alertDialog.dismiss();
            }
        });

        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                //finish();

                //requestOnline(ETransType.TOTAL_PRODUCT_PULSA_DATA.toString(),dialog).execute();
                new PulsaDataTransNew(PulseDetailsActivity.this, handler, productData.getBasePrice(), productData.getSellPrice(), productData.getFee(), phone_number, productData.getProductId(), productData.getProductName(), productData.getProductDescription(),
                        productData.getOperator(), mode, true, listener).execute();
                dialog.dismiss();
                // panggil trans
            }
        });
    }


    private AAction requestOnline(final String transTpye, CustomAlertDialog dialog) {

        ActionPulseSale onlineProcess = new ActionPulseSale(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionPulseSale) action).setParam(PulseDetailsActivity.this, transTpye);
                dialog.dismiss();

            }
        });

        onlineProcess.setEndListener(new AAction.ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                finish();
            }
        });

        return onlineProcess;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(ActionItem item, int position) {

    }

    @SuppressLint("SimpleDateFormat")
    private void initialList() {

        /***********************************************************************************************/
        NumberFormat formatter = new DecimalFormat("#,###");
        price = String.format("IDR %s", formatter.format(Double.parseDouble(productData.getSellPrice())));

        adminFee = "0"; //we should calculate here.....

        total = String.valueOf(formatter.format(Double.parseDouble(adminFee) + Double.parseDouble(productData.getSellPrice())));


        titles.clear();
        titles.add(getString(R.string.trans_operator).toUpperCase());
        titles.add(getString(R.string.trans_phone_number).toUpperCase());
        if (mode.equals("pulse"))
            titles.add(getString(R.string.trans_product_name).toUpperCase());
        else
            titles.add("");

        if (productData.getProductDescription() != null) //product description
            titles.add("");
        titles.add(getString(R.string.trans_pulse_sell_price).toUpperCase());
        titles.add(getString(R.string.trans_pulse_admin_fee).toUpperCase());
        titles.add(getString(R.string.total));


        values.clear();
        values.add(productData.getOperator());
        values.add(phone_number);
        values.add(productData.getProductName());
        if (productData.getProductDescription() != null)
            values.add(productData.getProductDescription());
        values.add(price);
        values.add(adminFee);
        values.add(total);


    }
}
