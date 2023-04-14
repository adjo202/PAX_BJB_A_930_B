package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.DispTransScrollActivity;
import com.pax.pay.trans.model.TransData;

import java.util.ArrayList;

public class ActionDispTransArrayList extends AAction {
    private static final String TAG = "ActionDispTransDetail";
    private Context context;
    private Handler handler;

    private ArrayList<String> leftColumns;
    private ArrayList<String> rightColumns;
    private String title, noAcc;

    private int inletType =0;
    private TransData transData = null;
    private boolean supportDoTrans =false;

    public ActionDispTransArrayList(AAction.ActionStartListener listener) {
        super(listener);
    }

    public ActionDispTransArrayList(Handler handler, String title) {
        super(null);
        this.handler = handler;
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    public void setParam(Context context, Handler handler, String title, ArrayList<String> leftcolumn, ArrayList<String> rightcolumn, String noAcc) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.leftColumns = leftcolumn;
        this.rightColumns = rightcolumn;
        this.noAcc = noAcc;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();

        handler.post(new ActionDispTransArrayList.DispTransRunnable());
    }


    class DispTransRunnable implements Runnable {
        @Override
        public void run() {


            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);
            bundle.putString("noacc",noAcc);

            Intent intent = new Intent(context, DispTransScrollActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);

        }
    }
}



