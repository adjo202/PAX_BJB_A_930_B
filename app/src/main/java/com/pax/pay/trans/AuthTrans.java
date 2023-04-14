package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
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
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

public class AuthTrans extends BaseTrans {
    private String amount;
    private static final String TAG = "AuthTrans";
    private boolean isNeedInputAmount = true; // 是否需要输入金额
    private boolean isFreePin = true;
    private boolean isSupportBypass = true;
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式

    public AuthTrans(Context context, Handler handler, boolean isFreePin, TransEndListener
            transListener) {
        super(context, handler, ETransType.AUTH, transListener);
        this.isFreePin = isFreePin;
        isNeedInputAmount = true;

    }

    public AuthTrans(Context context, Handler handler, String amount, TransEndListener
            transListener) {
        super(context, handler, ETransType.AUTH, transListener);
        this.amount = amount;
        isNeedInputAmount = false;
    }

    @Override
    protected void bindStateOnAction() {
        Log.d(TAG, "Sandy=bindStateOnAction:");
        searchCardMode = Component.getCardReadMode(transType);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(new AAction
                .ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                String title = "";
                if (isFreePin) {
                    title = context.getString(R.string.auth_trans);
                } else {
                    title = context.getString(R.string.quick_pass_auth_force_pin);
                }
                ((ActionInputTransData) action).setParam(getCurrentContext(), handler, title)
                        .setInfoTypeSale( context.getString(R.string.prompt_input_amount), EInputType.AMOUNT, 9, false);
            }
        }, 1);

        bind(State.ENTER_AMOUNT.toString(), amountAction);
        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                // 预授权凭密只支持挥卡
                if (!isFreePin) {
                    ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                    .string.auth_trans),
                            SearchMode.TAP, transData.getAmount(), null, null,
                            ESearchCardUIType.QUICKPASS);
                    return;
                }

                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                .string.auth_trans),
                        searchCardMode, transData.getAmount(), null, null,
                        ESearchCardUIType.DEFAULT);
            }
        });




        bind(State.CHECK_CARD.toString(), searchCardAction);
        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R
                                .string.auth_trans),
                        transData.getPan(), isSupportBypass, context.getString(R.string
                                .prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(),
                        EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);
        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

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

        // 执行的第一个action
        if (isNeedInputAmount) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            transData.setAmount(amount.replace(",", ""));
            gotoState(State.CHECK_CARD.toString());
        }

    }

    enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_PIN,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        ONLINE,
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
            if (transData.getIsFallback()) {
                searchCardMode = SearchMode.SWIPE;
                gotoState(State.CHECK_CARD.toString());
                return;
            }
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
            case CLSS_PREPROC:
                // 执行的第一个action
                gotoState(State.CLSS_PROC.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
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
        String amountInput = ((String) result.getData()).replace(",", "");
        transData.setAmount(amountInput);
        gotoState(State.CHECK_CARD.toString());


    }

    protected void afterCheckCard(ActionResult result) {

        Log.d(TAG, "Sandy=afterCheckCard:" + transData.getAmount());
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",transData.getAmount()));

        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();

        if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            // 输密码
            gotoState(State.ENTER_PIN.toString());
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

    protected void afterOnline() {
        Log.d(TAG, "Sandy=afterOnline:" + transData.getAmount());

        //sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }
        // 写交易记录
        transData.saveTrans();
        // 判断是否需要电子签名或打印
        toSignOrPrint();
    }

    protected void afterEmvProc(ActionResult result) {
        Log.d(TAG,"afterEmvProc = " + transData.getAmount());

        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);

        //Sandy :
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));


        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult
                .OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // 写交易记录
            transData.saveTrans();
            // 判断是否需要电子签名或打印
            toSignOrPrint();

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult
                .SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (!isFreePin) {
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
                return;
            }

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            if (Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    private void afterClssProcess(CTransResult transResult) {
        Log.d(TAG, "Sandy=afterClssProcess:" + transData.getAmount());
        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED ||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(), transData);
        // 写交易记录
        transData.saveTrans();

        if (transResult.getCvmResult() == ECvmResult.SIG) {
            transData.setSignFree(false);
            transData.setPinFree(true);
        } else {
            transData.setSignFree(true);
            transData.setPinFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult
                .getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setIsOnlineTrans(transResult.getTransResult() == ETransResult
                    .ONLINE_APPROVED);
            toSignOrPrint();

        }
    }

    protected void afterSignature(ActionResult result) {
        Log.d(TAG, "Sandy=afterSignature:" + transData.getAmount());
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
    private void toSignOrPrint() {
        Log.d(TAG, "Sandy=toSignOrPrint:" + transData.getAmount());
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            // 打印
            gotoState(State.PRINT_TICKET.toString());
        } else {
            // 电子签名
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.updateTrans();
    }
}
