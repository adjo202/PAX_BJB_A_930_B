package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

/**
 * sandy@indopay.com
 * 2019-06-28
 */

public class ActionQRGenerate extends AAction {



    public static final String TAG = "ActionQRGenerate";

    private TransData mTransData;

    public static int GENERATE = 0;
    public static int INQUIRY  = 1;

    int mode;

    public ActionQRGenerate(TransData data) {
        this(data, null);
        this.mode = GENERATE;
    }

    public ActionQRGenerate(TransData data, int mode) {
        this(data, null);
        this.mode = mode;
    }

    public ActionQRGenerate(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;
    }



    @Override
    protected void process() {
        new QRGenerateTask().execute();
    }



    class QRGenerateTask extends AsyncTask<Void, Void, Integer> {

        private Context mContext = TransContext.getInstance().getCurrentContext();
        private TransProcessListener mProcessListener;

        @Override
        protected void onPreExecute() {
            if (mProcessListener == null) {
                mProcessListener = new TransProcessListenerImpl(mContext);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            int result = -1;
            if(mode == GENERATE)
                result = createQR(mProcessListener);
            else if(mode == INQUIRY)
                result = inquiry(mProcessListener);


            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (mProcessListener != null) {
                mProcessListener.onHideProgress();
            }
            mProcessListener = null;
            mContext = null;
            setResult(new ActionResult(result, null));
        }
    }

    private int createQR(TransProcessListener listener) {

        int result = Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        if (result != TransResult.SUCC) {
            return result;
        }
        return result;
    }

    private int inquiry(TransProcessListener listener) {
        mTransData.setTransType(ETransType.QR_INQUIRY.toString());
        int result = Transmit.getInstance().transmit(mTransData, false, false, false, listener);
        if (result != TransResult.SUCC) {
            //listener.onShowErrMessageWithConfirm(mTransData.getField59(),10000);
            listener.onShowNormalMessageWithConfirm(mTransData.getField59(),10000);
            return result;
        }

        return result;
    }

}
