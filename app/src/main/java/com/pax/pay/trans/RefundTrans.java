package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionEmvProcess;
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
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class RefundTrans extends BaseTrans {
    private TransData origRecord;
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式
    private boolean isEntOrigData = true; // 是否需要输入原交易信息(原参考号、交易时间)
    private boolean isEnterAmount = true; // 是否需要输入金额

    public RefundTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.REFUND, transListener);
        isEntOrigData = true;
        isEnterAmount = true;
    }

    /**
     * @param context
     * @param handler
     * @param origTransData
     * @param isEntAmount
     * @param isEntOrigData
     * @param transListener
     */
    public RefundTrans(Context context, Handler handler, TransData origTransData, boolean
            isEntAmount, boolean isEntOrigData, TransEndListener transListener) {
        super(context, handler, ETransType.REFUND, transListener);
        this.isEnterAmount = isEntAmount;
        this.isEntOrigData = isEntOrigData;
        this.origRecord = origTransData;
    }

    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(transType);

        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                .string.trans_refund),
                        searchCardMode, null, null, null, ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入原交易参考号/交易日期
        ActionInputTransData enterInfosAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_AUTH, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_refund));
        enterInfosAction.setInfoTypeSale(context.getString(R.string.prompt_input_orig_refer),
                EInputType.NUM, 12, 12, false);
        enterInfosAction.setIntypeAuth(context.getString(R.string.prompt_input_date),
                EInputType.DATE, 4);
        bind(State.ENTER_INFO.toString(), enterInfosAction);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.trans_refund));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_refund_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

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

        // 撤销退货类是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant
                .YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.CHECK_CARD.toString());
        }
    }

    enum State {
        INPUT_PWD,
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
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case INPUT_PWD:
                afterInputPwd(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case EMV_PROC: // emv后续处理
                afterEMVProcess(result);
                break;
            case CLSS_PREPROC:
                // 撤销退货类是否需要输入主管密码
                gotoState(State.CLSS_PROC.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case ENTER_INFO:
                afterEnterInfo(result);
                break;
            case ENTER_AMOUNT:
                afterEnterAmount(result);
                break;
            case ONLINE: // 联机的后续处理
                // 写交易记录
                transData.saveTrans();
                gotoState(State.SIGNATURE.toString());
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

    // 确认交易金额
    private void confirmAmount() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                CustomAlertDialog dialog = new CustomAlertDialog(getCurrentContext(),
                        CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(context.getString(R.string.trans_refund));
                String amontStr = FinancialApplication.getConvert().amountMinUnitToMajor(transData
                                .getAmount(),
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
                        // 取消重新输入金额
                        gotoState(State.ENTER_AMOUNT.toString());
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 联机
                        gotoState(State.ONLINE.toString());
                    }
                });
            }
        });
    }

    private boolean checkAmount(String amountStr) {
        long amount = Long.parseLong(amountStr);
        long amountMax = Long.parseLong(FinancialApplication.getSysParam().get(SysParam
                .OTHTC_REFUNDLIMT)) * 100;
        return amount <= amountMax;
    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setAmount(origRecord.getAmount());
        transData.setOrigRefNo(origRecord.getRefNo());
        transData.setOrigDate(origRecord.getDate());
    }

    private void afterInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        gotoState(State.CHECK_CARD.toString());
    }

    private void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 保存原交易记录
        if (!isEntOrigData || !isEnterAmount) {
            copyOrigTransData();
        }
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            if (isEntOrigData) {
                gotoState(State.ENTER_INFO.toString());
            } else if (isEnterAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
            } else {
                if (!checkAmount(transData.getAmount())) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                confirmAmount();
            }
        } else if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // Clss处理
            gotoState(State.CLSS_PREPROC.toString());
        }
    }

    private void afterEMVProcess(ActionResult result) {
// 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult
                .OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // 写交易记录
            transData.saveTrans();

            // 电子签名
            gotoState(State.SIGNATURE.toString());
        } else if (transResult == ETransResult.ARQC || transResult == ETransResult
                .SIMPLE_FLOW_END) { // 请求联机/简化流程
            if (isEntOrigData) {
                gotoState(State.ENTER_INFO.toString());
            } else if (isEnterAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
            } else {
                if (!checkAmount(transData.getAmount())) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                confirmAmount();
            }
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    private void afterClssProcess(ActionResult result) {
        CTransResult clssResult = (CTransResult) result.getData();
        transData.setEmvResult((byte) clssResult.getTransResult().ordinal());

        if (clssResult.getTransResult() == ETransResult.ARQC) {
            ClssTransProcess.clssTransResultProcess(clssResult, FinancialApplication.getClss(), transData);
            if (isEntOrigData) {
                gotoState(State.ENTER_INFO.toString());
            } else if (isEnterAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
            } else {
                if (!checkAmount(transData.getAmount())) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                confirmAmount();
            }
        } else {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void afterEnterInfo(ActionResult result) {
        String[] infos = (String[]) result.getData();
        transData.setOrigRefNo(infos[0]);
        transData.setOrigDate(infos[1]);
        if (!isEnterAmount) {
            if (!checkAmount(transData.getAmount())) {
                transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                return;
            }
            confirmAmount();
            return;
        }

        gotoState(State.ENTER_AMOUNT.toString());
    }

    private void afterEnterAmount(ActionResult result) {
        String amount = ((String) result.getData()).replace(".", "");
        if (!checkAmount(amount)) {
            transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
            return;
        }
        transData.setAmount(amount);
        confirmAmount();
    }

    private void afterSignature(ActionResult result) {
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }


}