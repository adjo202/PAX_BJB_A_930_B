package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.abl.utils.TrackUtils;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.entity.PollingResult.EOperationType;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.device.DeviceManager;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.emv.EmvListenerImpl;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.KeyboardTouchListener;
import com.pax.pay.utils.SimpleStyleKeyboardUtil;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.CustomEditText;

import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat")
public class SearchCardCustomActivity extends BaseActivityWithTickForAction implements OnClickListener {
    private static final String TAG = "SearchCardActivity";

    private TextView tvPrompt; // 输入方式提示
    private TextView tvCardNo; // 卡号输入框

    private CustomEditText edtCardNo; // 输入框
    private CustomEditText edtDate; // 日期输入框

    private Button btnConfirm; // 确认按钮
    private SimpleStyleKeyboardUtil keyboardUtil;
    private ImageView ivBack; // 返回按钮
    private ImageView ivSwipe; // 刷卡图标
    private ImageView ivInsert; // 插卡图标
    private ImageView ivTap; // 非接图标

    private LinearLayout llAmount; // 交易金额布局
    private LinearLayout llAmountCodeDate; // 交易金额及授权码，交易日期等布局
    private LinearLayout llCardNo; // 卡号输入布局
    private LinearLayout llExpDate; // 有效期期输入布局

    private String navTitle;
    private String amount; // 交易金额
    private String cardNo; // 卡号
    private String code; // 授权码
    private String date; // 日期
    private String searchCardPrompt; // 寻卡提示

    private ESearchCardUIType searchCardUI; // 寻卡界面

    private final static int READ_CARD_OK = 1; // 读卡成功
    private final static int READ_CARD_CANCEL = 2; // 取消读卡
    private final static int READ_CARD_ERR = 3; // 读卡失败
    private final static int READ_CARD_PAUSE = 4; // 读卡暂停

    private final static int KEYBOARD_GONE = 5; // 隐藏键盘
    private final static int EDITTEXT_CARDNO = 6; // 卡号输入框
    private final static int EDITTEXT_DATE = 7; // 日期输入框
    private final static int EDITTEXT_CARDNO_ERR = 8; // 卡号输入错误
    private final static int EDITTEXT_DATE_ERR = 9; // 日期输入错误

    private final static int READ_PAN_EMV_OK = 10; //kd add 01042021
    private final static int READ_PAN_EMV_FALLBACK = 11; //kd add 01042021

    private boolean supportManual = false; // 是否支持手输
    private boolean isManual = false; // 是否是手输卡号

    private boolean isCardNo = true; // 输入框是否是输入卡号

    private EReaderType readerType = null; // 读卡类型

    private PollingResult pollingResult;

    private String PAN_EMV;//kd add 01042021
    /*
     * 外部调用参数
     */

    /**
     * 支持的寻卡类型{@link SearchMode}
     */
    private byte mode; // 寻卡模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SearchCardThread().start();
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        // 显示金额
        try {
            amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
            if (amount != null && amount.length() > 0) {
                amount = FinancialApplication.getConvert().amountMinUnitToMajor(amount,
                        currency.getCurrencyExponent(), true);

            }
        } catch (Exception e) {

            Log.e(TAG, "", e);
            amount = null;
        }

        // 寻卡方式
        try {
            mode = bundle.getByte(EUIParamKeys.CARD_SEARCH_MODE.toString(), SearchMode.SWIPE);
            if ((mode & SearchMode.KEYIN) == SearchMode.KEYIN) { // 是否支持手输卡号
                supportManual = true;
            } else {
                supportManual = false;
            }

            readerType = toReaderType(mode);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        // 获取授权码
        try {
            code = bundle.getString(EUIParamKeys.AUTH_CODE.toString());
        } catch (Exception e) {
            Log.e(TAG, "", e);
            code = null;
        }

        // 获取日期
        try {
            date = bundle.getString(EUIParamKeys.TRANS_DATE.toString());
            if (date != null && date.length() > 0) {
                date = date.substring(0, 2) + "/" + date.substring(2, 4);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            date = null;
        }

        // 获取UI界面显示类型
        try {
            searchCardUI = (ESearchCardUIType) bundle.getSerializable(EUIParamKeys.SEARCH_CARD_UI_TYPE.toString());
        } catch (Exception e) {
            Log.e(TAG, "", e);
            searchCardUI = ESearchCardUIType.DEFAULT;
        }

        // 获取寻卡提醒
        try {
            searchCardPrompt = bundle.getString(EUIParamKeys.SEARCH_CARD_PROMPT.toString());
            if (searchCardPrompt == null || searchCardPrompt.length() == 0) {
                searchCardPrompt = getString(R.string.prompt_default_serchcard_prompt);
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
            searchCardPrompt = getString(R.string.prompt_default_serchcard_prompt);
        }
    }

    /**
     * 获取ReaderType
     * 
     * @param mode
     * @return
     */
    private EReaderType toReaderType(byte mode) {
        mode &= ~SearchMode.KEYIN;
        EReaderType[] types = EReaderType.values();
        for (EReaderType type : types) {
            if (type.getEReaderType() == mode)
                return type;
        }
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bankcard_pay;
    }

    @Override
    protected void initViews() {
        keyboardUtil = new SimpleStyleKeyboardUtil(this);
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);
        initDefaultViews();
        // 初始化电子现金界面、初始化闪付界面
        if (searchCardUI == ESearchCardUIType.EC || searchCardUI == ESearchCardUIType.QUICKPASS) {
            initViewForEcAndQuickPass();
        }
    }

    // 默认寻卡界面初始化
    private void initDefaultViews() {
        llAmount = (LinearLayout) findViewById(R.id.trans_amount_layout);
        llAmountCodeDate = (LinearLayout) findViewById(R.id.trans_info_layout);

        // 金额及其他信息界面显示
        if (code == null || code.length() == 0) {
            if (amount == null || amount.length() == 0) { // 余额查询不显示金额
                llAmount.setVisibility(View.GONE);
            } else {
                TextView tvAmount = (TextView) findViewById(R.id.amount_txt); // 只显示交易金额
                tvAmount.setText(amount);
            }
        } else {
            // 显示交易金额， 授权码， 交易日期
            llAmount.setVisibility(View.GONE);
            llAmountCodeDate.setVisibility(View.VISIBLE);
            TextView tvAmount = (TextView) findViewById(R.id.amount_txt_extra);
            TextView tvCode = (TextView) findViewById(R.id.code_txt);
            tvAmount.setText(amount);
            tvCode.setText(code);
            if (date == null || date.length() == 0) {
                findViewById(R.id.date_txt_layout).setVisibility(View.GONE);
            } else {
                TextView dateTv = (TextView) findViewById(R.id.date_txt);
                dateTv.setText(date);
            }
        }

        llCardNo = (LinearLayout) findViewById(R.id.edit_cardno);
        llExpDate = (LinearLayout) findViewById(R.id.edit_date);

        edtCardNo = (CustomEditText) findViewById(R.id.bank_card_number);// 初始为卡号输入框
        edtCardNo.setIMEEnabled(false, true);

        edtDate = (CustomEditText) findViewById(R.id.prompt_edit_extranum);
        edtDate.setIMEEnabled(false, true);
        edtDate.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });// 限制输入的最大长度为4
        edtDate.setFocusable(true);
        edtDate.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edtDate.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

        btnConfirm = (Button) findViewById(R.id.ok_btn);
        btnConfirm.setEnabled(false);

        tvPrompt = (TextView) findViewById(R.id.tv_prompt_readcard);
        tvCardNo = (TextView) findViewById(R.id.tv_cardno);

        ivSwipe = (ImageView) findViewById(R.id.iv_swipe);
        ivInsert = (ImageView) findViewById(R.id.iv_insert);
        ivTap = (ImageView) findViewById(R.id.iv_untouch);

        if (supportManual) {
            tvPrompt.setText(searchCardPrompt);
            edtCardNo.setFocusable(true);// 支持手输卡号
            edtCardNo.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            edtCardNo.addTextChangedListener(new LocalTextWatch());
            edtCardNo.setFilters(new InputFilter[] { new InputFilter.LengthFilter(19 + 4) });// 4为卡号分隔符个数
            edtCardNo.setOnTouchListener(new KeyboardTouchListener(keyboardUtil));

            if (mode == SearchMode.KEYIN) {
                edtCardNo.requestFocusAndTouch(keyboardUtil);
            }

        } else {
            tvPrompt.setText(searchCardPrompt);
            edtCardNo.setFocusable(false);// 不支持手输入卡号
        }
        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT, (mode & SearchMode.TAP) == SearchMode.TAP);
    }

    // 闪付及电子现金支付寻卡界面
    private void initViewForEcAndQuickPass() {
        RelativeLayout defaultLayout = (RelativeLayout) findViewById(R.id.searchcard_defult_layout);
        RelativeLayout ecLayout = (RelativeLayout) findViewById(R.id.searchcard_ec_layout);

        defaultLayout.setVisibility(View.GONE);
        ecLayout.setVisibility(View.VISIBLE);

        TextView tvAmount = (TextView) findViewById(R.id.amount_txt_ec);
        tvAmount.setText(amount);

        ivInsert = (ImageView) findViewById(R.id.iv_insert_ec);
        ivTap = (ImageView) findViewById(R.id.iv_untouch_ec);

        tvPrompt = (TextView) findViewById(R.id.tv_prompt_readcard_ec);
        tvPrompt.setText(searchCardPrompt);

        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT, (mode & SearchMode.TAP) == SearchMode.TAP);

        // 电子现金余额查询界面
        if (amount == null || amount.length() == 0) {
            RelativeLayout amountLayout = (RelativeLayout) findViewById(R.id.searchcard_ec_layout_amount);

            ecLayout.setBackgroundColor(getResources().getColor(R.color.key_normal_color));
            amountLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        setKeyListener(edtCardNo);
        setKeyListener(edtDate);
        setKeyListener(btnConfirm);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.header_back:
                if (isSuccLeave) {
                    return;
                }
                handler.sendEmptyMessage(READ_CARD_CANCEL);
                break;
            case R.id.ok_btn:
                process();
                break;
            default:
                break;
        }

    }

    // 寻卡线程
    class SearchCardThread extends Thread {

        @Override
        public void run() {
            try {
                ICardReaderHelper cardReaderHelper = FinancialApplication.getDal().getCardReaderHelper();
                if (readerType == null) {
                    return;
                }
                pollingResult = cardReaderHelper.polling(readerType, 60 * 1000);
                cardReaderHelper.stopPolling();
                if (pollingResult.getOperationType() == EOperationType.TIMEOUT) {
                    handler.sendEmptyMessage(READ_CARD_CANCEL);
                } else if (pollingResult.getOperationType() == EOperationType.CANCEL) {
                    // 已经发过取消指令,不需要再发送
                } else if (pollingResult.getOperationType() == EOperationType.PAUSE) {
                    handler.sendEmptyMessage(READ_CARD_PAUSE);
                } else {
                    handler.sendEmptyMessage(READ_CARD_OK);
                }

            } catch (MagDevException | IccDevException | PiccDevException e) {
                Log.e(TAG, "", e);
                if (isSuccLeave) {
                    return;
                }
                // 读卡失败处理
                handler.sendEmptyMessage(READ_CARD_ERR);
            }
        }

    }

    // 卡号分割及输入长度检查
    class LocalTextWatch implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s.toString().length();
            // 判定是否启用寻卡线程
            if (length == 0) {
                confirmBtnChange();
                isManual = false;
                tvPrompt.setText(searchCardPrompt);
                new SearchCardThread().start();
            }

            if (length == 1 && edtCardNo.isFocusable()) {
                confirmBtnChange();
                isManual = true;
                tvPrompt.setText(getString(R.string.prompt_card_num_manual));
                FinancialApplication.getDal().getCardReaderHelper().setIsPause(true);
                FinancialApplication.getDal().getCardReaderHelper().stopPolling();

            }

            // 数字分割
            if (s == null || s.length() == 0)
                return;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (i != 4 && i != 9 && i != 14 && i != 19 && s.charAt(i) == ' ') {
                    continue;
                } else {
                    sb.append(s.charAt(i));
                    if ((sb.length() == 5 || sb.length() == 10 || sb.length() == 15 || sb.length() == 20)
                            && sb.charAt(sb.length() - 1) != ' ') {
                        sb.insert(sb.length() - 1, ' ');
                    }
                }
            }

            if (!sb.toString().equals(s.toString())) {
                int index = start + 1;
                if (sb.charAt(start) == ' ') {
                    if (before == 0) {
                        index++;
                    } else {
                        edtCardNo.setText(sb.subSequence(0, sb.length() - 1));
                        index--;
                    }
                } else {
                    if (before == 1) {
                        index--;
                    }
                }
                edtCardNo.setText(sb.toString());
                edtCardNo.setSelection(index);
            }

            if (sb.toString().charAt(sb.length() - 1) == ' ') {
                edtCardNo.setText(sb.toString().substring(0, sb.length() - 1));
                edtCardNo.setSelection(sb.length() - 1);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Do nothing
        }

    }

    @Override
    protected void handleMsg(Message msg) {
        ActionResult result;
        switch (msg.what) {
            case READ_CARD_OK:// 读卡成功
                setSearchCardImage(pollingResult.getReaderType() == EReaderType.MAG,
                        pollingResult.getReaderType() == EReaderType.ICC,
                        pollingResult.getReaderType() == EReaderType.PICC);

                if (pollingResult.getReaderType() == EReaderType.MAG) {
                    //在此增加（OTHTC_EMV_OPR）EMV输入方式可选弹出窗口  //add Fall back treatment
                    if ((SearchMode.INSERT == (mode & SearchMode.INSERT)) && TrackUtils.isIcCard(pollingResult.getTrack2())) {
                        ToastUtils.showMessage(SearchCardCustomActivity.this, getString(R.string.prompt_ic_card_input));
                        mode &= ~SearchMode.SWIPE;
                        readerType = toReaderType(mode);
                        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                                (mode & SearchMode.INSERT) == SearchMode.INSERT,
                                (mode & SearchMode.TAP) == SearchMode.TAP);
                        tvPrompt.setText(searchCardPrompt);
                        new SearchCardThread().start();
                        return;
                    }

                    // 有时刷卡成功，但没有磁道II，做一下防护
                    String track2 = pollingResult.getTrack2();
                    if (track2 == null || track2.length() == 0) {
                        //修改ui显示 Added by Steven.T 2017-6-15 14:28:13
                        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                                (mode & SearchMode.INSERT) == SearchMode.INSERT,
                                (mode & SearchMode.TAP) == SearchMode.TAP);
                        new SearchCardThread().start();
                        return;
                    }
                    Device.beepPrompt();
                    String pan = TrackUtils.getPan(track2);
                    edtCardNo.setFocusable(false);
                    edtCardNo.setText(PanUtils.separateWithSpace(pan));
                    keyboardUtil.hideKeyBoardForBack();
                    confirmBtnChange();
                } else if (pollingResult.getReaderType() == EReaderType.ICC) {
                    edtCardNo.setFocusable(false);
                    keyboardUtil.hideKeyBoardForBack();

                    //kd add call emv process 01042021
                    new GetPanEMVThread().start();

//close kd 01042021              finish(new ActionResult(0, new CardInformation(SearchMode.INSERT)));
                } else if (pollingResult.getReaderType() == EReaderType.PICC) {
                    edtCardNo.setFocusable(false);
                    keyboardUtil.hideKeyBoardForBack();
                    finish(new ActionResult(0, new CardInformation(SearchMode.TAP)));
                }

                break;
            case READ_PAN_EMV_OK:
                CardInformation cardInfoEMV = new CardInformation();
                String[] data = PAN_EMV.split("=");
                //String[] trk2 = data[1].split("D");
                cardInfoEMV.setPan(data[0]);
                cardInfoEMV.setTrack2(data[1]);
                cardInfoEMV.setSearchMode((byte)0x02);//insert card 0x02
                result = new ActionResult(TransResult.SUCC, cardInfoEMV);
                finish(result);
                break;
            case READ_CARD_CANCEL:
                FinancialApplication.getDal().getCardReaderHelper().stopPolling();
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            case READ_CARD_ERR:
                ToastUtils.showMessage(SearchCardCustomActivity.this, getString(R.string.prompt_swipe_failed_please_retry));
                new SearchCardThread().start();
                break;
            case KEYBOARD_GONE:
                keyboardUtil.hideKeyBoardForBack();
                break;
            case EDITTEXT_CARDNO:
                FinancialApplication.getDal().getCardReaderHelper().setIsPause(true);
                FinancialApplication.getDal().getCardReaderHelper().stopPolling();
                cardNo = edtCardNo.getText().toString().replace(" ", "");

                llCardNo.setVisibility(View.GONE);
                llExpDate.setVisibility(View.VISIBLE);

                tvCardNo.setText(PanUtils.separateWithSpace(cardNo));
                edtDate.requestFocusAndTouch(keyboardUtil);
                break;
            case EDITTEXT_DATE:
                CardInformation cardInfo = new CardInformation(SearchMode.KEYIN, cardNo);
                String date = edtDate.getText().toString();
                if (!TextUtils.isEmpty(date)) {
                    date = date.substring(2) + date.substring(0, 2);// 将MMyy转换成yyMM
                }
                cardInfo.setExpDate(date);
                result = new ActionResult(TransResult.SUCC, cardInfo);
                finish(result);
                break;
            case EDITTEXT_DATE_ERR:
                ToastUtils.showMessage(SearchCardCustomActivity.this, getString(R.string.prompt_card_date_err));
                edtDate.requestFocusAndTouch(keyboardUtil);
                break;
            case EDITTEXT_CARDNO_ERR:
                ToastUtils.showMessage(SearchCardCustomActivity.this, getString(R.string.prompt_card_num_err));
                edtCardNo.requestFocusAndTouch(keyboardUtil);
                break;
            default:
                break;
        }

    }

    /**
     * 设置图标显示
     * 
     * @param mag
     * @param icc
     * @param picc
     */
    private void setSearchCardImage(boolean mag, boolean icc, boolean picc) {
        // 默认界面图标设置
        if (searchCardUI == ESearchCardUIType.DEFAULT) {
            if (mag) {
                ivSwipe.setImageResource(R.drawable.swipe_card);
            } else {
                ivSwipe.setImageResource(R.drawable.no_swipe_card);
            }

            if (icc) {
                ivInsert.setImageResource(R.drawable.insert_card);
            } else {
                ivInsert.setImageResource(R.drawable.no_insert_card);
            }

            //sandy : temporary
            ivTap.setVisibility(View.GONE);
            if (picc) {
                ivTap.setImageResource(R.drawable.untouch_card);
            } else {
                ivTap.setImageResource(R.drawable.no_untouch_card);
            }

            return;
        }

        // 现金支付界面图标设置
        if (searchCardUI == ESearchCardUIType.EC) {
            if (icc) {
                ivInsert.setImageResource(R.drawable.insert_card_big);
            } else {
                ivInsert.setImageResource(R.drawable.no_insert_card_big);
            }

            if (picc) {
                ivTap.setImageResource(R.drawable.untouch_card_big);
            } else {
                ivTap.setImageResource(R.drawable.no_untouch_card_big);
            }

            return;
        }

        // 闪付界面图标设置
        if (searchCardUI == ESearchCardUIType.QUICKPASS) {
            ivInsert.setImageResource(R.drawable.phone_quick);
            ivTap.setImageResource(R.drawable.card_qucik);
            return;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isSuccLeave) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handler.sendEmptyMessage(READ_CARD_CANCEL);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 寻卡成功时，此界面还保留， 在后续界面切换时，还有机会跑到前台，此时按返回键，此activity finish，同时会有两个分支同时进行
    // 如果寻卡成功时， 此标志为true
    private boolean isSuccLeave = false;

    @Override
    public void finish(ActionResult result) {
        FinancialApplication.getDal().getCardReaderHelper().setIsPause(true);
        FinancialApplication.getDal().getCardReaderHelper().stopPolling();
        isSuccLeave = true;
        super.finish(result);
    }

    // 填写信息校验
    private void process() {
        if (isManual) {
            if (isCardNo) {
                String content = edtCardNo.getText().toString().replace(" ", "");
                if (content == null || content.length() == 0) {
                    handler.sendEmptyMessage(KEYBOARD_GONE);
                    return;
                }

                if (content.length() < 13) {
                    handler.sendEmptyMessage(EDITTEXT_CARDNO_ERR);
                    return;
                }

                if (content.length() > 12) {
                    handler.sendEmptyMessage(EDITTEXT_CARDNO);
                    isCardNo = false;
                    isManual = true;
                    return;
                }
            } else {
                String content = edtDate.getText().toString().replace(" ", "");
                if (TextUtils.isEmpty(content)) {
                    handler.sendEmptyMessage(EDITTEXT_DATE);
                } else if (dateProcess(content)) {
                    handler.sendEmptyMessage(EDITTEXT_DATE);
                } else {
                    handler.sendEmptyMessage(EDITTEXT_DATE_ERR);
                }
            }

        } else {
            String content = edtCardNo.getText().toString().replace(" ", "");
            if (content == null || content.length() == 0) {
                handler.sendEmptyMessage(KEYBOARD_GONE);
                return;
            }

            ActionResult result;
            CardInformation cardInfo = null;
            if (pollingResult.getReaderType() == EReaderType.MAG) {
                cardInfo = new CardInformation(SearchMode.SWIPE, pollingResult.getTrack1(), pollingResult.getTrack2(),
                        pollingResult.getTrack3(), TrackUtils.getPan(pollingResult.getTrack2()));
            }

            result = new ActionResult(TransResult.SUCC, cardInfo);
            finish(result);
        }
    }

    private boolean dateProcess(String content) {
        if (content.length() != 4) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(content);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }

        return true;
    }

    private void confirmBtnChange() {
        String content = edtCardNo.getText().toString();
        if (content != null && content.length() > 0) {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundResource(R.drawable.button_click_background);
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackgroundResource(R.drawable.gray_button_background);
        }
    }

    private void setKeyListener(View view) {
        view.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    process();
                    return true;
                }
                return false;
            }
        });
    }

    //kd add 01042021
    private TransData transData;
    class GetPanEMVThread extends Thread {

        @Override
        public void run() {
            transData = new TransData();
            transData.setAmount("0");
            transData.setTransType(ETransType.SALE.toString());
            TransProcessListener transProcessListener = new TransProcessListenerImpl(SearchCardCustomActivity.this);
            EmvListenerImpl emvListener = new EmvListenerImpl(SearchCardCustomActivity.this, FinancialApplication.getEmv(), handler, transData, transProcessListener);
            transProcessListener.onShowProgress(SearchCardCustomActivity.this.getString(R.string.process_please_wait), 0);

            try {
                EmvTransProcess emvTransProcess = EmvTransProcess.getInstance();
                String result = emvTransProcess.transProcessGetPanOnly(transData, emvListener);
                PAN_EMV = result;
                transProcessListener.onHideProgress();
                handler.sendEmptyMessage(READ_PAN_EMV_OK);
            } catch (EmvException e) {
                PAN_EMV = "";
                Log.e(TAG, "", e);
                Device.beepErr();
                if (e.getErrCode() != EEmvExceptions.EMV_ERR_UNKNOWN.ordinal()) {
                    if (e.getErrCode() == EEmvExceptions.EMV_ERR_FALL_BACK.ordinal()) {
                        transProcessListener.onShowErrMessageWithConfirm(
                                //mContext.getString(R.string.err_card_unsupport_demotion),
                                e.getErrMsg(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                        transData.setIsFallback(true);
                        transProcessListener.onHideProgress();

                        handler.sendEmptyMessage(READ_PAN_EMV_FALLBACK);
                    } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_USER_CANCEL.ordinal()) {
                        // 用户取消， 不提示
                        transProcessListener.onHideProgress();
                        handler.sendEmptyMessage(READ_CARD_ERR);
                    } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_PURE_EC_CARD_NOT_ONLINE.ordinal()) {// 纯电子现金卡不能联机
                        transProcessListener.onHideProgress();
                        handler.sendEmptyMessage(READ_CARD_ERR);
                    } else {
                        transProcessListener.onShowErrMessageWithConfirm(e.getErrMsg(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                        transProcessListener.onHideProgress();
                        handler.sendEmptyMessage(READ_CARD_ERR);
                    }
                }else {
                    transProcessListener.onHideProgress();
                    handler.sendEmptyMessage(READ_CARD_ERR);
                }
            }

        }

    }


}
