package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

public class QrRefundTrans extends BaseTrans {
    private TransData origRecord;

    private boolean isEntOrigC2bVoucher = true; // 是否需要输入原付款凭证码
    private boolean isEnterAmount = true; // 是否需要输入金额

    public QrRefundTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.QR_REFUND, transListener);
        isEntOrigC2bVoucher = true;
        isEnterAmount = true;
    }

    /**
     * @param context
     * @param handler
     * @param origTransData
     * @param isEntAmount
     * @param transListener
     */
    public QrRefundTrans(Context context, Handler handler, TransData origTransData, boolean isEntAmount,
            boolean isEntOrigC2bVoucher, TransEndListener transListener) {
        super(context, handler, ETransType.QR_REFUND, transListener);
        this.isEnterAmount = isEntAmount;
        this.isEntOrigC2bVoucher = isEntOrigC2bVoucher;
        this.origRecord = origTransData;
    }

    @Override
    protected void bindStateOnAction() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 输入原交易凭证码
        ActionInputTransData enterInfosAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE,
                null);
        enterInfosAction.setTitle(context.getString(R.string.scan_code_refund));
        enterInfosAction.setInfoTypeSale(context.getString(R.string.prompt_input_certificate),
                EInputType.NUM, 20, 12, false, false, true);
        bind(State.SCAN_CODE.toString(), enterInfosAction);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.scan_code_refund));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_scancode_refund_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 联机action
        ActionTransOnline onlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), onlineAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 撤销退货类是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            if (isEntOrigC2bVoucher) { // 需要填交易信息
                gotoState(State.SCAN_CODE.toString());
            } else {
                copyOrigTransData();
                if (isEnterAmount) {
                    gotoState(State.ENTER_AMOUNT.toString());
                    return;
                }
                if (!checkAmount(transData.getAmount())) {
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }
                confirmAmount();
            }
        }
    }

    enum State {
        INPUT_PWD,
        SCAN_CODE,
        ENTER_AMOUNT,
        ONLINE,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        // action结果检查，如果失败，结束交易
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        switch (state) {
            case INPUT_PWD:
                afterInputPwd(result);
                break;
            case SCAN_CODE:
                afterScanCode(result);
                break;
            case ENTER_AMOUNT:
                afterEnterAmount(result);
                break;
            case ONLINE: // 联机的后续处理
                transData.saveTrans();
                gotoState(State.PRINT_TICKET.toString());
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void afterInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }

        if (isEntOrigC2bVoucher) { // 需要填交易信息
            gotoState(State.SCAN_CODE.toString());
            return;
        }

        copyOrigTransData();
        if (isEnterAmount) {
            gotoState(State.ENTER_AMOUNT.toString());
            return;
        }
        if (!checkAmount(transData.getAmount())) {
            transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
            return;
        }
        confirmAmount();
    }

    protected void afterScanCode(ActionResult result) {
        String res = (String) result.getData();
        transData.setEnterMode(EnterMode.QR);
        transData.setOrigC2bVoucher(res);
        transData.setOrigDate(res.substring(4, 8));

        // 若不需要输入金额直接弹出确认金额界面(第三方调用)
        if (!isEnterAmount) {
            if (!checkAmount(origRecord.getAmount())) {
                transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                return;
            }
            transData.setAmount(origRecord.getAmount());
            confirmAmount();
            return;
        }
        // 扫码过后，提示输入金额
        gotoState(State.ENTER_AMOUNT.toString());
    }

    protected void afterEnterAmount(ActionResult result) {
        String scanAmount = ((String) result.getData()).replace(".", "");
        // 检测退货金额
        if (!checkAmount(scanAmount)) {
            transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
            return;
        }
        transData.setAmount(scanAmount);
        confirmAmount();
    }

    // 确认交易金额
    private void confirmAmount() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                CustomAlertDialog dialog = new CustomAlertDialog(getCurrentContext(), CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(context.getString(R.string.scan_code_refund));
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
                        // 取消重新输入金额
                        gotoState(State.ENTER_AMOUNT.toString());
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 联机处理
                        gotoState(State.ONLINE.toString());
                    }
                });
            }
        });
    }

    private boolean checkAmount(String amountStr) {
        long amount = Long.parseLong(amountStr);
        long amountMax = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.OTHTC_REFUNDLIMT)) * 100;

        return amount <= amountMax;
    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setEnterMode(EnterMode.QR);
        transData.setAmount(origRecord.getAmount());
        transData.setOrigC2bVoucher(origRecord.getC2bVoucher());
        transData.setOrigDate(origRecord.getC2bVoucher().substring(4, 8));
    }
}