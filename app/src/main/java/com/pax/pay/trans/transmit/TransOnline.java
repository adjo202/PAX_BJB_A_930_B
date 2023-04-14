package com.pax.pay.trans.transmit;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.JsonObject;
import com.pax.dal.IPed;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.device.GeneralParam;
import com.pax.eemv.enums.ETransResult;
import com.pax.gl.algo.IAlgo;
import com.pax.gl.convert.IConvert;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObj;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.CardBin;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.emv.EmvCapk;
import com.pax.pay.tmk.GlobalTmkData;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.ReceiptElements;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.model.TransData.OfflineStatus;
import com.pax.pay.trans.model.TransData.SignSendStatus;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.receipt.ReceiptPrintTrans;
import com.pax.pay.utils.CollectionUtils;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.ResponseCode;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.rate.LoadRate;
import com.pax.up.bjb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 单独联机处理， 例如签到
 *
 * @author Steven.W
 */
public class TransOnline {
    private static final String TAG = "TransOnline";

    /******************************* 1-8 *********************************************/
    private static final byte PR_PARA = 0x01; // 磁条卡参数下载
    private static final byte PR_STATU = 0x02; // 终端磁条卡状态上传
    private static final byte PR_LOGON = 0x04; // 重新签到
    private static final byte PR_CA = 0x08; // 公钥下载
    private static final byte PR_ICPARA = 0x10; // IC卡参数下载
    private static final byte PR_TMS = 0x20; // TMS参数下载
    private static final byte PR_BLACK = 0x40; // 卡bin黑名单下载
    private static final byte PR_RATE = (byte) 0x80; // 币种汇率下载

    /**
     * 终端签到， 包括工作密钥写入
     *
     * @param context
     * @param listener
     * @return
     */

    public static final IAlgo iAlgo = FinancialApplication.getGl().getAlgo();

    public static int posLogon(TransProcessListener listener) {
        boolean isTripDes = true;

        TransData transData = Component.transInit(); //交易初始化：设置一些交易参数
        String desType = FinancialApplication.getSysParam().get(SysParam.KEY_ALGORITHM);
        //从sharedPreferences中获取加密算法

        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
        if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { //支持国密和国密二期
            if (transData.getIsEncTrack()) { // 磁道是否加密
                ETransType.LOGON.setNetCode("006"); //磁道加密
            } else {
                ETransType.LOGON.setNetCode("005"); //磁道不加密
            }
        } else if (transData.getIsEncTrack() && Constant.TRIP_DES.equals(desType)) {
            //磁道加密并且加密算法为3DES
            ETransType.LOGON.setNetCode("004"); // f60.3
        } else if (Constant.TRIP_DES.equals(desType)) { //3DES算法磁道不加密
            ETransType.LOGON.setNetCode("003"); // f60.3
        } else {
            ETransType.LOGON.setNetCode("001"); //其他（非3DES算法）
            isTripDes = false;
        }
        transData.setTransType(ETransType.LOGON.toString());
        listener.onUpdateProgressTitle(ETransType.LOGON.getTransName());  //"终端签到"
        FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller.Constant.NO);
        //Log.d(TAG,"Sandy=posLogon called!" + transData.getTransType());
        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        // 保存批次号
        FinancialApplication.getSysParam().set(SysParam.BATCH_NO, String.valueOf(transData.getBatchNo
                ()));

        Calendar calender = Calendar.getInstance();
        int year = calender.get(Calendar.YEAR); //Copies a range of characters into a new string
        String timestamp = String.valueOf(year).substring(2) + transData.getDate() + transData
                .getTime();
        // 更新系统时间            //Converts the specified integer to its string representation
        Device.setSystemTime(timestamp);

        IConvert convert = FinancialApplication.getConvert();

        byte[] f62 = convert.strToBcd(transData.getField62(), EPaddingPosition.PADDING_LEFT);

        // 工作密钥，若长度域不为24或40或56或60或84,格式有误
        //if (f62 == null || f62.length != 40 && f62.length != 60) {
        //sandy :
        // message length should be 44 bytes
        if (f62 == null || f62.length != 44 && f62.length != 60) {
             return TransResult.ERR_TWK_LENGTH;
        }

        int index = 0;
        // PINKEY
        byte[] pinKey = new byte[16];
        byte[] pinKeyKCV = new byte[4];
        if (!isTripDes) {
            pinKey = new byte[8];
            pinKeyKCV = new byte[4];
        }
        System.arraycopy(f62, index, pinKey, 0, pinKey.length);
        index += pinKey.length;
        System.arraycopy(f62, index, pinKeyKCV, 0, pinKeyKCV.length);
        index += pinKeyKCV.length;
        try {
            Device.writeTPK(pinKey, pinKeyKCV);
        } catch (PedDevException e) {
            Log.e(TAG, "", e);
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(e.getErrMsg(), Constants
                        .FAILED_DIALOG_SHOW_TIME);
            }
            return TransResult.ERR_ABORTED;
        }
        // 保存TPK
        FinancialApplication.getGeneralParam().set(GeneralParam.TPK, FinancialApplication.getConvert()
                .bcdToStr(pinKey));

        // MACKEY
        byte[] macKey = new byte[16];
        byte[] macKeyKCV = new byte[4];
        if (isTripDes) {
            // 国密mackey取16字节
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals
                    (supportSmPeriod2)) {
                System.arraycopy(f62, index, macKey, 0, macKey.length); //注意此时的index是沿用上方index的值的
            } else {
                System.arraycopy(f62, index, macKey, 0, macKey.length);
                System.arraycopy(macKey, 0, macKey, 8, 8); //将mackey中的前8位复制到后8位
            }

            index += 16;
            System.arraycopy(f62, index, macKeyKCV, 0, 4);
            index += 4;
        } else {
            macKey = new byte[8];
            macKeyKCV = new byte[4];
            System.arraycopy(f62, index, macKey, 0, macKey.length);
            index += 8;
            System.arraycopy(f62, index, macKeyKCV, 0, 4);
            index += 4;
        }
        try {
            Device.writeTAK(macKey, macKeyKCV);
        } catch (PedDevException e) {

            Log.e(TAG, "", e);
            Device.beepErr();
            if (listener != null) {
                listener.onShowErrMessageWithConfirm(e.getErrMsg(), Constants
                        .FAILED_DIALOG_SHOW_TIME);
            }
            return TransResult.ERR_ABORTED;
        }
        // 保存TAK
        FinancialApplication.getGeneralParam().set(GeneralParam.TAK, FinancialApplication.getConvert()
                .bcdToStr(macKey));

        // TRACKKEY
        if (isTripDes) {
            String trackEncrypt = FinancialApplication.getSysParam().get(SysParam.OTHTC_TRACK_ENCRYPT);
            if (Constant.YES.equals(trackEncrypt) && f62 != null && f62.length > 40) {
                byte[] trackKey = new byte[16];
                byte[] trackKeyKCV = new byte[4];
                System.arraycopy(f62, index, trackKey, 0, 16);
                index += 16;
                System.arraycopy(f62, index, trackKeyKCV, 0, 4);

                try {
                    Device.writeTDK(trackKey, trackKeyKCV);
                } catch (PedDevException e) {

                    Log.e(TAG, "", e);
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(e.getErrMsg(), Constants
                                .FAILED_DIALOG_SHOW_TIME);
                    }
                    return TransResult.ERR_ABORTED;
                }
                // 保存TDK
                FinancialApplication.getGeneralParam()
                        .set(GeneralParam.TDK, FinancialApplication.getConvert().bcdToStr(trackKey));

            }
        }
        FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller.Constant.YES);
        return TransResult.SUCC;
    }

    /**
     * 终端签退,包括修改POS签到状态
     *
     * @param listener
     * @return
     */
    public static int posLogout(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.LOGOUT.toString());
        listener.onUpdateProgressTitle(ETransType.LOGOUT.getTransName());

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller.Constant.NO);
        return TransResult.SUCC;
    }

    // add abdul download product
    public static int posDownloadProduct(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.TOTAL_PRODUCT_PULSA_DATA.toString());
        listener.onUpdateProgressTitle(ETransType.TOTAL_PRODUCT_PULSA_DATA.getTransName());

        // get total product
        int ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        String f62 = Fox.Hex2Txt(transData.getField62());
        String f63 = Fox.Hex2Txt(transData.getField63());
        JsonObject bodyProdc = new JsonObject();
        String[] dat = f62.split("-");
        String[] nopref = f63.split("-");
        for (int i=0; i<dat.length; i++) {
            JsonObject isi = new JsonObject();
            isi.addProperty("operator", dat[i].replace("  ", ""));
            isi.addProperty("prefix", nopref[i].replace("  ", ""));
            bodyProdc.add("body"+i, isi);;
        }
        FinancialApplication.getSysParam().set("prefix", bodyProdc.toString());
        Log.i("abdul", "json prefix = " + FinancialApplication.getSysParam().get("prefix"));


        if (listener != null) {
            listener.onHideProgress();
        }
        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // download product
        int loop = Integer.parseInt(transData.getField48());
        // init ulang transdata
        transData = Component.transInit();
        transData.setTransType(ETransType.DOWNLOAD_PRODUCT_PULSA_DATA.toString());
        listener.onUpdateProgressTitle(ETransType.DOWNLOAD_PRODUCT_PULSA_DATA.getTransName());
        JsonObject body = new JsonObject();

        for (int i=0; i<loop; i++) {

            transData.setField48(String.valueOf(i));
            transData.setTransNo(transData.getTransNo()+1);
            ret = Online.getInstance().online(transData, listener);
            listener.onUpdateProgressTitle(String.format("%d/%d",(i+1),loop));

            //Sandy : Problem in Android 7
            /*
            if (listener != null) {
                listener.onHideProgress();
            }*/

            if (ret != TransResult.SUCC) {
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            String bit62 = Fox.Hex2Txt(transData.getField62());
            JsonObject isi = new JsonObject();

            //XPLOR     XL        PascabayarKARTU XL POSTPAID
            String productid    = bit62.substring(0, 10).trim();
            String operator     = bit62.substring(10, 20).trim();
            String tipe         = bit62.substring(20, 30).trim();
            String productname  = bit62.substring(30, 50).trim();
            String deskripsi    = Fox.Hex2Txt(transData.getField63().trim());

            isi.addProperty("productId", productid);
            isi.addProperty("productName", productname);
            isi.addProperty("productDesc", deskripsi);
            isi.addProperty("operator", operator);
            isi.addProperty("basePrice", transData.getAmount());
            long amt = Long.parseLong(transData.getAmount()) + Long.parseLong(transData.getBalance());
            isi.addProperty("sellPrice", String.valueOf(amt));
            isi.addProperty("fee", transData.getBalance());
            isi.addProperty("type", tipe);
            body.add("body" + i, isi);



        }

        //String xxx = body.toString();
        //Log.i("abdul", "body download = " + body);
        FinancialApplication.getSysParam().set("download", body.toString());
        FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transData.getTransNo()));
        return TransResult.SUCC;
    }



    public static int posDownloadBpjsTkProductLocation(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.TOTAL_PRODUCT_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.TOTAL_PRODUCT_BPJS_TK.getTransName());

        // get total product
        int ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        if (listener != null) {
            listener.onHideProgress();
        }

        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        String f48 = transData.getField48();
        String[] sF48 = f48.split("\\|");
        // location data
        int locationDataLoop = Integer.parseInt(sF48[1]);

        // re-init transaction
        transData = Component.transInit();
        transData.setTransType(ETransType.DOWNLOAD_LOCATION_DATA_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.DOWNLOAD_LOCATION_DATA_BPJS_TK.getTransName());
        int counter = 1;
        int devided = 3;
        Integer total = locationDataLoop / devided;
        int totalDataLocation = Double.valueOf(Math.round(total.intValue())).intValue();

        JSONArray array = new JSONArray();
        JSONObject body = new JSONObject();
        int xx = 0;
        for (int i=0; i<locationDataLoop; i=i+devided) {
            transData.setField48(String.valueOf(i));//index
            transData.setTransNo(transData.getTransNo()+1);
            ret = Online.getInstance().online(transData, listener);
            listener.onUpdateProgressTitle(String.format("%d/%d",counter,totalDataLocation));

            if (ret != TransResult.SUCC) {
                return ret;
            }


            if(transData.getResponseCode().equals("00")){
                String bit61 = Fox.Hex2Txt(transData.getField61());
                String bit62 = Fox.Hex2Txt(transData.getField62());
                String bit63 = Fox.Hex2Txt(transData.getField63());

                if(bit61!=null && Utils.isValidJSON(bit61)){
                    xx = xx+1;
                    array.put(bit61);
                }
                if(bit62!=null && Utils.isValidJSON(bit62)){
                    xx = xx+1;
                    array.put(bit62);
                }
                if(bit63!=null && Utils.isValidJSON(bit63)){
                    xx = xx+1;
                    array.put(bit63);
                }
            }

            counter++;
        }

        try {
            body.put("data", array);


            String x = body.toString();
            int tot = body.getJSONArray("data").length();
            int xxx = xx;
            String yyy= "";
        } catch (JSONException e) {
            e.printStackTrace();
        }



        FinancialApplication.getSysParam().set(SysParam.BPJS_LOCATION_DATA, body.toString());
        FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transData.getTransNo()));
        return TransResult.SUCC;
    }


    //Sandy : Branch Office
    public static int posDownloadBpjsTkProductBranchOffice(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.TOTAL_PRODUCT_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.TOTAL_PRODUCT_BPJS_TK.getTransName());

        // get total product
        int ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }



        if (listener != null) {
            listener.onHideProgress();
        }

        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        String f48 = transData.getField48();
        String[] sF48 = f48.split("\\|");
        // branch office
        int locationDataLoop = Integer.parseInt(sF48[0]);

        // re-init transaction
        transData = Component.transInit();
        transData.setTransType(ETransType.DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK.getTransName());

        int counter = 1;
        int devided = 3;
        Integer total = locationDataLoop / devided;
        int totalDataLocation = Double.valueOf(Math.floor(total.intValue())).intValue();

        JSONArray array = new JSONArray();
        JSONObject body = new JSONObject();
        for (int i=0; i<locationDataLoop; i=i+devided) {
            transData.setField48(String.valueOf(i));//index
            transData.setTransNo(transData.getTransNo()+1);
            ret = Online.getInstance().online(transData, listener);
            listener.onUpdateProgressTitle(String.format("%d/%d",counter,totalDataLocation));

            if (ret != TransResult.SUCC) {
                return ret;
            }

            if(transData.getResponseCode().equals("00")) {
                String bit61 = Fox.Hex2Txt(transData.getField61());
                String bit62 = Fox.Hex2Txt(transData.getField62());
                String bit63 = Fox.Hex2Txt(transData.getField63());
                if (bit61 != null && Utils.isValidJSON(bit61))
                    array.put(bit61);
                if (bit62 != null && Utils.isValidJSON(bit62))
                    array.put(bit62);
                if (bit63 != null && Utils.isValidJSON(bit63))
                    array.put(bit63);
            }

            counter++;
        }

        try{
            body.put("data",array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //String x = body.toString();

        FinancialApplication.getSysParam().set(SysParam.BPJS_BRANCH_OFFICE_DATA, body.toString());
        FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transData.getTransNo()));
        return TransResult.SUCC;
    }



    public static int posDownloadBpjsTkDistrict(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.TOTAL_PRODUCT_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.TOTAL_PRODUCT_BPJS_TK.getTransName());

        // get total product
        int ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        if (listener != null) {
            listener.onHideProgress();
        }

        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        String f48 = transData.getField48();
        String[] sF48 = f48.split("\\|");
        // location data
        int dataLoop = Integer.parseInt(sF48[2]);

        // re-init transaction
        transData = Component.transInit();
        transData.setTransType(ETransType.DOWNLOAD_DISTRICT_DATA_BPJS_TK.toString());
        listener.onUpdateProgressTitle(ETransType.DOWNLOAD_DISTRICT_DATA_BPJS_TK.getTransName());
        int counter = 1;
        int divided = 3;
        Integer total = dataLoop / divided;
        int totalDataLocation = Double.valueOf(Math.round(total.intValue())).intValue();

        JSONArray array = new JSONArray();
        JSONObject body = new JSONObject();

        for (int i=0; i<dataLoop; i=i+divided) {
            transData.setField48(String.valueOf(i));//index
            transData.setTransNo(transData.getTransNo()+1);
            ret = Online.getInstance().online(transData, listener);
            listener.onUpdateProgressTitle(String.format("%d/%d",counter,totalDataLocation));


            if (ret != TransResult.SUCC) {
                return ret;
            }

            if(transData.getResponseCode().equals("00")) {

                String bit61 = Fox.Hex2Txt(transData.getField61());
                String bit62 = Fox.Hex2Txt(transData.getField62());
                String bit63 = Fox.Hex2Txt(transData.getField63());
                if (bit61 != null && Utils.isValidJSON(bit61))
                    array.put(bit61);
                if (bit62 != null && Utils.isValidJSON(bit62))
                    array.put(bit62);
                if (bit63 != null && Utils.isValidJSON(bit63))
                    array.put(bit63);
            }

            counter++;
        }

        try {
            body.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //String x = body.toString();

        FinancialApplication.getSysParam().set(SysParam.BPJS_DISTRICT_DATA, body.toString());
        FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transData.getTransNo()));
        return TransResult.SUCC;
    }







    /**
     * 检查应答码
     *
     * @param transData
     * @param listener
     * @return
     */
    private static int checkRspCode(TransData transData, TransProcessListener listener) {
        if (!"00".equals(transData.getResponseCode())) {
            if (listener != null) {
                listener.onHideProgress();
            }
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                    .getResponseCode());
            transData.setResponseMsg(FinancialApplication.getRspCode().parse(transData.getResponseCode
                    ()).getMessage());
            if (listener != null) {
                //listener.onShowErrMessageWithConfirm(
                listener.onShowErrMessage(
                        TransContext.getInstance().getCurrentContext().getString(R.string.emv_err_code)
                                + responseCode.getCode()
                                + TransContext.getInstance().getCurrentContext().getString(R.string
                                .emv_err_info)
                                + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
            }
            return TransResult.ERR_HOST_REJECT;
        }
        return TransResult.SUCC;
    }

    /**
     * EMV CAPK下载
     *
     * @param listener
     * @return
     */
    public static int emvCapkDl(TransProcessListener listener) {
        TransData transData = null;
        Log.d(TAG,"Sandy=emvCapkDl");
        ArrayList<byte[]> capkInfo = new ArrayList<>();
        int cnt = 0;
        int ret;
        boolean needDl = true;
        EmvCapk.deleteAll();
        FinancialApplication.getController().set(Controller.NEED_DOWN_CAPK, Controller.Constant.YES);
        String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
        while (true) {
            transData = Component.transInit();
            if (supportSm.equals(SysParam.Constant.YES)) {
                ETransType.EMV_MON_CA.setNetCode("373"); // 国密
            } else {
                ETransType.EMV_MON_CA.setNetCode("372");
            }
            transData.setTransType(ETransType.EMV_MON_CA.toString());
            String caMonStr = "1" + String.format("%02d", cnt); //用0作为占位符充满空间
            transData.setField62(caMonStr);
            listener.onUpdateProgressTitle(ETransType.EMV_MON_CA.getTransName());
            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            byte[] field62 = FinancialApplication.getConvert().strToBcd(transData.getField62(),EPaddingPosition.PADDING_LEFT);
            Log.d(TAG,"Sandy=emvCapkDl-field62:" + FinancialApplication.getConvert().bcdToStr(field62) );
            if (field62[0] == 0x30) {
                // 无capk下载
                needDl = false;
                break;
            } else {
                int len = 0;
                len += 1;

                while (len < field62.length - 1) {
                    byte[] capk = new byte[23];
                    System.arraycopy(field62, len, capk, 0, 23); //以23位为一个基准
                    capkInfo.add(capk);
                    Log.d(TAG,"Sandy=emvCapkDl-capk:" + FinancialApplication.getConvert().bcdToStr(capk) );

                    len += 23;
                    cnt++;
                }
                if (field62[0] == 0x32) {
                    continue;
                }
                break;
            }
        }
        if (!needDl) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return TransResult.SUCC;
        }
        // 下载capk
        for (int i = 0; i < cnt; i++) {
            transData = Component.transInit();
            transData.setTransType(ETransType.EMV_CA_DOWN.toString());

            ITlv tlv = FinancialApplication.getPacker().getTlv(); //获取TLV接口
            ITlvDataObjList tlvList = tlv.createTlvDataObjectList(); //创建TLV数据对象列表
            byte[] data = null;
            try {
                ITlvDataObjList list = tlv.unpack(capkInfo.get(i)); //TLV数据解包为TLV数据对象列表
                ITlvDataObj tlv9F06 = list.getByTag(0x9f06); //用指定的TAG获取一个对象, 如果有多个数据对象具有相同的TAG,
                // 则返回第一个对象
                ITlvDataObj tlv9F22 = list.getByTag(0x9f22);
                tlvList.addDataObj(tlv9F06); //添加TVL对象
                tlvList.addDataObj(tlv9F22);
                data = tlv.pack(tlvList); //打包TLV数据对象
            } catch (TlvException e) {
                Log.e(TAG, "", e);
                continue;
            }
            String f62 = FinancialApplication.getConvert().bcdToStr(data);
            transData.setField62(f62);
            if (listener != null) {
                listener.onUpdateProgressTitle(ETransType.EMV_CA_DOWN.getTransName() + "[" + (i + 1) + "/" + cnt + "]");
            }

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                listener.onHideProgress();
                return ret;
            }

            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 保存capk
            byte[] bF62 = FinancialApplication.getConvert().strToBcd(transData.getField62(),
                    EPaddingPosition.PADDING_LEFT);
            if (bF62[0] == 0x30) // 无对应公钥下载
                continue;
            byte[] capk = new byte[bF62.length - 1];
            System.arraycopy(bF62, 1, capk, 0, capk.length);
            try {
                writeCapk(capk);
            } catch (TlvException e) {

                Log.e(TAG, "", e);
            }
        }

        // 下载capk结束
        listener.onUpdateProgressTitle(ETransType.EMV_CA_DOWN_END.getTransName());
        transData = Component.transInit();
        transData.setTransType(ETransType.EMV_CA_DOWN_END.toString());
        ret = Online.getInstance().online(transData, listener);

        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret == TransResult.SUCC) {
            ret = checkRspCode(transData, listener);
            if (ret == TransResult.SUCC) {
                FinancialApplication.getController().set(Controller.NEED_DOWN_CAPK, Controller
                        .Constant.NO);
            }
            return ret;
        }
        return ret;
    }

    /**
     * POS状态参数上送
     * Added by Steven 2017年4月6日17:46:20
     *
     * @param listener
     * @return
     */

    public static int posStatusSubmission(TransProcessListener listener) {
        listener.onUpdateProgressTitle(ETransType.EMV_MON_PARAM.getTransName());
        TransData transData = Component.transInit();
        StringBuilder f62 = new StringBuilder();
        f62.append("011"); //Keyboard status: normal
        f62.append("021"); //PIN pad status: normal
        f62.append("031"); //Card reader status: normal
        f62.append("041"); //Printer status: normal
        f62.append("051"); //Screen status: normal

        f62.append("1160"); //POS application type
        String timeout = FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_TIMEOUT);
        f62.append("12" + timeout); //Time out
        String tryLimit = FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_DTIMES);
        f62.append("13" + tryLimit); //Try limit

        String phoneNo1 = FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_TELNO1, "");
        String phoneNoFormat = String.format("%-14s", phoneNo1); //左对齐，右补空格（如果获得电话号码失败，则全补空格）
        f62.append("14" + phoneNoFormat); //No.1 of the three transaction phone number
        String phoneNo2 = FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_TELNO2, "");
        phoneNoFormat = String.format("%-14s", phoneNo2); //左对齐，右补空格
        f62.append("15" + phoneNoFormat); //No.2 of the three transaction phone number
        String phoneNo3 = FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_TELNO3, "");
        phoneNoFormat = String.format("%-14s", phoneNo3); //左对齐，右补空格
        f62.append("16" + phoneNoFormat); //No.3 of the three transaction phone number

        //"17"由于系统设置中没有，暂时不上送   //One management phone number

        String isSupportTip = FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP, "0");
        f62.append("18" + isSupportTip);  //Whether support tip transaction
        String tipPercentage = FinancialApplication.getSysParam().get(SysParam.TIP_RATE, "00");
        f62.append("19" + tipPercentage);  //Tip percentage
        String isSupportManualKeyIn = FinancialApplication.getSysParam().get(SysParam.OTHTC_KEYIN, "0");
        f62.append("20" + isSupportManualKeyIn);  //Whether support manual key-in of card number
        f62.append("211"); //Whether automatically sign on 设置默认“1”，自动签到
        String resendLimit = FinancialApplication.getSysParam().get(SysParam.RESEND_TIMES, "3");
        f62.append("23" + resendLimit); //Transaction resend limit
        String offlineSubmissionMethod = FinancialApplication.getSysParam().get(SysParam
                .OFFLINETC_UPLOAD_TYPE, "0");
        f62.append("24" + offlineSubmissionMethod); //Offline transaction submission method
        String primaryKeyIndex = FinancialApplication.getSysParam().get(SysParam.MK_INDEX, "1");
        f62.append("25" + primaryKeyIndex); //Primary key index
        f62.append("2710"); //Accumulative transactions to be submitted automatically
        f62.append("51001000100099"); //POS connection rate

        transData.setField62(f62.toString());
        f62.setLength(0);
        transData.setTransType(ETransType.EMV_POS_STATUS_UPLOAD.toString());
        int ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        return ret;
    }

    /**
     * EMV AID下载
     *
     * @param listener
     * @return
     */
    public static int emvAidDl(TransProcessListener listener) {


        TransData transData;
        ArrayList<byte[]> paramInfo = new ArrayList<>();
        ITlv tlv = FinancialApplication.getPacker().getTlv(); //获取TLV接口
        int cnt = 0;
        int ret;
        boolean needDl = true;
        EmvAid.deleteAll();
        FinancialApplication.getController().set(Controller.NEED_DOWN_AID, Controller.Constant.YES);
        while (true) {
            transData = Component.transInit();
            transData.setTransType(ETransType.EMV_MON_PARAM.toString());

            String caMonStr = "1" + String.format("%02d", cnt);
            transData.setField62(caMonStr);
            listener.onUpdateProgressTitle(ETransType.EMV_MON_PARAM.getTransName());
            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }
            byte[] field62 = FinancialApplication.getConvert().strToBcd(transData.getField62(),
                    EPaddingPosition.PADDING_LEFT);
            if (field62[0] == 0x30) {
                // NOTE:提示无参数下载
                needDl = false;
                break;
            } else {
                byte[] f62 = new byte[field62.length - 1];
                System.arraycopy(field62, 1, f62, 0, f62.length);
                List<ITlvDataObj> list;
                try {
                    list = tlv.unpack(f62).getDataObjectList(); //TLV数据解包为TLV数据对象列表//获取TLV数据对象列表
                    for (int i = 0; i < list.size(); i++) {
                        paramInfo.add(tlv.pack(list.get(i))); //打包TLV数据对象
                        cnt++;
                    }
                    // 还有后续参数下载
                    if (field62[0] == 0x32) {
                        continue;
                    }

                } catch (TlvException e) {
                    Log.e(TAG, "", e);
                }
                break;
            }
        }
        if (!needDl) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return TransResult.SUCC;
        }
        // 下载emv aid
        for (int i = 0; i < cnt; i++) {
            transData = Component.transInit();
            transData.setTransType(ETransType.EMV_PARAM_DOWN.toString());
            String f62 = FinancialApplication.getConvert().bcdToStr(paramInfo.get(i));
            transData.setField62(f62);
            if (listener != null) {
                listener.onUpdateProgressTitle(ETransType.EMV_PARAM_DOWN.getTransName() + "[" +
                        (i + 1) + "/" + cnt
                        + "]");
            }

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                listener.onHideProgress();
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 保存aid
            byte[] bF62 = FinancialApplication.getConvert().strToBcd(transData.getField62(),
                    EPaddingPosition.PADDING_LEFT);
            if (bF62[0] == 0x30) // 无对应aid下载
                continue;
            byte[] aid = new byte[bF62.length - 1];
            System.arraycopy(bF62, 1, aid, 0, aid.length);
            try {

                writeAidParam(aid);
            } catch (TlvException e) {

                Log.e(TAG, "", e);
            }
        }

        // 下载aid结束
        listener.onUpdateProgressTitle(ETransType.EMV_PARAM_DOWN_END.getTransName());
        transData = Component.transInit();
        transData.setTransType(ETransType.EMV_PARAM_DOWN_END.toString());
        ret = Online.getInstance().online(transData, listener);

        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret == TransResult.SUCC) {
            ret = checkRspCode(transData, listener);
            if (ret == TransResult.SUCC) {
                FinancialApplication.getController().set(Controller.NEED_DOWN_AID, Controller.Constant
                        .NO);
            }
            return ret;
        }
        return ret;
    }

    /**
     * 黑名单下载
     *
     * @param listener
     * @return
     */
    public static int blackDl(TransProcessListener listener) {
        int ret;
        int cnt = 0;
        TransData transData;

        CardBin.deleteBlack(); // 在下载名单之前，先删除之前的数据
        // 下载黑名单
        for (int i = 0; ; i++) {
            transData = Component.transInit();
            transData.setTransType(ETransType.BLACK_DOWN.toString());
            transData.setField62(String.format("%03d", cnt));
            if (listener != null) {
                listener.onUpdateProgressTitle(ETransType.BLACK_DOWN.getTransName() + "[" + (i +
                        1) + "]");
            }

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 保存黑名单
            byte[] bF62 = transData.getField62().getBytes();
            if (bF62[0] == 0x30) // 无黑名单下载
                break;
            cnt = Integer.parseInt(FinancialApplication.getConvert().bcdToStr(new byte[]{bF62[1],
                    bF62[2], bF62[3]})) + 1;
            byte[] black = new byte[bF62.length - 4];
            System.arraycopy(bF62, 4, black, 0, black.length);
            writeBlack(black);
            if (bF62[0] != 0x32) // 无黑名单下载
                break;
        }

        // 黑名单下载结束
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.BLACK_DOWN_END.getTransName());
        }
        transData = Component.transInit();
        transData.setTransType(ETransType.BLACK_DOWN_END.toString());
        ret = Online.getInstance().online(transData, listener);

        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret == TransResult.SUCC) {
            ret = checkRspCode(transData, listener);
            return ret;
        }
        return ret;

    }

    /**
     * 汇率下载
     *
     * @param listener
     * @return
     */
    public static int rateDl(TransProcessListener listener) {
        int ret;
        TransData transData;

        // 下载汇率
        transData = Component.transInit();
        transData.setTransType(ETransType.RATE_DOWN.toString());
        listener.onUpdateProgressTitle(ETransType.RATE_DOWN.getTransName());
        ret = Online.getInstance().online(transData, listener);

        if (ret != TransResult.SUCC) {
            listener.onHideProgress();
            return ret;
        }
        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // 保存汇率
        byte[] field48 = transData.getField48().getBytes();
        byte[] ratebuf = new byte[field48.length];
        System.arraycopy(field48, 0, ratebuf, 0, ratebuf.length);

        writeRate(ratebuf);

        return TransResult.SUCC;
    }


    /**
     * 保存CAPK参数
     *
     * @param capk
     * @throws TlvException
     */
    private static void writeCapk(byte[] capk) throws TlvException {
        if (capk == null || capk.length == 0) {
            return;
        }

        ITlv iTlv = FinancialApplication.getPacker().getTlv(); //Get TLV interface

        byte[] value = null;
        ITlvDataObjList capkTlvList = iTlv.unpack(capk); //The data is unpacked into a TLV data object list
        EmvCapk emvCapk = new EmvCapk();

        // 9f06 RID
        ITlvDataObj tlvDataObj = capkTlvList.getByTag(0x9f06); //Get an object with the specified TAG
        // If there are multiple data objects with the same TAG, the first object is returned
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue(); //获取VALUE的值
            if (value != null && value.length > 0) {
                emvCapk.setRID(FinancialApplication.getConvert().bcdToStr(value)); //Apply RID
            }
        }

        // 9F2201
        tlvDataObj = capkTlvList.getByTag(0x9f22);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setKeyID(value[0]); //index Key
            }
        }

        // DF02
        tlvDataObj = capkTlvList.getByTag(0xDF02);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setModul(FinancialApplication.getConvert().bcdToStr(value)); // Model
            }
        }

        // DF03
        tlvDataObj = capkTlvList.getByTag(0xDF03);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setCheckSum(FinancialApplication.getConvert().bcdToStr(value));  // Checksum
            }
        }


        // DF04
        tlvDataObj = capkTlvList.getByTag(0xDF04);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setExponent(FinancialApplication.getConvert().bcdToStr(value));  // Index
            }
        }

        // DF05
        tlvDataObj = capkTlvList.getByTag(0xDF05);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                String expDate = "";
                if (value.length == 4) {
                    expDate = FinancialApplication.getConvert().bcdToStr(value).substring(2, 8);
                } else {
                    expDate = new String(value);
                    expDate = expDate.substring(2, 8);
                }
                emvCapk.setExpDate(expDate); // Expiration date (YYMMDD)
            }
        }


        // DF06
        tlvDataObj = capkTlvList.getByTag(0xDF06);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setHashInd(value[0]);  // HASH Algorithm
            }
        }

        // DF07
        tlvDataObj = capkTlvList.getByTag(0xDF07);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvCapk.setArithInd(value[0]);  // RSA Algorithm
            }
        }

        emvCapk.save(); //Save to database
    }

    /**
     * 保持EMV　AID参数
     *
     * @param aid
     * @throws TlvException
     */
    private static void writeAidParam(byte[] aid) throws TlvException {
        if (aid == null || aid.length == 0) {
            return;
        }
        byte[] value = null;

        ITlv iTlv = FinancialApplication.getPacker().getTlv(); //获取Tlv接口

        ITlvDataObjList aidTlvList = iTlv.unpack(aid); //TLV数据解包为TLV数据对象列表
        EmvAid emvAidParam = new EmvAid();

        // 9f06 AID
        ITlvDataObj tlvDataObj = aidTlvList.getByTag(0x9f06); //Get an object with the specified TAG, if there are multiple data objects with the same TAG,
        // 则返回第一个对象
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setAid(FinancialApplication.getConvert().bcdToStr(value)); //aid, Application logo
                //Log.d(TAG, "Sandy=9f06:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF01
        tlvDataObj = aidTlvList.getByTag(0xDF01);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setSelFlag(value[0]); //Selection flag (PART_MATCH part match FULL_MATCH all match)
               // Log.d(TAG, "Sandy=df01:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // 9F08
        tlvDataObj = aidTlvList.getByTag(0x9f08);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setVersion(FinancialApplication.getConvert().bcdToStr(value)); //Application version
               // Log.d(TAG, "Sandy=0x9f08:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF11
        tlvDataObj = aidTlvList.getByTag(0xDF11);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setTacDefualt(FinancialApplication.getConvert().bcdToStr(value)); //Terminal behavior code
                // (缺省)
               // Log.d(TAG, "Sandy=0xDF11:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF12
        tlvDataObj = aidTlvList.getByTag(0xDF12);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setTacOnline(FinancialApplication.getConvert().bcdToStr(value)); //Terminal behavior code (online)
                //Log.d(TAG, "Sandy=0xDF12:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF13
        tlvDataObj = aidTlvList.getByTag(0xDF13);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setTacDenial(FinancialApplication.getConvert().bcdToStr(value)); //Terminal behavior code (rejected)
               // Log.d(TAG, "Sandy=0xDF13:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // 9F1B
        tlvDataObj = aidTlvList.getByTag(0x9F1B);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setFloorLimit(FinancialApplication.getConvert().bcdToStr(value)); //The minimum amount
                emvAidParam.setFloorlimitCheck(1); //Whether to check the minimum amount
               // Log.d(TAG, "Sandy=0x9F1B:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF15
        tlvDataObj = aidTlvList.getByTag(0xDF15);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setThreshold(FinancialApplication.getConvert().bcdToStr(value)); //Threshold
                //Log.d(TAG, "Sandy=0xDF15:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF16
        tlvDataObj = aidTlvList.getByTag(0xDF16);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setMaxTargetPer(Integer.parseInt(FinancialApplication.getConvert()
                        .bcdToStr(value))); //The maximum target percentage
                //Log.d(TAG, "Sandy=0xDF16:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF17
        tlvDataObj = aidTlvList.getByTag(0xDF17);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setTargetPer(Integer.parseInt(FinancialApplication.getConvert().bcdToStr
                        (value))); //The target percentage

               // Log.d(TAG, "Sandy=0xDF17:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF14
        tlvDataObj = aidTlvList.getByTag(0xDF14);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setDDOL(FinancialApplication.getConvert().bcdToStr(value)); //Terminal default DDOL
            }
        }

        // DF18
        tlvDataObj = aidTlvList.getByTag(0xDF18);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                if ((value[0] & 0x01) == 0x01) {
                    emvAidParam.setOnlinePin(true); //Terminal online PIN support capabilities
                } else {
                    emvAidParam.setOnlinePin(false); //Terminal online PIN support capabilities
                }

                Log.d(TAG, "Sandy.TransOnline.0xDF18:" + emvAidParam.getOnlinePin());
            }
        }

        // 9F7B
        tlvDataObj = aidTlvList.getByTag(0x9F7B);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setEcTTLVal(FinancialApplication.getConvert().bcdToStr(value)); //Electronic cash terminal transaction limit
                emvAidParam.setEcTTLFlg(1); //TTL exists? 1- There is an electronic cash terminal transaction limit（EC Terminal Transaction Limit）(9F7B)

                //Log.d(TAG, "Sandy=0x9F7B:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF19
        tlvDataObj = aidTlvList.getByTag(0xDF19);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setRdClssFLmt(FinancialApplication.getConvert().bcdToStr(value)); //Reader non-contact offline minimum (DF19)
                emvAidParam.setRdClssFLmtFlg(1); //Is there a minimum non-contact card reader offline?
                //Log.d(TAG, "Sandy=0xDF19:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // DF20
        tlvDataObj = aidTlvList.getByTag(0xDF20);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                //Reader non-contact transaction limits(DF20)
                emvAidParam.setRdClssTxnLmt(FinancialApplication.getConvert().bcdToStr(value));
                emvAidParam.setRdClssTxnLmtFlg(1); //Is there a card reader contactless transaction limit?
                Log.d(TAG, "Sandy.TransOnline.0xDF20:" +  FinancialApplication.getConvert().bcdToStr(value));

            }
        }

        // DF21
        tlvDataObj = aidTlvList.getByTag(0xDF21);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                emvAidParam.setRdCVMLmt(FinancialApplication.getConvert().bcdToStr(value)); //Card reader non-contact CVM limit(DF21)
                emvAidParam.setRdCVMLmtFlg(1); //Is there a card reader contactless CVM quota?

                //Log.d(TAG, "Sandy=0xDF21:" + FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        //Log.d(TAG, "Sandy================================");
        //sandy, get hardcoded, it should be comes from tag 0xDF20
        //need to change at the host Indopay's side
        emvAidParam.setRdClssTxnLmt("00000000");
        emvAidParam.setRdClssTxnLmtFlg(1);
        boolean s = emvAidParam.save(); //保存AID
        Log.d(TAG, "Sandy================================" + s);
    }

    /**
     * 保存黑名单
     *
     * @param blackList
     */
    private static void writeBlack(byte[] blackList) {
        if (blackList == null)
            return;
        int loc = 0;
        while (loc < blackList.length) {
            int len = Integer.parseInt(new String(new byte[]{blackList[loc], blackList[loc + 1]}));
            byte[] cardNo = new byte[len];
            if (len + loc + 2 > blackList.length) {
                return;
            }
            System.arraycopy(blackList, loc + 2, cardNo, 0, len);
            CardBin cardBin = new CardBin();
            cardBin.setBin(new String(cardNo)); //卡号
            cardBin.saveBlack();
            loc += 2 + len;
        }
    }

    /**
     * 保存汇率
     *
     * @param rateList
     * @throws TlvException
     */
    private static void writeRate(byte[] rateList) {
        if (rateList == null)
            return;
        int loc = 0;
        while (loc < rateList.length) {
            byte[] transCode = new byte[3];
            if (loc + 3 + 3 + 8 > rateList.length) {
                return;
            }
            System.arraycopy(rateList, loc, transCode, 0, 3);
            loc += 3;
            byte[] cardCode = new byte[3];
            if (loc + 3 + 8 > rateList.length) {
                return;
            }
            System.arraycopy(rateList, loc, cardCode, 0, 3);
            loc += 3;
            byte[] rate = new byte[8];
            if (loc + 8 > rateList.length) {
                return;
            }
            System.arraycopy(rateList, loc, rate, 0, 8);
            LoadRate loadRate = new LoadRate();
            loadRate.setTransCode(new String(transCode));
            loadRate.setCardCode(new String(cardCode));
            loadRate.setRate(new String(rate));
            loadRate.saveRate();
            loc += 8;
        }
    }

    /**
     * 下载磁条卡参数
     *
     * @param listener
     * @return
     */
    public static int downloadParam(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.DOWNLOAD_PARAM.toString());
        listener.onUpdateProgressTitle(ETransType.DOWNLOAD_PARAM.getTransName());

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        // 平台拒绝
        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        String f62 = transData.getField62();
        IConvert convert = FinancialApplication.getConvert();
        byte[] paramData = convert.strToBcd(f62, EPaddingPosition.PADDING_LEFT);
        //parse field62, It's format is: code + value.
        parseDownParam(paramData);
        FinancialApplication.getController().set(Controller.NEED_DOWN_CLPARA, Controller.Constant.NO);
        return TransResult.SUCC;
    }

    /**
     * 解析参数
     *
     * @param paramData
     * @return
     */
    private static void parseDownParam(byte[] paramData) {
        int pos = 0;
        String f62 = new String(paramData);
        while (pos < f62.length()) {
            String tag = f62.substring(pos, pos + 2);
            pos += 2;
            //pos application type
            if ("11".equals(tag)) {
                pos += 2;
                continue;
            }
            //Timeout
            if ("12".equals(tag)) {
                String commWaitTime = f62.substring(pos, pos + 2);
                if (commWaitTime.compareTo("40") < 0)
                    commWaitTime = "40";
                FinancialApplication.getSysParam().set(SysParam.COMM_TIMEOUT, commWaitTime);
                pos += 2;
                continue;
            }
            //Try limit
            if ("13".equals(tag)) {
                String tag13 = f62.substring(pos, pos + 1);
                Log.d("teg", "tag13 : "+tag13);
                //FinancialApplication.getSysParam().set(SysParam.PTAG_MODEM_DTIMES, f62.substring(pos, pos + 1));
                FinancialApplication.getSysParam().set(SysParam.PTAG_MODEM_DTIMES, tag13);
                pos++;
                continue;

            }
            //phone 1
            if ("14".equals(tag)) {
                String phone = f62.substring(pos, pos + 14);
                String[] phoneItem = phone.split(" ");
                if (phoneItem.length > 0)
                    FinancialApplication.getSysParam().set(SysParam.PTAG_MODEM_TELNO1, phoneItem[0]);
                pos += 14;
                continue;
            }
            //phone 2
            if ("15".equals(tag)) {
                String phone = f62.substring(pos, pos + 14);
                String[] phoneItem = phone.split(" ");
                if (phoneItem.length > 0)
                    FinancialApplication.getSysParam().set(SysParam.PTAG_MODEM_TELNO2, phoneItem[0]);
                pos += 14;
                continue;
            }
            //phone 3
            if ("16".equals(tag)) {
                String phone = f62.substring(pos, pos + 14);
                String[] phoneItem = phone.split(" ");
                if (phoneItem.length > 0)
                    FinancialApplication.getSysParam().set(SysParam.PTAG_MODEM_TELNO3, phoneItem[0]);
                pos += 14;
                continue;
            }
            //management phone
            if ("17".equals(tag)) {
                pos += 14;
                continue;
            }
            //tip : 0:disable  1.tip 2.adjust
            if ("18".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.SUPPORT_TIP,
                        "0".equals(f62.substring(pos, pos + 1)) ?
                        SysParam.Constant.NO : SysParam.Constant.YES);
                FinancialApplication.getSysParam().set(SysParam.TIP_MODE, f62.substring(pos, pos + 1));
                pos++;
                continue;
            }
            //tip rate
            if ("19".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.TIP_RATE, f62.substring(pos, pos + 2));
                pos += 2;
                continue;
            }

            //manual key-in
            if ("20".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.OTHTC_KEYIN,
                        "1".equals(f62.substring(pos, pos + 1)) ?
                        SysParam.Constant.YES : SysParam.Constant.NO);
                pos++;
                continue;
            }
            //auto sign off
            if ("21".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.SETTLETC_AUTOLOGOUT,
                        "1".equals(f62.substring(pos, pos + 1)) ?
                        SysParam.Constant.YES : SysParam.Constant.NO);
                pos++;
                continue;
            }
            //merchant name
            if ("22".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.MERCH_EN, f62.substring(pos, pos + 40).trim());
                FinancialApplication.getSysParam().set(SysParam.MERCH_CN, f62.substring(pos, pos + 40).trim());

                pos += 40;
                continue;
            }
            //resend times
            if ("23".equals(tag)) {
                String reSendTime = f62.substring(pos, pos + 1);
                if ("0".equals(reSendTime))
                    reSendTime = "1";
                FinancialApplication.getSysParam().set(SysParam.RESEND_TIMES, reSendTime);
                pos++;
                continue;
            }
            //offline method
            if ("24".equals(tag)) {
                FinancialApplication.getSysParam().set(SysParam.OFFLINETC_UPLOAD_TYPE,
                        "1".equals(f62.substring(pos, pos + 1)) ?
                        SysParam.Constant.OFFLINETC_UPLOAD_NEXT : SysParam.Constant
                        .OFFLINETC_UPLOAD_BATCH);
                pos++;
                continue;
            }
            //key index
            if ("25".equals(tag)) {
                pos++;
                continue;
            }
            //support transaction
            if ("26".equals(tag)) {
                byte[] supTxns = new byte[4];
                System.arraycopy(paramData, pos, supTxns, 0, 4);
                maskTrans(supTxns, SysParam.TTS_BALANCE, SysParam.TTS, 1);
                maskTrans(supTxns, SysParam.TTS_PREAUTH, SysParam.TTS, 2);
                maskTrans(supTxns, SysParam.TTS_PAVOID, SysParam.TTS, 3);
                maskTrans(supTxns, SysParam.TTS_PACREQUEST, SysParam.TTS, 4);
                maskTrans(supTxns, SysParam.TTS_PACVOID, SysParam.TTS, 5);
                maskTrans(supTxns, SysParam.TTS_SALE, SysParam.TTS, 6);
                maskTrans(supTxns, SysParam.TTS_VOID, SysParam.TTS, 7);
                maskTrans(supTxns, SysParam.TTS_REFUND, SysParam.TTS, 8);
                maskTrans(supTxns, SysParam.TTS_OFFLINE_SETTLE, SysParam.TTS, 9);
                maskTrans(supTxns, SysParam.TTS_ADJUST, SysParam.TTS, 10);
                maskTrans(supTxns, SysParam.TTS_PACADVISE, SysParam.TTS, 11);
                maskTrans(supTxns, SysParam.OTTS_IC_SCRIPT_PROCESS_RESULT_ADVICE, SysParam.OTTS,12);
                maskTrans(supTxns, SysParam.ECTS_SALE, SysParam.ECTS, 13);
                maskTrans(supTxns, SysParam.OTTS_INSTALLMENT, SysParam.OTTS, 16);
                maskTrans(supTxns, SysParam.OTTS_INSTALLMENTVOID, SysParam.OTTS, 17);
                maskTrans(supTxns, SysParam.OTTS_MOTO, SysParam.OTTS, 23);
                maskTrans(supTxns, SysParam.OTTS_RECURRING, SysParam.OTTS, 26);
                pos += 4;
                continue;
            }

            //sandy : added some parameter
            //need swipe on void?
            if ("27".equals(tag)) {
                pos++;
                continue;
            }

            //sandy : added some parameter
            //need swipe on preauth?
            if ("28".equals(tag)) {
                pos++;
                continue;
            }

            //sandy : added some parameter
            //support refund?
            if ("29".equals(tag)) {
                pos++;
                continue;
            }

            if ("30".equals(tag)) {
                pos++;
                continue;
            }

            if ("31".equals(tag)) {
                pos++;
                continue;
            }
            //sandy : added some parameter
            //MCC
            if ("32".equals(tag)) {
                String MCC = f62.substring(pos, pos + 4);
                Log.d(TAG,"sandy.MCC:" + MCC);
                FinancialApplication.getSysParam().set(SysParam.MCC, MCC);
                pos += 4;
                continue;
            }
            //sandy : added some parameter
            //support preauth?
            if ("33".equals(tag)) {
                pos++;
                continue;
            }

            //ECR?
            if ("34".equals(tag)) {
                pos++;
                continue;
            }

            // add abdul addr 1, 2
            if ("35".equals(tag)) {
                String addr1 = f62.substring(pos, pos + 30).trim();
                Log.d(TAG,"abdul.addr1 :" + addr1);
                FinancialApplication.getSysParam().set(SysParam.ADDR1, addr1);
                pos += 30;
                continue;
            }

            if ("36".equals(tag)) {
                String addr2 = f62.substring(pos, pos + 30).trim();
                //Log.d(TAG,"abdul.addr2 :" + addr2);
                FinancialApplication.getSysParam().set(SysParam.ADDR2, addr2);
                pos += 30;
                continue;
            }
            //show admin fee
            if ("37".equals(tag)) {
                String showAdmin = f62.substring(pos, pos + 1);
                FinancialApplication.getSysParam().set(SysParam.SHOW_ADMIN_FEE, showAdmin);
                pos++;
                continue;
            }

        }
    }

    /**
     * 根据byte[]更新SysParam交易开关
     *
     * @param supTxns
     * @param key
     * @param bitNo
     * @param keyGroup
     * @return
     */
    private static void maskTrans(byte[] supTxns, String key, String keyGroup, int bitNo) {
        byte mask = (byte) 0x80;
        int index = 0;

        if (bitNo > 8) {
            index = 1;
        }
        if (bitNo > 16) {
            index = 2;
        }
        if (bitNo > 24) {
            index = 3;
        }

        mask = (byte) ((mask & 0xff) >> ((bitNo - 1) % 8));
        boolean isSupport = (supTxns[index] & mask) == mask ? true : false;
        if (isSupport) {
            FinancialApplication.getSysParam().updateTSParam(Constant.ACTION_ADD, key, keyGroup);
        } else {
            FinancialApplication.getSysParam().updateTSParam(Constant.ACTION_DELETE, key, keyGroup);
        }
    }

    /**
     * 非接参数下载
     *
     * @param listener
     * @return
     */
    public static int piccDownloadParam(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.PICC_DOWNLOAD_PARAM.toString());
        listener.onUpdateProgressTitle(ETransType.PICC_DOWNLOAD_PARAM.getTransName());

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        // 平台拒绝
        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        // 解析返回数据
        String f62 = transData.getField62();
        IConvert convert = FinancialApplication.getConvert();
        f62 = new String(convert.strToBcd(f62, EPaddingPosition.PADDING_LEFT));

        // 非接交易通道开关
        String value = getPiccParamValue("FF805D", f62, 1);
        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_SWITCH,
                "1".equals(value) ? SysParam.Constant.YES : SysParam.Constant.NO);

        // 闪卡当笔重刷处理时间
        value = getPiccParamValue("FF803A", f62, 3);
        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_BRUSH_TIMES, value);

        // 闪卡记录可处理时间
        value = getPiccParamValue("FF803C", f62, 3);
        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_TIMES, value);
//        // 非接快速业务（QPS）免密限额
//        value = getPiccParamValue("FF8058", f62, 12);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_PIN_FREE_AMOUNT,
//                String.valueOf(Long.parseLong(value)));
//        // 非接快速业务标识
//        value = getPiccParamValue("FF8054", f62, 1);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_FLAG, value.equals("1") ?
// SysParam.Constant.YES
//                : SysParam.Constant.NO);
//        // BIN表A标识
//        value = getPiccParamValue("FF8055", f62, 1);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_BIN_A_FLAG,
//                value.equals("1") ? SysParam.Constant.YES : SysParam.Constant.NO);
//        // BIN表B标识
//        value = getPiccParamValue("FF8056", f62, 1);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_BIN_B_FLAG,
//                value.equals("1") ? SysParam.Constant.YES : SysParam.Constant.NO);
        // CDCVM标识
        value = getPiccParamValue("FF8057", f62, 1);
        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_CDCVM_FLAG,
                "1".equals(value) ? SysParam.Constant.YES : SysParam.Constant.NO);
        // 免签免密限额
        value = getPiccParamValue("FF8059", f62, 12);
        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT,
                String.valueOf(Long.parseLong(value)));

//        // 免签限额
//        value = getPiccParamValue("FF8059", f62, 12);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_SIGN_FREE_AMOUNT,
//                String.valueOf(Long.parseLong(value)));
//        // 免签标识
//        value = getPiccParamValue("FF805A", f62, 1);
//        FinancialApplication.getSysParam().set(SysParam.QUICK_PASS_TRANS_SIGN_FREE_FLAG,
//                value.equals("1") ? SysParam.Constant.YES : SysParam.Constant.NO);

        return piccDownloadParamEnd(listener);

    }

    /**
     * 解析PICC参数
     *
     * @param tag
     * @param piccParam
     * @param valueLen
     * @return
     */
    private static String getPiccParamValue(String tag, String piccParam, int valueLen) {
        int index = piccParam.indexOf(tag); //Searches in this string for the first index of the
        // specified string
        if (index != -1) {
            return piccParam.substring(index + 9, index + 9 + valueLen);
        }
        return null;
    }

    /**
     * PICC参数下载结束
     *
     * @param listener
     * @return
     */
    private static int piccDownloadParamEnd(TransProcessListener listener) {
        // PICC参数下载结束通知
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.PICC_DOWNLOAD_PARAM_END.toString());
        listener.onUpdateProgressTitle(ETransType.PICC_DOWNLOAD_PARAM_END.getTransName());

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        checkRspCode(transData, listener);
        return TransResult.SUCC;
    }

    /**
     * BIN B/C下载
     *
     * @param listener
     * @param isBinB   true-B表， false - C表
     * @return
     */
    public static int binDownload(TransProcessListener listener, boolean isBinB) {
        TransData transData;
        IConvert convert = FinancialApplication.getConvert();
        ETransType transType;
        ArrayList<CardBin> binAllList = new ArrayList<>();
        ArrayList<CardBin> binList;
        int binCnt = -1;
        int i = 1;
        while (true) {
            transData = Component.transInit();
            if (isBinB) {
                transType = ETransType.BIN_B_DOWNLOAD;
            } else {
                transType = ETransType.BIN_C_DOWNLOAD;
            }
            transData.setTransType(transType.toString());
            transData.setField62(String.format("%03d", binCnt + 1));
            if (listener != null) {
                listener.onUpdateProgressTitle(transType.getTransName() + "[" + (i++) + "]");
            }

            int ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }

            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }

            String f62 = transData.getField62();
            f62 = new String(convert.strToBcd(f62, EPaddingPosition.PADDING_LEFT));

            // 解析BIN表
            binList = parseBin(f62);
            if (binList == null) {
                if (listener != null) {
                    listener.onShowErrMessageWithConfirm(
                            FinancialApplication.getAppContext().getString(R.string.err_card_bin),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                    listener.onHideProgress();
                }

                return TransResult.ERR_ABORTED;
            }
            if (isBinB) {
                CardBin.saveBinB(binList);
            } else {
                binAllList.addAll(binList);
            }
            binCnt = Integer.parseInt(f62.substring(1, 4));
            if (f62.charAt(0) != '2') {
                break;
            }
        }
        // 下载结束
        transData = Component.transInit();
        if (isBinB) {
            transType = ETransType.BIN_B_DOWNLOAD_END;
        } else {
            transType = ETransType.BIN_C_DOWNLOAD_END;
        }

        transData.setTransType(transType.toString());
        if (listener != null) {
            listener.onUpdateProgressTitle(transType.getTransName());
        }
        int ret = Online.getInstance().online(transData, listener);
        if (ret == TransResult.SUCC) {
            ret = checkRspCode(transData, listener);
            if (ret == TransResult.SUCC && !isBinB) {
                CardBin.deleteBinC();
                CardBin.saveBinC(binList);
            }
        }
        if (listener != null) {
            listener.onHideProgress();
        }

        return ret;
    }

    /**
     * 解析BIN B/C 表
     *
     * @param f62
     */
    private static ArrayList<CardBin> parseBin(String f62) {
        ArrayList<CardBin> binList = new ArrayList<>();
        String binData = f62.substring(4); //Copies a range of characters into a new string
        int index = 0;
        while (index < binData.length()) {
            int len = Integer.parseInt(binData.substring(index, index + 2));
            if (len > 19 || len < 16) {
                return null;
            }
            index += 2;
            String bin = binData.substring(index, index + 6);
            index += 6;
            CardBin cardBin = new CardBin(bin, len);
            binList.add(cardBin);

        }

        return binList;
    }

    public static int downLoadCheck(boolean checkHeader, boolean checkFirst, TransProcessListener
            listener) {
        int ret = 0;
        Controller controller = FinancialApplication.getController();
        Log.d(TAG,"Sandy=TransOnline.downLoadCheck called!");
        // 报文头
        if (checkHeader) {
            Log.d(TAG,"Sandy=TransOnline.downLoadCheck.checkHeader called!");
            // check REQ_A
            while (true) {
                byte procReqA = (byte) controller.get(Controller.HEADER_PROC_REQ_A);
                if (procReqA == 0x00)
                    break;


                if ((procReqA & PR_STATU) == PR_STATU) {// 上传终端磁条卡状态信息--如果位1为1
                    procReqA &= ~PR_STATU; //将位1置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }

                if ((procReqA & PR_LOGON) == PR_LOGON) { // 签到--如果位2为1
                    ret = TransOnline.posLogon(listener);
                    if (ret != 0) {
                        return ret;
                    }
                    procReqA &= ~PR_LOGON; //将位2置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }

                if ((procReqA & PR_CA) == PR_CA) { //公钥下载--如果位3为1
                    ret = TransOnline.emvCapkDl(listener); // emv CAPK下载
                    if (ret != 0) {
                        return ret;
                    }
                    procReqA &= ~PR_CA; //将位3置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }

                if ((procReqA & PR_ICPARA) == PR_ICPARA) {//IC卡参数下载--如果位4为1
                    ret = TransOnline.emvAidDl(listener); // emv AID下载
                    if (ret != 0) {
                        return ret;
                    }

                    procReqA &= ~PR_ICPARA; //将位4置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }
                if ((procReqA & PR_TMS) == PR_TMS) { // TMS参数下载--如果位5为1
                    procReqA &= ~PR_TMS; //将位5置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }
                if ((procReqA & PR_BLACK) == PR_BLACK) {//--如果位6为1
                    ret = TransOnline.blackDl(listener); // 黑名单下载
                    if (ret != 0) {
                        return ret;
                    }

                    procReqA &= ~PR_BLACK; //将位6置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }

                if ((procReqA & PR_RATE) == PR_RATE) {// 币种汇率下载--如果位7为1
                    procReqA &= ~PR_RATE; //将位7置为0
                    controller.set(Controller.HEADER_PROC_REQ_A, procReqA);
                }
                break;

            }
        }

        // 第一下载检测
        if (checkFirst) {

            Log.d(TAG,"Sandy.TransOnline.downLoadCheck.1!" + controller.get(Controller.NEED_DOWN_CLPARA));

            //Sandy : added this function to get parameter at the first time
            if (controller.get(Controller.NEED_DOWN_CLPARA) == Controller.Constant.YES) {
                ret = TransOnline.downloadParam(listener);
                if (ret != 0) {
                    return ret;
                }
            }

            // emv公钥下载
            if (controller.get(Controller.NEED_DOWN_CAPK) == Controller.Constant.YES) {
                ret = TransOnline.emvCapkDl(listener);
                ret = Integer.parseInt(String.valueOf(ret).replace(" ", ""));
                if (ret != 0) {
                    return ret;
                }
            }
            // emv 参数下载
            if (controller.get(Controller.NEED_DOWN_AID) == Controller.Constant.YES) {
                ret = TransOnline.emvAidDl(listener);
                if (ret != 0) {
                    return ret;
                }

            }

            // 黑名单下载
            if (SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam
                    .FORCE_DL_BLACK))
                    && controller.get(Controller.NEED_DOWN_BLACK) == Controller.Constant.YES) {
                ret = TransOnline.blackDl(listener);
                if (ret != 0) {
                    return ret;
                }
                controller.set(Controller.NEED_DOWN_BLACK, Controller.Constant.NO);
            }

        }

        return TransResult.SUCC;

    }

    /**
     * 结算
     *
     * @param listener
     * @return
     */
    public static int settle(TransTotal total, TransProcessListener listener) {
        int ret;
        if (FinancialApplication.getController().get(Controller.BATCH_UP_STATUS) != Controller
                .Constant.BATCH_UP) {

            // 上送联机交易的电子签名
            /*ret = Transmit.getInstance().sendOnlineSignature(
                    Long.valueOf(FinancialApplication.getSysParam().get(SysParam.TRANS_NO)), listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }*/
            // 处理脱机交易
            ret = Transmit.getInstance().sendOfflineTrans(listener, true);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 上送脱机交易的电子签名
            /*ret = Transmit.getInstance().sendOfflineSignature(listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }*/

            // 重新上送 上送失败的电子签名
            /*ret = Transmit.getInstance().resendErrSignature(listener);
            if (ret != TransResult.SUCC) {
                return ret;
            }*/

            // 处理脚本
            ret = Transmit.getInstance().sendScriptResult(listener);
            if (ret == TransResult.ERR_ABORTED) {
                return ret;
            }
            // 处理冲正
            ret = Transmit.getInstance().sendReversal(listener);
            if (ret == TransResult.ERR_ABORTED) {
                return ret;
            }

            ret = TransOnline.settleRequest(total, listener);
            if (ret != TransResult.SUCC) {
                listener.onHideProgress();
                return ret;
            }
        }

        ret = TransOnline.batchUp(listener);
        if (ret != TransResult.SUCC) {
            listener.onHideProgress();
            return ret;
        }

        return TransResult.SUCC;
    }

    /**
     * 结算请求
     *
     * @param listener
     * @return
     */
    @SuppressLint("DefaultLocale")
    private static int settleRequest(TransTotal total, TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.SETTLE.toString());
        listener.onUpdateProgressTitle(ETransType.SETTLE.getTransName());

        String debitAmt;
        String debitNum;
        String creditAmt;
        String creditNum;

        String fDebitAmt;
        String fDebitNum;
        String fCreditAmt;
        String fCreditNum;

        String buf;
        debitAmt = String.format("%012d", total.getRmbDebitAmount());
        debitNum = String.format("%03d", total.getRmbDebitNum());
        creditAmt = String.format("%012d", total.getRmbCreditAmount());
        creditNum = String.format("%03d", total.getRmbCreditNum());
        buf = debitAmt + debitNum + creditAmt + creditNum + "0";
        fDebitAmt = String.format("%012d", total.getFrnDebitAmount());
        fDebitNum = String.format("%03d", total.getFrnDebitNum());
        fCreditAmt = String.format("%012d", total.getFrnCreditAmount());
        fCreditNum = String.format("%03d", total.getFrnCreditNum());
        buf += fDebitAmt + fDebitNum + fCreditAmt + fCreditNum + "0";
        transData.setField48(buf);

        int ret = Online.getInstance().online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }
        String field48 = transData.getField48();
        char rmbResult = field48.charAt(30);
        char frnResult = field48.charAt(61);
        FinancialApplication.getController().set(Controller.RMB_RESULT,
                Integer.parseInt(Character.toString(rmbResult)));
        FinancialApplication.getController().set(Controller.FRN_RESULT,
                Integer.parseInt(Character.toString(frnResult)));
        // 存结算应答码
        if ((rmbResult == '1') && frnResult == '1') { // 对账平
            FinancialApplication.getController().set(Controller.BATCH_UP_TYPE, Controller.Constant
                    .ICLOG);
        } else if (rmbResult != '1' && frnResult == '1') { // 内卡对账不平
            FinancialApplication.getController().set(Controller.BATCH_UP_TYPE, Controller.Constant
                    .RMBLOG);
        } else if (rmbResult == '1' && frnResult != '1') { // 外卡对账不平
            FinancialApplication.getController().set(Controller.BATCH_UP_TYPE, Controller.Constant
                    .FRNLOG);
        } else if (rmbResult != '1' && frnResult != '1') { // 内外卡对账不平
            FinancialApplication.getController().set(Controller.BATCH_UP_TYPE, Controller.Constant
                    .ALLLOG);
        }
        FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant
                .BATCH_UP);
        FinancialApplication.getController().set(Controller.BATCH_NUM, 0);
        return TransResult.SUCC;
    }

    /**
     * 批上送
     *
     * @param listener
     * @return
     */
    private static int batchUp(TransProcessListener listener) {
        int ret = 0;
        listener.onUpdateProgressTitle(ETransType.BATCH_UP.getTransName());
        // 获取交易记录条数
        long cnt = TransData.getTransCount();
        if (cnt <= 0) {
            FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant
                    .WORKED);
            return TransResult.ERR_NO_TRANS;
        }
        // 获取交易重复次数
        int resendTimes = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam
                .RESEND_TIMES));
        int sendCnt = 0;
        final boolean[] left = new boolean[]{false};
        int batchUpType = FinancialApplication.getController().get(Controller.BATCH_UP_TYPE);
        while (sendCnt < resendTimes + 1) {
            // 1)(对账平不送)全部磁条卡离线类交易，包括离线结算和结算调整
            ret = allMagOfflineBatch(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // 2)(对账平不送)基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)成功交易
            ret = allPbocOfflineBatch(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 3)(不存在)基于PBOC标准的电子钱包IC卡脱机消费成功交易 --- 不存在
            // 4)(对账平不送)全部磁条卡的请求类联机成功交易明细
            ret = allMagCardTransBatch(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 5)(对账平不送)磁条卡和基于PBOC借/贷记标准IC卡的通知类交易明细，包括退货和预授权完成(通知)交易
            ret = adviceTransBatchUp(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 6)(对账平也送)为了上送基于PBOC标准的借/贷记IC卡成功交易产生的TC值，所有成功的IC卡借贷记联机交易明细全部重新上送
            ret = allICCardTransBatchUp(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 7)(对账平也送)为了让发卡方了解基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)交易的全部情况，上送所有失败的脱机消费交易明细
            // 8)(对账平也送)为了让发卡方防范基于PBOC标准的借/贷记IC卡风险交易，上送所有ARPC错但卡片仍然承兑的IC卡借贷记联机交易明细
            ret = allArpcErrIccTransBatchUp(batchUpType, listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            });
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 9)(不存在)为了上送基于PBOC标准的电子钱包IC卡成功圈存交易产生的TAC值，上送所有圈存确认的交易明细
            if (left[0]) {
                left[0] = false;
                sendCnt++;
                continue;
            }
            break;
        }
        // 10)(对账平也送)最后需上送批上送结束报文
        ret = batchUpEnd(batchUpType, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        return TransResult.SUCC;
    }

    /**
     * 结算结束
     *
     * @param listener
     * @return
     */
    private static int batchUpEnd(int batchUpType, TransProcessListener listener) {
        listener.onUpdateProgressTitle(ETransType.BATCH_UP_END.getTransName());
        TransData transData = Component.transInit();
        String f60 = "00" + String.format("%06d", Long.parseLong(FinancialApplication.getSysParam()
                .get(SysParam.BATCH_NO)));
        if (batchUpType != Controller.Constant.ICLOG) { // 对账不平
            f60 += "202";
        } else {
            f60 += "207";
        }
        int batchUpNum = FinancialApplication.getController().get(Controller.BATCH_NUM);
        transData.setField48(String.format("%04d", batchUpNum));
        transData.setField60(f60);
        transData.setTransType(ETransType.BATCH_UP_END.toString());
        int ret = Online.getInstance().online(transData, listener);
        if (ret == TransResult.SUCC) {
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                    .getResponseCode());
            transData.setResponseMsg(responseCode.getMessage());
            // 返回码失败处理
            if (!"00".equals(transData.getResponseCode())
                    && !"94".equals(transData.getResponseCode())) {
                Device.beepErr();
                if (listener != null) {
                    Log.d("teg", "4");
                    listener.onShowErrMessageWithConfirm(

                            TransContext.getInstance().getCurrentContext().getString(R.string
                                    .emv_err_code)
                                    + responseCode.getCode()
                                    + TransContext.getInstance().getCurrentContext().getString(R
                                    .string.emv_err_info)
                                    + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME);
                }
            }
        }

        return ret;
    }

    public interface BatchUpListener {
        void onLeftResult(boolean left);
    }

    /**
     * (对账平不送)全部磁条卡离线类交易，包括离线结算和结算调整
     *
     * @return
     */
    private static int allMagOfflineBatch(int batchUpType, TransProcessListener listener,
                                          BatchUpListener batchUpListener) {
        int ret;
        int cnt;
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        TransData transLog;
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);

        for (cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            if (transLog.getIsUpload()) {
                continue;
            }

            if (batchUpType == Controller.Constant.ICLOG) {
                continue;
            }
            String interOrgCode = transLog.getInterOrgCode();
            if (batchUpType == Controller.Constant.RMBLOG && !"CUP".equals(interOrgCode)) {
                continue;
            }
            if (batchUpType == Controller.Constant.FRNLOG && "CUP".equals(interOrgCode)) {
                continue;
            }

            TransData transLogClone = transLog.clone();
            ETransType transType = ETransType.valueOf(transLog.getTransType());
            if (transType == ETransType.OFFLINE_SETTLE || transType == ETransType.SETTLE_ADJUST
                    || transType == ETransType.SETTLE_ADJUST_TIP) {
                transLogClone.setOrigTransType(transType.toString());

                transLogClone.setTransType(ETransType.NOTICE_TRANS_BAT.toString());

                TransData transData = Component.transInit();
                transLogClone.setHeader(transData.getHeader());
                transLogClone.setTpdu(transData.getTpdu());
                transLogClone.setMerchID(transData.getMerchID());
                transLogClone.setTermID(transData.getTermID());
                ret = Online.getInstance().online(transLogClone, listener);
                if (ret != TransResult.SUCC) {
                    if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                        batchUpListener.onLeftResult(true);
                        continue;
                    }
                    return ret;
                } else if (ret == TransResult.SUCC) {
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transLogClone
                            .getResponseCode());
                    transData.setResponseMsg(responseCode.getMessage());
                    // 返回码失败处理
                    if (!"00".equals(transLogClone.getResponseCode()) &&
                            !"94".equals(transLogClone.getResponseCode())) {
                        Device.beepErr();
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(

                                    TransContext.getInstance().getCurrentContext().getString(R
                                            .string.emv_err_code)
                                            + responseCode.getCode()
                                            + TransContext.getInstance().getCurrentContext()
                                            .getString(R.string.emv_err_info) + responseCode
                                            .getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }

                transLog.setIsUpload(true);
                if (!transLog.updateTrans()) {
                    return TransResult.ERR_ABORTED;
                }
                batchNum++;
                FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
            }
        }
        return TransResult.SUCC;
    }

    /**
     * (对账平不送)基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)成功交易
     */
    private static int allPbocOfflineBatch(int batchUpType, TransProcessListener listener,
                                           BatchUpListener batchUpListener) {
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);

        for (TransData transData : allTrans) {
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transType != ETransType.SALE && transType != ETransType.EC_SALE) {
                continue;
            }
            if (transData.getEmvResult() != ETransResult.OFFLINE_APPROVED.ordinal()) {
                continue;
            }
            String interOrgCode = transData.getInterOrgCode();
            if ((batchUpType == Controller.Constant.RMBLOG) && !"CUP".equals(interOrgCode)) {
                continue;
            }
            if ((batchUpType == Controller.Constant.FRNLOG) && "CUP".equals(interOrgCode)) {
                continue;
            }

            // 已送过的，不送
            if (transData.getIsUpload()) {
                continue;
            }
            // 对账平不送
            if (batchUpType == Controller.Constant.ICLOG) {
                continue;
            }

            TransData transLogClone = transData.clone();
            TransData temptTransData = Component.transInit();
            transLogClone.setHeader(temptTransData.getHeader());
            transLogClone.setTpdu(temptTransData.getTpdu());
            transLogClone.setMerchID(temptTransData.getMerchID());
            transLogClone.setTermID(temptTransData.getTermID());
            transLogClone.setTransType(ETransType.OFFLINE_TRANS_SEND_BAT.toString());
            int ret = Online.getInstance().online(transLogClone, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    batchUpListener.onLeftResult(true);
                    continue;
                }
                return ret;
            } else if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transLogClone
                        .getResponseCode());
                transData.setResponseMsg(responseCode.getMessage());
                // 返回码失败处理
                if (!"00".equals(transLogClone.getResponseCode())
                        && !"94".equals(transLogClone.getResponseCode())) {
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(

                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
            }

            transData.setIsUpload(true);
            if (!transData.updateTrans()) {
                return TransResult.ERR_ABORTED;
            }
            batchNum++;
            FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
        }
        return 0;
    }

    /**
     * 全部磁条卡的请求类联机成功交易明细上送
     *
     * @param batchUpType     批上送类型
     * @param listener
     * @param batchUpListener
     * @return
     */
    private static int allMagCardTransBatch(int batchUpType, TransProcessListener listener,
                                            BatchUpListener batchUpListener) {
        boolean left;
        int ret = TransResult.SUCC;
        int[] sendLoc = new int[8];
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        int offSendCnt = 0;
        String f48 = "";
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);
        TransData transLog = null;

        for (int cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            ETransType transType = ETransType.valueOf(transLog.getTransType());
            transLog.setOrigTransType(transType.toString());
            if (transType == ETransType.AUTH || transType == ETransType.AUTHVOID) {
                continue;
            }
            if (transType == ETransType.REFUND || transType == ETransType.QR_REFUND) {
                continue;
            }
            if (transType == ETransType.AUTH_SETTLEMENT) {
                continue;
            }
            int enterMode = transLog.getEnterMode();
            if (enterMode == EnterMode.QPBOC || enterMode == EnterMode.INSERT || enterMode ==
                    EnterMode.CLSS_PBOC) {
                // 如果是IC卡的简化流程交易，如撤销，预授权完成请求，预授权完成请求撤销等，也是当作磁条卡交易进行上送的
                if (transType != ETransType.VOID && transType != ETransType.AUTHCM
                        && transType != ETransType.AUTHCMVOID) {
                    continue;
                }
            }
            // 已上送的交易不再上送
            if (transLog.getIsUpload()) {
                continue;
            }
            // 对账平不送
            if (batchUpType == Controller.Constant.ICLOG) {
                continue;
            }
            String interOrgCode = transLog.getInterOrgCode();
            if (batchUpType == Controller.Constant.RMBLOG && !"CUP".equals(interOrgCode)) {
                continue;
            }
            if (batchUpType == Controller.Constant.FRNLOG && "CUP".equals(interOrgCode)) {
                continue;
            }
            sendLoc[offSendCnt] = cnt;
            if ("CUP".equals(interOrgCode)) {
                f48 += "00";
            } else {
                f48 += "01";
            }
            f48 += String.format("%06d", transLog.getTransNo());
            if (ETransType.valueOf(transLog.getTransType()) == ETransType.QR_VOID
                    || ETransType.valueOf(transLog.getTransType()) == ETransType.QR_SALE) {
                String c2b = "0000000000" + transLog.getC2b();
                c2b = c2b.substring(c2b.length() - 20, c2b.length());
                f48 += c2b;
            } else {
                String pan = "0000000000" + transLog.getPan();
                pan = pan.substring(pan.length() - 20, pan.length());
                f48 += pan;
            }
            String amt = "000000000000" + transLog.getAmount();
            amt = amt.substring(amt.length() - 12, amt.length());
            f48 += amt;
            offSendCnt++;
            if (offSendCnt != 8) {
                continue;
            }
            TransData transData = Component.transInit();
            f48 = String.format("%1$02d", offSendCnt) + f48;
            transData.setField48(f48);
            transData.setOrigTransType(transType.toString());
            transData.setTransType(ETransType.BATCH_UP.toString());

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) {
                    offSendCnt = 0;
                    left = true; // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    batchUpListener.onLeftResult(left);
                    f48 = "";
                    continue;
                }
                return ret;
            } else if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                        .getResponseCode());
                transData.setResponseMsg(responseCode.getMessage());
                // 返回码失败处理
                if (!"00".equals(transData.getResponseCode())
                        && !"94".equals(transData.getResponseCode())) {
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
            }

            // 更新交易状态
            for (offSendCnt = 0; offSendCnt < 8; offSendCnt++) {
                transData = allTrans.get(sendLoc[offSendCnt]);
                transData.setOrigTransType(transData.getTransType());
                transData.setIsUpload(true);
                transData.updateTrans();
            }
            batchNum += 8;
            FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
            offSendCnt = 0;
            f48 = "";
        }

        // 最后未达8笔的
        if (offSendCnt != 0) {
            TransData transData = Component.transInit();
            f48 = String.format("%1$02d", offSendCnt) + f48;
            transData.setField48(f48);
            transData.setOrigTransType(transLog.getOrigTransType());
            transData.setTransType(ETransType.BATCH_UP.toString());

            ret = Online.getInstance().online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    left = true;
                    batchUpListener.onLeftResult(left);
                    return TransResult.SUCC; // 此处返回SUCC是为了流程能继续
                }
                return ret;
            } else if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                        .getResponseCode());
                transData.setResponseMsg(responseCode.getMessage());
                // 返回码失败处理
                if (!"00".equals(transData.getResponseCode()) &&
                        !"94".equals(transData.getResponseCode())) {
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
            }

            // 更新交易状态
            for (int cnt = 0; cnt < offSendCnt; cnt++) {
                transData = allTrans.get(sendLoc[cnt]);
                transData.setOrigTransType(transData.getTransType());
                transData.setIsUpload(true);
                transData.updateTrans();
            }
            batchNum += offSendCnt;
            FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
        }
        return ret;
    }

    private static int adviceTransBatchUp(int batchUpType, TransProcessListener listener,
                                          BatchUpListener batchUpListener) {
        int ret;
        int cnt;
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        TransData transLog;
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);
        for (cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            if (transLog.getIsUpload()) {
                continue;
            }

            if (batchUpType == Controller.Constant.ICLOG) {
                continue;
            }
            String interOrgCode = transLog.getInterOrgCode();
            if (batchUpType == Controller.Constant.RMBLOG && !"CUP".equals(interOrgCode)) {
                continue;
            }
            if (batchUpType == Controller.Constant.FRNLOG && "CUP".equals(interOrgCode)) {
                continue;
            }

            TransData transLogClone = transLog.clone();
            ETransType transType = ETransType.valueOf(transLog.getTransType());
            if (transType == ETransType.REFUND || transType == ETransType.QR_REFUND
                    || transType == ETransType.AUTH_SETTLEMENT) {
                transLogClone.setOrigTransType(transType.toString());

                transLogClone.setTransType(ETransType.NOTICE_TRANS_BAT.toString());

                TransData transData = Component.transInit();
                transLogClone.setHeader(transData.getHeader());
                transLogClone.setTpdu(transData.getTpdu());
                transLogClone.setMerchID(transData.getMerchID());
                transLogClone.setTermID(transData.getTermID());
                ret = Online.getInstance().online(transLogClone, listener);
                if (ret != TransResult.SUCC) {
                    if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                        batchUpListener.onLeftResult(true);
                        continue;
                    }
                    return ret;
                } else if (ret == TransResult.SUCC) {
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transLogClone
                            .getResponseCode());
                    // 返回码失败处理
                    if (!"00".equals(responseCode.getCode())
                            && !"94".equals(responseCode.getCode())) {
                        Device.beepErr();
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(
                                    TransContext.getInstance().getCurrentContext().getString(R
                                            .string.emv_err_code)
                                            + responseCode.getCode()
                                            + TransContext.getInstance().getCurrentContext()
                                            .getString(R.string.emv_err_info) + responseCode
                                            .getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                }

                transLog.setIsUpload(true);
                if (!transLog.updateTrans()) {
                    return TransResult.ERR_ABORTED;
                }
                batchNum++;
                FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
            }
        }
        return TransResult.SUCC;
    }

    private static int allICCardTransBatchUp(int batchUpType, TransProcessListener listener,
                                             BatchUpListener batchUpListener) {
        int ret;
        int cnt;
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        TransData transLog;
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);
        for (cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            if (transLog.getIsUpload()) {
                continue;
            }
            int enterMode = transLog.getEnterMode();
            if (enterMode != EnterMode.INSERT && enterMode != EnterMode.QPBOC && enterMode !=
                    EnterMode.CLSS_PBOC) {
                continue;
            }
            ETransType transType = ETransType.valueOf(transLog.getTransType());
            if (transType != ETransType.SALE && transType != ETransType.AUTH) {
                continue;
            }
            byte emvResult = transLog.getEmvResult();
            if (emvResult != ETransResult.ONLINE_APPROVED.ordinal()) {
                continue;
            }
            IConvert convert = FinancialApplication.getConvert();
            byte[] tvr;
            String s = transLog.getTvr();
            tvr = convert.strToBcd(s, EPaddingPosition.PADDING_LEFT);
            if ((tvr[4] & 0x40) != 0x00) { // ARPC错
                continue;
            }

            TransData transLogClone = transLog.clone();
            Component.transInit(transLogClone);
            transLogClone.setTransType(ETransType.IC_TC_BAT.toString());
            transLogClone.setTransNo(transLog.getTransNo());
            String f60 = "00" + String.format("%06d", transLog.getBatchNo());
            if (batchUpType != Controller.Constant.ICLOG) { // 对账不平
                f60 += "205";
            } else {
                f60 += "203";
            }
            f60 += "60";
            transLogClone.setField60(f60);
            String f62 = "61";
            if ("CUP".equals(transLog.getInterOrgCode())) {
                f62 += "00";
            } else {
                f62 += "01";
            }
            f62 += "00";
            f62 += transLog.getAmount();
            f62 += FinancialApplication.getSysParam().getCurrency().getCode();
            transLogClone.setField62(f62);
            ret = Online.getInstance().online(transLogClone, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    batchUpListener.onLeftResult(true);
                    continue;
                }
                return ret;
            } else if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transLogClone
                        .getResponseCode());
                transLogClone.setResponseMsg(responseCode.getMessage());
                // 返回码失败处理
                if (!"00".equals(transLogClone.getResponseCode())
                        && !"94".equals(transLogClone.getResponseCode())) {
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
            }

            transLog.setIsUpload(true);
            if (!transLog.updateTrans()) {
                return TransResult.ERR_ABORTED;
            }
            batchNum++;
            FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
        }
        return TransResult.SUCC;
    }

    private static int allArpcErrIccTransBatchUp(int batchUpType, TransProcessListener listener,
                                                 BatchUpListener batchUpListener) {
        int ret;
        int cnt;
        List<TransData> allTrans = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(allTrans)) {
            return TransResult.ERR_NO_TRANS;
        }
        int transCnt = allTrans.size();
        TransData transLog;
        int batchNum = FinancialApplication.getController().get(Controller.BATCH_NUM);
        for (cnt = 0; cnt < transCnt; cnt++) {
            transLog = allTrans.get(cnt);
            if (transLog.getIsUpload()) {
                continue;
            }
            int enterMode = transLog.getEnterMode();
            if (enterMode != EnterMode.INSERT && enterMode != EnterMode.QPBOC && enterMode !=
                    EnterMode.CLSS_PBOC) {
                continue;
            }
            ETransType transType = ETransType.valueOf(transLog.getTransType());
            if (transType != ETransType.SALE && transType != ETransType.AUTH) {
                continue;
            }
            byte emvResult = transLog.getEmvResult();
            if (emvResult != ETransResult.ONLINE_APPROVED.ordinal()) {
                continue;
            }
            IConvert convert = FinancialApplication.getConvert();
            byte[] tvr;
            String s = transLog.getTvr();
            tvr = convert.strToBcd(s, EPaddingPosition.PADDING_LEFT);
            if ((tvr[4] & 0x40) == 0x00) { // ARPC不错不在此送
                continue;
            }
            TransData transLogClone = transLog.clone();
            Component.transInit(transLogClone);
            transLogClone.setTransType(ETransType.IC_FAIL_BAT.toString());
            transLogClone.setTransNo(transLog.getTransNo());
            String f60 = "00" + String.format("%06d", transLog.getBatchNo());
            if (batchUpType != Controller.Constant.ICLOG) { // 对账不平
                f60 += "206";
            } else {
                f60 += "204";
            }
            transLogClone.setField60(f60);
            String f62 = "71";
            if ("CUP".equals(transLog.getInterOrgCode())) {
                f62 += "00";
            } else {
                f62 += "01";
            }
            f62 += "05";
            f62 += transLog.getAmount();
            f62 += FinancialApplication.getSysParam().getCurrency().getCode();
            f62 += "22";
            transLogClone.setField62(f62);
            ret = Online.getInstance().online(transLogClone, listener);
            if (ret != TransResult.SUCC) {
                if (ret == TransResult.ERR_RECV) { // 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
                    batchUpListener.onLeftResult(true);
                    continue;
                }
                return ret;
            } else if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transLogClone
                        .getResponseCode());
                transLogClone.setResponseMsg(responseCode.getMessage());
                // 返回码失败处理
                if (!"00".equals(transLogClone.getResponseCode())
                        && !"94".equals(transLogClone.getResponseCode())) {
                    Device.beepErr();
                    if (listener != null) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
            }

            transLog.setIsUpload(true);
            if (!transLog.updateTrans()) {
                return TransResult.ERR_ABORTED;
            }
            batchNum++;
            FinancialApplication.getController().set(Controller.BATCH_NUM, batchNum);
        }
        return TransResult.SUCC;
    }

    /**
     * 脱机交易上送
     *
     * @param listener isOnline 是否为下一笔联机交易
     * @return
     */
    public static int offlineTransSend(TransProcessListener listener, boolean
            isSendAllOfflineTrans) {
        int ret;
        int sendMaxtime = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam
                .OFFLINETC_UPLOADTIMES)); //离线上送次数
        int maxOfflineNum = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam
                .OFFLINETC_UPLOADNUM)); //自动上送累计笔数

        List<ETransType> types = new ArrayList<>();
        types.add(ETransType.EC_SALE); //电子现金消费
        types.add(ETransType.SALE); //消费
        types.add(ETransType.SETTLE_ADJUST); //结算调整
        types.add(ETransType.SETTLE_ADJUST_TIP); //结算调整
        List<TransData> records = TransData.readTrans(types); //根据交易类型获得交易记录
        List<TransData> notSendRecords = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            return TransResult.SUCC;
        }

        for (TransData transData : records) {
            // 脱机交易未上送过滤  //是否为联机交易
            if (!transData.getIsOnlineTrans() && !transData.getIsOffUploadState() // 是否已脱机上送,
                    // true:脱机上送成功
                    && transData.getSendFailFlag() == 0x00) { //脱机上送失败类型 ：上送失败/平台拒绝
                notSendRecords.add(transData);  // ==的优先级大于&&
            }
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.OFFLINE_TRANS_SEND.getTransName());
        }

        // 累计达到设置中“满足自动上送的累计笔数”，终端应主动拨号上送当前所有的离线类交易和IC卡脱机交易
        if (!isSendAllOfflineTrans && notSendRecords.size() < maxOfflineNum) {
            return TransResult.SUCC;
        }

        // 离线交易上送
        ret = offlineTransProc(sendMaxtime, notSendRecords, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // IC卡脱机交易上送
        ret = icOfflineTransProc(sendMaxtime, notSendRecords, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        return TransResult.SUCC;
    }

    /****************************************************************************
     * offlineTransProc 离线交易上送处理
     ****************************************************************************/
    private static int offlineTransProc(int sendMaxtime, List<TransData> records,
                                        TransProcessListener listener) {
        int dupNum = 0;// 重发次数
        int ret;
        boolean isLastTime = false;
        while (dupNum < sendMaxtime + 1) {
            int sendCount = 0;
            if (dupNum == sendMaxtime) {
                isLastTime = true;
            }
            for (int cnt = 0; cnt < records.size(); cnt++) { // 逐笔上送
                TransData record = records.get(cnt);

                // 跳过非消费类和电子现金消费交易
                if (!record.getTransType().equals(ETransType.OFFLINE_SETTLE.toString()) //离线结算
                        && !record.getTransType().equals(ETransType.SETTLE_ADJUST.toString()) //结算调整
                        && !record.getTransType().equals(ETransType.SETTLE_ADJUST_TIP.toString())
                        ) { //结算调整
                    continue;
                }

                // 已经上送成功的脱机交易记录不需再送
                if (record.getIsOffUploadState()) { ///是否已脱机上送,true:脱机上送成功
                    continue;
                }
                // 跳过上送不成功的和应答码非"00"的交易
                if (record.getSendFailFlag() != 0x00) { //脱机上送失败类型 ：上送失败/平台拒绝
                    continue;
                }
                sendCount++;
                listener.onUpdateProgressTitle(ETransType.OFFLINE_TRANS_SEND.getTransName() + "["
                        + sendCount + "]");
                TransData transData = record.clone();
                Component.transInit(transData);
                transData.setTransNo(record.getTransNo());
                ret = Online.getInstance().online(transData, listener);
                if (ret != TransResult.SUCC) {
                    if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret ==
                            TransResult.ERR_PACK
                            || ret == TransResult.ERR_MAC) {
                        // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败、MAC错)，则直接退出，不进行重发
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(
                                    TransResult.getMessage(TransContext.getInstance()
                                            .getCurrentContext(), ret),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    } else if (ret == TransResult.ERR_RECV && !isLastTime) { //
                        // BCTC要求离线交易上送时，如果平台无应答要离线交易上送次数上送
                        // 未达到上送次数，继续送， 如果已达到上送次数，但接收失败按失败处理，不再上送
                        continue;
                    }

                    record.setSendFailFlag(OfflineStatus.OFFLINE_ERR_SEND);
                    record.updateTrans();
                    continue;

                } else {
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                            .getResponseCode());
                    transData.setResponseMsg(responseCode.getMessage());
                    // 返回码失败处理
                    if ("A0".equals(transData.getResponseCode())) {
                        Device.beepErr();
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    }
                    if (!"00".equals(transData.getResponseCode())
                            && !"94".equals(transData.getResponseCode())) {
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }

                        record.setSendFailFlag(OfflineStatus.OFFLINE_ERR_RESP);
                        record.updateTrans();
                        continue;
                    }

                    record.setSettleDate(transData.getSettleDate() != null ? transData
                            .getSettleDate() : "");
                    record.setAuthCode(transData.getAuthCode() != null ? transData.getAuthCode()
                            : "");
                    record.setRefNo(transData.getRefNo());

                    record.setAcqCenterCode(transData.getAcqCenterCode() != null ? transData
                            .getAcqCenterCode() : "");
                    record.setAcqCode(transData.getAcqCode() != null ? transData.getAcqCode() : "");
                    record.setIsserCode(transData.getIsserCode() != null ? transData.getIsserCode
                            () : "");

                    record.setReserved(transData.getReserved() != null ? transData.getReserved()
                            : "");
                    record.setIssuerResp(transData.getIssuerResp() != null ? transData
                            .getIssuerResp() : "");
                    record.setCenterResp(transData.getCenterResp() != null ? transData
                            .getCenterResp() : "");
                    record.setRecvBankResp(transData.getRecvBankResp() != null ? transData
                            .getRecvBankResp() : "");

                    record.setAuthCode(transData.getAuthCode());
                    record.setIsOffUploadState(true);
                    record.updateTrans();
                }
            }
            dupNum++;
        }
        listener.onHideProgress();
        return TransResult.SUCC;
    }

    /****************************************************************************
     * icOfflineTransProc IC卡脱机交易上送处理
     ****************************************************************************/
    private static int icOfflineTransProc(int sendMaxtime, List<TransData> records,
                                          TransProcessListener listener) {
        int dupNum = 0;// 重发次数
        int ret;
        int sendCount = 0;
        boolean isLastTime = false;
        while (dupNum < sendMaxtime + 1) {
            if (dupNum == sendMaxtime) {
                isLastTime = true;
            }
            for (int cnt = 0; cnt < records.size(); cnt++) { // 逐笔上送
                TransData record = records.get(cnt);

                // 跳过非消费类和电子现金消费交易
                if (!record.getTransType().equals(ETransType.EC_SALE.toString()) //电子现金消费
                        && !record.getTransType().equals(ETransType.SALE.toString())) { //消费
                    continue;
                }
                // 跳过非脱机交易
                if (record.getEmvResult() != (byte) (ETransResult.OFFLINE_APPROVED.ordinal())) {
                    continue;
                }
                // 已经上送成功的脱机交易记录不需再送
                if (record.getIsOffUploadState()) { // 是否已脱机上送,true:脱机上送成功
                    continue;
                }
                // 跳过上送不成功的和应答码非"00"的交易
                if (record.getSendFailFlag() != 0x00) { // 脱机上送失败类型 ：上送失败/平台拒绝
                    continue;
                }
                sendCount++;
                listener.onUpdateProgressTitle(ETransType.OFFLINE_TRANS_SEND.getTransName() + "["
                        + sendCount + "]");
                TransData transData = record.clone();
                Component.transInit(transData);
                transData.setTransType(ETransType.OFFLINE_TRANS_SEND.toString());
                transData.setTransNo(record.getTransNo());

                ret = Online.getInstance().online(transData, listener);
                if (ret != TransResult.SUCC) {
                    if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret ==
                            TransResult.ERR_PACK
                            || ret == TransResult.ERR_MAC) {
                        // 如果是发送数据时发生错误(连接错、发送错、数据包错、MAC错)，则直接退出，不进行重发
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(
                                    TransResult.getMessage(TransContext.getInstance()
                                            .getCurrentContext(), ret),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    } else if (ret == TransResult.ERR_RECV && !isLastTime) { //
                        // BCTC要求脱机交易上送时，如果平台无应答要脱机交易上送次数上送
                        // 未达到上送次数，继续送， 如果已达到上送次数，但接收失败按失败处理，不再上送
                        continue;
                    }
                    record.setSendFailFlag(OfflineStatus.OFFLINE_ERR_SEND);
                    record.updateTrans();
                    continue;

                } else {
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                            .getResponseCode());
                    transData.setResponseMsg(responseCode.getMessage());
                    // 返回码失败处理
                    if ("A0".equals(transData.getResponseCode())) {
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        return TransResult.ERR_ABORTED;
                    }
                    if (!"00".equals(transData.getResponseCode())
                            && !"94".equals(transData.getResponseCode())) {
                        if (listener != null) {
                            listener.onShowErrMessageWithConfirm(responseCode.getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        record.setSendFailFlag(OfflineStatus.OFFLINE_ERR_RESP);
                        record.updateTrans();
                        continue;
                    }

                    record.setSettleDate(transData.getSettleDate() != null ? transData
                            .getSettleDate() : "");
                    record.setRefNo(transData.getRefNo());
                    record.setAcqCenterCode(transData.getAcqCenterCode() != null ? transData
                            .getAcqCenterCode() : "");
                    record.setAcqCode(transData.getAcqCode() != null ? transData.getAcqCode() : "");
                    record.setIsserCode(transData.getIsserCode() != null ? transData.getIsserCode
                            () : "");

                    record.setReserved(transData.getReserved() != null ? transData.getReserved()
                            : "");
                    record.setIssuerResp(transData.getIssuerResp() != null ? transData
                            .getIssuerResp() : "");
                    record.setCenterResp(transData.getCenterResp() != null ? transData
                            .getCenterResp() : "");
                    record.setRecvBankResp(transData.getRecvBankResp() != null ? transData
                            .getRecvBankResp() : "");

                    record.setIsOffUploadState(true);
                    record.updateTrans();
                }
            }
            dupNum++;
        }
        listener.onHideProgress();
        return TransResult.SUCC;
    }

    /**
     * 联机交易的电子签名上送
     *
     * @param listener
     * @return
     */
    public static int onlineSignatureSend(long currTransNo, TransProcessListener listener) {

        int ret;
        int cnt;
        List<TransData> records;
        TransData record = new TransData();
        records = TransData.readAllTrans(); //从数据库中读取所有交易记录
        if (CollectionUtils.isEmpty(records)) {
            return TransResult.SUCC;
        }

        for (cnt = records.size() - 1; cnt >= 0; cnt--) {
            record = records.get(cnt);

            // 没有签名数据
            if (record.getSignData() == null || record.getSignData().length == 0) {
                continue;
            }

            // 只处理联机的未上送的签名交易
            if (record.getSignSendState() != SignSendStatus.SEND_SIG_NO || !record
                    .getIsOnlineTrans()) {
                continue;
            }
            // 本笔流水要等到下笔交易才上送
            if (record.getTransNo() == currTransNo) {
                continue;
            }

            break;
        }

        if (cnt < 0)// 没找到未上送的联机交易的电子签名
        {
            return TransResult.SUCC;
        }

        TransData transData = record.clone();
        Component.transInit(transData);
        transData.setTransNo(record.getTransNo());
        transData.setDate(record.getDate());
        transData.setTime(record.getTime());
        Log.d("teg onlineSignatureSend", "time :"+transData.getTime());
        // 设置55域
        String field55 = prepareSignField55(transData);
        transData.setReceiptElements(field55);
        // 设置交易类型为“电子签名上送”
        transData.setTransType(ETransType.SIG_SEND.toString());

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SIG_SEND.getTransName());
        }
        ret = Online.getInstance().online(transData, listener);
        if (ret == TransResult.SUCC) {
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                    .getResponseCode());
            transData.setResponseMsg(responseCode.getMessage());

            if (!"00".equals(transData.getResponseCode())
                    && !"94".equals(transData.getResponseCode())) {
                // 上送失败
                record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
                // 已经上送过（不论是否成功）
                record.setSignUpload(true);
                record.updateTrans();

                if (listener != null) {
                    if ("55".equals(transData.getResponseCode())) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .sign_upload_failed),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    } else {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .emv_err_code)
                                        + responseCode.getCode()
                                        + TransContext.getInstance().getCurrentContext()
                                        .getString(R.string.emv_err_info) + responseCode
                                        .getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }

                }
            } else {
                // 上送成功
                record.setSignSendState(SignSendStatus.SEND_SIG_SUCC);
                record.setSignUpload(true);
                record.updateTrans();
            }

        } else {

            if (listener != null) {

                if (ret == TransResult.ERR_RECV) {
                    listener.onShowErrMessageWithConfirm(
                            TransContext.getInstance().getCurrentContext().getString(R.string
                                    .sign_recv_failed),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                } else {
                    listener.onShowErrMessageWithConfirm(
                            TransResult.getMessage(TransContext.getInstance().getCurrentContext()
                                    , ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }

            }
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret ==
                    TransResult.ERR_PACK) {
                // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败)，则直接退出

                return TransResult.ERR_ABORTED;
            }
            // 上送失败
            record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
            record.setSignUpload(true);
            record.updateTrans();
        }

        if (listener != null) {
            listener.onHideProgress();
        }

        return TransResult.SUCC;
    }

    /**
     * 离线类交易的电子签名上送
     *
     * @param listener
     * @return
     */
    public static int offlineSignatureSend(TransProcessListener listener) {

        int ret;
        List<TransData> records;

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SIG_SEND.getTransName());
        }
        records = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(records)) {
            return TransResult.SUCC;
        }

        for (int cnt = 0; cnt < records.size(); cnt++) {
            TransData record = records.get(cnt);
            // 没有签名数据
            if (record.getSignData() == null || record.getSignData().length == 0) {
                continue;
            }

            // 只处理离线/脱机的未上送的电子签名               // 未上送          // 是否为联机交易
            if (record.getSignSendState() != SignSendStatus.SEND_SIG_NO || record
                    .getIsOnlineTrans()) {
                continue;
            }

            TransData transData = record.clone();
            Component.transInit(transData);
            transData.setTransNo(record.getTransNo());
            transData.setDate(record.getDate());
            transData.setTime(record.getTime());
            Log.d("teg", "offlineSignatureSend time :"+transData.getTime());
            // 设置55域
            String field55 = prepareSignField55(record);
            transData.setReceiptElements(field55);
            // 设置交易类型为“电子签名上送”
            transData.setTransType(ETransType.SIG_SEND.toString());

            listener.onUpdateProgressTitle(ETransType.SIG_SEND.getTransName());
            ret = Online.getInstance().online(transData, listener);
            if (ret == TransResult.SUCC) {

                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                        .getResponseCode());
                transData.setResponseMsg(responseCode.getMessage());

                if (!"00".equals(transData.getResponseCode())
                        && !"94".equals(transData.getResponseCode())) {
                    // 上送失败
                    record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
                    record.setSignUpload(true);
                    record.updateTrans();
                    if (listener != null) {
                        // 当返回码为55时，提示“电子签名上送失败”
                        if ("55".equals(transData.getResponseCode())) {
                            listener.onShowErrMessageWithConfirm(
                                    TransContext.getInstance().getCurrentContext()
                                            .getString(R.string.sign_upload_failed), Constants
                                            .FAILED_DIALOG_SHOW_TIME);
                        } else {
                            listener.onShowErrMessageWithConfirm(
                                    TransContext.getInstance().getCurrentContext().getString(R
                                            .string.emv_err_code)
                                            + responseCode.getCode()
                                            + TransContext.getInstance().getCurrentContext()
                                            .getString(R.string.emv_err_info) + responseCode
                                            .getMessage(),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                } else {
                    // 上送成功
                    record.setSignSendState(SignSendStatus.SEND_SIG_SUCC);
                    record.setSignUpload(true);
                    record.updateTrans();
                }

            } else {

                if (listener != null) {
                    if (ret == TransResult.ERR_RECV) {
                        listener.onShowErrMessageWithConfirm(
                                TransContext.getInstance().getCurrentContext().getString(R.string
                                        .sign_recv_failed),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    } else {
                        listener.onShowErrMessageWithConfirm(
                                TransResult.getMessage(TransContext.getInstance()
                                        .getCurrentContext(), ret),
                                Constants.FAILED_DIALOG_SHOW_TIME);
                    }
                }
                if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret ==
                        TransResult.ERR_PACK) {
                    // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败)，则直接退出
                    return TransResult.ERR_ABORTED;
                }

                // 上送失败
                record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
                record.setSignUpload(true);
                record.updateTrans();
            }

        }
        listener.onHideProgress();
        return TransResult.SUCC;
    }

    /**
     * 上送失败的电子签名再次上送
     *
     * @param listener
     * @return
     */
    public static int errorSignatureResend(TransProcessListener listener) {

        int ret;
        int dupNum = 0;

        boolean isLastTime = false;
        int sendMaxtime = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam
                .RESEND_SIG_TIMES));
        List<TransData> records;

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SIG_SEND.getTransName());
        }
        records = TransData.readAllTrans();
        if (CollectionUtils.isEmpty(records)) {
            return TransResult.SUCC;
        }

        while (dupNum < sendMaxtime) {

            if (dupNum == sendMaxtime - 1) {
                isLastTime = true;
            }

            for (int cnt = 0; cnt < records.size(); cnt++) {
                TransData record = records.get(cnt);
                if (record.getSignData() == null || record.getSignData().length == 0) {
                    continue;
                }

                // 上送失败的未重上送的交易
                if (record.getSignSendState() != SignSendStatus.SEND_SIG_ERR || !record
                        .getSignUpload()) {
                    continue;
                }

                TransData transData = record.clone();
                Component.transInit(transData);
                transData.setTransNo(record.getTransNo());
                transData.setDate(record.getDate());
                transData.setTime(record.getTime());
                Log.d("teg", "errorSignatureResend time :"+transData.getTime());

                // 设置55域
                String field55 = prepareSignField55(record);
                transData.setReceiptElements(field55);
                // 设置交易类型为“电子签名上送”
                transData.setTransType(ETransType.SIG_SEND.toString());

                listener.onUpdateProgressTitle(ETransType.SIG_SEND.getTransName());
                ret = Online.getInstance().online(transData, listener);
                if (ret == TransResult.SUCC) {
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData
                            .getResponseCode());
                    transData.setResponseMsg(responseCode.getMessage());

                    if (!"00".equals(transData.getResponseCode())
                            && !"94".equals(transData.getResponseCode())) {
                        record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
                        record.updateTrans();
                        if (isLastTime) {
                            printUploadSignFailedTrans(record);
                        }
                    } else {
                        // 上送成功
                        record.setSignSendState(SignSendStatus.SEND_SIG_SUCC);
                        record.updateTrans();
                    }
                } else {
                    if (listener != null) {

                        if (ret == TransResult.ERR_RECV) {
                            listener.onShowErrMessageWithConfirm(
                                    TransContext.getInstance().getCurrentContext().getString(R
                                            .string.sign_recv_failed),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        } else {
                            listener.onShowErrMessageWithConfirm(
                                    TransResult.getMessage(TransContext.getInstance()
                                            .getCurrentContext(), ret),
                                    Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }

                    if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret ==
                            TransResult.ERR_PACK) {
                        // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败)，则直接退出
                        return TransResult.ERR_ABORTED;
                    }

                    // 上送失败
                    record.setSignSendState(SignSendStatus.SEND_SIG_ERR);
                    record.setSignUpload(true);
                    record.updateTrans();
                    if (isLastTime) {
                        printUploadSignFailedTrans(record);
                    }
                }

            }
            dupNum++;
        }

        listener.onHideProgress();
        return TransResult.SUCC;
    }

    /**
     * 电子签名上送55域
     *
     * @param transData
     * @return
     */
    private static String prepareSignField55(TransData transData) {

        ITlv tlv = FinancialApplication.getPacker().getTlv(); //获取TLV接口
        ITlvDataObjList tlvList = tlv.createTlvDataObjectList(); //创建TLV数据对象列表
        ITlvDataObj tlvData; //创建TLV数据对象

        /**
         * 1-交易通用信息
         */
        // 商户名称
        tlvData = buildTLV(ReceiptElements.TAG_FF00, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 交易类型
        tlvData = buildTLV(ReceiptElements.TAG_FF01, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 操作员号
        tlvData = buildTLV(ReceiptElements.TAG_FF02, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 收单机构
        tlvData = buildTLV(ReceiptElements.TAG_FF03, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 发卡机构
        tlvData = buildTLV(ReceiptElements.TAG_FF04, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 有效期
        tlvData = buildTLV(ReceiptElements.TAG_FF05, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 日期时间
        tlvData = buildTLV(ReceiptElements.TAG_FF06, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 授权码
        tlvData = buildTLV(ReceiptElements.TAG_FF07, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 小费金额
        tlvData = buildTLV(ReceiptElements.TAG_FF08, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 卡组织
        tlvData = buildTLV(ReceiptElements.TAG_FF09, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 交易币种
        tlvData = buildTLV(ReceiptElements.TAG_FF0A, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 持卡人电话号码
        tlvData = buildTLV(ReceiptElements.TAG_FF0B, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        /**
         * 2-IC卡有关信息
         */

        // 应用标签
        tlvData = buildTLV(ReceiptElements.TAG_FF30, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 应用名称
        tlvData = buildTLV(ReceiptElements.TAG_FF31, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // 应用标识
        tlvData = buildTLV(ReceiptElements.TAG_FF22, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // IC卡交易证书
        tlvData = buildTLV(ReceiptElements.TAG_FF23, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 充值后卡片余额
        tlvData = buildTLV(ReceiptElements.TAG_FF24, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 转入出卡卡号
        tlvData = buildTLV(ReceiptElements.TAG_FF25, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 不可预知数
        tlvData = buildTLV(ReceiptElements.TAG_FF26, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 应用交互特征
        tlvData = buildTLV(ReceiptElements.TAG_FF27, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 终端验证结果
        tlvData = buildTLV(ReceiptElements.TAG_FF28, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 交易状态信息
        tlvData = buildTLV(ReceiptElements.TAG_FF29, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // 应用交易计数器
        tlvData = buildTLV(ReceiptElements.TAG_FF2A, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 发卡应用数据
        tlvData = buildTLV(ReceiptElements.TAG_FF2B, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        /**
         * 3-创新业务信息
         */

        // 扣持卡人金额备注信息
        tlvData = buildTLV(ReceiptElements.TAG_FF40, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 分期付款期数
        tlvData = buildTLV(ReceiptElements.TAG_FF41, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 分期付款首期金额
        tlvData = buildTLV(ReceiptElements.TAG_FF42, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 分期付款还款币种
        tlvData = buildTLV(ReceiptElements.TAG_FF43, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 持卡人手续费
        tlvData = buildTLV(ReceiptElements.TAG_FF44, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 商品代码
        tlvData = buildTLV(ReceiptElements.TAG_FF45, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 兑换积分数
        tlvData = buildTLV(ReceiptElements.TAG_FF46, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 积分余额数
        tlvData = buildTLV(ReceiptElements.TAG_FF47, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 自付金额
        tlvData = buildTLV(ReceiptElements.TAG_FF48, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 承兑金额
        tlvData = buildTLV(ReceiptElements.TAG_FF49, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 可用金额
        tlvData = buildTLV(ReceiptElements.TAG_FF4A, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 手机号码
        tlvData = buildTLV(ReceiptElements.TAG_FF4B, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        /**
         * 4-原交易信息
         */
        // 原凭证号
        tlvData = buildTLV(ReceiptElements.TAG_FF60, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 原批次号
        tlvData = buildTLV(ReceiptElements.TAG_FF61, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 原参考号
        tlvData = buildTLV(ReceiptElements.TAG_FF62, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 原交易日期
        tlvData = buildTLV(ReceiptElements.TAG_FF63, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 原授权码
        tlvData = buildTLV(ReceiptElements.TAG_FF64, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 原终端号
        tlvData = buildTLV(ReceiptElements.TAG_FF65, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // 打印联数(位数不足前补0)
        tlvData = buildTLV(ReceiptElements.TAG_FF70, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        /**
         * 5-保留信息
         */
        // 发卡方保留域
        tlvData = buildTLV(ReceiptElements.TAG_FF80, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 中国银联保留域
        tlvData = buildTLV(ReceiptElements.TAG_FF81, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }
        // 受理机构保留域
        tlvData = buildTLV(ReceiptElements.TAG_FF82, transData);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        try {
            byte[] data = tlv.pack(tlvList);
            return FinancialApplication.getConvert().bcdToStr(data);
        } catch (TlvException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 函数名:buildTLV 功能描述:组TLV数据包
     *
     * @param elements
     * @return
     */
    private static ITlvDataObj buildTLV(ReceiptElements elements, TransData transData) {
        ITlv tlv = FinancialApplication.getPacker().getTlv(); //获取TLV接口
        ITlvDataObj obj = tlv.createTlvDataObject(); //创建TLV数据对象
        byte[] tag = elements.getTag();
        byte[] value = elements.getValue(transData);

        if (value != null && value.length > 0) {
            obj.setTag(tag);
            obj.setValue(value);
            return obj;
        }
        return null;
    }

    /**
     * 回响功能
     *
     * @param listener
     * @return
     */
    public static int echo(TransProcessListener listener) {
        TransData transData = Component.transInit();
        int ret;
        transData.setTransType(ETransType.ECHO.toString());
        listener.onUpdateProgressTitle(ETransType.ECHO.getTransName());
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        return ret;
    }


    /**
     * 解析RSA公钥
     *
     * @param rsaKey
     * @throws TlvException
     * @throws TlvException
     */
    private static int writeRsaKey(byte[] rsaKey) throws TlvException {

        if (rsaKey == null || rsaKey.length == 0) {
            return TransResult.ERR_PARAM;
        }

        byte[] value;

        ITlv iTlv = FinancialApplication.getPacker().getTlv();
        ITlvDataObjList rsakTlvList = iTlv.unpack(rsaKey);
        GlobalTmkData rsaPukData = new GlobalTmkData();

        // 9F06 RID
        ITlvDataObj tlvDataObj = rsakTlvList.getByTag(0x9F06);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                rsaPukData.setRsaPukRID(FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        // 9F22
        tlvDataObj = rsakTlvList.getByTag(0x9F22);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                rsaPukData.setRsaPukID(value[0]);
            }
        }

        // DF02
        tlvDataObj = rsakTlvList.getByTag(0xDF02);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                rsaPukData.setRsaPukModul(FinancialApplication.getConvert().bcdToStr(value));
                rsaPukData.setRsaPukModulLen(value.length);
            }
        }

        // DF04
        tlvDataObj = rsakTlvList.getByTag(0xDF04);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                rsaPukData.setRsaPukExponent(FinancialApplication.getConvert().bcdToStr(value));
            }
        }

        return TransResult.SUCC;
    }

    /**
     * 解析tmk，并存到PED中
     *
     * @param tmk
     * @throws TlvException
     * @throws TlvException
     */
    private static int writeTMK(byte[] tmk) throws TlvException, PedDevException {

        if (tmk == null || tmk.length == 0) {
            return TransResult.ERR_PARAM;
        }

        IPed ped = Device.getPed();
        ITlv iTlv = FinancialApplication.getPacker().getTlv();

        byte[] value = null;
        byte[] tmkKcv = null;
        byte[] plainTmk = null;
        byte[] tmkCheckValue = null;
        byte[] randomKey = null;
        byte[] checkData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        ITlvDataObjList tmkTlvList = iTlv.unpack(tmk);
        GlobalTmkData tmkData = new GlobalTmkData();

        // DF21 Encrypted TMK
        ITlvDataObj tlvDataObj = tmkTlvList.getByTag(0xDF21);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                randomKey = FinancialApplication.getConvert().strToBcd(tmkData.getRandomKey(),
                        EPaddingPosition.PADDING_LEFT);

                plainTmk = iAlgo.des(IAlgo.ECryptOperation.DECRYPT, IAlgo.ECryptOption.ECB, IAlgo
                                .ECryptPaddingOption.NO_PADDING,
                        value, randomKey, null);
            }
        }

        // DF22 TMK KCV
        tlvDataObj = tmkTlvList.getByTag(0xDF22);
        if (tlvDataObj != null) {
            value = tlvDataObj.getValue();
            if (value != null && value.length > 0) {
                tmkKcv = value;
            }
        }

        tmkCheckValue = iAlgo.des(IAlgo.ECryptOperation.ENCRYPT, IAlgo.ECryptOption.CBC, IAlgo
                        .ECryptPaddingOption.NO_PADDING,
                checkData, plainTmk, null);

        byte[] caclKcv = new byte[4];
        System.arraycopy(tmkCheckValue, 0, caclKcv, 0, 4);

        if (Arrays.equals(caclKcv, tmkKcv)) {
            int tmkIndex = Utils.getMainKeyIndex(Integer.parseInt(FinancialApplication.getSysParam()
                    .get(SysParam.MK_INDEX)));

            ped.writeKey(EPedKeyType.TMK, (byte) 0, EPedKeyType.TMK, (byte) tmkIndex, plainTmk,
                    ECheckMode.KCV_NONE, null);
        } else {
            return TransResult.ERR_TMK_TO_PED;
        }

        return TransResult.SUCC;
    }


    /**
     * TMK下载    added by Wangyb at 20170401
     *
     * @param listener
     * @return
     */

    public static int downloadTmk(TransProcessListener listener) {

        //TMK下载阶段一
        int ret;
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.POS_RSA_KEY_DOWN.toString());
        listener.onUpdateProgressTitle(ETransType.POS_RSA_KEY_DOWN.getTransName());
        ret = Online.getInstance().online(transData, listener);



        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        // 平台拒绝
        if (!"00".equals(transData.getResponseCode())) {
            Log.i(TAG, "TMK Response Code : " + transData.getResponseCode());
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
            listener.onShowErrMessage(String.format("%s \n %s",responseCode.getCode(),responseCode.getMessage()),
                    Constants.FAILED_DIALOG_SHOW_TIME);

            return TransResult.ERR_REAPONSE;
        }

        // 解析返回数据
        byte[] bF62 = FinancialApplication.getConvert().strToBcd(transData.getField62(),EPaddingPosition.PADDING_LEFT);
        try {
            ret = writeRsaKey(bF62);
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } catch (TlvException e) {
            Log.e(TAG, "", e);
        }

        //TMK下载阶段二
        transData = Component.transInit();
        transData.setTransType(ETransType.POS_TMK_DOWN.toString());
        listener.onUpdateProgressTitle(ETransType.POS_TMK_DOWN.getTransName());
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        // 平台拒绝
        if (!"00".equals(transData.getResponseCode())) {
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
            listener.onShowErrMessage(String.format("%s \n %s",responseCode.getCode(),responseCode.getMessage()),
                    Constants.FAILED_DIALOG_SHOW_TIME);
            return TransResult.ERR_REAPONSE;
        }

        //解析返回数据
        bF62 = FinancialApplication.getConvert().strToBcd(transData.getField62(), EPaddingPosition.PADDING_LEFT);
        try {
            ret = writeTMK(bF62);
            if (ret != TransResult.SUCC) {
                return ret;
            }
        } catch (TlvException e) {
            Log.e(TAG, "", e);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return TransResult.ERR_TMK_TO_PED;
        }

        //TMK下载阶段三 激活TMK
        transData = Component.transInit();
        transData.setTransType(ETransType.POS_ACTIVATE_TMK.toString());
        listener.onUpdateProgressTitle(ETransType.POS_ACTIVATE_TMK.getTransName());
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        // 平台拒绝
        if (!"00".equals(transData.getResponseCode())) {
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
            listener.onShowErrMessage(String.format("%s \n %s",responseCode.getCode(),responseCode.getMessage()),
                    Constants.FAILED_DIALOG_SHOW_TIME);

            return TransResult.ERR_REAPONSE;
        }

        return ret;
    }


    /**
     * 参数传递
     *
     * @param listener
     * @return
     */
    public static int paramTransmit(TransProcessListener listener) {
        TransData transData = Component.transInit();
        int ret;
        transData.setTransType(ETransType.PARAM_TRANSMIT.toString());
        listener.onUpdateProgressTitle(ETransType.PARAM_TRANSMIT.getTransName());
        ret = Online.getInstance().online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        return ret;
    }

    /**
     * 打印上送失败的电子签名交易
     *
     * @param transData
     */
    public static void printUploadSignFailedTrans(final TransData transData) {
        transData.setOper(TransContext.getInstance().getOperID());
        ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
        receiptPrintTrans.print(transData, false, null);
    }
}
