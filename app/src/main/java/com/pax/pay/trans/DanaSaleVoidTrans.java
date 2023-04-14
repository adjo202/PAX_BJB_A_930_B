package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

public class DanaSaleVoidTrans extends BaseTrans {

    public static final String TAG = "DanaSaleVoidTrans";
    private boolean isNeedFindOrigTrans = true;
    private boolean isNeedInputTransNo = true;
    protected TransData origTransData;
    private String origTransNo;

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        ONLINE_NORMAL,
        PRINT_TICKET
    }

    public DanaSaleVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.DANA_QR_VOID, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;

    }

    public DanaSaleVoidTrans(Context context, Handler handler, TransData origTransData, TransEndListener transListener) {
        super(context, handler, ETransType.DANA_QR_VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;

    }





    @Override
    protected void bindStateOnAction() {

        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6, context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(context.getString(R.string.trans_void));
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno), ActionInputTransData.EInputType.NUM, 6, true);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);


        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler, context.getString(R.string.trans_void));
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);


        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE_NORMAL.toString(), transOnlineAction);



        //action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);
        gotoState(State.INPUT_PWD.toString());
    }





    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        switch (state) {
            case INPUT_PWD:
                afterInputPwd(result);
                break;
            case ENTER_TRANSNO:
                afterEnterTransNo(result);
                break;
            case TRANS_DETAIL:
                afterTransDetail();
                break;
            case ONLINE_NORMAL:
                toSignOrPrint();
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }




    private void afterInputPwd(ActionResult result) {
        Log.d(TAG,"afterInputPwd:");

        if (!FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            Log.d(TAG,"sandy.1");

            gotoState(State.ENTER_TRANSNO.toString());
        } else {
            String data = (String) result.getData();
            Log.d(TAG,"sandy.2");
            if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
                Log.d(TAG,"sandy.3");

                transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                return;
            }

            if (isNeedInputTransNo) {
                Log.d(TAG,"iisNeedInputTransNo:");

                gotoState(State.ENTER_TRANSNO.toString());
            } else {// 不需要输入流水号
                if (isNeedFindOrigTrans) {
                    Log.d(TAG,"isNeedFindOrigTrans:");

                    Log.d(TAG,"sandy.4");

                    validateOrigTransData(Long.parseLong(origTransNo));
                } else { // 不需要读交易记录

                    Log.d(TAG,"sandy.5");

                    copyOrigTransData();
                    afterTransDetail();
                }
            }
        }
    }

    private void afterEnterTransNo(ActionResult result) {
        String content = (String) result.getData();
         Log.d(TAG,"afterEnterTransNo : " + content);

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



    private void afterTransDetail() {

        gotoState(State.ONLINE_NORMAL.toString());

    }


    protected void validateOrigTransData(long origTransNo) {
        origTransData = TransData.readTrans(origTransNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType transType = ETransType.valueOf(origTransData.getTransType());


        // 脱机消费交易不能撤销
        if (!origTransData.getIsOnlineTrans()) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        String trStatus = origTransData.getTransState();
        if (trStatus.equals(TransData.ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        }
        if (trStatus.equals(TransData.ETransStatus.ADJUST.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        copyOrigTransData();
        ActionDispTransDetail action = (ActionDispTransDetail)getAction(SaleVoidTrans.State.TRANS_DETAIL.toString()) ;
        action.setTransData(origTransData);
        gotoState(State.TRANS_DETAIL.toString());
    }



    protected void toSignOrPrint() {
        //Sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

        if(transData.getResponseCode().equals("00")){
            transData.setSignFree(true);
            // 写交易记录
            transData.saveTrans();

            gotoState(State.PRINT_TICKET.toString());
            origTransData.setTransState(TransData.ETransStatus.VOID.toString());
            origTransData.updateTrans();

        }



    }



    protected void copyOrigTransData() {

        Log.d(TAG,"sandy.copyOrigTransData");

        //sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",origTransData.getAmount() ));
        else
            transData.setAmount(origTransData.getAmount());

        //sandy added field 7 here
        transData.setOrigDateTimeTrans(origTransData.getDateTimeTrans());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setField62(origTransData.getCouponNo());


    }




}
