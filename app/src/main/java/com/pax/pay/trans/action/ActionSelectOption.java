package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.SelectOptionActivity;

import java.util.ArrayList;

public class ActionSelectOption extends AAction {

    private Handler handler;
    private Context context;
    private String title;
    private String subTitle;

    private ArrayList<String> nameList;

    public ActionSelectOption(ActionStartListener listener) {
        super(listener);
    }

    public ActionSelectOption(Handler handler, ActionStartListener listener) {
        super(listener);
        this.handler = handler;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setNameList(ArrayList<String> nameList) {
        this.nameList = nameList;
    }

    public void setParam(Context context, Handler handler, String title, String subTitle, ArrayList<String> list) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.subTitle = subTitle;
        this.nameList = list;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new SelectionOptionRunnable());
    }

    class SelectionOptionRunnable implements Runnable {
        @Override
        public void run() {
            Intent intent = new Intent(context, SelectOptionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), subTitle);
            bundle.putStringArrayList(EUIParamKeys.CONTENT.toString(), nameList);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}
