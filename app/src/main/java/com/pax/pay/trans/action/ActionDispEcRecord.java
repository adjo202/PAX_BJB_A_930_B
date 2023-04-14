package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.DispEcRecordActivity;

import java.util.ArrayList;

public class ActionDispEcRecord extends AAction {

    public ActionDispEcRecord(ActionStartListener listener) {
        super(listener);
    }

    private Handler handler;
    private Context context;
    private String title;
    private ArrayList<? extends Parcelable> list;

    /**
     * 参数设置
     * 
     * @param context
     *            ：应用上下文
     * @param handler
     *            ：handler
     * @param title
     *            ：抬头
     * @param list
     *            ：log内容
     */
    public void setParam(Context context, Handler handler, String title, ArrayList<? extends Parcelable> list) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.list = list;
    }

    @Override
    protected void process() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(context, DispEcRecordActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putParcelableArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), list);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

}
