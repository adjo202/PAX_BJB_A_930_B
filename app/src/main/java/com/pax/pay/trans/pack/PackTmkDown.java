package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.algo.IAlgo;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.tmk.GlobalTmkData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;

import java.util.Arrays;
import java.util.Random;

import static com.pax.pay.trans.transmit.TransOnline.iAlgo;

/**
 * Created by wangyb on 2017/4/1.
 */

public class PackTmkDown extends PackIso8583 {
    private static final String TAG = "PackTmkDown";

    public PackTmkDown(PackListener listener) {
        super(listener);
    }


    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 25, 41, 42, 60, 62};
    }

    /**
     * 产生numSize位16进制的数
     */
    private String getRandomValue(int numSize) {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < numSize; i++) {
            char temp = 0;
            boolean key = random.nextBoolean();
            if (key) {
                temp = (char) (random.nextInt(10) + 48);//产生随机数字
            } else {
                temp = (char) (random.nextInt(6) + 'a');//产生a-f
            }
            str.append(temp);
        }

        return str.toString();
    }

    /**
     * tmk 下载第二阶段，对62域进行TLV打包
     *
     * @param tag
     * @param value
     * @return
     */
    private static ITlv.ITlvDataObj structureTLV(byte[] tag, byte[] value) {
        ITlv tlv = FinancialApplication.getPacker().getTlv();
        ITlv.ITlvDataObj obj = tlv.createTlvDataObject();

        if (value != null && value.length > 0) {
            obj.setTag(tag);
            obj.setValue(value);
            return obj;
        }
        return null;
    }

    /**
     * TMK下载第二阶段使用,组装62域
     *
     * @throws TlvException
     */
    @Override
    protected void setBitData62(TransData transData) throws Iso8583Exception {

        GlobalTmkData tmkData = new GlobalTmkData();

        ITlv tlv = FinancialApplication.getPacker().getTlv();
        ITlv.ITlvDataObjList tlvList = tlv.createTlvDataObjectList();
        ITlv.ITlvDataObj tlvData;

        int rsaPukId;
        String rsaPukRid;
        String randomValue;
        byte[] checkValue;
        byte[] randomTmk;
        byte[] cipherRandomTmk;
        byte[] checkData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] kcv = new byte[4];

        rsaPukId = tmkData.getRsaPukID();  //RSA_PUK 后台索引号

        rsaPukRid = tmkData.getRsaPukRID();// RSA_PUK RID

        randomValue = getRandomValue(32);  //生成32位随机数

        tmkData.setRandomKey(randomValue);

        randomTmk = FinancialApplication.getConvert().strToBcd(randomValue, IConvert.EPaddingPosition.PADDING_LEFT);//转换成16字节BCD码，随机密钥

        checkValue = iAlgo.des(IAlgo.ECryptOperation.ENCRYPT, IAlgo.ECryptOption.CBC, IAlgo.ECryptPaddingOption.NO_PADDING,
                checkData, randomTmk, null);

        System.arraycopy(checkValue, 0, kcv, 0, 4);

        int moudlLen = tmkData.getRsaPukModulLen();
        byte[] randomTmkPack = new byte[moudlLen];
        randomTmkPack[0] = 0x00;
        randomTmkPack[1] = 0x02;
        Arrays.fill(randomTmkPack, 2, moudlLen - 16 - 1, (byte) 0xFF);
        randomTmkPack[moudlLen - 1 - 16] = 0x00;
        System.arraycopy(randomTmk, 0, randomTmkPack, moudlLen - 16, 16);

        byte[] pukExp = FinancialApplication.getConvert().strToBcd(tmkData.getRsaPukExponent(),IConvert.EPaddingPosition.PADDING_LEFT);
        //为兼容A920C modulus 前补上一个字节 0x00
        byte[] pukMoudl = FinancialApplication.getConvert().strToBcd("00" + tmkData.getRsaPukModul(),IConvert.EPaddingPosition.PADDING_LEFT);
        cipherRandomTmk = iAlgo.getRsa().encryptWithPublicKey(iAlgo.getRsa().genPublicKey(pukMoudl, pukExp), randomTmkPack, IAlgo.IRsa.PaddingOption.NO_PADDING);

        /**
        Log.d(TAG,"Sandy=randomKey=" + FinancialApplication.getConvert().bcdToStr(randomTmk));
        Log.d(TAG,"Sandy=checkValue=" + FinancialApplication.getConvert().bcdToStr(checkValue) );
        Log.d(TAG,"Sandy=cipherRandomTmk=" + FinancialApplication.getConvert().bcdToStr(cipherRandomTmk));
        Log.d(TAG,"Sandy=modulus=" + FinancialApplication.getConvert().bcdToStr(pukMoudl));
        **/



        // 9F06 RID
        tlvData = structureTLV(new byte[]{(byte) 0x9F, (byte) 0x06},
                FinancialApplication.getConvert().strToBcd(rsaPukRid, IConvert.EPaddingPosition.PADDING_LEFT));
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // 9F22  PUK ID
        tlvData = structureTLV(new byte[]{(byte) 0x9F, (byte) 0x22},
                FinancialApplication.getConvert().strToBcd(Integer.toString(rsaPukId), IConvert.EPaddingPosition.PADDING_LEFT));
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // DF23 encrypted random key
        tlvData = structureTLV(new byte[]{(byte) 0xDF, (byte) 0x23}, cipherRandomTmk);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);
        }

        // DF24 KCV
        tlvData = structureTLV(new byte[]{(byte) 0xDF, (byte) 0x24}, kcv);
        if (tlvData != null) {
            tlvList.addDataObj(tlvData);

        }

        try {
            byte[] data = tlv.pack(tlvList);
            //Log.d(TAG,"Sandy=setBitData62 called!" + FinancialApplication.getConvert().bcdToStr(data));
            String sdata = FinancialApplication.getConvert().bcdToStr(data);
            setBitData("62", data);

        } catch (TlvException | Iso8583Exception e) {
            AppLog.e(TAG, "", e);
        }
    }


    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        StringBuilder f60 = new StringBuilder(transType.getFuncCode()); // f60.1：transaction
        // type// code[N2]
        f60.append(String.format("%06d", transData.getBatchNo())); // f60.2: Batch number[N6]
        f60.append(transType.getNetCode());// f60.3: network management information code[N3]


        Log.d(TAG,"Sandy=TMKsetBitData60 called!" + f60);
        setBitData("60", f60.toString());
    }


}

