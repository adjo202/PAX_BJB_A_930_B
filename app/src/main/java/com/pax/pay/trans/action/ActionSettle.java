package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SettleActivity;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.settings.SysParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionSettle extends AAction {

    private Context context;
    private Handler handler;
    private String title;
    private Map<String, Object> map;
    private TransTotal total;


    public ActionSettle(ActionStartListener listener) {
        super(listener);

    }

    public ActionSettle(Handler handler, ActionStartListener listener) {
        super(listener);
        this.handler = handler;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public void setTotal(TransTotal total) {
        this.total = total;
    }

    public void setParam(Context context, Handler handler, String title, Map<String, Object> map,
                         TransTotal total) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.map = map;
        this.total = total;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new SettleRunnable());

    }

    class SettleRunnable implements Runnable {
        @Override
        public void run() {



            // add abdul
            List<ETransType> list = new ArrayList<>();
            //add denny change pin, buka rekening, batal rekening tidak termasuk dalam summary transaksi
            //list.add(ETransType.CHANGE_PIN);
            //Sandy : add here if you want to display in a settlement transaction
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
            list.add(ETransType.PASCABAYAR_INQUIRY);
            list.add(ETransType.PDAM_INQUIRY);

            list.add(ETransType.BPJS_TK_PENDAFTARAN);
            list.add(ETransType.BPJS_TK_PEMBAYARAN);



            List<TransData> record = TransData.readTrans(list);
            List<TransData> details = new ArrayList<>();

            if (record == null) {
                ActionResult result = new ActionResult(TransResult.ERR_NO_TRANS, null);
                setResult(result);
                return;
            }

            Intent intent = new Intent(context, SettleActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            ArrayList<String> titles = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            Set<String> keys = map.keySet();
            for (String key : keys) {
                titles.add(key);
                values.add(map.get(key).toString());
            }

            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), titles);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), values);
            bundle.putSerializable(EUIParamKeys.CONTENT.toString(), total);

            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}
