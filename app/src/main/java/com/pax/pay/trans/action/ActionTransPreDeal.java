package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;

/**
 * 下载参数action
 * 
 * @author Steven.W
 * 
 */
public class ActionTransPreDeal extends AAction {

    public ActionTransPreDeal(ActionStartListener listener) {
        super(listener);

    }

    private Context context;
    private ETransType transType;

    /**
     * 设置action运行时参数
     * 
     * @param context
     * @param transType
     */
    public void setParam(Context context, ETransType transType) {
        this.context = context;
        this.transType = transType;
    }

    @Override
    protected void process() {
        PreDealTask preDealTask = new PreDealTask();
        preDealTask.execute();
    }

    class PreDealTask extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            int ret = Component.transPreDeal(context, transType);
            return ret;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            setResult(new ActionResult(integer, null));
        }
    }

}
