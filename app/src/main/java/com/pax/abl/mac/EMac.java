package com.pax.abl.mac;

import android.util.Log;

import com.pax.dal.IPed;
import com.pax.dal.exceptions.PedDevException;
import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.settings.SysParam;

public enum EMac {
    CUP { // 银联Mac算法
        @Override
        public byte[] getMac(IPed ped, byte keyIndex, byte[] data) {
            String supportSm = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM);
            String supportSmPeriod2 = FinancialApplication.getSysParam().get(SysParam.SUPPORT_SM_PERIOD_2);
            byte[] tmpbuf = new byte[8];
            if (SysParam.Constant.YES.equals(supportSm) && SysParam.Constant.YES.equals(supportSmPeriod2)) { // 国密mac
                tmpbuf = new byte[16];
            }
            int len;
            byte[] dataIn = new byte[data.length + tmpbuf.length];
            len = data.length / tmpbuf.length + 1;

            System.arraycopy(data, 0, dataIn, 0, data.length);

            for (int i = 0; i < len; i++) {
                for (int k = 0; k < tmpbuf.length; k++) {
                    tmpbuf[k] ^= dataIn[i * tmpbuf.length + k];  //按位异或运算(将data的前tmpbuf.length位与接下来的tmpbuf.length
                }                                                //位异或，得到的值与后续的tmpbuf.length位异或，做len次)
            }

            String beforeCalcMacData = FinancialApplication.getConvert().bcdToStr(tmpbuf);

            try {
                byte[] mac = Device.calcMac(beforeCalcMacData.getBytes());
                return FinancialApplication.getConvert().bcdToStr(mac).substring(0, 8).getBytes();
            } catch (PedDevException e) {
                Log.e("Emac", "", e);
            }
            return null;
        }
    },

    ;
    public abstract byte[] getMac(IPed ped, byte keyIndex, byte[] data);
}
