package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

public class ActionQrisSale extends AAction {

    public static final String TAG = "ActionDanaSale";
    private TransData mTransData;

    public ActionQrisSale(TransData data) {
        this(data, null);
    }

    public ActionQrisSale(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;
    }


    @Override
    protected void process() {
        //Log.d(TAG,"sandy.Coupon.process");
        new QRSaleTask().execute();
    }

    class QRSaleTask extends AsyncTask<Void, Void, Integer> {

        private Context mContext = TransContext.getInstance().getCurrentContext();
        private TransProcessListener mProcessListener;

        @Override
        protected void onPreExecute() {
            Log.d(TAG,"sandy.Coupon.onPreExecute");
            if (mProcessListener == null) {
                mProcessListener = new TransProcessListenerImpl(mContext);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG,"sandy.Dana.doInBackground");
            int result = danaSale(mProcessListener);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG,"Dana.onPostExecute");
            if (mProcessListener != null) {
                mProcessListener.onHideProgress();
            }
            mProcessListener = null;
            mContext = null;
            setResult(new ActionResult(result, null));
        }
    }

    private int danaSale(TransProcessListener listener) {
        Log.d(TAG,"sandy.danaSale");

        int result = Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        if (result != TransResult.SUCC) {
            return result;
        }

        mTransData.setPan("123456789012345679"); //set it for temporary
        mTransData.saveTrans();
        return result;
    }






}
