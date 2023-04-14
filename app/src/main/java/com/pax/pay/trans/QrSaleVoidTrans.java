package com.pax.pay.trans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

public class QrSaleVoidTrans extends BaseTrans {

    private TransData origTransData;
    private String origTransNo;

    /**
     * 是否需要读交易记录
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * 是否需要输入流水号
     */
    private boolean isNeedInputTransNo = true;

    public QrSaleVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.QR_VOID, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public QrSaleVoidTrans(Context context, Handler handler, TransData origTransData, TransEndListener transListener) {
        super(context, handler, ETransType.QR_VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public QrSaleVoidTrans(Context context, Handler handler, String origTransNo, TransEndListener transListener) {
        super(context, handler, ETransType.QR_VOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 输入凭证码
        ActionInputTransData inputCertificateNoAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        inputCertificateNoAction.setTitle(context.getString(R.string.scan_code_void));
        inputCertificateNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_certificate),
                EInputType.NUM, 20, 12, true, false, true);
        bind(State.SCAN_CODE.toString(), inputCertificateNoAction);

        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler, context.getString(R.string
                .scan_code_void));
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransOnline) action).setParam(getCurrentContext(), transData);
            }
        });
        bind(State.ONLINE.toString(), transOnlineAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 撤销是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// 需要输入凭证码
            gotoState(State.SCAN_CODE.toString());
        } else {// 不需要输入流水号
            if (isNeedFindOrigTrans) {
                validateOrigTransData(origTransNo);
            } else { // 不需要读交易记录
                copyOrigTransData();
            }
        }

    }

    enum State {
        INPUT_PWD,
        SCAN_CODE,
        TRANS_DETAIL,
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
            case TRANS_DETAIL:
                gotoState(State.ONLINE.toString());
                break;
            case ONLINE: // 联机的后续处理
                afterOnline(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }

    }

    protected void afterInputPwd(ActionResult result) {
        if (!FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            gotoState(State.SCAN_CODE.toString());
        } else {
            String data = (String) result.getData();
            if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
                transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                return;
            }
            if (isNeedInputTransNo) {// 需要输入流水号
                gotoState(State.SCAN_CODE.toString());
            } else {// 不需要输入流水号
                if (isNeedFindOrigTrans) {
                    validateOrigTransData(origTransNo);
                } else { // 不需要读交易记录
                    copyOrigTransData();
                    transData.setC2bVoucher(origTransData.getC2bVoucher());
                    gotoState(State.ONLINE.toString());
                }
            }
        }
    }

    protected void afterScanCode(ActionResult result) {
        String content = (String) result.getData();
        if (content == null) {
            TransData transData = TransData.readLastTrans();
            if (transData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, transData));
                return;
            }
            if (ETransType.valueOf(transData.getTransType()) == ETransType.QR_SALE) {
                content = transData.getC2bVoucher();
            } else {
                content = transData.getOrigC2bVoucher();
            }
        }
        // 要把码放到59域
        transData.setOrigC2bVoucher(content);
        transData.setEnterMode(EnterMode.QR);
        validateOrigTransData(content);
    }

    protected void afterOnline(ActionResult result) {
        // 写交易记录
        transData.saveTrans();
        // 更新原交易记录
        origTransData.setTransState(ETransStatus.VOID.toString());
        origTransData.updateTrans();
        gotoState(State.PRINT_TICKET.toString());
    }

    // 检查原交易信息
    private void validateOrigTransData(String origTransNo) {
        origTransData = TransData.readTransByVoucher(origTransNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        String trType = origTransData.getTransType();
        if (!trType.equals(ETransType.QR_SALE.toString())) {
            // 非消费交易不能撤销
            if (!trType.equals(ETransType.SALE.toString())) {
                transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
                return;
            } else {
                // 脱机消费交易不能撤销
                if (!origTransData.getIsOnlineTrans()) {
                    transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
                    return;
                }
            }
        }
        String trStatus = origTransData.getTransState();
        // 已撤销交易，不能重复撤销/已调整交易不可撤销
        if (trStatus.equals(ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        } else if (trStatus.equals(ETransStatus.ADJUST.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        copyOrigTransData();
        ActionDispTransDetail action = (ActionDispTransDetail)getAction(State.TRANS_DETAIL
                .toString()) ;
        action.setTransData(origTransData);
        gotoState(State.TRANS_DETAIL.toString());

    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setC2b(origTransData.getC2b());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setOrigC2bVoucher(origTransData.getC2bVoucher());
    }

}
