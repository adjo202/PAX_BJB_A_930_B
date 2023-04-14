package com.pax.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pax.dal.ICardReaderHelper;
import com.pax.dal.IPed;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EBeepMode;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.ECryptOperate;
import com.pax.dal.entity.ECryptOpt;
import com.pax.dal.entity.ENavigationKey;
import com.pax.dal.entity.EPedDesMode;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.entity.EPedMacMode;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.EPinBlockMode;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.EScannerType;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PedDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;


public class Device {

    public static final String TAG = "Device";

    /**
     * beep 成功
     */
    public static void beepOk() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                try {
                    FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
                    FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_4, 100);
                    FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }

            }
        }).start();
    }

    /**
     * beep 失败
     */
    public static void beepErr() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                if (FinancialApplication.getDal() != null) {
                    try {
                        FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 200);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        }).start();
    }

    /**
     * beep 提示音
     */

    public static void beepPrompt() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_2, 50);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }).start();
    }

    /**
     * 设置系统时间
     *
     * @param time
     */
    public static void setSystemTime(String time) {
        if (isValidDate(time)) {
            FinancialApplication.getDal().getSys().setDate(time);
        }
    }

    private static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        try {
            format.setLenient(false); //Specifies whether or not date/time parsing shall be lenient
            format.parse(str);
        } catch (ParseException e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 获取日期YYYYMMDD
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    /**
     * 获取时间HHMMSS
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    public static void enableStatusBar(boolean enable) {
        FinancialApplication.getDal().getSys().enableStatusBar(enable);
    }

    // add abdul
    public static void enableBackKey(boolean enable) {
        FinancialApplication.getDal().getSys().enableNavigationKey(ENavigationKey.BACK, enable);
    }

    public static void enableHomeRecentKey(boolean enable) {
        FinancialApplication.getDal().getSys().enableNavigationKey(ENavigationKey.HOME, enable);
        FinancialApplication.getDal().getSys().enableNavigationKey(ENavigationKey.RECENT, enable);
    }

    /**
     * 获取扫码器
     */
    public static IScanner getScanner() {
        EScannerType scannerType = getScannerType();
        return getScannerByType(scannerType);
    }

    /**
     * 获取扫码类型，以及扫码摄像头选择
     *
     * @return
     */
    public static EScannerType getScannerType() {

        EScannerType scannerType = null;
        String temp = "";
        if (SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam.SUPPORT_EXTERNAL_SCANNER))) {
            scannerType = EScannerType.EXTERNAL;
        } else {
            temp = FinancialApplication.getSysParam().get(SysParam.INTERNAL_SCANNER);
            switch (temp) {
                case Constant.SCANNER_FRONT:
                    scannerType = EScannerType.FRONT;
                    break;
                case Constant.SCANNER_REAR:
                    scannerType = EScannerType.REAR;
                    break;
                case Constant.SCANNER_LEFT:
                    scannerType = EScannerType.LEFT;
                    break;
                case Constant.SCANNER_RIGHT:
                    scannerType = EScannerType.RIGHT;
                    break;
                default:
                    break;
            }
        }
        return scannerType;
    }

    /**
     * 指定摄像头类型获取扫码器
     *
     * @param scannerType
     * @return
     */
    public static IScanner getScannerByType(EScannerType scannerType) {
        return FinancialApplication.getDal().getScanner(scannerType);
    }

    /**
     * 获取密码键盘类型
     *
     * @return EPedType.EXTERNAL_TYPEA, 外置密码键盘；EPedType.INTERNAL,内置密码键盘
     */
    public static IPed getPed() {
        EPedType pedType = null;
        String pinPad = FinancialApplication.getSysParam().get(SysParam.EX_PINPAD); //modified by richard 20170518, merge APPT_V0509 SysParam.CHOOSE_PINPAD
        if (Constant.PAD_S200.equals(pinPad) || Constant.PAD_SP20.equals(pinPad)) {
            pedType = EPedType.EXTERNAL_TYPEA;
        } else if (Constant.PAD_INTERNAL.equals(pinPad)) {
            pedType = EPedType.INTERNAL;
        }

        return FinancialApplication.getDal().getPed(pedType);
    }

    /**
     * 写主密钥， 包括国密主密钥
     *
     * @param tmkIndex
     * @param tmkValue
     * @return
     */
    public static boolean writeTMK(int tmkIndex, byte[] tmkValue) {
        // 写主密钥
        try {
            IPed ped = Device.getPed();
            String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
            String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密主密钥
                ped.writeKey(EPedKeyType.SM4_TMK, (byte) 0, EPedKeyType.SM4_TMK,
                        (byte) Utils.getMainKeyIndex(tmkIndex), tmkValue, ECheckMode.KCV_NONE, null);
            } else { // 非国密主密钥
                ped.erase();
                ped.writeKey(EPedKeyType.TLK, (byte) 0, EPedKeyType.TMK, (byte) Utils.getMainKeyIndex(tmkIndex),
                        tmkValue, ECheckMode.KCV_NONE, null);
            }
            return true;
        } catch (PedDevException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    /**
     * 写TPK，包括国密
     *
     * @param tpkValue
     * @param tpkKcv
     * @throws PedDevException
     */
    public static void writeTPK(byte[] tpkValue, byte[] tpkKcv) throws PedDevException {
        int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.MK_INDEX)));
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        IPed ped = Device.getPed();
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密tpk
            ECheckMode checkMode = ECheckMode.KCV_SM4_ENCRYPT_0;
            if (tpkKcv == null || tpkKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }           //srcKeyType,srcKeyIndes,destKeyType,destKeyIndex,destKeyValue,checkMode,checkBuf
            ped.writeKey(EPedKeyType.SM4_TMK, (byte) mKeyIndex, EPedKeyType.SM4_TPK, Constants.INDEX_TPK, tpkValue,
                    checkMode, tpkKcv); //终端主密钥，或者称为收单行主密钥|用于应用输入PIN后，计算PIN Block
        } else { // 非国密tpk
            ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
            if (tpkKcv == null || tpkKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }

            ped.writeKey(EPedKeyType.TMK, (byte) mKeyIndex, EPedKeyType.TPK, Constants.INDEX_TPK, tpkValue, checkMode,
                    tpkKcv);
        }
    }

    /**
     * 写tak，包括国密
     *
     * @param takValue
     * @param takKcv
     * @throws PedDevException
     */
    public static void writeTAK(byte[] takValue, byte[] takKcv) throws PedDevException {
        int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.MK_INDEX)));
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        IPed ped = Device.getPed();
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密tak
            ECheckMode checkMode = ECheckMode.KCV_SM4_ENCRYPT_0;
            if (takKcv == null || takKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }
            ped.writeKey(EPedKeyType.SM4_TMK, (byte) mKeyIndex, EPedKeyType.SM4_TAK, Constants.INDEX_TAK, takValue,
                    checkMode, takKcv);
        } else { // 非国密tpk
            ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
            if (takKcv == null || takKcv.length == 0) {
                checkMode = ECheckMode.KCV_NONE;
            }

            ped.writeKey(EPedKeyType.TMK, (byte) mKeyIndex, EPedKeyType.TAK, Constants.INDEX_TAK, takValue, checkMode,
                    takKcv);
        }
    }

    /**
     * 写tdk, 包括国密
     *
     * @param tdkValue
     * @param tdkKcv
     * @throws PedDevException
     */
    public static void writeTDK(byte[] tdkValue, byte[] tdkKcv) throws PedDevException {
        int mKeyIndex = Utils.getMainKeyIndex(Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.MK_INDEX)));
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        IPed ped = Device.getPed();
        // 外置密码键盘
        if (!FinancialApplication.getSysParam().get(SysParam.EX_PINPAD).equals(Constant.PAD_INTERNAL)) {//modified by richard 20170518, merge APPT_V0509 SysParam.CHOOSE_PINPAD
            // 外置国密未处理
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密tak

            } else { // 非国密tpk，作为mac密钥写入， 计算des时，按mac计算
                ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
                if (tdkKcv == null || tdkKcv.length == 0) {
                    checkMode = ECheckMode.KCV_NONE;
                }
                // 目的密钥tak， 索引用tdk的
                ped.writeKey(EPedKeyType.TMK, (byte) mKeyIndex, EPedKeyType.TAK, Constants.INDEX_TDK, tdkValue,
                        checkMode, tdkKcv);
            }
        } else {
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密tak
                ECheckMode checkMode = ECheckMode.KCV_SM4_ENCRYPT_0;
                if (tdkKcv == null || tdkKcv.length == 0) {
                    checkMode = ECheckMode.KCV_NONE;
                }
                ped.writeKey(EPedKeyType.SM4_TMK, (byte) mKeyIndex, EPedKeyType.SM4_TDK, Constants.INDEX_TDK, tdkValue,
                        checkMode, tdkKcv);
            } else { // 非国密tpk

                ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
                if (tdkKcv == null || tdkKcv.length == 0) {
                    checkMode = ECheckMode.KCV_NONE;
                }          //终端主密钥，或者称为收单行主密钥  |  用于对应用中敏感数据进行DES/TDES加密传输或存储
                ped.writeKey(EPedKeyType.TMK, (byte) mKeyIndex, EPedKeyType.TDK, Constants.INDEX_TDK, tdkValue,
                        checkMode, tdkKcv);
            }
        }
    }

    /**
     * 计算pinblock(包括国密)
     *
     * @param panBlock
     * @return
     * @throws PedDevException
     */
    public static byte[] getPinBlock(String panBlock, boolean supportBypass) throws PedDevException {

        byte[] pinBlock = null;
        // 写工作密钥
        String tpk = FinancialApplication.getGeneralParam().get(GeneralParam.TPK);
        if (tpk != null && tpk.length() > 0) {
            writeTPK(FinancialApplication.getConvert().strToBcd(tpk, EPaddingPosition.PADDING_RIGHT), null);
        }
        IPed ped = Device.getPed();
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        String pinLen = "4,6";
        if (supportBypass) {
            pinLen = "0," + pinLen;
        }
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密
            while (true) {
                pinBlock = ped.getPinBlockSM4(Constants.INDEX_TPK, pinLen, panBlock.getBytes(),
                        EPinBlockMode.ISO9564_0, 60 * 1000);
                if (!supportBypass) { // 凭密
                    if (pinBlock != null && pinBlock.length == 0) {
                        continue;
                    }
                }
                break;
            }
        } else {
            while (true) {
                pinBlock = ped.getPinBlock(Constants.INDEX_TPK, pinLen, panBlock.getBytes(), EPinBlockMode.ISO9564_0,
                        60 * 1000);
                if (!supportBypass) { // 主要处理外置密码键盘凭密的时候，不输入密码直接点确认键;
                    if (pinBlock != null && pinBlock.length == 0) {
                        continue;
                    }
                }
                break;
            }

        }
        return pinBlock;
    }

    /**
     * 计算mac， 包括国密
     *
     * @param data
     * @return
     * @throws PedDevException
     */
    public static byte[] calcMac(byte[] data) throws PedDevException {
        // 写工作密钥
        String tak = FinancialApplication.getGeneralParam().get(GeneralParam.TAK);
        if (tak != null && tak.length() > 0) {
            writeTAK(FinancialApplication.getConvert().strToBcd(tak, EPaddingPosition.PADDING_RIGHT), null);
        }
        IPed ped = Device.getPed();
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) {
            byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00};
            return ped.getMacSM(Constants.INDEX_TAK, initVector, data, (byte) 0x00);
        } else {
            return ped.getMac(Constants.INDEX_TAK, data, EPedMacMode.MODE_00);
        }
    }

    /**
     * 计算DES，包含国密
     *
     * @param data
     * @return
     * @throws PedDevException
     */
    public static byte[] calcDes(byte[] data) throws PedDevException {
        // 写工作密钥
        String tdk = FinancialApplication.getGeneralParam().get(GeneralParam.TDK);
        if (tdk != null && tdk.length() > 0) {
            writeTDK(FinancialApplication.getConvert().strToBcd(tdk, EPaddingPosition.PADDING_RIGHT), null);
        }
        IPed ped = Device.getPed();
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        // 外置
        if (!FinancialApplication.getSysParam().get(SysParam.EX_PINPAD).equals(Constant.PAD_INTERNAL)) {//modified by richard 20170518, merge APPT_V0509 SysParam.CHOOSE_PINPAD
            // 外置国密未处理
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) {
                return null;
            } else {
                byte[] des = new byte[data.length + data.length % 8];
                int count = des.length / 8;
                byte[] dataIn = new byte[des.length];
                System.arraycopy(data, 0, dataIn, 0, data.length);

                for (int i = 0; i < count; i++) {
                    byte[] desData = new byte[8];
                    System.arraycopy(dataIn, i * 8, desData, 0, 8);
                    byte[] desResult = ped.getMac(Constants.INDEX_TDK, desData, EPedMacMode.MODE_00);
                    System.arraycopy(desResult, 0, des, i * 8, 8);
                }
                return des;
            }
        } else {
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) {
                return ped.SM4(Constants.INDEX_TDK, null, data, ECryptOperate.ENCRYPT, ECryptOpt.ECB);
            } else {
                return ped.calcDes(Constants.INDEX_TDK, data, EPedDesMode.ENCRYPT);
            }
        }
    }

    /***
     * Return the available storage space of the device.
     * @param context
     * @return the available storage space by bytes.
     */
    public static long getAvailableSpace(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return 0;
        }

        long blockSize;
        long availbleBlocks;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            blockSize = stat.getBlockSizeLong();
            availbleBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availbleBlocks = stat.getAvailableBlocks();
        }

        return blockSize * availbleBlocks;
    }

    public interface RemoveCardListener {
        void onShowMsg(PollingResult result);
    }

    public static void removeCard(RemoveCardListener listener) {
        ICardReaderHelper helper = FinancialApplication.getDal().getCardReaderHelper();

        try {
            PollingResult result;
            while ((result = helper.polling(EReaderType.ICC_PICC, 100)).getReaderType() == EReaderType.ICC || result.getReaderType() == EReaderType.PICC) {
                // remove card prompt
                if (listener != null)
                    listener.onShowMsg(result);
                SystemClock.sleep(500);
                Device.beepErr();
            }
        } catch (MagDevException | IccDevException | PiccDevException e) {
            //ignore the warning
        }
    }


    public static JSONObject getBaseInfo() {
        Map<ETermInfoKey, String> map = FinancialApplication.getDal().getSys().getTermInfo();
        String terminalSn = map.get(ETermInfoKey.SN);
        String appVersion = FinancialApplication.version;
        JSONObject json = new JSONObject();
        try {

            json.put("terminalSn", terminalSn);
            json.put("appVersion", appVersion);

            JSONObject SIMInformation = getSIMInfo();
            if (SIMInformation != null) {
                String SIMSerialNumber = (String) SIMInformation.get("SIMSerialNumber");
                String SIMNumber = (String) SIMInformation.get("SIMNumber");
                json.put("SIMSerialNumber", SIMSerialNumber);
                json.put("SIMNumber", SIMNumber);
            }

            JSONObject LocationInformation = getLatLon();
            if (LocationInformation != null) {
                String lon = String.valueOf((double) LocationInformation.get("lon"));
                String lat = String.valueOf((double) LocationInformation.get("lat"));
                json.put("lon", lon);
                json.put("lat", lat);
            }


            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;

    }

    private static Double[] getLastKnownLocation() {
        FinancialApplication context = FinancialApplication.getAppContext();
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Sandy : Loop over the array backwards,
            and if you get an accurate location,
            then break out the loop
        */
        Location l = null;
        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        Double[] gps = new Double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
            return gps;
        }else
            return null;
    }
    public static JSONObject getLatLon(){
        Double[] loc = getLastKnownLocation();

        if(loc == null)
           return null;

        Double latitude = loc[0];
        Double longitude = loc[1];
        JSONObject json = new JSONObject();
        try {
            json.put("lon"  , longitude);
            json.put("lat"  , latitude);
            return json;
        }catch(JSONException e){

        }

            return null;
       }

    public static JSONObject getSIMInfo(){

        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        //Sandy : if WIFI, then it is not required to gather SIM information
        if(commType.equals(SysParam.Constant.COMMTYPE_WIFI)){
            return null;
        }

        FinancialApplication context = FinancialApplication.getAppContext();
        TelephonyManager teleManager    = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        //Sandy : check if the SIM Card is existed
        if(teleManager.getSimState() != TelephonyManager.SIM_STATE_READY)
            return null;


        String getSimSerialNumber       = teleManager.getSimSerialNumber();
        String getSimNumber             = teleManager.getLine1Number();

        JSONObject json = new JSONObject();
        try {
            json.put("SIMSerialNumber"  , getSimSerialNumber.toString());
            json.put("SIMNumber"        , getSimNumber.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



}
