package com.pax.pay.emv;

import android.util.Log;

import com.pax.eemv.IEmvListener;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.Config;
import com.pax.eemv.entity.EcRecord;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;

import java.util.ArrayList;

public class EmvTransProcess {
    private static final String TAG = "EmvTransProcess";

    private static EmvTransProcess emvTransProcess;

    private EmvTransProcess() {

    }

    public static synchronized EmvTransProcess getInstance() {
        if (emvTransProcess == null) {
            emvTransProcess = new EmvTransProcess();
        }
        return emvTransProcess;
    }


    public CTransResult transProcess(TransData transData, IEmvListener listener)
            throws EmvException {
        Log.i(TAG, "EMV PROC 1 :" );

        CTransResult result = null;
        FinancialApplication.getEmv().setListener(listener);
        result = FinancialApplication.getEmv().process(Component.toInputParam(transData));
        // Restore transaction type

        Log.i(TAG, "EMV PROC 2:" + result.toString());
        return result;
    }

    //kd add 01042021
    public String transProcessGetPanOnly(TransData transData, IEmvListener listener)
            throws EmvException {
        Log.i(TAG, "EMV PROC 1 :" );

        String result = null;
        FinancialApplication.getEmv().setListener(listener);
        result = FinancialApplication.getEmv().processGetPanOnly(Component.toInputParam(transData));
        // Restore transaction type

        Log.i(TAG, "EMV PROC 2:" + result.toString());
        return result;
    }                                                                                                                                                                            

    public long getEcBalance(IEmvListener listener, TransData transData)
            throws EmvException {
        FinancialApplication.getEmv().setListener(listener);
        return FinancialApplication.getEmv().getEcBalance(transData.getTransNo());
    }

    public ArrayList<EcRecord> getAllLogRecord(IEmvListener listener) throws EmvException {
        FinancialApplication.getEmv().setListener(listener);
        return FinancialApplication.getEmv().readAllLoadLogs();
    }

    /**
     * EMV初始化，设置aid，capk和emv配置
     */
    public void init() {
        Log.i(TAG, "Sandy.EMVTransProcess.init is called!");
        try {
            FinancialApplication.getEmv().init();
            FinancialApplication.getEmv().setConfig(genEmvConfig());

        } catch (EmvException e) {
            AppLog.e(TAG, "", e);
        }
        FinancialApplication.getEmv().setAidParamList(EmvAid.toAidParams()); //设置AID参数到内存列表中
        FinancialApplication.getEmv().setCapkList(EmvCapk.toCapk()); //设置CAPK参数到内存列表中
    }

    private static Config genEmvConfig() {
        Log.d(TAG, "Sandy.EMVTransProcess.genEmvConfig is called!");
        Config cfg = Component.genCommonEmvConfig();
        cfg.setCapability("E0E1C8");
        cfg.setExCapability("E000F0A001");
        cfg.setTransType((byte) 0x02);
        return cfg;
    }

}
