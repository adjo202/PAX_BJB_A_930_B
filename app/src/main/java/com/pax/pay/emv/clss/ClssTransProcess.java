/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-28
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv.clss;

import android.util.Log;

import com.pax.eemv.IClss;
import com.pax.eemv.IClssListener;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.Config;
import com.pax.eemv.entity.EcRecord;
import com.pax.eemv.entity.TagsTable;
import com.pax.eemv.enums.EKernelType;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EmvException;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.TlvException;
import com.pax.jemv.emv.api.EMVApi;
import com.pax.jemv.emv.model.EmvParam;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class ClssTransProcess {

    private static final String TAG = "ClssTransProcess";

    private IClss clss;

    public ClssTransProcess(IClss clss) {
        this.clss = clss;
    }

    public static Config genClssConfig() {
        Config cfg = Component.genCommonEmvConfig();
        cfg.setCapability("E0E1C8");
        cfg.setExCapability("E000F0A001");
        cfg.setTransType((byte) 0);
        cfg.setUnpredictableNumberRange("0060");
        cfg.setSupportOptTrans(true);
        cfg.setTransCap("D8B04000");
        cfg.setDelayAuthFlag(false);
        return cfg;
    }

    public CTransResult transProcess(TransData transData, IClssListener listener) throws
            EmvException {
        clss.setListener(listener);
        CTransResult result = clss.process(Component.toInputParam(transData));

        // 将交易类型恢复
        Log.i(TAG, "clss PROC:" + result.toString());
        return result;
    }

    public long getEcBalance(IClssListener listener, TransData transData) throws EmvException {
        clss.setListener(listener);
        return clss.getEcBalance(transData.getTransNo());
    }

    public ArrayList<EcRecord> getAllLogRecord(IClssListener listener) throws EmvException {
        clss.setListener(listener);
        return clss.readAllLoadLogs();
    }

    public static void clssTransResultProcess(CTransResult result, IClss clss, TransData
            transData) {
        updateEmvInfo(clss, transData);
        List<ClssDE55Tag> clssDE55TagList = ClssDE55Tag.genClssDE55Tags();

        //FIXME
        if (result.getTransResult() == ETransResult.CLSS_OC_ONLINE_REQUEST) {
            try {
                clss.setTlv(TagsTable.CRYPTO, Utils.str2Bcd("80"));
            } catch (EmvException e) {
                Log.w(TAG, "", e);
                transData.setEmvResult((byte) ETransResult.ABORT_TERMINATED.ordinal());
                return;
            }

            // prepare online DE55 data
            if (setStdDe55(clss, result, transData, clssDE55TagList) != 0) {
                transData.setEmvResult((byte) ETransResult.ABORT_TERMINATED.ordinal());
            }
        } else if (result.getTransResult() == ETransResult.CLSS_OC_APPROVED) {
            // save for upload
            setStdDe55(clss, result, transData, clssDE55TagList);
            transData.setSendFailFlag(TransData.OfflineStatus.OFFLINE_NOT_SENT);
            transData.setTransType(ETransType.EC_SALE.toString());

            //ECBalance
            byte[] value = clss.getTlv(TagsTable.EC_BALANCE);
            if (value != null && value.length >= 15) {
                String balance = FinancialApplication.getConvert().bcdToStr(value);
                transData.setBalance(balance.substring(20, 30));
            }

            transData.saveTrans();

            // increase trans no.
            Component.incTransNo();
        }
    }

    private static void updateEmvInfo(IClss clss, TransData transData) {
        //AppLabel
        byte[] value = clss.getTlv(TagsTable.APP_LABEL);
        if (value != null) {
            transData.setEmvAppLabel(new String(value));
        }

        //TVR
        value = clss.getTlv(TagsTable.TVR);
        if (value != null) {
            transData.setTvr(Utils.bcd2Str(value));
        }

        //TSI
        value = clss.getTlv(TagsTable.TSI);
        if (value != null) {
            transData.setTsi(Utils.bcd2Str(value));
        }

        //ATC
        value = clss.getTlv(TagsTable.ATC);
        if (value != null) {
            transData.setAtc(Utils.bcd2Str(value));
        }

        //AppCrypto
        value = clss.getTlv(TagsTable.APP_CRYPTO);
        if (value != null) {
            transData.setArqc(Utils.bcd2Str(value));
        }

        //AppName
        value = clss.getTlv(TagsTable.APP_NAME);
        if (value != null) {
            transData.setEmvAppName(Utils.bcd2Str(value));
        }

        //AID
        value = clss.getTlv(TagsTable.CAPK_RID);
        if (value != null) {
            transData.setAid(Utils.bcd2Str(value));
        }

        //TC
        value = clss.getTlv(TagsTable.APP_CRYPTO);
        if (value != null) {
            transData.setTc(Utils.bcd2Str(value));
        }
    }

    // set ADVT/TIP bit 55
    private static int setStdDe55(IClss clss, CTransResult result, TransData transData,
                                  List<ClssDE55Tag> clssDE55TagList) {
        ITlv tlv = FinancialApplication.getPacker().getTlv();
        ITlv.ITlvDataObjList list = tlv.createTlvDataObjectList();

        for (ClssDE55Tag i : clssDE55TagList) {
            ITlv.ITlvDataObj tag = tlv.createTlvDataObject();
            if (0x9F33 == i.getEmvTag()) {
                EmvParam emvParam = new EmvParam();
                EMVApi.EMVGetParameter(emvParam);
                tag.setTag(i.getEmvTag());
                tag.setValue(emvParam.capability);
            } else {
                tag.setTag(i.getEmvTag());
                tag.setValue(clss.getTlv(i.getEmvTag()));
            }
            list.addDataObj(tag);
        }

        if (clss.getKernelType() == EKernelType.MC) {
            ITlv.ITlvDataObj tag = tlv.createTlvDataObject();
            tag.setTag(0x9F53);
            tag.setValue(Utils.str2Bcd("82"));
            list.addDataObj(tag);

            if (result.getTransResult() == ETransResult.CLSS_OC_APPROVED) {
                tag.setTag(0x91);
                tag.setValue(clss.getTlv(0x91));
                list.addDataObj(tag);
            }
        }

        byte[] f55Data;
        try {
            f55Data = tlv.pack(list);
        } catch (TlvException e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_PACK;
        }

        if (f55Data.length > 255) {
            return TransResult.ERR_BAG;
        }
        transData.setSendIccData(Utils.bcd2Str(f55Data));
        return TransResult.SUCC;
    }
}
