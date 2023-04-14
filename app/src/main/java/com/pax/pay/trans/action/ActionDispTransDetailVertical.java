package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.DetailsActivity;
import com.pax.pay.record.DetailsNewActivity;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.DispTransDetailNewActivity;
import com.pax.pay.trans.action.activity.DispTransDetailUsing2ButtonActivity;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ActionDispTransDetailVertical extends AAction {
    private Context context;
    private Handler handler;

    private Map<String, String> map;
    private String title;
    private String cancelationContent;


    private int inletType =0;
    private TransData transData = null;
    private boolean supportDoTrans =false;
    private boolean enable =false;


    public ActionDispTransDetailVertical(ActionStartListener listener) {
        super(listener);
    }

    public ActionDispTransDetailVertical(Handler handler, String title) {
        super(null);
        this.handler = handler;
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInletType(int inletType) {
        this.inletType = inletType;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    public void setSupportDoTrans(boolean supportDoTrans) {
        this.supportDoTrans = supportDoTrans;
    }

    public void setCancelationContent(String content){
        this.cancelationContent = content;
    }

    /**
     * 参数设置
     * 
     * @param context
     *            ：应用上下文
     * @param handler
     *            ：handler
     * @param title
     *            ：抬头
     * @param map
     *            ：确认信息
     */
    public void setParam(Context context, Handler handler, String title, Map<String, String> map) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.map = map;
    }

    /**
     * 参数设置
     *
     * @param context
     *            ：应用上下文
     * @param handler
     *            ：handler
     * @param title
     *            ：抬头
     * @param transData
     *          交易详情
     * @param supportDoTrans
     *         是否支持交易
     * @param  inletType   默认为0 ，1代表交易查询入口（进入交易详情页面）
     *         入口类型
     */
    public void setParam(Context context, Handler handler, String title, TransData transData,boolean supportDoTrans,int inletType) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.transData = transData;
        this.supportDoTrans =supportDoTrans;
        this.inletType = inletType;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        /*if (map == null) {
            map = prepareDispTransDetail();
        }*/
        handler.post(new DispTransRunnable());
    }

    class DispTransRunnable implements Runnable {
        @Override
        public void run() {

            if(inletType == 0) {
                ArrayList<String> leftColumns = new ArrayList<>();
                ArrayList<String> rightColumns = new ArrayList<>();

                Set<String> keys = map.keySet();
                for (String key : keys) {
                    leftColumns.add(key);
                    Object value = map.get(key);
                    if (value != null) {
                        rightColumns.add((String) value);
                    } else {
                        rightColumns.add("");
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);

                Intent intent = new Intent(context, DispTransDetailNewActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }else if(inletType == 1){

                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putSerializable(EUIParamKeys.CONTENT.toString(), transData);
                bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), supportDoTrans);
                bundle.putString(EUIParamKeys.OPTIONS.toString(), DetailsNewActivity.Options.ACTION.toString());

                intent.putExtras(bundle);
                context.startActivity(intent);
            }else if(inletType == 2){
                ArrayList<String> leftColumns = new ArrayList<>();
                ArrayList<String> rightColumns = new ArrayList<>();

                Set<String> keys = map.keySet();
                    for (String key : keys) {
                        leftColumns.add(key);
                        Object value = map.get(key);
                        if (value != null) {
                            rightColumns.add((String) value);
                        } else {
                            rightColumns.add("");
                        }
                    }

                String theCancelationContent = FinancialApplication.getSysParam().get(SysParam.BPJS_REGISTER_REQUEST_DATA);

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putString(EUIParamKeys.CONTENT.toString(), theCancelationContent);

                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);

                Intent intent = new Intent(context, DispTransDetailUsing2ButtonActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        }
    }
}
