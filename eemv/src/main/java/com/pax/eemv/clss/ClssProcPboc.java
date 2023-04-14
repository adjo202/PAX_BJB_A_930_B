package com.pax.eemv.clss;

import android.util.Log;

import com.pax.eemv.IClssListener;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.TagsTable;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EmvException;
import com.pax.eemv.utils.Converter;
import com.pax.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_ReaderParam;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.DDAFlag;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;
import com.pax.jemv.qpboc.api.ClssPbocApi;
import com.pax.jemv.qpboc.model.Clss_PbocAidParam;
import com.pax.jemv.qpboc.model.Clss_PbocTornConfig;

import java.util.Arrays;

import static com.pax.eemv.utils.Tools.bcd2Str;
import static com.pax.jemv.clcommon.RetCode.EMV_DATA_ERR;
import static com.pax.jemv.clcommon.RetCode.EMV_NOT_ACCEPT;

/**
 * Created by wangyq on 2017/7/28.
 */

public class ClssProcPboc extends ClssProc {

    private Clss_PreProcInfo clssPreProcInfo;
    private String track2;
    private boolean isOffline = false;
    private static final String TAG = "ClssProcPBOC";

    static {
        System.loadLibrary("JniQPBOC_V1.00.00_20170706");
        System.loadLibrary("F_QPBOC_LIB_Android");
    }

    ClssProcPboc(IClssListener listener) {
        super(listener);
    }


    void ClearExpiredTornLog() {
        ByteArray delTornFlag = new ByteArray();
        while (true) {
            int ret = ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 2, delTornFlag);
            if (ret == RetCode.EMV_NO_DATA && delTornFlag.data[0] != 1) {
                return;
            }
        }
    }

    private int exchangeClssCardCurrency() {

        if(cfg.getForceOnline()){
            return EMV_NOT_ACCEPT;
        }
        ApduSendL2 apduSendL2 = new ApduSendL2();
        ApduRespL2 apduRespL2 = new ApduRespL2();
        byte[] sendCommand = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x51};
        System.arraycopy(sendCommand, 0, apduSendL2.command, 0, sendCommand.length);
        apduSendL2.lc = 0;
        apduSendL2.le = 256;

        int ret = DeviceManager.getInstance().iccCommand(apduSendL2, apduRespL2);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        if (apduRespL2.swa != (byte) 0x90 || apduRespL2.swb != 0x00) {
            return EMV_DATA_ERR;
        }

        String currency = bcd2Str(apduRespL2.dataOut, apduRespL2.lenOut).substring(7, 10);
        Log.d(TAG, "dataOut " + currency);

        long amount = 0;
        if (listener != null) {
            amount = listener.exchangeRate(currency);
            if (amount == 0) {
                return EMV_DATA_ERR;
            }
        }
        Log.d(TAG, "amount " + amount);
        transParam.ulAmntAuth = amount;

        Clss_ReaderParam readerParam = Converter.toClssReaderParam(cfg);

        readerParam.aucTmCntrCode = Tools.str2Bcd(currency);
        readerParam.aucTmTransCur = Tools.str2Bcd(currency);
        readerParam.aucTmRefCurCode = Tools.str2Bcd(currency);
        ClssPbocApi.Clss_SetReaderParam_Pboc(readerParam);
        return 0;
    }

    private int init() {
        ByteArray version = new ByteArray(16);
        ClssPbocApi.Clss_ReadVerInfo_Pboc(version);
        Log.i(TAG, "pboc version " + new String(version.data));

        int ret = ClssPbocApi.Clss_CoreInit_Pboc();
        Log.i(TAG, "clssPBOCCoreInit ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ClssPbocApi.Clss_SetQUICSFlag_Pboc((byte) 2);
        Clss_PbocTornConfig pbocTornConfig = new Clss_PbocTornConfig(300, (short) 3, (short) 1, new byte[4]);
        ret = ClssPbocApi.Clss_TornSetConfig_Pboc(pbocTornConfig);
        Log.i(TAG, "Pboc_ornSetConfig ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ClearExpiredTornLog();

        ret = exchangeClssCardCurrency();
        if(ret != RetCode.EMV_OK) {
            ret = ClssPbocApi.Clss_SetReaderParam_Pboc(Converter.toClssReaderParam(cfg));
            Log.i(TAG, "PbocSetReaderParam ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                return ret;
            }
        }

        ret = ClssPbocApi.Clss_SetPbocAidParam_Pboc(new Clss_PbocAidParam(aid.getFloorLimit(), new byte[4]));
        Log.i(TAG, "PbocSetPbocAidParam ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ret = ClssPbocApi.Clss_SetFinalSelectData_Pboc(finalSelectData, finalSelectDataLen);
        Log.i(TAG, "PbocSetFinalSelectData ret = " + ret);
        if (ret != RetCode.EMV_OK) {

        }
        return ret;
    }

    private int setTransParam() {

        int ret = ClssPbocApi.Clss_SetTransData_Pboc(transParam, preProcInterInfo);
        Log.i(TAG, "PbocSetTransData ret = " + ret);
        if (ret != RetCode.EMV_OK) {

        }

        for (Clss_PreProcInfo i : arrayPreProcInfo) {
            if (Arrays.equals(aid.getAid(), i.aucAID)) {
                clssPreProcInfo = i;
                break;
            }
        }

        return ret;
    }

    private int wholeTornProcess(ACType acType) {
        byte[] tornBuff = new byte[4];
        int ret = ClssPbocApi.Clss_TornProcessing_Pboc((byte) 0, tornBuff);
        Log.i(TAG, "tornBuff: " + bcd2Str(tornBuff));
        if (ret != RetCode.EMV_OK) {
            Log.e(TAG, "Clss_TornProcessing_Pboc return " + ret);
            return ret;
        } else if (tornBuff[1] == 0) {
            ByteArray failFlag = new ByteArray(1);
            ret = ClssPbocApi.Clss_GetTornFailFlag_Pboc(failFlag);
            if (ret == RetCode.EMV_OK) {
                if (failFlag.data[0] == 2) {
                    ByteArray clearFlag = new ByteArray(1);
                    clearFlag.data[0] = 1;
                    while (clearFlag.data[0] == 1) {
                        ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 0, clearFlag);
                    }
                }
            }
            return RetCode.EMV_OK;
        } else if (tornBuff[1] == 1) {
            isOffline = true;
            return offlineTornProcess(acType);
        }
        return RetCode.EMV_OK;
    }

    private int offlineTornProcess(ACType acType) {
        int ret = 0;
        //according to EDC
        boolean isNeedSaveTorn = false;
        boolean isNeedDelTorn = false;
        ClssPbocApi.Clss_DelAllRevocList_Pboc();
        ClssPbocApi.Clss_DelAllCAPK_Pboc();
        addCapkRevList();

        DDAFlag flag = new DDAFlag();
        ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
        Log.i(TAG, "clssPbocCardAuth ret = " + ret + "acType = " + acType.type + "fddaFlag" + flag.flag);
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return ret;
            }
            ByteArray tornFailFlag = new ByteArray(1);
            int ret1 = ClssPbocApi.Clss_GetTornFailFlag_Pboc(tornFailFlag);
            if (ret1 == RetCode.EMV_OK) {
                if (tornFailFlag.data[0] == 2) { //Application should delete the fail torn log
                    isNeedDelTorn = true;
                } else if (tornFailFlag.data[0] == 1) {//There is a fail torn log deleted and the data of the deleted torn log are saved in the
                    isNeedSaveTorn = true;                            // TLV database.
                }
            }
            return ret;
        } else if (flag.flag == DDAFlag.FAIL) {
            return RetCode.CLSS_TERMINATE;
        }

        if (acType.type == ACType.AC_AAC) {
            isNeedSaveTorn = false;
        } else if (acType.type == ACType.AC_TC) {
            isNeedDelTorn = true;
        } else if (acType.type == ACType.AC_ARQC) {
            isNeedSaveTorn = false;
        }

        ByteArray clearFlag = new ByteArray(1);
        clearFlag.data[0] = 1;
        while (clearFlag.data[0] == 1) {
            if (isNeedSaveTorn) {
                ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 1, clearFlag);
            }
            if (isNeedDelTorn) {
                ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 0, clearFlag);
            }
        }

        return RetCode.EMV_OK;
    }

    @Override
    protected CTransResult processTrans() throws EmvException {
        int ret = init();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        ret = setTransParam();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        // TODO TornProcess
        ACType acType = new ACType();

        ret = wholeTornProcess(acType);
        if (isOffline) {
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            } else if (ret == RetCode.CLSS_TERMINATE) {
                new CTransResult(ETransResult.ABORT_TERMINATED);
            }
            if (acType.type == ACType.AC_TC) {
                return new CTransResult(ETransResult.CLSS_OC_APPROVED);
            } else if (acType.type == ACType.AC_AAC) {
                return new CTransResult(ETransResult.CLSS_OC_DECLINED);
            } else if (acType.type == ACType.AC_ARQC) {
                return new CTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
            }
        }

        ret = ClssPbocApi.Clss_Proctrans_Pboc(transactionPath, acType);
        Log.i(TAG, "clssPbocmaiProcTrans ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.CLSS_LAST_CMD_ERR) {
                // TODO current tornProcess
                throw new EmvException(ret);
            }
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            }
        }
        Log.i(TAG, "clssPbocTrans TransPath = " + transactionPath.path + ", ACType = " + acType.type);

        if (listener != null) {
            listener.onPromptRemoveCard();
        }

        CTransResult result = new CTransResult(ETransResult.ABORT_TERMINATED);
        if (!continueUpdateResult(acType, result)) {
            return result;
        }

        updateResult(result);
        Log.i(TAG, "result ret = " + result.getCvmResult() + "  " + result.getTransResult());
        return result;
    }

    private void updateResult(CTransResult result) throws EmvException {
        CvmType cvmType = new CvmType();
        int ret = ClssPbocApi.Clss_GetCvmType_Pboc(cvmType);
        Log.i(TAG, "clssPbocGetCvmType CVMType = " + cvmType.type);
        if (ret < 0) {
            if (ret == RetCode.CLSS_PARAM_ERR) {
                result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            }
            throw new EmvException(ret);
        }

        result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_CONSUMER_DEVICE));

        boolean needPin = false;
        if (isNeedPin(cvmType)) {
            Log.i(TAG, " NeedPin");
            needPin = true;
            result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_ONLINE_PIN));
        }

        if (isNeedSig(cvmType)) {
            Log.i(TAG, " NeedSig");
            if (needPin) {
                result.setCvmResult(ECvmResult.ONLINE_PIN_SIG);
            } else {
                result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_SIG));
            }
            return;
        }
    }

    private int processQVSDC(ACType acType, CTransResult result) throws EmvException {
        int ret = 0;

        if ((acType.type == com.pax.jemv.clcommon.ACType.AC_TC)
                && transParam.ucTransType != 0x20) { //no refund
            //according to EDC
            ClssPbocApi.Clss_DelAllRevocList_Pboc();
            ClssPbocApi.Clss_DelAllCAPK_Pboc();
            addCapkRevList();

            DDAFlag flag = new DDAFlag();
            ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
            Log.i(TAG, "clssPbocCardAuth ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                if (ret == RetCode.CLSS_USE_CONTACT) {
                    result.setTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
                }
                throw new EmvException(ret);
            } else if (flag.flag == DDAFlag.FAIL) {
                result.setTransResult(ETransResult.ABORT_TERMINATED);
                return -1;
            }
        }
        return RetCode.EMV_OK;
    }

    private int processVSDC(ACType acType, CTransResult result) throws EmvException {
        int ret = 0;

        if ((acType.type == com.pax.jemv.clcommon.ACType.AC_TC)
                && transParam.ucTransType != 0x20) { //no refund
            //according to EDC
            ClssPbocApi.Clss_DelAllRevocList_Pboc();
            ClssPbocApi.Clss_DelAllCAPK_Pboc();
            addCapkRevList();

            DDAFlag flag = new DDAFlag();
            ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
            Log.i(TAG, "clssPbocCardAuth ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ETransResult.OFFLINE_DENIED);
                return -1;
            }
        }
        return RetCode.EMV_OK;
    }

    private boolean continueUpdateResult(ACType acType, CTransResult result) throws EmvException {
        if (acType.type == com.pax.jemv.clcommon.ACType.AC_AAC) {
            result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            return false;
        }

        if (transactionPath.path == TransactionPath.CLSS_VISA_QVSDC) {
            if (processQVSDC(acType, result) != RetCode.EMV_OK) {
                return false;
            }
        } else if (transactionPath.path == TransactionPath.CLSS_VISA_VSDC) {
            if (processVSDC(acType, result) != RetCode.EMV_OK) {
                return false;
            }
        } else {
            return false;
        }

        if (acType.type == ACType.AC_TC) {
            result.setTransResult(ETransResult.CLSS_OC_APPROVED);
        } else if (acType.type == ACType.AC_ARQC) {
            result.setTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
        }
        return true;
    }

    @Override
    protected int setTlv(int tag, byte[] value) {
        return ClssPbocApi.Clss_SetTLVData_Pboc((short) tag, value, value.length);
    }

    @Override
    protected int getTlv(int tag, ByteArray value) {
        return ClssPbocApi.Clss_GetTLVData_Pboc((short) tag, value);
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        ClssPbocApi.Clss_AddCAPK_Pboc(emvCapk);
        ClssPbocApi.Clss_AddRevocList_Pboc(emvRevoclist);
        Log.i(TAG, "ClssProc set PBOC capk and revoclist");
    }

    @Override
    String getTrack1() {
        ByteArray pbocGetTrack1List = new ByteArray();
        ClssPbocApi.Clss_nGetTrack1MapData_Pboc(pbocGetTrack1List);
        return bcd2Str(pbocGetTrack1List.data, pbocGetTrack1List.length);
    }

    @Override
    String getTrack2() {
        if (track2 == null) {
            ByteArray pbocGetTrack2List = new ByteArray();
            getTlv(TagsTable.TRACK2, pbocGetTrack2List);
            track2 = bcd2Str(pbocGetTrack2List.data, pbocGetTrack2List.length).split("F")[0];
        }
        return track2;
    }

    @Override
    String getTrack3() {
        return "";
    }

    @Override
    protected CTransResult completeTrans(ETransResult result, byte[] tag91, byte[] tag71, byte[] tag72) throws EmvException {
        return new CTransResult(ETransResult.CLSS_OC_APPROVED);
    }

    private boolean isDebit() {
        String clssAid = bcd2Str(aid.getAid());
        Log.i(TAG, "Aid " + clssAid);
        return (boolean) (clssAid.equals("A000000333010101") || clssAid.equals("A000000333010106"));
    }

    private boolean isCredit() {
        String clssAid = bcd2Str(aid.getAid());
        Log.i(TAG, "Aid " + clssAid);
        return (boolean) (clssAid.equals("A000000333010102") || clssAid.equals("A000000333010103"));
    }

    private boolean isAmountExceed() {
        Log.i(TAG, "AmountExceed " + transParam.ulAmntAuth + " " + clssPreProcInfo.ulRdCVMLmt);
        if (transParam.ulAmntAuth > clssPreProcInfo.ulRdCVMLmt) {
            return true;
        }
        return false;
    }

    private boolean isNeedPin(CvmType cvmType) {
        if (isCredit()) {
            Log.i(TAG, "Credit card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN) {
                return isAmountExceed();
            }
        } else if (isDebit()) {
            Log.i(TAG, "Debit card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN) {
                return true;
            }
        }
        return false;
    }

    private boolean isNeedSig(CvmType cvmType) {
        if (isCredit()) {
            Log.i(TAG, "Credit card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN
                    || cvmType.type == CvmType.RD_CVM_SIG) {
                return isAmountExceed();
            }
        } else if (isDebit()) {
            Log.i(TAG, "Debit card");
            if (cvmType.type == CvmType.RD_CVM_SIG) {
                return isAmountExceed();
            }
        }
        return false;
    }

}

