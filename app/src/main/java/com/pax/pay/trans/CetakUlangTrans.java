package com.pax.pay.trans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionInputCetakUlangData;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.util.List;


public class CetakUlangTrans extends BaseTrans {

    private static final String TAG = "CetakUlangTrans";
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

    public CetakUlangTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.CETAK_ULANG, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    public CetakUlangTrans(Context context, Handler handler, TransData origTransData,
                           TransEndListener transListener) {
        super(context, handler, ETransType.CETAK_ULANG, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    public CetakUlangTrans(Context context, Handler handler, String origTransNo, TransEndListener
            transListener) {
        super(context, handler, ETransType.CETAK_ULANG, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {

        ActionInputCetakUlangData enterInfosAction = new ActionInputCetakUlangData(handler, null);
        enterInfosAction.setTitle("Cetak Ulang");
        bind(State.INPUT_DATA.toString(), enterInfosAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(transType.getTransName());
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno), EInputType.NUM, 6, false);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE_NORMAL.toString(), transOnlineAction);

        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.INPUT_DATA.toString());
        //gotoState(State.ENTER_TRANSNO.toString());

    }

    enum State {
        INPUT_DATA,
        ENTER_TRANSNO, //gk dipake, BJB fase 2
        ONLINE_NORMAL,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        switch (state) {
            case ENTER_TRANSNO: //fase 1
                afterEnterTransNo(result);
                break;
            case INPUT_DATA: //fase 2
                //afterInputData(result);
                //afterInputData2(result);
                afterInputData2New(result); //uat
                break;
            case ONLINE_NORMAL: // 联机的后续处理
                afterOnline(result);
                break;
            case PRINT_TICKET:
            default:
                // 交易结束
                transEnd(result);
                break;
        }

    }

    void afterInputData(ActionResult result){
        String[] data = (String[]) result.getData();
        transData.setBillingId(data[0]);
        transData.setNtb(data[1]);
        gotoState(State.ONLINE_NORMAL.toString());
    }

    void afterInputData2(ActionResult result){
        String[] data = (String[]) result.getData();
        transData.setBillingId(data[0]);
        transData.setNtb(data[1]);

        //String sql = "select * from trans where transtype in (?,?,?) and billingId = ? and refNo = ? limit " + total + " offset " + offset;
        String[] condition = new String[] {
                ETransType.DIRJEN_PAJAK.toString(),
                ETransType.DIRJEN_BEA_CUKAI.toString(),
                ETransType.DIRJEN_ANGGARAN.toString(),
                transData.getBillingId(),
                transData.getNtb()
        };

        List<TransData> listTrans = TransData.readTransMpn(1, 0, condition);
        if (listTrans.size() < 1) {
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        origTransData = listTrans.get(0);

        copyOrigTransData();

        gotoState(State.ONLINE_NORMAL.toString());
    }

    void afterInputData2New(ActionResult result){
        String[] data = (String[]) result.getData();
        transData.setBillingId(data[0]);
        transData.setNtb(data[1]);


        //String sql = "select * from trans where transtype in (?,?,?) and billingId = ? and refNo = ? limit " + total + " offset " + offset;
        /*String[] condition = new String[] {
                ETransType.DIRJEN_PAJAK.toString(),
                ETransType.DIRJEN_BEA_CUKAI.toString(),
                ETransType.DIRJEN_ANGGARAN.toString(),
                transData.getBillingId(),
                transData.getNtb()
        };

        List<TransData> listTrans = TransData.readTransMpn(1, 0, condition);
        if (listTrans.size() < 1) {
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        origTransData = listTrans.get(0);

        copyOrigTransData();*/

        gotoState(State.ONLINE_NORMAL.toString());
    }



    private boolean isSaleTrans(ETransType transType) {
        if (ETransType.RECURRING_VOID == this.transType) {
            if (ETransType.RECURRING_SALE != transType) {
                return false;
            }

        } else if (ETransType.MOTO_VOID == this.transType) {
            if (ETransType.MOTO_SALE != transType) {
                return false;
            }
        } else {
            if (transType != ETransType.SALE
                    && transType != ETransType.COUPON_SALE
                    && transType != ETransType.EMV_QR_SALE
                    && transType != ETransType.DANA_QR_SALE
                    && transType != ETransType.DIRJEN_PAJAK
                    && transType != ETransType.DIRJEN_BEA_CUKAI
                    && transType != ETransType.DIRJEN_ANGGARAN
            ) {
                return false;
            }
        }
        return true;
    }

    // 检查原交易信息
    protected void validateOrigTransData(long origTransNo) {
        origTransData = TransData.readTrans(origTransNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        ETransType transType = ETransType.valueOf(origTransData.getTransType());

        // 非消费交易不能撤销
        if (!isSaleTrans(transType)) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        // 脱机消费交易不能撤销
        if (!origTransData.getIsOnlineTrans()) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        copyOrigTransData();
        gotoState(State.ONLINE_NORMAL.toString());
    }

    protected void copyOrigTransData() {

        transData.setOrigDateTimeTrans(origTransData.getDateTimeTrans());

        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());

        transData.setNtb(origTransData.getRefNo());
        transData.setTrack2(origTransData.getTrack2());
        transData.setEnterMode(origTransData.getEnterMode());
//        transData.setSendIccData(origTransData.getSendIccData());
        transData.setSettleDate(origTransData.getSettleDate());
        transData.setPin(origTransData.getPin());
        transData.setBillingId(origTransData.getBillingId());
        transData.setReprintData(origTransData.getReprintData());
        transData.setFeeTotalAmount(origTransData.getFeeTotalAmount());
        transData.setSendIccData(origTransData.getSendIccData()); //fase 2

    }

    private void afterEnterTransNo(ActionResult result) {
        String content = (String) result.getData();
        transData.setPrintTimeout("n");
        long transNo = 0;
        if (content == null) {
            transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
            return;
            /*TransData transData = TransData.readLastTrans();
            if (transData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, transData));
                return;
            }
            transNo = transData.getTransNo();*/
        } else {
            transNo = Long.parseLong(content);
        }
        validateOrigTransData(transNo);
    }


    private void afterOnline(ActionResult result) {
        //gak dipake, spek fase 2
        /*try {
            if (transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69")) {
                transData.setPrintTimeout("y");
                gotoState(State.PRINT_TICKET.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (!transData.getResponseCode().equals("00")) {
            transEnd(result);
            return;
        }

        transData.setReprintData(transData.getField48());

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

//        transData.saveTrans();

        gotoState(State.PRINT_TICKET.toString());
    }
}
