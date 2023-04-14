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
import com.pax.up.bjb.R;


/**
 * 预授权完成撤销
 *
 * @author Steven.W
 */
public class AuthCMVoidTrans extends BaseTrans {
    protected TransData origRecord;
    private String origTransNo;

    /**
     * 是否需要读交易记录
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * 是否需要输入流水号
     */
    private boolean isNeedInputTransNo = true;

    public AuthCMVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.AUTHCMVOID, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public AuthCMVoidTrans(Context context, Handler handler, TransData origTransData, TransEndListener transListener) {
        super(context, handler, ETransType.AUTHCMVOID, transListener);
        this.origRecord = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public AuthCMVoidTrans(Context context, Handler handler, String origTransNo, TransEndListener transListener) {
        super(context, handler, ETransType.AUTHCMVOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @Override
    protected void bindStateOnAction() {
        // 输入主管密码
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 输入交易流水号
        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(context.getString(R.string.auth_cm_void_all));
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno),
                EInputType.NUM, 6, true);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler, context.getString(
                R.string.auth_cm_void_all));
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);

        // 寻卡
        byte searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(transData, null);
        searchCardAction.setTitle(context.getString(R.string.auth_cm_void_all));
        searchCardAction.setMode(searchCardMode);
        searchCardAction.setUiType(ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.auth_cm_void_all),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // EMV处理流程
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

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
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {
                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setParam(getCurrentContext(), handler, transData);
                    }
                });
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.CLSS_PREPROC.toString());

    }

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        CHECK_CARD,
        ENTER_PIN,
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
            ActionSearchCard action = (ActionSearchCard) getAction(State.CHECK_CARD.toString());
            action.setMode(SearchMode.SWIPE);
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
                afterEmvProc(result);
                break;
            case CLSS_PREPROC:
                afterClssPreProc();
                break;
            case CLSS_PROC:
                afterClssProc(result);
                break;
            case ONLINE: // 联机的后续处理
                afterOnline();
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

    protected void afterInputPwd(ActionResult result) {
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

    protected void afterEnterTransNo(ActionResult result) {
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
            transNo = Long.parseLong((String) result.getData());
        }
        validateOrigTransData(transNo);
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            // 检查卡号
            if (!transData.getPan().equals(origRecord.getPan())) {
                transEnd(new ActionResult(TransResult.ERR_CARD_NO, null));
                return;
            }

            checkPin();
        } else if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // Clss处理
            gotoState(State.CLSS_PREPROC.toString());
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

    protected void afterEmvProc(ActionResult result) {
        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程
            // 检查卡号
            if (!transData.getPan().equals(origRecord.getPan())) {
                transEnd(new ActionResult(TransResult.ERR_CARD_NO, null));
                return;
            }
            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            checkPin();
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            // GPO返回AAC，继续交易
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            checkPin();
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssPreProc() {
        // 撤销是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
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

    protected void afterClssProc(ActionResult result) {
        CTransResult clssResult = (CTransResult) result.getData();
        transData.setEmvResult((byte) clssResult.getTransResult().ordinal());
        ClssTransProcess.clssTransResultProcess(clssResult, FinancialApplication.getClss(), transData);
        if (clssResult.getTransResult() == ETransResult.ARQC) {

            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }

            checkPin();
        } else {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    protected void afterOnline() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
           transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));


            // 写交易记录
        transData.saveTrans();
        // 更新原交易记录
        origRecord.setTransState(ETransStatus.VOID.toString());
        origRecord.updateTrans();
        gotoState(State.SIGNATURE.toString());
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

    // 验证原始交易
    protected void validateOrigTransData(long origTransNo) {
        origRecord = TransData.readTrans(origTransNo);
        if (origRecord == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        String trType = origRecord.getTransType();
        // 非预授权完成请求交易不能撤销
        if (!trType.equals(ETransType.AUTHCM.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        String trStatus = origRecord.getTransState();
        // 已撤销交易，不能重复撤销
        if (trStatus.equals(ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        }
        copyOrigTransData();

        ActionDispTransDetail action = (ActionDispTransDetail) getAction(State.TRANS_DETAIL
                .toString());
        action.setTransData(origRecord);
        gotoState(State.TRANS_DETAIL.toString());
    }

    // 设置原交易记录
    protected void copyOrigTransData() {
        //sandy
        transData.setDateTimeTrans(origRecord.getDateTimeTrans());
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
             if(SysParam.Constant.YES.equals(isIndopayMode))
                 transData.setAmount(String.format("%s00",origRecord.getAmount()));
            else
                transData.setAmount(origRecord.getAmount());
        transData.setOrigBatchNo(origRecord.getBatchNo());
        transData.setOrigAuthCode(origRecord.getOrigAuthCode());
        transData.setOrigRefNo(origRecord.getRefNo());
        transData.setOrigDate(origRecord.getDate());
        transData.setOrigTransNo(origRecord.getTransNo());
        transData.setPan(origRecord.getPan());
        transData.setExpDate(origRecord.getExpDate());
    }

    // 检查是否需要刷卡和输密码
    private void checkCardAndPin() {
        // 撤销是否需要刷卡
        if (FinancialApplication.getSysParam().get(SysParam.UCTC_PACVOID).equals(Constant.YES)) {
            gotoState(State.CHECK_CARD.toString());
        } else {
            // 撤销不需要刷卡
            transData.setEnterMode(EnterMode.MANUAL);
            checkPin();
        }
    }

    // 检查是否需要输密码
    private void checkPin() {
        // 预授权完成撤销需要输密码
        if (FinancialApplication.getSysParam().get(SysParam.IPTC_PACVOID).equals(Constant.YES)) {
            gotoState(State.ENTER_PIN.toString());
        } else {
            // 预授权完成撤销不需要输密码
            transData.setPin("");
            transData.setHasPin(false);
            gotoState(State.ONLINE.toString());
        }
    }
}
