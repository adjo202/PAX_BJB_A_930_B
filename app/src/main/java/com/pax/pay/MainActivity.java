package com.pax.pay;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.menu.QrisMenuActivity;
import com.pax.pay.menu.FunctionMenuActivity;
import com.pax.pay.menu.ManageMenuActivity;
import com.pax.pay.menu.MiniBankingActivity;
import com.pax.pay.menu.PaymentMenuActivity;
import com.pax.pay.menu.QrisMenuActivity;
import com.pax.pay.record.TransQueryActivity;
import com.pax.pay.trans.AuthTrans;
import com.pax.pay.trans.PosLogon;
import com.pax.pay.trans.QrSaleTrans;
import com.pax.pay.trans.RedeemPoinTrans;
import com.pax.pay.trans.SaleTrans;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.EnterAmountTextWatcher;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.PosStyleKeyboardUtil;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.UpdateListener;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;
import com.pax.view.MenuPage;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.DialogUtils;

import java.util.LinkedHashMap;

public class MainActivity extends BaseActivity implements OnClickListener {
    //test push denny
    public static final String TAG = "MainActivity";

    private static final int BANK_CARD = 1;
    private static final int QUICK_PASS = 2;
    private static final int QR = 3;

    private static final Byte NORMAL_TRANS  = -1;

    private static final int CHECK_OPER_LOGIN = 0;
    private static final int KEY_BOARD_CANCEL = 1;
    private static final int KEY_BOARD_OK = 2;

    private static final int REQ_OPER_LOGON = 1;

    private Button btnQRPay; // 扫码支付按钮
    private Button btnBankCardPay; // 银行支付按钮
    private Button btnQuickPay; // 闪付按钮

    private int payType = 0; // 是否点击银行卡支付按钮(默认为银行卡)

    private CustomEditText edtAmount; // 金额输入框

    private FrameLayout flkeyBoardContainer;

    private MenuPage menuPage;

    private boolean isInstalledNeptune = true;
    private boolean hasDoTrans = false;// 交易标志位(防双击)

    private ATransaction.TransEndListener listener = new ATransaction.TransEndListener() {
        @Override
        public void onEnd(ActionResult result) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    hasDoTrans = false;// reset flag transaction
                    resetUI();
                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Device.enableBackKey(true);
        //getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE); // remark

        // abdul add disable home bar
        Device.enableHomeRecentKey(false);
        Device.enableStatusBar(false);

        try {
            Log.i( "abdul", "cek debug = " + FinancialApplication.getSysParam().getPrintDebug() );
        } catch (Exception e) {
            e.printStackTrace();
        }

        isInstalledNeptune = Component.neptuneInstalled(this, new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                android.os.Process.killProcess(android.os.Process.myPid()); //Kill the process with the given PID.
            }
        });

        if (!isInstalledNeptune) {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Device.enableBackKey(true);
        if (!isInstalledNeptune) {
            return;
        }
        hasDoTrans = false;// 重置交易标志位
        ActivityStack.getInstance().popAllButBottom();
        //Device.enableHomeRecentKey(true);
        handler.sendEmptyMessage(CHECK_OPER_LOGIN);  //检查操作员是否登录ip
        resetUI();
        SysParam.setUpdateListener(new UpdateListener() {

            @Override
            public void onErr(String prompt) {
                DialogUtils.showUpdateDialog(MainActivity.this, prompt, new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        new SettleTrans(MainActivity.this, handler, null).execute();
                    }
                });
            }
        });
        SysParam.init(MainActivity.this);
//        testparsing("30362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F20202020202020202020202030362F31392F313920442020202020202020202020202020322C30303020425920494E464F2053414C444F202020202020202020202020");
    }

    @Override
    protected void loadParam() {
        Log.d(TAG,"Sandy=loadParam-main-activity");
        try {
            FinancialApplication.init();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            CustomAlertDialog dialog = new CustomAlertDialog(this, CustomAlertDialog.ERROR_TYPE, 5);
            dialog.setContentText(e.getMessage());
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        }
        //sandy
        //Log.d(TAG,"loadParam!!!!!!!!!!!!!!=" + this);

        // 设置操作员签退
        FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS, Controller.Constant.NO);
        //Device.enableStatusBar(true);
    }

    /**
     * 重置MainActivity界面
     */
    private void resetUI() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        menuPage.setCurrentPager(0);
        edtAmount.setText("");
        edtAmount.setHint(currency.getFormat());
        edtAmount.setFocusable(true);
        edtAmount.setFocusableInTouchMode(true);
        edtAmount.requestFocus();
        setPayTypeButtonBackground(false, true, false);
        PosStyleKeyboardUtil.hide(MainActivity.this, flkeyBoardContainer);
        payType = BANK_CARD;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;

    }

    @Override
    protected void initViews() {
        //app 名字标识

        TextView appFlag =(TextView) findViewById(R.id.app_flag);
        appFlag.bringToFront() ;
        // 金额输入框处理
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        edtAmount = (CustomEditText) findViewById(R.id.amount_edtext);
        edtAmount.setHint(currency.getFormat());
        edtAmount.setInputType(InputType.TYPE_NULL);
        edtAmount.setIMEEnabled(false, true);

        //sandy : disable temporary
        btnQRPay = (Button) findViewById(R.id.scan_bar_btn);
        btnQRPay.setVisibility(View.GONE);
        btnBankCardPay = (Button) findViewById(R.id.bank_card_btn);
        btnBankCardPay.setVisibility(View.GONE);
        btnQuickPay = (Button) findViewById(R.id.quick_pass_btn);
        btnQuickPay.setVisibility(View.GONE);

        flkeyBoardContainer = (FrameLayout) findViewById(R.id.fl_trans_softkeyboard);
        // abdul ilangin amount
        View relativeLayout = findViewById(R.id.amountfragment);
        relativeLayout.setVisibility(View.GONE);

        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText("BANK BJB");
        ImageView IvBack = (ImageView) findViewById(R.id.header_back);
        IvBack.setVisibility(View.GONE);

        LinearLayout mLayout = (LinearLayout) findViewById(R.id.ll_gallery);
        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        menuPage = createMenu();
        mLayout.addView(menuPage, params);
    }

    /*
     * 创建菜单
     */
    private MenuPage createMenu() {

        MenuPage.Builder builder = new MenuPage.Builder(MainActivity.this, 15, 3);

        //sandy : Mini Banking
        builder.addMenuItem(getString(R.string.trans_mini_banking), R.drawable.ic_mini_banking, MiniBankingActivity.class);

        //payment
        builder.addMenuItem("Payment", R.drawable.ic_ppob, PaymentMenuActivity.class);

        builder.addMenuItem("QRIS", R.drawable.ic_qris, QrisMenuActivity.class);

        // Transaction inquiry(include print), modified by richard 20170420
        builder.addMenuItem("Trans History", R.drawable.app_query, TransQueryActivity.class);

        //Sandy : voucher
        builder.addTransItem(getString(R.string.trans_redeem), R.drawable.app_voucher, new RedeemPoinTrans(MainActivity.this, handler, null));

        //Added by Steven 2017-4-14 13:59:33
        builder.addMenuItem(getString(R.string.othermanage_menu_set_download), R.drawable.app_other, FunctionMenuActivity.class);

        // Management
        builder.addMenuItem(getString(R.string.manage), R.drawable.app_manage, ManageMenuActivity.class);

        //builder.addMenuItem("QRIS", )


        return builder.create();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void setListeners() {
        btnQRPay.setOnClickListener(this);
        btnBankCardPay.setOnClickListener(this);
        btnQuickPay.setOnClickListener(this);

        edtAmount.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtAmount.setFocusable(true);
                PosStyleKeyboardUtil.show(MainActivity.this, flkeyBoardContainer);
                return false;
            }
        });

        edtAmount.addTextChangedListener(new EnterAmountTextWatcher());

        edtAmount.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        handler.sendEmptyMessage(KEY_BOARD_CANCEL);
                    } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        handler.sendEmptyMessage(KEY_BOARD_OK);
                    }
                }
                return false;
            }
        });

    }

    private void doQrSale(String amount) {
        if (hasDoTrans) {
            return;
        }

        hasDoTrans = true;
        new QrSaleTrans(MainActivity.this, handler, amount, listener).execute();
    }

    private void doSale(String amount, byte searchCardMode) {
        if (hasDoTrans) {
            return;
        }
        //Log.d(TAG,"Sandy=doSale:" + amount);
        //Sandy :
        //add two digit of 00 for suitable Indopay host
        //amount = amount.replace(",","");
        //Log.d(TAG,"Sandy=doSale:" + amount);
        hasDoTrans = true;
        if (searchCardMode == SearchMode.TAP) {
            new SaleTrans(MainActivity.this, handler, amount, searchCardMode, true, listener).execute();
        } else {
            new SaleTrans(MainActivity.this, handler, amount, true, listener).execute();
        }
    }

    /**
     *  @author Richard
     *  @time 2017/4/25  17:38
     */
    private void doPreAuth(String amount){
        if (hasDoTrans) {
            return;
        }

        hasDoTrans = true;
        new AuthTrans(MainActivity.this, handler, amount, listener).execute();
    }

    /**
     *  @author Richard
     *  @time 2017/4/25  18:53
     */
    private void doHomeTrans(String amount){

        String homeTransType = FinancialApplication.getSysParam().get(SysParam.HOME_TRANS);

        if(SysParam.Constant.HOME_TRANS_SALE.equals(homeTransType)){
            doSale(amount, NORMAL_TRANS);
        } else {
            doPreAuth(amount);
        }
    }

    @Override
    public void onClick(View v) {
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();

        int resId = v.getId();
        String amount;
        switch (resId) {
            case R.id.scan_bar_btn:
                setPayTypeButtonBackground(true, false, false);
                payType = QR;
                amount = getInputAmount();
                if (amount == null) {
                    quickClickProtection.stop();
                    break;
                }
                doQrSale(amount);
                break;

            case R.id.bank_card_btn:
                setPayTypeButtonBackground(false, true, false);
                payType = BANK_CARD;
                amount = getInputAmount();
                if (amount == null) {
                    quickClickProtection.stop();
                    break;
                }
                doHomeTrans(amount);
                break;
            case R.id.quick_pass_btn:
                setPayTypeButtonBackground(false, false, true);
                payType = QUICK_PASS;
                amount = getInputAmount();
                if (amount == null) {
                    quickClickProtection.stop();
                    break;
                }
                doSale(amount, SearchMode.TAP);
                break;
            default:
                break;
        }
    }

    /**
     * 设置扫码支付、银行卡、闪付/云闪付按钮的背景
     *
     * @param isQR
     * @param isBK
     * @param isQK
     */
    private void setPayTypeButtonBackground(boolean isQR, boolean isBK, boolean isQK) {
        if (isQR) {
            btnQRPay.setBackgroundResource(R.drawable.sm1_click);
        } else {
            btnQRPay.setBackgroundResource(R.drawable.sm1);
        }

        if (isBK) {
            btnBankCardPay.setBackgroundResource(R.drawable.sm2_click);
        } else {
            btnBankCardPay.setBackgroundResource(R.drawable.sm2);
        }

        if (isQK) {
            btnQuickPay.setBackgroundResource(R.drawable.sm3_click);
        } else {
            btnQuickPay.setBackgroundResource(R.drawable.sm3);
        }

    }

    private String getInputAmount() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String amount = edtAmount.getText().toString().trim();
        if (amount == null || amount.length() == 0 || amount.equals(currency.getFormat())) {
            edtAmount.setFocusable(true);
            edtAmount.requestFocus();
            PosStyleKeyboardUtil.show(MainActivity.this, flkeyBoardContainer);
            return null;
        }
        return amount;
    }

    @Override
    protected void handleMsg(Message msg) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        switch (msg.what) {
            case KEY_BOARD_OK:
                String amount = edtAmount.getText().toString().trim();
                if (amount != null && !amount.equals(currency.getFormat())) {
                    if (payType == BANK_CARD) {
                        doHomeTrans(amount);
                    } else if (payType == QUICK_PASS) {
                        doSale(amount, SearchMode.TAP);
                    } else if (payType == QR) {
                        doQrSale(amount);
                    }
                }
                PosStyleKeyboardUtil.hide(MainActivity.this, flkeyBoardContainer);
                edtAmount.setFocusable(true);
                edtAmount.setFocusableInTouchMode(true);
                edtAmount.requestFocus();
                break;
            case KEY_BOARD_CANCEL:
                edtAmount.setText("");
                PosStyleKeyboardUtil.hide(MainActivity.this, flkeyBoardContainer);
                break;
            case CHECK_OPER_LOGIN:
                // 是否有交易记录未清除
                if (FinancialApplication.getController().get(Controller.CLEAR_LOG) == Controller.Constant.YES
                        && TransData.deleteAllTrans()) {
                    FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.NO);
                    FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
                    FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller.Constant.NO);
                }
                if (FinancialApplication.getController().get(Controller.OPERATOR_LOGON_STATUS) == Controller.Constant.NO) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, REQ_OPER_LOGON);  //返回结果可由onActivityResult处理
                    return;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //为了处理当登录界面的Activity关闭后的参数
        switch (requestCode) {                                                      //为了得到前一个Activity的参数必须重写该方法
            case REQ_OPER_LOGON:                                                    //处理startActivityForResult的返回结果
                if (data == null) {
                    return;
                }
                String operId = data.getExtras().getString(LoginActivity.OPER_ID);
                if (SysParam.OPER_MANAGE.equals(operId) || SysParam.OPER_SYS.equals(operId)) {
                    return;
                }
                TransContext.getInstance().setOperID(operId);
                if (FinancialApplication.getController().get(Controller.POS_LOGON_STATUS) == Controller.Constant.NO) {
                    new PosLogon(MainActivity.this, handler, listener).execute();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (flkeyBoardContainer.getVisibility() == View.VISIBLE) {
                PosStyleKeyboardUtil.hide(MainActivity.this, flkeyBoardContainer);
                return true;
            }
            // 退出当前应用
            DialogUtils.showExitAppDialog(MainActivity.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void testparsing(String data){

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String bit47 = Fox.Hex2Txt(data);
        String[] date = new String[bit47.length()/52];
        String[] sign = new String[bit47.length()/52];
        String[] amount = new String[bit47.length()/52];
        String[] desc = new String[bit47.length()/52];

        int lendata = bit47.length()/52;
        int panjangsatudata = 0;

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Long amt;

        for (int i=0; i<lendata; i++){
            date[i] = bit47.substring(panjangsatudata, 8);
            date[i] = Fox.Substr(bit47, panjangsatudata+1, 8);
            sign[i] = Fox.Substr(bit47, 8+panjangsatudata, 1);

            amt = Long.valueOf(Fox.Substr(bit47, 9+panjangsatudata, 18));
            amount[i] = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amt),
                    currency.getCurrencyExponent(), true);

            desc[i] = Fox.Substr(bit47, 27+panjangsatudata, 25);

            String fullamt;
            if (sign[i].equals("D")){
                fullamt = "- "+amount[i];
            }else {
                fullamt = "+ "+amount[i];
            }
            map.put(desc[i] + "(" + sign[i] +")", fullamt);
            map.put(date[i], "");

            i++;
            panjangsatudata += 52;

        }

    }

}
