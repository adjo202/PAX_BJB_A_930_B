package com.pax.pay.trans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.PanUtils;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

public class InstalVoidTrans extends BaseTrans {

    protected TransData origTransData;
    private String origTransNo;
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式
    /**
     * 是否需要读交易记录
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * 是否需要输入流水号
     */
    private boolean isNeedInputTransNo = true;

    public InstalVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.INSTAL_VOID, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public InstalVoidTrans(Context context, Handler handler, TransData origTransData,
                           TransEndListener transListener) {
        super(context, handler, ETransType.INSTAL_VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public InstalVoidTrans(Context context, Handler handler, String origTransNo, TransEndListener
            transListener) {
        super(context, handler, ETransType.INSTAL_VOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(ETransType.INSTAL_VOID);

        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(context.getString(R.string.installment_Void));
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno),
                EInputType.NUM, 6, true);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(new AAction
                .ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                LinkedHashMap<String, String> map = copyDispData();
                ((ActionDispTransDetail) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.installment_Void), map);
            }
        });
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);

        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                .string.trans_void),
                        searchCardMode, transData.getAmount(), null, null,
                        ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R
                                .string.installment_Void),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(),
                        EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // EMV处理流程
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        // 签名action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 撤销是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// 需要输入流水号
            gotoState(State.ENTER_TRANSNO.toString());
        } else {// 不需要输入流水号
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Long.parseLong(origTransNo));
            } else { // 不需要读交易记录
                copyOrigTransData();
                checkCardAndPin();
            }
        }

    }

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        CHECK_CARD,
        ENTER_PIN,
        ONLINE,
        EMV_PROC,
        SIGNATURE,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        //fall back treatment
        if (state == State.EMV_PROC && transData.getIsFallback()) {
            searchCardMode = SearchMode.SWIPE;
            gotoState(State.CHECK_CARD.toString());
            return;
        }

        if (state != State.SIGNATURE) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_PWD:
                afterInputPwd(result);
                break;
            case ENTER_TRANSNO:
                afterEnterTransNo(result);
                break;
            case TRANS_DETAIL:
                checkCardAndPin();
                break;
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                afterEnterPin(result);
                break;
            case EMV_PROC: // emv后续处理
                afterEMVProcess(result);
                break;
            case ONLINE: // 联机的后续处理
                afterOnline(result);
                break;
            case SIGNATURE:
                afterSignature(result);
                break;
            case PRINT_TICKET:
            default:
                // 交易结束
                transEnd(result);
                break;
        }

    }

    // 检查原交易信息
    protected void validateOrigTransData(long origTransNo) {
        origTransData = TransData.readTrans(origTransNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        String trType = origTransData.getTransType();

        // 非分期消费交易不能撤销
        if (!trType.equals(ETransType.INSTAL_SALE.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        String trStatus = origTransData.getTransState();
        // 已撤销交易，不能重复撤销/已调整交易不可撤销
        if (trStatus.equals(ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        }

        copyOrigTransData();
        gotoState(State.TRANS_DETAIL.toString());
    }

    // 设置原交易记录
    protected void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        //复制分期信息
        transData.setInstalNum(origTransData.getInstalNum());
        transData.setPrjCode(origTransData.getPrjCode());
        transData.setFirstAmount(origTransData.getFirstAmount());
        transData.setFeeTotalAmount(origTransData.getFeeTotalAmount());
        transData.setInstalCurrCode(origTransData.getInstalCurrCode());
    }

    // 检查是否需要刷卡和输密码
    private void checkCardAndPin() {
        // 撤销是否需要刷卡
        if (FinancialApplication.getSysParam().get(SysParam.UCTC_VOID).equals(Constant.YES)) {
            gotoState(State.CHECK_CARD.toString());
        } else {
            // 撤销不需要刷卡
            transData.setEnterMode(EnterMode.MANUAL);
            checkPin();
        }
    }

    // 检查是否需要输密码
    private void checkPin() {
        // 撤销需要输密码
        if (FinancialApplication.getSysParam().get(SysParam.IPTC_VOID).equals(Constant.YES)) {
            gotoState(State.ENTER_PIN.toString());
        } else {
            // 撤销不需要输密码
            transData.setPin("");
            transData.setHasPin(false);
            gotoState(State.ONLINE.toString());
        }
    }

    private void afterEMVProcess(ActionResult result) {
        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { //
            // 请求联机/简化流程

            // 纯电子现金卡联机拒绝
            if (Component.pureEcOnlineReject(transData) != 0) {
                transEnd(new ActionResult(TransResult.ERR_PURE_CARD_CAN_NOT_ONLINE, null));
                return;
            }
            // 检查卡号
            if (!transData.getPan().equals(origTransData.getPan())) {
                transEnd(new ActionResult(TransResult.ERR_CARD_NO, null));
                return;
            }
            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            checkPin();
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    private void afterInputPwd(ActionResult result) {
        if (!FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(Constant.YES)) {
            gotoState(State.ENTER_TRANSNO.toString());
        } else {
            String data = (String) result.getData();
            if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
                transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                return;
            }

            if (isNeedInputTransNo) {// 需要输入流水号
                gotoState(State.ENTER_TRANSNO.toString());
            } else {// 不需要输入流水号
                if (isNeedFindOrigTrans) {
                    validateOrigTransData(Long.parseLong(origTransNo));
                } else { // 不需要读交易记录
                    copyOrigTransData();
                    checkCardAndPin();
                }
            }
        }
    }

    private void afterEnterTransNo(ActionResult result) {
        String content = (String) result.getData();
        long transNo = 0;
        if (content == null) {
            TransData transData = TransData.readLastTrans();
            if (transData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, transData));
                return;
            }
            transNo = transData.getTransNo();
        } else {
            transNo = Long.parseLong(content);
        }
        validateOrigTransData(transNo);
    }

    private void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.SWIPE || mode == SearchMode.KEYIN) {
            // 输密码
            if (!transData.getPan().equals(origTransData.getPan())) {
                transEnd(new ActionResult(TransResult.ERR_CARD_NO, null));
                return;
            }
            checkPin();
        } else if (mode == SearchMode.INSERT || mode == SearchMode.TAP) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        }
    }

    private void afterEnterPin(ActionResult result){
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState(State.ONLINE.toString());
    }

    protected void afterOnline(ActionResult result) {
        // 写交易记录
        transData.saveTrans();
        // 更新原交易记录
        origTransData.setTransState(ETransStatus.VOID.toString());
        origTransData.updateTrans();
        gotoState(State.SIGNATURE.toString());
    }

    private void afterSignature(ActionResult result){
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();
        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

    private LinkedHashMap<String, String> copyDispData() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        ETransType transType = ETransType.valueOf(origTransData.getTransType());
        String formater = FinancialApplication.getConvert().amountMinUnitToMajor(
                String.valueOf(Long.parseLong(origTransData.getAmount())),
                currency.getCurrencyExponent(), true);
        String amount = context.getString(R.string.trans_amount_default, currency.getName(),
                formater);

        // 日期时间
        String date = origTransData.getDate();
        String time = origTransData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        String temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date
                .substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(context.getString(R.string.trans_type), transType.getTransName());
        map.put(context.getString(R.string.trans_amount), amount);
        map.put(context.getString(R.string.trans_card_no), PanUtils.maskedCardNo(transType,
                origTransData.getPan()));
        map.put(context.getString(R.string.detail_auth_code), origTransData.getAuthCode());
        map.put(context.getString(R.string.detail_trans_ref_no), origTransData.getRefNo());
        map.put(context.getString(R.string.detail_trans_no), String.format("%06d", origTransData
                .getTransNo()));
        map.put(context.getString(R.string.trans_date), temp);
        return map;
    }

}
