package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionEmvProcess;
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
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;


/**
 * 预授权完成通知
 *
 * @author Steven.W
 */
public class AuthSettlementTrans extends BaseTrans {

    private TransData origTransData;
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式
    private boolean isEntOrigData = true; // 是否需要输入原信息
    private boolean isEntAmount = true; // 是否需要输入金额

    /**
     * 从预授权菜单进入
     */
    public AuthSettlementTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.AUTH_SETTLEMENT, transListener);
        isEntOrigData = true;
        isEntAmount = true;
    }

    /**
     * 从交易查询界面进入
     *
     * @param context
     * @param handler
     * @param origTransData
     * @param isEntAmount   , 第三方调用时，为false， 其他为true
     * @param isEntOrigData
     * @param transListener
     */
    public AuthSettlementTrans(Context context, Handler handler, TransData origTransData, boolean isEntAmount,
                               boolean isEntOrigData, TransEndListener transListener) {
        super(context, handler, ETransType.AUTH_SETTLEMENT, transListener);
        this.isEntAmount = isEntAmount;
        this.origTransData = origTransData;
        this.isEntOrigData = isEntOrigData;
    }

    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(transType);
        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.auth_cm_adv_all));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 输入授权码/交易日期
        ActionInputTransData enterAuthCodeAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_AUTH, null);
        enterAuthCodeAction.setTitle(context.getString(R.string.auth_cm_adv_all));
        enterAuthCodeAction.setInfoTypeSale(context.getString(R.string.prompt_input_auth_code), EInputType.ALPHNUM, 6, 2,
                false);
        enterAuthCodeAction.setIntypeAuth(context.getString(R.string.prompt_input_date), EInputType
                .DATE, 4);
        bind(State.ENTER_INFO.toString(), enterAuthCodeAction);

        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R.string.auth_cm_adv_all),
                        searchCardMode, transData.getAmount(),
                        transData.getOrigAuthCode(), transData.getOrigDate(), ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

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

        gotoState(State.CLSS_PREPROC.toString());

    }

    enum State {
        ENTER_AMOUNT,
        ENTER_INFO,
        CHECK_CARD,
        ONLINE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
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
            // 纯电子现金不能联机，转成预授权类交易不可以使用纯电子现金， BCTC要求
            if (ret == TransResult.ERR_PURE_CARD_CAN_NOT_ONLINE) {
                transEnd(new ActionResult(TransResult.ERR_AUTH_TRANS_CAN_NOT_USE_PURE_CARD, null));
                return;
            }
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                afterEnterAmount(result);
                break;
            case ENTER_INFO: // 输授权码日期后续处理
                String[] info = (String[]) result.getData();
                transData.setOrigAuthCode(info[0]);
                transData.setOrigDate(info[1]);
                confirmAmount();
                break;
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case EMV_PROC: // emv后续处理
                afterEmvProc(result);
                break;
            case CLSS_PROC:
                afterClssProc(result);
                break;
            case CLSS_PREPROC:
                afterClssPreProc();
                break;
            case ONLINE: // 联机的后续处理
                transData.saveTrans();
                gotoState(State.SIGNATURE.toString());
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
        String amount = ((String) result.getData()).replace(",", "");
        transData.setAmount(amount);
        if (!isEntOrigData) {
            confirmAmount();
            return;
        }
        gotoState(State.ENTER_INFO.toString());
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            // 联机处理
            gotoState(State.ONLINE.toString());
        } else if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void afterEmvProc(ActionResult result) {
        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程
            // 联机处理
            gotoState(State.ONLINE.toString());
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            // GPO返回AAC，继续交易
            gotoState(State.ONLINE.toString());
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssProc(ActionResult result) {
        CTransResult clssResult = (CTransResult) result.getData();
        transData.setEmvResult((byte) clssResult.getTransResult().ordinal());
        ClssTransProcess.clssTransResultProcess(clssResult, FinancialApplication.getClss(), transData);
        if (clssResult.getTransResult() == ETransResult.ARQC) {
            gotoState(State.ONLINE.toString());
        } else {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    protected void afterClssPreProc() {
        if (isEntOrigData) { // 不需要读交易记录
            if (isEntAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
            } else {
                transData.setAmount(origTransData.getAmount());
                gotoState(State.ENTER_INFO.toString());
            }
        } else {
            copyOrigTransData();
            if (isEntAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
                return;
            }
            confirmAmount();
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

    /**
     * 确认交易金额
     */
    private void confirmAmount() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                CustomAlertDialog dialog =
                        new CustomAlertDialog(getCurrentContext(), CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(context.getString(R.string.auth_trans_liff, context.getString(R.string.auth_cm_adv)));
                String amontStr = FinancialApplication.getConvert().amountMinUnitToMajor(transData.getAmount(),
                        currency.getCurrencyExponent(), true);
                dialog.setContentText(context.getString(R.string.trans_amount_info, amontStr));
                dialog.setCanceledOnTouchOutside(false);

                dialog.show();

                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 交易结束
                        transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 寻卡
                        gotoState(State.CHECK_CARD.toString());
                    }
                });
            }
        });
    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigDate(origTransData.getDate());
    }
}
