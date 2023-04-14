/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-7-27
 * Module Author: wangyq
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.DeviceImplNeptune;
import com.pax.eemv.entity.AidParam;
import com.pax.eemv.entity.Capk;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.device.DeviceManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.emv.EmvCapk;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;

import java.util.List;


public class ActionClssPreProc extends AAction {
    private TransData transData;
    private static final String TAG = "ActionClssPreProc";
    /**
     * 子类构造方法必须调用super设置ActionStartListener
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionClssPreProc(ActionStartListener listener) {
        super(listener);
    }

    public ActionClssPreProc(TransData transData, ActionStartListener listener) {
        super(listener);
        this.transData = transData;
    }

    public void setParam( TransData transData) {
        this.transData = transData;
    }
    @Override
    protected void process() {
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FinancialApplication.getClss().init();
                    FinancialApplication.getClss().setConfig(ClssTransProcess.genClssConfig());
                    FinancialApplication.getClss().setAidParamList(EmvAid.toAidParams());
                    FinancialApplication.getClss().setCapkList(EmvCapk.toCapk());
                    String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
                    if(SysParam.Constant.YES.equals(isIndopayMode))
                        transData.setAmount(transData.getAmount().replace(",", ""));

                    Log.d(TAG, "Sandy.ActionClssPreProc=" +  transData.getAmount());

                    /**
                    List<Capk> capkarr = EmvCapk.toCapk();
                    List<AidParam> aidarr = EmvAid.toAidParams();
                    for(int i = 0;i<capkarr.size();i++){
                        Capk c = (Capk) capkarr.get(i);
                        Log.d(TAG, "Sandy=Mod:" +  FinancialApplication.getConvert().bcdToStr(c.getModul()) );
                        //Log.d(TAG, "Sandy=" +  FinancialApplication.getConvert().bcdToStr(c.getExponent()) );

                    }
                     **/
                    //Log.d(TAG, "Sandy=aidarr.size:" +aidarr.size());
                    /**
                    for(int i = 0;i<aidarr.size();i++){
                        AidParam aid = (AidParam) aidarr.get(i);

                        Log.d(TAG, "Sandy=AID:" +  FinancialApplication.getConvert().bcdToStr(aid.getAid()) );

                    }**/


                    //sandy
                    //error goes here
                    FinancialApplication.getClss().preTransaction(Component.toClssInputParam(transData));
                    setResult(new ActionResult(TransResult.SUCC, null));
                } catch (EmvException e) {
                    Log.e(TAG, "", e);
                    setResult(new ActionResult(TransResult.ERR_CLSS_PRE_PROC, null));
                }
            }
        }).start();
    }
}
