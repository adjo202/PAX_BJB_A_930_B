package com.pax.pay.record;

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
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.AuthCMTrans;
import com.pax.pay.trans.AuthCMVoidTrans;
import com.pax.pay.trans.AuthSettlementTrans;
import com.pax.pay.trans.CouponVoidTrans;
import com.pax.pay.trans.DanaSaleVoidTrans;
import com.pax.pay.trans.QrSaleVoidTrans;
import com.pax.pay.trans.SaleVoidTrans;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.utils.Utils;
import com.pax.pay.utils.ViewUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.MenuPopupWindow;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DetailsNewActivity extends BaseActivity implements OnClickListener, OnItemOnClickListener {
    private static final String TAG = "DetailsActivity";
    private TextView headerText;
    private ImageView backBtn;
    private LinearLayout mLayout;

    private ImageView printBtn;
    private Button confirmBtn;

    private TransData transData;

    private MenuPopupWindow popupWindow;

    private String transType;
    private String transState;
    private String navTitle;

    private boolean supportDoTrans;

    private ArrayList<String> titles = new ArrayList<String>();
    private ArrayList<Object> values = new ArrayList<Object>();
    //Added by Steven.T 2017-6-8 16:34:42
    private String options = null;
    public enum Options{
        INTNET,//页面跳转
        ACTION,//action 跳转
    };
    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail_layout;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        transData = (TransData) bundle.getSerializable(EUIParamKeys.CONTENT.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
        //Added by Steven.T 2017-6-15 18:25:28
        options = getIntent().getStringExtra(EUIParamKeys.OPTIONS.toString());
        initialList();
    }

    @Override
    protected void initViews() {
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        backBtn = (ImageView) findViewById(R.id.header_back);
        printBtn = (ImageView) findViewById(R.id.print_btn);
        if (supportDoTrans) {
            printBtn.setVisibility(View.VISIBLE);
        }

        confirmBtn = (Button) findViewById(R.id.ok_btn);

        mLayout = (LinearLayout) findViewById(R.id.detail_layout);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;
        RelativeLayout layer = new RelativeLayout(DetailsNewActivity.this);
        layer.setLayoutParams(params);

        if (Utils.isScreenOrientationPortrait(DetailsNewActivity.this)) {
            for (int i = 0; i < titles.size(); i++) {
                layer = ViewUtils.genSingleLineLayout(DetailsNewActivity.this, titles.get(i), values.get(i));
                mLayout.addView(layer, params);
            }
        } else { //横屏不处理，Modified by Steven 2017-4-13 14:26:53
            /*
            ListView listview = (ListView) findViewById(R.id.query_listview);
            List<HashMap<String, Object>> transList = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < titles.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("transType", titles.get(i));
                map.put("transValue", values.get(i));
                transList.add(map);
            }

            DetailListAdapter transDetailListAdapter = new DetailListAdapter(DetailsActivity.this, transList);
            listview.setAdapter(transDetailListAdapter);
            */
        }

        if (supportDoTrans) {
            initMenuPopupWindow();
        }

    }

    private void initMenuPopupWindow() {
        // 实例化标题栏弹窗
        popupWindow = new MenuPopupWindow(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // 给标题栏弹窗添加子类

        transType = transData.getTransType();
        transState = transData.getTransState();
        if (transType.equals(ETransType.SALE.toString())
                || transType.equals(ETransType.COUPON_SALE.toString())
                || transType.equals(ETransType.QR_SALE.toString())
                || transType.equals(ETransType.AUTHCM.toString())
                || transType.equals(ETransType.DANA_QR_SALE.toString())

        ) {

            if (transState.equals(ETransStatus.NORMAL.toString())) {
                popupWindow.addAction(new ActionItem(this, getString(R.string.trans_void), R.drawable.cx)); //撤销
            }
        } else if (transType.equals(ETransType.AUTH.toString())) {
            popupWindow.addAction(new ActionItem(this, getString(R.string.auth_cm_adv), R.drawable.tz)); //完成通知
            popupWindow.addAction(new ActionItem(this, getString(R.string.auth_cm_req), R.drawable.req)); //完成请求
        }
        popupWindow.addAction(new ActionItem(this, getString(R.string.trans_reprint), R.drawable.cd)); //重新打印
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
        if (supportDoTrans) {
            popupWindow.setItemOnClickListener(this);
        }

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
            case R.id.header_back:
                //finish();
                //Modified by Steven.T 2017-6-15 18:24:29
                if(options !=null && options.equals(Options.ACTION.toString())){
                    AAction action = TransContext.getInstance().getCurrentAction();
                    ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
                    action.setResult(result);

                }else{
                    finish();
                }
                break;
            case R.id.print_btn:
                popupWindow.show(v);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //finish();
            //Modified by Steven.T 2017-6-15 18:24:29
            if(options!=null && options.equals(Options.ACTION.toString())){
                AAction action = TransContext.getInstance().getCurrentAction();
                ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
                action.setResult(result);

            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(ActionItem item, int position) {
        CharSequence title = item.getTitle();
        // 重打印
        if (title.equals(getString(R.string.trans_reprint))) {
            // 添加操作员号
            transData.setOper(TransContext.getInstance().getOperID());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Printer.printTransAgain(DetailsNewActivity.this, handler, transData);
                }
            }).start();
        }
        // 撤销
        if (title.equals(getString(R.string.trans_void))) {

            transData.setOrigDateTimeTrans(transData.getDateTimeTrans());

           // Log.d(TAG,"Void has been executed");
           // Log.d(TAG,transType);

            if (transType.equals(ETransType.SALE.toString())) {
                new SaleVoidTrans(DetailsNewActivity.this, handler, transData, null).execute();
            } else if (transType.equals(ETransType.COUPON_SALE.toString())) {
                new CouponVoidTrans(DetailsNewActivity.this, handler, transData, null).execute();
            }else if (transType.equals(ETransType.AUTHCM.toString())) {
                // 预授权完成请求撤销
                new AuthCMVoidTrans(DetailsNewActivity.this, handler, transData, null).execute();
            } else if (transType.equals(ETransType.QR_SALE.toString())) {
                // 扫码消费撤销
                new QrSaleVoidTrans(DetailsNewActivity.this, handler, transData, null).execute();
            } else if(transType.equals(ETransType.DANA_QR_SALE.toString())){
                new DanaSaleVoidTrans(DetailsNewActivity.this,handler,transData,null).execute();
            }
        }
        // 预授权完成通知
        if (title.equals(getString(R.string.auth_cm_adv))) {
            new AuthSettlementTrans(DetailsNewActivity.this, handler, transData, true, false, null).execute();
        }
        // 预授权完成请求
        if (title.equals(getString(R.string.auth_cm_req))) {
            new AuthCMTrans(DetailsNewActivity.this, handler, transData, true, false, null).execute();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void initialList() {

        /***********************************************************************************************/
        ETransType transType = ETransType.valueOf(transData.getTransType());
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String formater = FinancialApplication.getConvert().amountMinUnitToMajor(
                String.valueOf(Long.parseLong(transData.getAmount())),
                currency.getCurrencyExponent(), true);
        String amount = getString(R.string.trans_amount_default, currency.getName(), formater);

        String temp = transData.getTransState();
        String state = "";
        // 联机交易
        if (transData.getIsOnlineTrans()) {

            if (temp.equals(ETransStatus.NORMAL.toString())) {
                state = getString(R.string.normal);
            } else if (temp.equals(ETransStatus.VOID.toString())) {
                state = getString(R.string.voided);
            } else if (temp.equals(ETransStatus.ADJUST.toString())) {
                state = getString(R.string.adjust);
            }

        } else {
            // 对于脱机交易，显示 已上送、未上送
            if (transData.getIsOffUploadState()) {
                // true:脱机上送成功
                state = getString(R.string.upload);
            } else {
                state = getString(R.string.un_upload);
            }

            if (temp.equals(ETransStatus.ADJUST.toString())) {
                state = getString(R.string.adjust);
            }

        }

        String cardNo = "", cardTitle = getString(R.string.trans_card_no);
        // 卡号
        if (transType == ETransType.AUTH || transType == ETransType.EC_SALE) {
            cardNo = transData.getPan();
        } else if (transType == ETransType.QR_SALE
                || transType == ETransType.QR_VOID
                || transType == ETransType.QR_REFUND) {
            cardTitle = getString(R.string.scan_pay_codes); //支付码号
            if (transData.getC2b() != null && transData.getC2b().length() > 0) { // 55域TagA3 扫码付C2B信息码
                cardNo = PanUtils.maskedCardNo(transType, transData.getC2b());
            }
        } else {
            cardNo = PanUtils.maskedCardNo(transType, transData.getPan());
            if (!transData.getIsOnlineTrans()) {
                cardNo = transData.getPan();
            }
        }

        String authCode = "", authTitle = "";
        if (transType == ETransType.QR_SALE) {
            authTitle = getString(R.string.pay_voucher_num); //付款凭证号
            authCode = transData.getC2bVoucher(); // 55域 应答TagA4 扫码付付款凭证码
        } else if (transType== ETransType.QR_VOID || transType == ETransType.QR_REFUND) {
            authTitle = getString(R.string.orig_pay_voucher_num); //原付款凭证号
            authCode = transData.getOrigC2bVoucher(); // 原付款凭证码
        } else {
            authTitle = getString(R.string.detail_auth_code); //授权码
            authCode = transData.getAuthCode(); // 授权码
        }
        String refNo = transData.getRefNo(); // 系统参考号

        // 日期时间
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        //Sandy : Added Discount and Cost amount also Coupon
        String couponValue = "-", couponTitle = "-",
                discountTitle = "-", discountValue = "-",
                costTitle = "-", costValue = "-";

        if (transType == ETransType.COUPON_SALE || transType == ETransType.COUPON_SALE_VOID) {
            //coupon
            couponValue = transData.getCouponNo();
            couponTitle = getString(R.string.trans_coupon);

            //discount
            discountValue = String.format("%s %s",currency.getName(),
                    FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getDiscountAmount())),
                    currency.getCurrencyExponent(), true));
            discountTitle = getString(R.string.detail_discount);

            //cost
            costValue = String.format("%s %s",currency.getName(),FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getActualPayAmount())),
                    currency.getCurrencyExponent(), true));
            costTitle = getString(R.string.detail_cost);

        }





        values.clear();
        values.add(transType.getTransName());
        values.add(amount);

        //sandy
        if (transType == ETransType.COUPON_SALE || transType == ETransType.COUPON_SALE_VOID ) {
            values.add(costValue);
            values.add(discountValue);
            values.add(couponValue);
        }

        values.add(state);
        values.add(cardNo);
        values.add(authCode != null ? authCode : "");
        values.add(refNo != null ? refNo : "");
        values.add(String.format("%06d", transData.getTransNo()));
        values.add(temp); // 交易时间

        titles.clear();
        titles.add(getString(R.string.trans_type));
        titles.add(getString(R.string.trans_amount));

        //sandy
       if (transType == ETransType.COUPON_SALE || transType == ETransType.COUPON_SALE_VOID) {
            titles.add(costTitle);
            titles.add(discountTitle);
            titles.add(couponTitle);
       }

        titles.add(getString(R.string.trans_state));
        titles.add(cardTitle);
        titles.add(authTitle);
        titles.add(getString(R.string.detail_trans_ref_no));
        titles.add(getString(R.string.detail_trans_no));
        titles.add(getString(R.string.trans_date));
    }
}
