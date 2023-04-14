package com.pax.pay.record;

import android.app.Activity;
import android.os.Handler;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintFailedTransDetail;
import com.pax.pay.trans.receipt.ReceiptPrintSettle;
import com.pax.pay.trans.receipt.ReceiptPrintTotal;
import com.pax.pay.trans.receipt.ReceiptPrintTrans;
import com.pax.pay.trans.receipt.ReceiptPrintTransDetail;
import com.pax.pay.utils.CollectionUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.List;

public class Printer {

    /**
     * 打印最后一笔交易记录
     * 
     * @param activity
     * @param handler
     */
    public static int printLastTrans(final Activity activity, final Handler handler) {
        TransData transData = TransData.readLastTrans();

        if (transData == null) {
            return TransResult.ERR_NO_TRANS;
        }

        transData.setOper(TransContext.getInstance().getOperID());

        ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
        receiptPrintTrans.print(transData, true, new PrintListenerImpl(activity, handler));

        return TransResult.SUCC;
    }

    /**
     * 打印交易明细
     * 
     * @param activity
     * @param handler
     */
    public static int printTransDetail(final String title, final Activity activity, final Handler handler) {
        // 交易查询
        List<ETransType> list = new ArrayList<>();
        list.add(ETransType.SALE);
        list.add(ETransType.QR_SALE);
        list.add(ETransType.AUTHCM);
        list.add(ETransType.AUTH_SETTLEMENT);
        list.add(ETransType.REFUND);
        list.add(ETransType.QR_REFUND);
        list.add(ETransType.EC_SALE);
        list.add(ETransType.OFFLINE_SETTLE);
        list.add(ETransType.SETTLE_ADJUST);
        list.add(ETransType.SETTLE_ADJUST_TIP);
        list.add(ETransType.MOTO_SALE);
        list.add(ETransType.MOTO_REFUND);
        list.add(ETransType.MOTO_AUTHCM);
        list.add(ETransType.MOTO_AUTH_SETTLEMENT);
        list.add(ETransType.RECURRING_SALE);
        list.add(ETransType.INSTAL_SALE);
        list.add(ETransType.COUPON_SALE);

        //add denny
        list.add(ETransType.CHANGE_PIN);
        list.add(ETransType.MINISTATEMENT);
        list.add(ETransType.PBB_PAY);
        list.add(ETransType.SETOR_TUNAI);
        list.add(ETransType.TARIK_TUNAI);
        list.add(ETransType.TARIK_TUNAI_2);

        list.add(ETransType.OVERBOOKING);
        list.add(ETransType.OVERBOOKING_2);

        list.add(ETransType.BALANCE_INQUIRY);
        list.add(ETransType.BALANCE_INQUIRY_2);

        list.add(ETransType.TRANSFER);
        list.add(ETransType.TRANSFER_2);

        list.add(ETransType.DIRJEN_PAJAK);
        list.add(ETransType.DIRJEN_BEA_CUKAI);
        list.add(ETransType.DIRJEN_ANGGARAN);
        list.add(ETransType.INQ_PULSA_DATA);

        List<TransData> record = TransData.readTrans(list);
        List<TransData> details = new ArrayList<>();

        if (record == null) {
            return TransResult.ERR_NO_TRANS;
        }

        for (TransData data : record) {
            // 离线结算先上送，后调整的交易不打印
            // 已被撤销的交易不打印(上送失败的脱机交易需打印)
            if (data.getTransType().equals(ETransType.OFFLINE_SETTLE.toString())
                    && data.getIsAdjustAfterUpload()
                    || data.getTransState().equals(ETransStatus.VOID.toString())) {
                continue;
            }

            details.add(data);
        }

        if (CollectionUtils.isEmpty(details)) {
            if (!CollectionUtils.isEmpty(record)) {
                return TransResult.ERR_NO_VALID_TRANS;
            }
            return TransResult.ERR_NO_TRANS;
        }

        ReceiptPrintTransDetail receiptPrintTransDetail = ReceiptPrintTransDetail.getInstance();
        receiptPrintTransDetail.print(title, details, new PrintListenerImpl(activity, handler));
        return TransResult.SUCC;
    }

    /**
     * 打印交易汇总
     * 
     * @param activity
     * @param handler
     */
    public static int printTransTotal(final Activity activity, final Handler handler, boolean settle) {
        TransTotal total = TransTotal.calcTotal();
        /*if (total.getTransTotalAmt() == 0) {
            return TransResult.ERR_NO_TRANS;
        }*/

        List<ETransType> list = new ArrayList<>();
        //add denny change pin, buka rekening, batal rekening tidak termasuk dalam summary transaksi
        //list.add(ETransType.CHANGE_PIN);
        list.add(ETransType.MINISTATEMENT);
        list.add(ETransType.PBB_PAY);
        list.add(ETransType.SETOR_TUNAI);
        list.add(ETransType.TARIK_TUNAI);
        list.add(ETransType.TARIK_TUNAI_2);

        list.add(ETransType.OVERBOOKING);
        list.add(ETransType.OVERBOOKING_2);

        list.add(ETransType.BALANCE_INQUIRY);
        list.add(ETransType.BALANCE_INQUIRY_2);

        list.add(ETransType.TRANSFER);
        list.add(ETransType.TRANSFER_2);

        list.add(ETransType.DIRJEN_PAJAK);
        list.add(ETransType.DIRJEN_BEA_CUKAI);
        list.add(ETransType.DIRJEN_ANGGARAN);
        list.add(ETransType.INQ_PULSA_DATA);
        list.add(ETransType.BPJS_TK_PENDAFTARAN);
        list.add(ETransType.BPJS_TK_PEMBAYARAN);


        List<TransData> record = TransData.readTrans(list);
        //List<TransData> details = new ArrayList<>();

        if (record == null) {
            return TransResult.ERR_NO_TRANS;
        }
        ReceiptPrintTotal.getInstance().print(activity.getString(R.string.trans_total_list), total,
                new PrintListenerImpl(activity, handler), settle);
        return TransResult.SUCC;
    }

    /**
     * 打印上批交易汇总
     */
    public static int printLastBatch(final Activity activity, final Handler handler) {
        TransTotal total = TransTotal.getLastBatchToatlNumAndAmount();
        if (total == null) {
            return TransResult.ERR_NO_TRANS;
        }
        ReceiptPrintTotal.getInstance().printLastSettle(activity.getString(R.string.trans_last_total_list), total,
                new PrintListenerImpl(activity, handler), true, true);
        return TransResult.SUCC;

    }

    /**
     * 打印上批交易结算
     * @param activity
     * @param handler
     * @return
     */
    public static int printLastSettlement(Activity activity, Handler handler) {
        TransTotal total = TransTotal.getLastBatchToatlNumAndAmount();
        if (total == null) {
            return TransResult.ERR_NO_TRANS;
        }
        printSettle(activity, handler, total);

        return TransResult.SUCC;
    }

    // 重打印
    public static void printTransAgain(final Activity activity, final Handler handler, final TransData transData) {
        ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
        receiptPrintTrans.print(transData, true, new PrintListenerImpl(activity, handler));

    }

    /**
     * 打印结算总计单
     */
    public static void printSettle(final Activity activity, final Handler handler, TransTotal total) {
        int rmbResult = FinancialApplication.getController().get(Controller.RMB_RESULT);
        int frnResult = FinancialApplication.getController().get(Controller.FRN_RESULT);
        String rmbResultMsg = null;
        String frnResultMsg = null;
        if (rmbResult == 1) {
            rmbResultMsg = activity.getString(R.string.print_inside_card_check);
        } else if (rmbResult == 2) {
            rmbResultMsg = activity.getString(R.string.print_inside_card_check_uneven);
        } else {
            rmbResultMsg = activity.getString(R.string.print_inside_card_check_err);
        }
        if (frnResult == 1) {
            frnResultMsg = activity.getString(R.string.print_outside_card_check);
        } else if (frnResult == 2) {
            frnResultMsg = activity.getString(R.string.print_outside_card_check_uneven);
        } else {
            frnResultMsg = activity.getString(R.string.print_outside_card_check_err);
        }

        ReceiptPrintSettle.getInstance().print(rmbResultMsg, frnResultMsg, total,
                new PrintListenerImpl(activity, handler));
    }

    /**
     * 打印脱机交易上送失败明细单
     */
    public static int printFailDetail(final Activity activity, final Handler handler) {
        // 交易查询
        List<ETransType> list = new ArrayList<>();
        list.add(ETransType.EC_SALE);
        list.add(ETransType.SALE);
        list.add(ETransType.OFFLINE_SETTLE);
        list.add(ETransType.SETTLE_ADJUST);
        list.add(ETransType.SETTLE_ADJUST_TIP);

        List<TransData> records = TransData.readTrans(list);
        List<TransData> details = new ArrayList<>();
        if (records == null) {
            return TransResult.ERR_NO_TRANS;
        }

        for (TransData record : records) {

            if (!record.getIsOffUploadState() && !record.getIsOnlineTrans()) {
                // 未成功上送的交易记录 以及 被平台拒绝的交易记录
                details.add(record);
            }
        }

        if (CollectionUtils.isEmpty(details)) {
            return TransResult.ERR_NO_TRANS;
        }

        ReceiptPrintFailedTransDetail.getInstance().print(details,
                new PrintListenerImpl(activity, handler));
        return TransResult.SUCC;
    }

}
