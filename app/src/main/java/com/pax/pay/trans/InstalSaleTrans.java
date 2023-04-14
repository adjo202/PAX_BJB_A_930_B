package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
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
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.up.bjb.R;

import java.io.UnsupportedEncodingException;

public class InstalSaleTrans extends BaseTrans {
    public static final String TAG = "InstalSaleTrans";

    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式
    private String amount;

    public InstalSaleTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.INSTAL_SALE, transListener);
    }

    @Override
    public void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(ETransType.INSTAL_SALE);
        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.installment_Sale));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 获取分期期数
        ActionInputTransData inputInstalNumAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        inputInstalNumAction.setTitle(context.getString(R.string.installment_Sale));
        inputInstalNumAction.setInfoTypeSale(context.getString(R.string.installment_num),
                EInputType.NUM, 2, 1, false, false);
        bind(State.ENTER_NUM.toString(), inputInstalNumAction);

        // 输入项目编码
        ActionInputTransData inputInstalCodeAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        inputInstalCodeAction.setInfoTypeSale(
                context.getString(R.string.installment_code), EInputType.NUM, 30, 0, false, false);
        inputInstalCodeAction.setTitle(context.getString(R.string.installment_Sale));
        bind(State.ENTER_CODE.toString(), inputInstalCodeAction);

        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R.string.installment_Sale),
                        searchCardMode, transData.getAmount(), null, null,
                        ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.installment_Sale),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        // 签名action
        ActionSignature signatureAction = new ActionSignature(new ActionStartListener() {
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

        // 执行的第一action
        if (amount == null || amount.length() == 0) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            gotoState(State.CHECK_CARD.toString());
        }

    }

    public enum State {
        ENTER_AMOUNT,
        ENTER_NUM,
        ENTER_CODE,
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
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(FinancialApplication.getEmv(), transType, true, false);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55Dup));
            }
            //fall back treatment
            if(transData.getIsFallback()){
                searchCardMode = SearchMode.SWIPE;
                gotoState(State.CHECK_CARD.toString());
                return;
            }
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
            case ENTER_AMOUNT:
                afterEnterAmount(result);
                break;
            case ENTER_NUM:
                afterEnterNum(result);
                break;
            case ENTER_CODE:
                afterEnterCode(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                afterEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                afterOnline();
                break;
            case EMV_PROC: // emv后续处理
                afterEmvProc(result);
                break;
            case SIGNATURE:
                afterSignature(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void afterEnterAmount(ActionResult result) {
        // 保存交易金额
        String amount = ((String) result.getData()).replace(".", "");
        transData.setAmount(amount);
        gotoState(State.ENTER_NUM.toString());
    }

    protected void afterEnterNum(ActionResult result) {
        // 保存分期期数
        String instalNum = ((String) result.getData());
        transData.setInstalNum(instalNum);
        gotoState(State.ENTER_CODE.toString());
    }

    protected void afterEnterCode(ActionResult result) {
        // 保存项目编码
        String prjCode = ((String) result.getData());
        transData.setPrjCode(prjCode);
        gotoState(State.CHECK_CARD.toString());
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.SWIPE || mode == SearchMode.INSERT) {
            if(mode == SearchMode.SWIPE ){
                transData.setTransType(ETransType.INSTAL_SALE.toString());
                gotoState(State.ENTER_PIN.toString());
            }
            else {
                gotoState(State.EMV_PROC.toString());
            }
        }
    }

    protected void afterEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState(State.ONLINE.toString());
    }

    protected void afterOnline() {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }
        setFeeAmount();
        transData.saveTrans();
        toSignOrPrint();
    }

    protected void afterEmvProc(ActionResult result) {
        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准
            // 写交易记录
            setFeeAmount();
            transData.saveTrans();
            toSignOrPrint();
            return;
        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            transData.setPinFree(false);
            gotoState(State.ENTER_PIN.toString());
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterSignature(ActionResult result) {
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();
        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            gotoState(State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.updateTrans();
    }

    /**
     * Get fee amount from the field62 in the response data.
     */
    private boolean setFeeAmount() {

        String temp=null;
        byte[] f62 = FinancialApplication.getConvert().strToBcd(transData.getField62(), IConvert.EPaddingPosition.PADDING_LEFT);

        try {
            temp = new String(f62, "GBK");
        }catch (UnsupportedEncodingException e) {
            Log.e(TAG, "", e);
        }

        if (temp != null && temp.length() > 0) {
            //首期还款金额
            transData.setFirstAmount(temp.substring(0,12));
            //还款币种
            transData.setInstalCurrCode(temp.substring(12,15));
            //分期手续费
            transData.setFeeTotalAmount(temp.substring(15,27));

            return  true;
        }

        return false;
    }
}
