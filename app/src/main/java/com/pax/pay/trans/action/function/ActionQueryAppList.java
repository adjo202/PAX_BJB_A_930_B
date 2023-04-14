package com.pax.pay.trans.action.function;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.menu.QueryAppListActivity;
import com.pax.pay.trans.TransResult;
import com.pax.up.bjb.R;
import com.pax.view.dialog.DialogUtils;

import java.util.List;

/**
 * Created by yangsh on 2017/4/26.
 */

public class ActionQueryAppList extends AAction {

    private Context mContext;
    private Handler mHandler;
    /**
     * 子类构造方法必须调用super设置ActionStartListener
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionQueryAppList(Context context, ActionStartListener listener) {
        super(listener);
        mContext = context;
        mHandler = new Handler();
    }

    @Override
    protected void process() {
        List<EmvAid> emvAidList = EmvAid.readAllAid();
        if (emvAidList != null) {
            Intent intent = new Intent(mContext, QueryAppListActivity.class);
            mContext.startActivity(intent);
        } else {
            DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    setResult(new ActionResult(TransResult.ERR_NO_APP, null));
                }
            };
            DialogUtils.showErrMessage(mContext, mHandler, mContext.getString(R.string.settings_title),
                    mContext.getString(R.string.no_app), dismissListener, Constants.FAILED_DIALOG_SHOW_TIME);
        }
    }
}
