package com.pax.pay.trans;

import android.annotation.SuppressLint;
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
import com.pax.pay.trans.action.ActionVoidCoupon;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.up.bjb.R;


public class CekStatusPulsaDataTrans extends BaseTrans {

    private static final String TAG = "CekStatusPulsaDataTrans";
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

    public CekStatusPulsaDataTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.INQ_PULSA_DATA, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(transType);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(context.getString(R.string.trans_inq_pulsa_and_data));
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno), EInputType.NUM, 6, false);
        bind( State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler, context.getString(R.string.trans_inq_pulsa_and_data));
        bind( State.TRANS_DETAIL.toString(), confirmInfoAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind( State.ONLINE_NORMAL.toString(), transOnlineAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind( State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 撤销是否需要输入主管密码
        /*if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals( Constant.YES)) {
            gotoState( State.ENTER_TRANSNO.toString());
        } else if (isNeedInputTransNo) {// 需要输入流水号
            gotoState( State.ENTER_TRANSNO.toString());
        } else {// 不需要输入流水号
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Long.parseLong(origTransNo));
            } else { // 不需要读交易记录
                copyOrigTransData();
                checkCardAndPin();
            }
        }*/
        gotoState( State.ENTER_TRANSNO.toString());
    }

    enum State {
        ENTER_TRANSNO,
        TRANS_DETAIL,
        ONLINE_NORMAL,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        //fall back treatment
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        switch (state) {
            case ENTER_TRANSNO:
                afterEnterTransNo(result);
                break;
            case TRANS_DETAIL:
                afterTransDetail();
                break;
            case ONLINE_NORMAL: // 联机的后续处理
                /*if (!transData.getResponseCode().equals("00") || result.getRet() == TransResult.ERR_RECV) {
                    String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
                    if(Constant.YES.equals(isIndopayMode))
                        transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));
                    transData.setPrintTimeout("Y");
                    transData.updateTrans();
                    gotoState(State.PRINT_TICKET.toString());
                } else {
                    afterOnline();
                }*/
                afterOnline(result);
                break;
            case PRINT_TICKET:
            default:
                // 交易结束
                transEnd(result);
                break;
        }

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

        // cek stan transtype nya hanya inqpulsadata
        /*if (!isSaleTrans(transType)) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }*/

        transData.setTransType(transType.toString());

        copyOrigTransData();
        ActionDispTransDetail action = (ActionDispTransDetail)getAction( State.TRANS_DETAIL.toString()) ;
        action.setTransData(origTransData);
        gotoState( State.TRANS_DETAIL.toString());
    }

    // 设置原交易记录
    protected void copyOrigTransData() {
        //sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",origTransData.getAmount() ));
        else
            transData.setAmount(origTransData.getAmount());
        Log.d(TAG,"Sandy.entermode : " + origTransData.getEnterMode());
        Log.d(TAG,"Sandy.serialno  : " + origTransData.getCardSerialNo());

        //sandy added field 7 here
        transData.setOrigDateTimeTrans(origTransData.getDateTimeTrans());
        transData.setDateTimeTrans(origTransData.getDateTimeTrans());
        transData.setSellPrice(origTransData.getSellPrice());
        transData.setFeeTotalAmount(origTransData.getFeeTotalAmount());
        //sandy :  please check checkCardAndPin()
        //transData.setEnterMode(origTransData.getEnterMode());
        //transData.setCardSerialNo(origTransData.getCardSerialNo());
        String[] f47 = origTransData.getField47().split("#"); // di 47 ada data pulsa data
        transData.setField47(origTransData.getField47());
        transData.setField48(f47[0]);
        transData.setPhoneNo(f47[1]);
        transData.setProduct_code(f47[2]);
        transData.setTypeProduct(f47[3]);
        transData.setOperator(f47[4]);
        transData.setKeterangan(f47[5]);
        transData.setProduct_name(f47[6]);

        transData.setPrintTimeout(origTransData.getPrintTimeout());
        transData.setTransNo(origTransData.getTransNo());
        Log.d("teg", "trans no "+origTransData.getTransNo());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setActualPayAmount(origTransData.getActualPayAmount());
        transData.setDiscountAmount(origTransData.getDiscountAmount());
        transData.setCouponNo(origTransData.getCouponNo());
    }

    private void afterTransDetail() {
        ETransType transType = ETransType.valueOf(origTransData.getTransType());
        if (transType == ETransType.COUPON_SALE) {
            transData.setTransType(ETransType.COUPON_SALE_VOID.toString());
            transData.setEnterMode(EnterMode.MANUAL);
            transData.setPin("");
            transData.setHasPin(false);
        } else if (transType == ETransType.EMV_QR_SALE) {
            transData.setTransType(ETransType.EMV_QR_VOID.toString());
            transData.setEnterMode(EnterMode.QR);
            transData.setCardSerialNo(origTransData.getCardSerialNo());
            transData.setTrack2(origTransData.getTrack2());
        }else if (transType == ETransType.DANA_QR_SALE) {
            transData.setTransType(ETransType.DANA_QR_VOID.toString());
            transData.setEnterMode(EnterMode.QR);
            transData.setField62(origTransData.getCouponNo());
        }
        gotoState( State.ONLINE_NORMAL.toString());
    }

    private void afterEnterTransNo(ActionResult result) {
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
            transNo = Long.parseLong(content);
        }
        validateOrigTransData(transNo);
    }

    private void afterOnline(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            Log.d("teg", "trans no 2 "+transData.getTransNo());
            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));
            transData.setPrintTimeout("N");
            transData.setReprintData(transData.getField63());

            //Sandy : sampai disini.. 2022-11-22
            transData.setTransType(ETransType.INQ_PULSA_DATA.toString());

            transData.updateTrans();
            gotoState( State.PRINT_TICKET.toString());
        }else {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }
}
