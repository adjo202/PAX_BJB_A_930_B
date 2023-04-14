package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

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
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.up.bjb.R;


public class AuthVoidTrans extends BaseTrans {
    private static final String TAG = "AuthVoidTrans";
    private TransData origRecord;
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式
    private boolean isEntOrigData = true; // 是否需要输入原交易信息
    private boolean isEnterAmount = true; // 是否需要输入金额

    public AuthVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.AUTHVOID, transListener);
        isEntOrigData = true;
        isEnterAmount = true;
    }

    public AuthVoidTrans(Context context, Handler handler, TransData origTransData, boolean isEntAmount,
                         boolean isEntOrigData, TransEndListener transListener) {
        super(context, handler, ETransType.AUTHVOID, transListener);
        this.isEnterAmount = isEntAmount;
        this.isEntOrigData = isEntOrigData;
        this.origRecord = origTransData;
    }

    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(transType);
        // 输入主管密码
        ActionInputPasword inputPaswordAction = new ActionInputPasword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPasword) action).setParam(getCurrentContext(), handler, 6,
                        context.getString(R.string.prompt_director_pwd), null);
            }
        });
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.auth_void));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 输入授权码/交易日期
        ActionInputTransData enterAuthCodeAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_AUTH, null);
        enterAuthCodeAction.setTitle(context.getString(R.string.auth_void));
        enterAuthCodeAction.setInfoTypeSale(context.getString(R.string.prompt_input_auth_code),
                EInputType.ALPHNUM, 6, 2, false);
        enterAuthCodeAction.setIntypeAuth(context.getString(R.string.prompt_input_date),
                EInputType.DATE, 4);
        bind(State.ENTER_INFO.toString(), enterAuthCodeAction);

        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R.string.auth_void),
                        searchCardMode, transData.getAmount(),
                        transData.getOrigAuthCode(), transData.getOrigDate(), ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.auth_void),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

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
        INPUT_PWD,
        ENTER_AMOUNT,
        ENTER_INFO,
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
            searchCardMode = SearchMode.SWIPE;
            gotoState(State.CHECK_CARD.toString());
            return;
        }
        if (state != State.SIGNATURE) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            // "纯电子现金不能联机"，转成"预授权类交易不可以使用纯电子现金"， BCTC要求
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
            case INPUT_PWD: // 主管密码检查后续处理
                afterInputPwd(result);
                break;
            case ENTER_AMOUNT: // 输交易金额后续处理
                afterEnterAmount(result);
                break;
            case ENTER_INFO: // 输授权码/日期后续处理
                afterEnterInfo(result);
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
            case CLSS_PROC:
                afterClssProc(result);
                break;
            case CLSS_PREPROC:
                afterClssPreProc();
                break;
            case ONLINE: // 联机的后续处理
                String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
                //sandy
                if(SysParam.Constant.YES.equals(isIndopayMode))
                    transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
                // 写交易记录
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

    protected void afterInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        if (isEntOrigData) { // 需要填交易信息
            if (isEnterAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
            } else {
                transData.setAmount(origRecord.getAmount());
                gotoState(State.ENTER_INFO.toString());
            }
        } else {
            copyOrigTransData();
            if (isEnterAmount) {
                gotoState(State.ENTER_AMOUNT.toString());
                return;
            }
            gotoState(State.CHECK_CARD.toString());
        }
    }

    protected void afterEnterAmount(ActionResult result) {
        String amount = ((String) result.getData()).replace(",", "");
        transData.setAmount(amount);
        if (!isEntOrigData) {
            gotoState(State.CHECK_CARD.toString());
            return;
        }
        // 输入授权码/日期
        gotoState(State.ENTER_INFO.toString());
    }

    protected void afterEnterInfo(ActionResult result) {
        String[] info = (String[]) result.getData();
        transData.setOrigAuthCode(info[0]);
        transData.setOrigDate(info[1]);
        // 寻卡
        gotoState(State.CHECK_CARD.toString());
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            checkPin();
        } else if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
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

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",transData.getAmount()));

        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
             // 写交易记录
            transData.saveTrans();
            // 是否支持电子签名
            if (FinancialApplication.getSysParam().get(SysParam.OTHTC_SINGATURE).equals(SysParam.Constant.YES)) {
                gotoState(State.SIGNATURE.toString());
            } else {
                gotoState(State.PRINT_TICKET.toString());
            }
        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程
            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            checkPin();
        } else {
            emvAbnormalResultProcess(transResult);
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

    protected void afterClssPreProc() {
        // 撤销是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            if (isEntOrigData) { // 需要填交易信息
                if (isEnterAmount) {
                    gotoState(State.ENTER_AMOUNT.toString());
                } else {
                    transData.setAmount(origRecord.getAmount());
                    gotoState(State.ENTER_INFO.toString());
                }
            } else {
                copyOrigTransData();
                if (isEnterAmount) {
                    gotoState(State.ENTER_AMOUNT.toString());
                    return;
                }
                gotoState(State.CHECK_CARD.toString());
            }
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

    // 检查是否需要输密码
    private void checkPin() {
        // 预授权撤销需要输密码
        if (FinancialApplication.getSysParam().get(SysParam.IPTC_PAVOID).equals(Constant.YES)) {
            gotoState(State.ENTER_PIN.toString());
        } else {
            // 预授权撤销不需要输密码
            transData.setPin("");
            transData.setHasPin(false);
            gotoState(State.ONLINE.toString());
        }
    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setAmount(origRecord.getAmount());
        transData.setOrigAuthCode(origRecord.getAuthCode());
        transData.setOrigDate(origRecord.getDate());
    }
}
