/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-6-15
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.eemv.clss;

import android.util.Log;

import com.pax.eemv.EmvBase;
import com.pax.eemv.IClss;
import com.pax.eemv.IClssListener;
import com.pax.eemv.entity.AidParam;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.ClssInputParam;
import com.pax.eemv.entity.ClssTornLogRecord;
import com.pax.eemv.entity.EcRecord;
import com.pax.eemv.entity.InputParam;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.EFlowType;
import com.pax.eemv.enums.EKernelType;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.eemv.utils.Converter;
import com.pax.eemv.utils.Tools;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_PreProcInterInfo;
import com.pax.jemv.clcommon.Clss_TransParam;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.emv.api.EMVApi;
import com.pax.jemv.entrypoint.api.ClssEntryApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClssImpl extends EmvBase implements IClss {

    private static final String TAG = "ClssImpl";

    private KernType kernType;
    private List<Clss_PreProcInfo> preProcInfos = new ArrayList<>();
    private Clss_TransParam transParam;
    private ClssInputParam inputParam;
    private List<ClssTornLogRecord> tornLogRecords;

    private IClssListener listener;
    private ClssProc clssProc = null;

    static {
        System.loadLibrary("F_PUBLIC_LIB_Android");
        System.loadLibrary("F_ENTRY_LIB_Android");
        System.loadLibrary("JniEntry_V1.00.00_20170616");
    }

    public ClssImpl() {
        kernType = new KernType();
    }

    @Override
    public IClss getClss() {
        return this;
    }

    @Override
    public EKernelType getKernelType() {
        switch (kernType.kernType) {
            case KernType.KERNTYPE_VIS:
                return EKernelType.VIS;
            case KernType.KERNTYPE_MC:
                return EKernelType.MC;
            case KernType.KERNTYPE_AE:
                return EKernelType.AE;
            case KernType.KERNTYPE_JCB:
                return EKernelType.JCB;
            case KernType.KERNTYPE_ZIP:
                return EKernelType.ZIP;
            case KernType.KERNTYPE_EFT:
                return EKernelType.EFT;
            case KernType.KERNTYPE_FLASH:
                return EKernelType.FLASH;
            case KernType.KERNTYPE_PBOC:
                return EKernelType.PBOC;
            case KernType.KERNTYPE_DEF:
            default:
                return EKernelType.DEF;
        }
    }

    @Override
    public void init() throws EmvException {
        super.init();
        if (ClssEntryApi.Clss_CoreInit_Entry() != RetCode.EMV_OK) {
            throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
        }
    }

    private void addApp() {
        preProcInfos.clear();
        for (AidParam i : aidParamList) {
            ClssEntryApi.Clss_AddAidList_Entry(i.getAid(), (byte) i.getAid().length,
                    i.getSelFlag(), (byte) KernType.KERNTYPE_DEF);
            Clss_PreProcInfo clssPreProcInfo = Converter.genClssPreProcInfo(i, inputParam);
            preProcInfos.add(clssPreProcInfo);
            ClssEntryApi.Clss_SetPreProcInfo_Entry(clssPreProcInfo);
            Log.i("clssPreProcInfo", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            Log.i("clssPreProcInfo", " " + Tools.bcd2Str(clssPreProcInfo.aucAID));
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ulTermFLmt);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ucTermFLmtFlg);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ulRdClssTxnLmt);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ucRdClssTxnLmtFlg);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ulRdCVMLmt);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ucRdCVMLmtFlg);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ulRdClssFLmt);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ucRdClssFLmtFlg);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ulRdCVMLmt);
            Log.i("clssPreProcInfo", " " + clssPreProcInfo.ucRdCVMLmtFlg);
            Log.i("clssPreProcInfo", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    @Override
    public void preTransaction(ClssInputParam inputParam) throws EmvException {
        this.inputParam = inputParam;
        addApp();

        long ulAmtAuth = Long.parseLong(inputParam.getAmount());
        Log.i(TAG, "amount = " + ulAmtAuth);
        String date = inputParam.getTransDate();
        String time = inputParam.getTransTime();
        transParam = new Clss_TransParam(ulAmtAuth, 0,
                Integer.parseInt(inputParam.getTransTraceNo()),
                inputParam.getTag9CValue(), Tools.str2Bcd(date.substring(2)), Tools.str2Bcd(time));

        int ret = ClssEntryApi.Clss_PreTransProc_Entry(transParam);
        Log.d(TAG, "Sandy=" + ret);
        //sandy, disable for error
       // if (ret != RetCode.EMV_OK) {
      //      throw new EmvException(ret);
       // }
    }

    @Override
    public CTransResult process(InputParam inputParam) throws EmvException {
        int ret = ClssEntryApi.Clss_SetMCVersion_Entry((byte) 0x03);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        ret = ClssEntryApi.Clss_AppSlt_Entry(0, 0);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        CTransResult result = startTransaction();
        if (result.getTransResult() == ETransResult.CLSS_OC_TRY_AGAIN) {
            return result;
        }

        updateCardInfo();

        if (result.getTransResult() != ETransResult.CLSS_OC_ONLINE_REQUEST) {
            return result;
        }


        if(inputParam.getFlowType() == EFlowType.SIMPLE){
            return new CTransResult(ETransResult.ARQC);
        }
        cvmResult(result.getCvmResult());

        ETransResult transResult = onlineProc(result);
        if (transResult != ETransResult.ONLINE_APPROVED)
            throw new EmvException(EEmvExceptions.EMV_ERR_ONLINE_TRANS_ABORT);

        result.setTransResult(completeTransaction(transResult).getTransResult());
        return result;
    }

    private void updateCardInfo() throws EmvException {
        if (clssProc != null) {
            clssProc.updateCardInfo();
            return;
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
    }

    private CTransResult startTransaction() throws EmvException {
        while (true) {
            ByteArray daArray = new ByteArray();
            if (continueSelectKernel(daArray))
                continue;

            Clss_PreProcInterInfo clssPreProcInterInfo = new Clss_PreProcInterInfo();
            int ret = ClssEntryApi.Clss_GetPreProcInterFlg_Entry(clssPreProcInterInfo);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            ByteArray finalSelectData = new ByteArray();
            ret = ClssEntryApi.Clss_GetFinalSelectData_Entry(finalSelectData);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            AidParam aid = selectApp(daArray);
            if (aid == null) {
                throw new EmvException(EEmvExceptions.EMV_ERR_NO_APP);
            }

            cfg.setAcquirerId(aid.getAcquirerId());

            try {
                clssProc = ClssProc.generate(kernType.kernType, listener)
                        .setAid(aid)
                        .setCapkList(capkList)
                        .setFinalSelectData(finalSelectData.data, finalSelectData.length)
                        .setTransParam(transParam)
                        .setConfig(cfg)
                        .setInputParam(inputParam)
                        .setPreProcInfo(preProcInfos.toArray(new Clss_PreProcInfo[0]))
                        .setPreProcInterInfo(clssPreProcInterInfo)
                        .setTornLogRecord(tornLogRecords);

                return clssProc.processTrans();
            } catch (EmvException e) {
                Log.w(TAG, "", e);
                if (e.getErrCode() != RetCode.CLSS_RESELECT_APP) {
                    throw e;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "", e);
                throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
            }

            //to re-select app
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }
    }

    private boolean continueSelectKernel(ByteArray daArray) throws EmvException {
        kernType = new KernType();

        int ret = ClssEntryApi.Clss_FinalSelect_Entry(kernType, daArray);// output parameter?
        Log.i("clssEntryFinalSelect", "ret = " + ret + ", Kernel Type = " + kernType.kernType);
        if (ret == RetCode.EMV_RSP_ERR || ret == RetCode.EMV_APP_BLOCK
                || ret == RetCode.ICC_BLOCK || ret == RetCode.CLSS_RESELECT_APP) {
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                // 候选列表为空，进行相应错误处理，退出
                throw new EmvException(ret);
            }
            return true;
        } else if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        return false;
    }

    private AidParam selectApp(ByteArray daArray) {
        AidParam aid = null;
        String da = Tools.bcd2Str(daArray.data, daArray.length);
        for (AidParam i : aidParamList) {
            if (da.contains(Tools.bcd2Str(i.getAid()))) {
                aid = i;
                break;
            }
        }

        return aid;
    }

    private CTransResult completeTransaction(ETransResult transResult) throws EmvException {
        if (!need2ndTap())
            return new CTransResult(ETransResult.ONLINE_APPROVED);

        if (detect2ndTap()) {
            if (clssProc != null) {
                return clssProc.completeTrans(transResult, getTlv(0x91), getTlv(0x71), getTlv(0x72));
            }
            throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
        }
        return new CTransResult(ETransResult.ABORT_TERMINATED);
    }

    private boolean detect2ndTap() throws EmvException {
        if (listener != null) {
            return listener.onDetect2ndTap();
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    @Override
    public byte[] getTlvSub(int tag) {
        if (clssProc != null) {
            ByteArray value = new ByteArray();
            if (clssProc.getTlv(tag, value) == RetCode.EMV_OK) {
                return Arrays.copyOf(value.data, value.length);
            }
        }
        return null;
    }

    @Override
    public void setTlvSub(int tag, byte[] value) throws EmvException {
        if (clssProc != null) {
            clssProc.setTlv(tag, value);
            return;
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
    }

    @Override
    public void setTornLogRecords(List<ClssTornLogRecord> tornLogRecords) {
        this.tornLogRecords = tornLogRecords;
    }

    @Override
    public List<ClssTornLogRecord> getTornLogRecords() {
        return tornLogRecords;
    }

    @Override
    public void setListener(IClssListener listener) {
        this.listener = listener;
    }

    @Override
    public String getVersion() {
        return "1.00.00";
    }

    @Override
    public long getEcBalance(long transNo) throws EmvException {

        int ret = preEcProcess();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        long amount = EMVApi.EMVGetCardECBalance();
        if (amount < RetCode.EMV_OK) {
            throw new EmvException((int) amount);
        }
        return amount;

    }

    @Override
    public ArrayList<EcRecord> readAllLoadLogs() throws EmvException {

        int ret = preEcProcess();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        int Reco = 1;
        ArrayList<EcRecord> ecRecords = new ArrayList<EcRecord>();
        while (true) {
            ret = EMVApi.ReadLogRecord(Reco);
            if (ret == RetCode.RECORD_NOTEXIST) {
                if (Reco == 1) {
                    throw new EmvException(ret);
                } else {
                    break;
                }
            } else if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
            EcRecord recorder = getLogData();
            if (recorder != null) {
                ecRecords.add(recorder);
            }
            Reco++;
        }
        return ecRecords;
    }

    int preEcProcess() throws EmvException {
        int ret = ClssEntryApi.Clss_AppSlt_Entry(0, 1);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        while (true) {
            ByteArray daArray = new ByteArray();
            if (continueSelectKernel(daArray))
                continue;

            ByteArray finalSelectData = new ByteArray();
            ret = ClssEntryApi.Clss_GetFinalSelectData_Entry(finalSelectData);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            ret = EMVApi.EMVSwitchClss(transParam, finalSelectData.data, finalSelectData.length, new ByteArray().data, 0);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
            return ret;
        }
    }


    EcRecord getLogData() {
        ByteArray value = new ByteArray();
        EcRecord recorder = new EcRecord();
        EMVApi.GetLogItem((short) 0x9A, value);
        recorder.setDate(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9F21, value);
        recorder.setTime(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9F02, value);
        recorder.setAuthAmount(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9F03, value);
        recorder.setOtherAmount(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9F1A, value);
        recorder.setCountryCode(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x5F2a, value);
        recorder.setCurrencyCode(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9C, value);
        recorder.setTransType(Tools.bcd2Str(value.data, value.length));
        EMVApi.GetLogItem((short) 0x9F36, value);
        recorder.setAtc(Tools.bcd2Str(value.data, value.length));
        return recorder;
    }

    private boolean need2ndTap() {
        return (getKernelType() != EKernelType.MC
                && getTransPath() != TransactionPath.CLSS_VISA_MSD
                && getKernelType() != EKernelType.PBOC);
    }

    private int getTransPath() {
        if (clssProc != null) {
            return clssProc.getTransPath();
        }
        return TransactionPath.CLSS_PATH_NORMAL;
    }

    private void cvmResult(ECvmResult result) throws EmvException {
        if (listener == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
        }
        int ret = listener.onCvmResult(result);
        if (ret != EEmvExceptions.EMV_OK.getErrCodeFromBasement() &&
                ret != EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement()) {
            throw new EmvException(ret);
        }
    }

    private ETransResult onlineProc(CTransResult result) throws EmvException {
        if (listener == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
        }
        EOnlineResult ret = listener.onOnlineProc(result);
        if (ret == EOnlineResult.APPROVE) {
            return ETransResult.ONLINE_APPROVED;
        } else if (ret == EOnlineResult.ABORT) {
            return ETransResult.ABORT_TERMINATED;
        } else {
            return ETransResult.ONLINE_DENIED;
        }
    }
}
