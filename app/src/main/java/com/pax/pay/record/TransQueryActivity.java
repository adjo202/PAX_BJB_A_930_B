package com.pax.pay.record;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.PagerSlidingTabStrip;
import com.pax.view.dialog.DialogUtils;
import com.pax.view.dialog.MenuPopupWindow;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

public class TransQueryActivity extends BaseActivity implements OnClickListener, OnItemOnClickListener {

    private PagerSlidingTabStrip tabs;

    private ViewPager pager;
    private ImageView backBtn;
    private TextView headerText;

    private MenuPopupWindow popupWindow;

    private ImageView searchBtn;
    private ImageView printBtn;

    private String[] titles;

    private TransDetailFragment detailFragment;
    private TransTotalFragment totalFragment;
    private String navTitle;
    private boolean supportDoTrans;
    private static final String TAG = "TransQueryActivity";

    private static final int PRINT_LAST_TRANSACTION     = 0;
    private static final int PRINT_DETAIL_TRANSACTION   = 1;
    private static final int PRINT_SUMMARY_TRANSACTION  = 2;
    private static final int PRINT_LAST_SETTLE          = 3;





    @Override
    protected void loadParam() {
        titles = new String[] { "Riwayat Trx", getString(R.string.trans_total) };
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
    }

    private void initMenuPopupWindow() {
        // 实例化标题栏弹窗
        popupWindow = new MenuPopupWindow(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // 给标题栏弹窗添加子类
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_trans_last), R.drawable.i1));
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_trans_detail), R.drawable.i2));
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_trans_total), R.drawable.i3));
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_last_total), R.drawable.i4));
    }

    public class MyAdapter extends FragmentPagerAdapter {
        String[] _titles;

        public MyAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            _titles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return _titles[position];
        }

        @Override
        public int getCount() {
            return _titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (detailFragment == null) {
                        detailFragment = new TransDetailFragment();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), supportDoTrans);
                        detailFragment.setArguments(bundle);
                    }
                    //Log.i("Test","-----"+position+"-------");
                    return detailFragment;
                case 1:
                    if (totalFragment == null) {
                        totalFragment = new TransTotalFragment();
                    }
                    //Log.i("Test","-----"+position+"-------");
                    return totalFragment;
                default:
                    return null;
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.header_back:
                setResult(100);
                finish();
                break;
            case R.id.print_btn:
                popupWindow.show(v);

                break;
            case R.id.search_btn:
                queryTransRecordByTransNo();
                break;
            default:
                break;
        }

    }

    /**
     * 根据流水号查询交易记录
     */
    private void queryTransRecordByTransNo() {
        Log.d(TAG,"Sandy.TransQueryActivity.queryTransRecordByTransNo "  );
        ActionInputTransData inputTransDataAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(TransQueryActivity.this, handler,
                        getString(R.string.trans_query)).setInfoTypeSale(getString(R.string.prompt_input_transno),
                        EInputType.NUM, 6, 1, false);

                TransContext.getInstance().setCurrentAction(action);
            }

        }, 1);


        inputTransDataAction.setEndListener(new AAction.ActionEndListener() {


            @Override
            public void onEnd(AAction action, ActionResult result) {
                Log.d(TAG,"Sandy.TransQueryActivity.onEnd " + result.getRet());

                if (result.getRet() != TransResult.SUCC) {
                    ActivityStack.getInstance().pop();
                    return;
                }

                String content = (String) result.getData();
                long transNo = Long.parseLong(content);
                final TransData transData = TransData.readTrans(transNo);


                if (transData == null) {
                    DialogUtils.showErrMessage(TransContext.getInstance().getCurrentContext(), handler,
                            getString(R.string.trans_query), getString(R.string.orig_trans_no_exist),
                            new OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    ActivityStack.getInstance().pop();
                                }
                            }, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }

               // final LinkedHashMap<String, String> map = prepareValuesForDisp(transData);

                ActionDispTransDetail dispTransDetailAction = new ActionDispTransDetail(
                        new AAction.ActionStartListener() {

                            @Override
                            public void onStart(AAction action) {

                                Log.d(TAG,"Sandy.TransQueryActivity.dispTransDetailAction.onStart " + TransContext.getInstance().getCurrentAction());
                            ((ActionDispTransDetail) action).setParam(TransQueryActivity.this, handler,
                                        getString(R.string.trans_detail), transData, supportDoTrans, 1);

                                TransContext.getInstance().setCurrentAction(action);
                            }
                        });





                dispTransDetailAction.setEndListener(new AAction.ActionEndListener() {

                    @Override
                    public void onEnd(AAction action, ActionResult result) {
                        // 此处必须pop两次,目的是退出输入交易流水号的界面

                        Log.d(TAG,"Sandy.TransQueryActivity.dispTransDetailAction.onEnd " + result.getRet());
                        ActivityStack.getInstance().pop();
                        ActivityStack.getInstance().pop();
                    }
                });



                dispTransDetailAction.execute();
            }
        });

        inputTransDataAction.execute();

    }

    @SuppressLint("SimpleDateFormat")
    private LinkedHashMap<String, String> prepareValuesForDisp(TransData transData) {
        AppLog.e("TransQueryActivity", "transType value==============>"+transData.getTransType());

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        ETransType transType = ETransType.valueOf(transData.getTransType());
        String formater = FinancialApplication.getConvert().amountMinUnitToMajor(
                String.valueOf(Long.parseLong(transData.getAmount())),
                currency.getCurrencyExponent(), true);
        String amount = getString(R.string.trans_amount_default, currency.getName(), formater);

        // 日期时间
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        String temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        map.put(getString(R.string.trans_type), transType.getTransName()); //交易类型
        if (transType == ETransType.QR_SALE //扫码消费
                || transType == ETransType.QR_VOID //扫码撤销
                || transType == ETransType.QR_REFUND) { //扫码退货

            if (transData.getC2b() != null && transData.getC2b().length() > 0) { //55域TagA3 扫码付C2B信息码
                map.put(getString(R.string.scan_pay_codes), PanUtils.maskedCardNo(transType, transData.getC2b())); //支付码号
            } else {
                map.put(getString(R.string.scan_pay_codes), "");
            }

            if (transType == ETransType.QR_VOID //扫码撤销
                    || transType == ETransType.QR_REFUND) {  //扫码退货
                map.put(getString(R.string.orig_pay_voucher_num), transData.getOrigC2bVoucher()); //原付款凭证号
            } else {
                map.put(getString(R.string.pay_voucher_num), transData.getC2bVoucher()); //付款凭证号
            }
        } else {
            map.put(getString(R.string.trans_card_no), PanUtils.maskedCardNo(transType, transData.getPan())); //交易卡号
            map.put(getString(R.string.detail_auth_code), transData.getAuthCode()); //授权码
        }
        map.put(getString(R.string.trans_amount), amount); //交易金额
        map.put(getString(R.string.detail_trans_ref_no), transData.getRefNo()); //参考号
        map.put(getString(R.string.detail_trans_no), String.format("%06d", transData.getTransNo())); //流水号
        map.put(getString(R.string.trans_date), temp); //交易时间
        return map;
    }

    @Override
    public void onItemClick(ActionItem item, int position) {

        switch (position) {
            case PRINT_LAST_TRANSACTION:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printLastTrans(TransQueryActivity.this, handler);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this, handler,
                                    getString(R.string.transtype_print), getString(R.string.trans_record_no_exist),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }).start();
                break;
            case PRINT_DETAIL_TRANSACTION:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printTransDetail(getString(R.string.trans_detail_list),
                                TransQueryActivity.this, handler);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this, handler,
                                    getString(R.string.transtype_print),
                                    TransResult.getMessage(TransQueryActivity.this, result), null,
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }).start();
                break;
            case PRINT_SUMMARY_TRANSACTION:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printTransTotal(TransQueryActivity.this, handler, false);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this, handler,
                                    getString(R.string.transtype_print), getString(R.string.trans_record_no_exist),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }).start();
                break;
            case PRINT_LAST_SETTLE:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int result = Printer.printLastSettlement(TransQueryActivity.this, handler);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this, handler,
                                    getString(R.string.transtype_print), getString(R.string.trans_record_no_exist),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }).start();
                break;
            default:
                break;
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trans_query_layout;
    }

    @Override
    protected void initViews() {
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        backBtn = (ImageView) findViewById(R.id.header_back);

        searchBtn = (ImageView) findViewById(R.id.search_btn);
        //searchBtn.setVisibility(View.VISIBLE);
        //sandy : because its error, temporary disabled
        searchBtn.setVisibility(View.GONE);

        printBtn = (ImageView) findViewById(R.id.print_btn);
        if (supportDoTrans) {
            printBtn.setVisibility(View.VISIBLE);
        }

        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager(), titles));
        tabs.setViewPager(pager);

        if (supportDoTrans) {
            initMenuPopupWindow();
        }
    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
        if (supportDoTrans) {
            popupWindow.setItemOnClickListener(this);
        }

    }

    @Override
    protected void handleMsg(Message msg) {

    }

}
