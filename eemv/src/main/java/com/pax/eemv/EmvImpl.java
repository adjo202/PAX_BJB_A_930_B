package com.pax.eemv;

import android.util.Log;

import com.pax.eemv.entity.AidParam;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.CandList;
import com.pax.eemv.entity.Capk;
import com.pax.eemv.entity.EcRecord;
import com.pax.eemv.entity.InputParam;
import com.pax.eemv.enums.EFlowType;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.eemv.utils.Converter;
import com.pax.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.EMV_APPLIST;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.emv.api.EMVCallback;
import com.pax.jemv.emv.model.EmvEXTMParam;
import com.pax.jemv.emv.model.EmvMCKParam;
import com.pax.jemv.emv.model.EmvParam;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class EmvImpl extends EmvBase implements IEmv {
    private static final String TAG = "EmvImpl";
    private EMVCallback emvCallback;
    private EmvTrans paxEmvTrans;
    private EmvParam emvParam;
    private EmvMCKParam mckParam;

    static {
        System.loadLibrary("JniEMV_V1.00.00_20171114");
    }

    private class Callback implements EMVCallback.EmvCallbackListener {
        @Override
        public void emvWaitAppSel(int tryCnt, EMV_APPLIST[] list, int appNum) {
            CandList[] candLists = new CandList[list.length];
            int size = Math.min(list.length, appNum);
            for (int i = 0; i < size; ++i) {
                candLists[i] = Converter.toCandList(list[i]);
            }
            try {
                int idx = paxEmvTrans.waitAppSelect(tryCnt, candLists);
                if (idx >= 0)
                    emvCallback.setCallBackResult(idx);
                else {
                    emvCallback.setCallBackResult(RetCode.EMV_USER_CANCEL);
                }
            } catch (EmvException e) {
                Log.w(TAG, "", e);
                emvCallback.setCallBackResult(RetCode.EMV_USER_CANCEL);
            }
        }

        @Override
        public void emvInputAmount(long[] amt) {
            Amount amount = paxEmvTrans.getAmount();
            if (amount != null) {
                amt[0] = Long.parseLong(amount.getAmount());
                if (amt.length > 1) {
                    if (amount.getCashBackAmt() == null || amount.getCashBackAmt().isEmpty()) {
                        amt[1] = 0;
                    } else {
                        amt[1] = Long.parseLong(amount.getCashBackAmt());
                    }
                }
            }
            emvCallback.setCallBackResult(RetCode.EMV_OK);
        }

        @Override
        public void emvGetHolderPwd(int tryFlag, int remainCnt, byte[] pin) {
            if (pin == null) {
                Log.i("log", "emvGetHolderPwd pin is null, tryFlag" + tryFlag + " remainCnt:" + remainCnt);
            } else {
                Log.i("log", "emvGetHolderPwd pin is not null, tryFlag" + tryFlag + " remainCnt:" + remainCnt);
            }

            int result;
            try {
                int ret = paxEmvTrans.cardHolderPwd(pin == null, remainCnt, pin);
                if (ret == EEmvExceptions.EMV_OK.getErrCodeFromBasement())
                    result = RetCode.EMV_OK;
                else if (ret == EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement()) {
                    result = RetCode.EMV_NO_PASSWORD;
                } else {
                    result = RetCode.EMV_USER_CANCEL;
                }
            } catch (EmvException e) {
                Log.w(TAG, "", e);
                result = RetCode.EMV_USER_CANCEL;
            }

            emvCallback.setCallBackResult(result);
        }

        @Override
        public void emvAdviceProc() {
            //do nothing
        }

        @Override
        public void emvVerifyPINOK() {
            //do nothing
        }

        @Override
        public int emvUnknowTLVData(short tag, ByteArray data) {
            Log.i("EMV", "emvUnknowTLVData tag: " + Integer.toHexString(tag) + " data:" + data.data.length);
            switch ((int) tag) {
                case 0x9A:
                    byte[] date = new byte[7];
                    DeviceManager.getInstance().getTime(date);
                    System.arraycopy(date, 1, data.data, 0, 3);
                    break;
                case 0x9F1E:
                    byte[] sn = new byte[10];
                    DeviceManager.getInstance().readSN(sn);
                    System.arraycopy(sn, 0, data.data, 0, Math.min(data.data.length, sn.length));
                    break;
                case 0x9F21:
                    byte[] time = new byte[7];
                    DeviceManager.getInstance().getTime(time);
                    System.arraycopy(time, 4, data.data, 0, 3);
                    break;
                case 0x9F37:
                    byte[] random = new byte[4];
                    DeviceManager.getInstance().getRand(random, 4);
                    System.arraycopy(random, 0, data.data, 0, data.data.length);
                    break;
                case 0xFF01:
                    Arrays.fill(data.data, (byte) 0x00);
                    break;
                default:
                    return RetCode.EMV_PARAM_ERR;
            }
            data.length = data.data.length;
            return RetCode.EMV_OK;
        }

        @Override
        public void certVerify() {
            emvCallback.setCallBackResult(RetCode.EMV_OK);
        }

        @Override
        public int emvSetParam() {
            return RetCode.EMV_OK;
        }

        @Override
        public int cRFU1() {
            return 0;
        }

        @Override
        public int cRFU2() {
            return 0;
        }
    }

    public EmvImpl() {
        super();
        emvParam = new EmvParam();
        mckParam = new EmvMCKParam();
        mckParam.extmParam = new EmvEXTMParam();

        paxEmvTrans = new EmvTrans();
        emvCallback = EMVCallback.getInstance();
        emvCallback.setCallbackListener(new Callback());
    }

    @Override
    public void init() throws EmvException {
        super.init();
        int ret = EMVCallback.EMVCoreInit();
        if (ret == RetCode.EMV_OK) {
            EMVCallback.EMVSetCallback();
            EMVCallback.EMVGetParameter(emvParam);
            EMVCallback.EMVGetMCKParam(mckParam);
            paramToConfig();
            return;
        }

        throw new EmvException(EEmvExceptions.EMV_ERR_FILE);
    }

    private void paramToConfig() {

        Log.d(TAG, "Sandy.EmvImpl.paramToConfig is called!");
        cfg.setCapability(Tools.bcd2Str(emvParam.capability));
        cfg.setCountryCode(Tools.bcd2Str(emvParam.countryCode));
        cfg.setExCapability(Tools.bcd2Str(emvParam.exCapability));
        cfg.setForceOnline(Tools.byte2Boolean(emvParam.forceOnline));
        cfg.setGetDataPIN(Tools.byte2Boolean(emvParam.getDataPIN));
        cfg.setMerchCateCode(Tools.bcd2Str(emvParam.merchCateCode));
        cfg.setReferCurrCode(Tools.bcd2Str(emvParam.referCurrCode));
        cfg.setReferCurrCon(emvParam.referCurrCon);
        cfg.setReferCurrExp(emvParam.referCurrExp);
        cfg.setSurportPSESel(Tools.byte2Boolean(emvParam.surportPSESel));
        cfg.setTermType(emvParam.terminalType);
        cfg.setTransCurrCode(Tools.bcd2Str(emvParam.transCurrCode));
        cfg.setTransCurrExp(emvParam.transCurrExp);
        cfg.setTransType(emvParam.transType);
        cfg.setTermId(Arrays.toString(emvParam.termId));
        cfg.setMerchId(Arrays.toString(emvParam.merchId));
        cfg.setMerchName(Arrays.toString(emvParam.merchName));

        cfg.setBypassPin(Tools.byte2Boolean(mckParam.ucBypassPin));
        cfg.setBatchCapture(mckParam.ucBatchCapture);

        cfg.setTermAIP(Tools.bcd2Str(mckParam.extmParam.aucTermAIP));
        cfg.setBypassAllFlag(Tools.byte2Boolean(mckParam.extmParam.ucBypassAllFlg));
        cfg.setUseTermAIPFlag(Tools.byte2Boolean(mckParam.extmParam.ucUseTermAIPFlg));

         Log.d(TAG, "Sandy.EmvImpl.paramToConfig.capability " +cfg.getCapability());
        //Log.d(TAG, "Sandy.EmvImpl.paramToConfig.forceOnline " +Tools.byte2Boolean(emvParam.forceOnline));

    }

    private void configToParam() {
        emvParam.capability = Tools.str2Bcd(cfg.getCapability());
        emvParam.countryCode = Tools.str2Bcd(cfg.getCountryCode());
        emvParam.exCapability = Tools.str2Bcd(cfg.getExCapability());
        emvParam.forceOnline = Tools.boolean2Byte(cfg.getForceOnline());
        emvParam.getDataPIN = Tools.boolean2Byte(cfg.getGetDataPIN());
        emvParam.merchCateCode = Tools.str2Bcd(cfg.getMerchCateCode());
        emvParam.referCurrCode = Tools.str2Bcd(cfg.getReferCurrCode());
        emvParam.referCurrCon = cfg.getReferCurrCon();
        emvParam.referCurrExp = cfg.getReferCurrExp();
        emvParam.surportPSESel = Tools.boolean2Byte(cfg.getSurportPSESel());
        emvParam.terminalType = cfg.getTermType();
        emvParam.transCurrCode = Tools.str2Bcd(cfg.getTransCurrCode());
        emvParam.transCurrExp = cfg.getTransCurrExp();
        emvParam.transType = cfg.getTransType();
        emvParam.termId = cfg.getTermId().getBytes();
        emvParam.merchId = cfg.getMerchId().getBytes();
        emvParam.merchName = cfg.getMerchName().getBytes();

        mckParam.ucBypassPin = Tools.boolean2Byte(cfg.getBypassPin());
        mckParam.ucBatchCapture = cfg.getBatchCapture();

        mckParam.extmParam.aucTermAIP = Tools.str2Bcd(cfg.getTermAIP());
        mckParam.extmParam.ucBypassAllFlg = Tools.boolean2Byte(cfg.getBypassAllFlag());
        mckParam.extmParam.ucUseTermAIPFlg = Tools.boolean2Byte(cfg.getUseTermAIPFlag());
    }

    @Override
    public byte[] getTlvSub(int tag) {
        ByteArray byteArray = new ByteArray();
        if (EMVCallback.EMVGetTLVData((short) tag, byteArray) == RetCode.EMV_OK) {
            return Arrays.copyOfRange(byteArray.data, 0, byteArray.length);
        }
        return null;
    }

    @Override
    public void setTlvSub(int tag, byte[] value) throws EmvException {
        int ret = EMVCallback.EMVSetTLVData((short) tag, value, value.length);
        if (ret != EEmvExceptions.EMV_OK.getErrCodeFromBasement()) {
            throw new EmvException(ret);
        }
    }

    // Run callback
    // Parameter settings, loading aid,
    // select the application, application initialization,read application data, offline data authentication,
    // terminal risk management,cardholder authentication, terminal behavior analysis,
    // issuing bank data authentication, execution script
    @Override
    public CTransResult process(InputParam inputParam) throws EmvException {

        Log.i(TAG, "Sandy.EmvImpl.CTransResult.process "  );
        configToParam();
        EMVCallback.EMVSetParameter(emvParam);
        int ret = EMVCallback.EMVSetMCKParam(mckParam);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        EMVCallback.EMVSetPCIModeParam((byte) 1, "0,4,5,6,7,8,9,10,11,12".getBytes(), inputParam.getPciTimeout());

        for (AidParam i : aidParamList) {
            ret = EMVCallback.EMVAddApp(Converter.toEMVApp(i));
            if (ret != RetCode.EMV_OK) {
                Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 1"  );

                throw new EmvException(ret);
            }
        }

        ret = EMVCallback.EMVAppSelect(0, Long.parseLong(inputParam.getTransTraceNo()));   //callback emvWaitAppSel
        if (ret != RetCode.EMV_OK) {
            //Sandy : set here for fallback?
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 2"  );
            throw new EmvException(ret);
        }

        ret = EMVCallback.EMVReadAppData(); //callback emvInputAmount
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 3"  );
            throw new EmvException(ret);
        }

        ByteArray pan = new ByteArray();
        EMVCallback.EMVGetTLVData((byte) 0x5A, pan);
        String filtPan = Tools.bcd2Str(pan.data, pan.length);
        int indexF = filtPan.indexOf('F');

        if (pan.length > 0 && pan.data != null) {
            // add abdul jika tarik tunai ga perlu confirm card
            Log.i("teg", "transtipe : "+inputParam.getTransType());
            if ( (!inputParam.getTransType().equals("TARIK_TUNAI")) &&
                    (!inputParam.getTransType().equals("TARIK_TUNAI_2")) &&
                    (!inputParam.getTransType().equals("BPJS_OVERBOOKING")) &&
                    (!inputParam.getTransType().equals("PEMBUKAAN_REK")) &&
                    (!inputParam.getTransType().equals("PEMBATAL_REK")) &&
                    (!inputParam.getTransType().equals("DIRJEN_PAJAK")) &&
                    (!inputParam.getTransType().equals("OVERBOOKING")) &&
                    (!inputParam.getTransType().equals("OVERBOOKING_2")) &&
                    (!inputParam.getTransType().equals("DIRJEN_BEA_CUKAI")) &&
                    (!inputParam.getTransType().equals("DIRJEN_ANGGARAN")) &&
                    (!inputParam.getTransType().equals("SETOR_TUNAI")) &&
                    (!inputParam.getTransType().equals("TRANSFER")) &&
                    (!inputParam.getTransType().equals("TRANSFER_2")) &&
                    (!inputParam.getTransType().equals("TRANSFER_INQ")) &&
                    (!inputParam.getTransType().equals("TRANSFER_INQ_2")) &&
                    (!inputParam.getTransType().equals("PBB_PAY")) &&
                    (!inputParam.getTransType().equals("E_SAMSAT_INQUIRY")) &&
                    (!inputParam.getTransType().equals("E_SAMSAT")) &&
                    (!inputParam.getTransType().equals("PASCABAYAR_OVERBOOKING")) &&
                    (!inputParam.getTransType().equals("PDAM_OVERBOOKING")) &&
                    (!inputParam.getTransType().equals("PBB_INQ")) &&
                    (!inputParam.getTransType().equals("REDEEM_POIN_DATA_INQ")) //added by sandy
            ) {
                ret = paxEmvTrans.confirmCardNo(filtPan.substring(0, indexF != -1 ? indexF : filtPan.length()));
                if (ret != RetCode.EMV_OK) {
                    Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 4"  );
                    throw new EmvException(ret);
                }
            }
        }

        addCapkIntoEmvLib(); // ignore return value for some case which the card doesn't has the capk index

        ret = EMVCallback.EMVSetTLVData((short) 0x9C, new byte[]{inputParam.getTag9CValue()}, 1);
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 5"  );

            throw new EmvException(ret);
        }

        ret = EMVCallback.EMVCardAuth();
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 6"  );

            throw new EmvException(ret);
        }
        //简易流程
        if (inputParam.getFlowType() == EFlowType.SIMPLE) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 7"  );

            return new CTransResult(ETransResult.SIMPLE_FLOW_END);
        }

        ACType acType = new ACType();
        ret = EMVCallback.EMVStartTrans(Long.parseLong(inputParam.getAmount()),
                Long.parseLong(inputParam.getCashBackAmount()), acType);
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 8"  );

            throw new EmvException(ret);
        }
        //

        if (acType.type == ACType.AC_TC) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 9"  );

            return new CTransResult(ETransResult.OFFLINE_APPROVED);
        } else if (acType.type == ACType.AC_AAC) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 10"  );

            return new CTransResult(ETransResult.OFFLINE_DENIED);
        }
        ETransResult result = onlineProc();
        //AET-146
        if (result != ETransResult.ONLINE_APPROVED &&
                result != ETransResult.ONLINE_DENIED) {
            throw new EmvException(EEmvExceptions.EMV_ERR_ONLINE_TRANS_ABORT); //disini?
        }

        byte[] script = combine7172(getTlv(0x71), getTlv(0x72));
        if (script == null) {
            script = new byte[0];
        }

        ret = EMVCallback.EMVCompleteTrans(Converter.toOnlineResult(result), script, script.length, acType);
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 11"  );
            // sementara
            if (result == ETransResult.ONLINE_APPROVED) {
                return new CTransResult(ETransResult.ONLINE_APPROVED);
            }
            ByteArray scriptResult = new ByteArray();
            EMVCallback.EMVGetScriptResult(scriptResult);
            throw new EmvException(ret);
        }

        if (acType.type == ACType.AC_TC) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 12"  );

            return new CTransResult(ETransResult.ONLINE_APPROVED);
        } else if (acType.type == ACType.AC_AAC) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 13"  );

            return new CTransResult(ETransResult.ONLINE_CARD_DENIED);
        }

        ETransResult transResult = Tools.getEnum(ETransResult.class, ret - 1);
        if (transResult == null) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 14"  );
            throw new EmvException(EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement());
        }
        Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 15"  );

        return new CTransResult(transResult);
    }

    // Run callback
    // Parameter settings, loading aid,
    // select the application, application initialization,read application data, offline data authentication,
    // terminal risk management,cardholder authentication, terminal behavior analysis,
    // issuing bank data authentication, execution script
    @Override //kd add 01042021
    public String processGetPanOnly(InputParam inputParam) throws EmvException {

        Log.i(TAG, "Sandy.EmvImpl.CTransResult.process "  );
        configToParam();
        EMVCallback.EMVSetParameter(emvParam);
        int ret = EMVCallback.EMVSetMCKParam(mckParam);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        EMVCallback.EMVSetPCIModeParam((byte) 1, "0,4,5,6,7,8,9,10,11,12".getBytes(), inputParam.getPciTimeout());

        for (AidParam i : aidParamList) {
            ret = EMVCallback.EMVAddApp(Converter.toEMVApp(i));
            if (ret != RetCode.EMV_OK) {
                Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 1"  );

                throw new EmvException(ret);
            }
        }

        ret = EMVCallback.EMVAppSelect(0, Long.parseLong(inputParam.getTransTraceNo()));   //callback emvWaitAppSel
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 2"  );
            throw new EmvException(ret);
        }

        ret = EMVCallback.EMVReadAppData(); //callback emvInputAmount
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 3"  );
            throw new EmvException(ret);
        }

        ByteArray pan = new ByteArray();
        EMVCallback.EMVGetTLVData((byte) 0x5A, pan);
        String filtPan = Tools.bcd2Str(pan.data, pan.length);
        int indexF = filtPan.indexOf('F');

        if (pan.length > 0 && pan.data != null) {
            ret = paxEmvTrans.confirmCardNo(filtPan.substring(0, indexF != -1 ? indexF : filtPan.length()));
            if (ret != RetCode.EMV_OK) {
                Log.i(TAG, "Sandy.EmvImpl.CTransResult.process 4"  );
                throw new EmvException(ret);
            }
        }

        ByteArray holderNameBCD = new ByteArray();
        EMVCallback.EMVGetTLVData((byte) 0x5F20, holderNameBCD);

        ByteArray track2 = new ByteArray();
        EMVCallback.EMVGetTLVData((byte) 0x57, track2);

        String data = Tools.bcd2Str(pan.data, pan.length) + "=" +  Tools.bcd2Str(track2.data, track2.length) + "=" + Tools.bcd2Str(holderNameBCD.data, holderNameBCD.length) ;

        return data;
    }


    private static byte[] combine7172(byte[] f71, byte[] f72) {
        if (f71 == null || f71.length == 0)
            return f72;
        if (f72 == null || f72.length == 0)
            return f71;

        ByteBuffer bb = ByteBuffer.allocate(f71.length + f72.length + 6);

        bb.put((byte) 0x71);
        if (f71.length > 127)
            bb.put((byte) 0x81);
        bb.put((byte) f71.length);
        bb.put(f71, 0, f71.length);

        bb.put((byte) 0x72);
        if (f72.length > 127)
            bb.put((byte) 0x81);
        bb.put((byte) f72.length);
        bb.put(f72, 0, f72.length);

        int len = bb.position();
        bb.position(0);

        byte[] script = new byte[len];
        bb.get(script, 0, len);

        return script;
    }

    public static byte[] combine917172(byte[] f91, byte[] f71, byte[] f72) {
        if (f91 == null || f91.length == 0)
            return combine7172(f71, f72);
        if (f71 == null || f71.length == 0)
            return f72;
        if (f72 == null || f72.length == 0)
            return f71;

        ByteBuffer bb = ByteBuffer.allocate(f71.length + f72.length + 6);

        bb.put((byte) 0x91);
        bb.put((byte) f91.length); //fix 16
        bb.put(f91, 0, f91.length);

        byte[] f7172 = combine7172(f71, f72);
        bb.put(f7172, 0, f7172.length);

        int len = bb.position();
        bb.position(0);

        byte[] script = new byte[len];
        bb.get(script, 0, len);

        return script;
    }

    @Override
    public void setListener(IEmvListener listener) {
        paxEmvTrans.setEmvListener(listener);
    }

    @Override
    public String getVersion() {
        ByteArray byteArray = new ByteArray();
        EMVCallback.EMVReadVerInfo(byteArray);
        return Arrays.toString(byteArray.data);
    }

    private int addCapkIntoEmvLib() {
        int ret;
        ByteArray dataList = new ByteArray();
        ret = EMVCallback.EMVGetTLVData((short) 0x4F, dataList);
        if (ret != RetCode.EMV_OK) {
            ret = EMVCallback.EMVGetTLVData((short) 0x84, dataList);
        }
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        byte[] rid = new byte[5];
        System.arraycopy(dataList.data, 0, rid, 0, 5);
        ret = EMVCallback.EMVGetTLVData((short) 0x8F, dataList);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }
        byte keyId = dataList.data[0];
        for (Capk capk : capkList) {
            if (Tools.bytes2String(capk.getRid()).equals(new String(rid)) && capk.getKeyID() == keyId) {
                EMV_CAPK emvCapk = Converter.toEMVCapk(capk);
                ret = EMVCallback.EMVAddCAPK(emvCapk);
            }
        }
        return ret;
    }

    private ETransResult onlineProc() {
        EOnlineResult ret;
        try {
            ret = paxEmvTrans.onlineProc();
        } catch (EmvException e) {
            Log.e("EmvImpl", "", e);
            return ETransResult.ABORT_TERMINATED;
        }
        if (ret == EOnlineResult.APPROVE) {
            return ETransResult.ONLINE_APPROVED;
        } else if (ret == EOnlineResult.ABORT) {
            return ETransResult.ABORT_TERMINATED;
        } else {
            return ETransResult.ONLINE_DENIED;
        }
    }

    @Override
    public long getEcBalance(long transNo) throws EmvException {

        int ret;
        for (AidParam i : aidParamList) {
            ret = EMVCallback.EMVAddApp(Converter.toEMVApp(i));
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }

        ret = EMVCallback.EMVAppSelect(0, transNo);   //callback emvWaitAppSel
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        long amount = EMVCallback.EMVGetCardECBalance();
        if (amount < RetCode.EMV_OK) {
            throw new EmvException((int) amount);
        }
        return amount;
    }

    @Override
    public ArrayList<EcRecord> readAllLoadLogs() throws EmvException {
        int ret;
        for (AidParam i : aidParamList) {
            ret = EMVCallback.EMVAddApp(Converter.toEMVApp(i));
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }

        ret = EMVCallback.EMVAppSelect(0, 0);   //callback emvWaitAppSel
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        int Reco = 1;
        ArrayList<EcRecord> ecRecords = new ArrayList<EcRecord>();
        while (true) {
            ret = EMVCallback.ReadLogRecord(Reco);
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

    EcRecord getLogData() {
        ByteArray value = new ByteArray();
        EcRecord recorder = new EcRecord();
        EMVCallback.GetLogItem((short) 0x9A, value);
        recorder.setDate(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9F21, value);
        recorder.setTime(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9F02, value);
        recorder.setAuthAmount(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9F03, value);
        recorder.setOtherAmount(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9F1A, value);
        recorder.setCountryCode(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x5F2a, value);
        recorder.setCurrencyCode(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9C, value);
        recorder.setTransType(Tools.bcd2Str(value.data, value.length));
        EMVCallback.GetLogItem((short) 0x9F36, value);
        recorder.setAtc(Tools.bcd2Str(value.data, value.length));
        return recorder;
    }


}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.EmvImpl
 * JD-Core Version:    0.6.0
 */