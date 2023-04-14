package com.pax.pay.trans.transmit;

import android.util.Log;

import com.pax.dal.IComm;
import com.pax.dal.IComm.EConnectStatus;
import com.pax.dal.IDalCommManager;
import com.pax.dal.entity.ModemParam;
import com.pax.dal.exceptions.CommException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ModemCommunicate extends ACommunicate {
    public static final String TAG = "ModemCommunicate";

    protected IComm modemClient;
    private static ModemCommunicate instance;

    private ModemCommunicate() {
    }

    public synchronized static ModemCommunicate getInstance() {
        if (instance == null) {
            instance = new ModemCommunicate();
        }
        return instance;
    }

    @Override
    public int onConnect() {
        try {
            // 预拨号情况处理
            while (true) {
                if (modemClient == null) {
                    break;
                }
                EConnectStatus status = modemClient.getConnectStatus(); //获取连接状态
                if (status == EConnectStatus.CONNECTED) {
                    return TransResult.SUCC;
                } else if (status == EConnectStatus.CONNECTING) {
                    continue;
                } else if (status == EConnectStatus.DISCONNECTED) {
                    break;
                }
            }

            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_dail)); //dial
            int dialTimes = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_DTIMES));
            for (int i = 0; i < dialTimes; i++) {
                modemClient = getModemClient(setModemCommParam(true));
                if (modemClient == null) {
                    return TransResult.ERR_COMM_CHANNEL;
                }
                if (modemClient.getConnectStatus() == EConnectStatus.CONNECTED) {
                    return TransResult.SUCC;
                }
                modemClient.connect(); //连接
            }
        } catch (CommException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "", e);
        }

        return TransResult.ERR_CONNECT;
    }

    @Override
    public int onSend(byte[] data) {
        try {
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_send));
            // 最大可发送2K字节
            modemClient.send(data); //发送数据
            return TransResult.SUCC;
        } catch (CommException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "", e);
        }
        return TransResult.ERR_SEND;
    }

    @Override
    public CommResponse onRecv() {
        try {
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_recv));

            // 单位10s
            long timeout = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_TIMEOUT));
            long start = System.currentTimeMillis();
            byte[] result = null;
            while (true) {
                if (System.currentTimeMillis() - start > timeout * 10 * 1000) {
                    break;
                }
                if (result == null || result.length == 0) {
                    result = modemClient.recvNonBlocking(); //在非阻塞模式下尽可能多的接收数据（即立即返回）
                }
                if (result != null && result.length != 0) {
                    // 校验第一个字节是否为 60
                    if (result[0] != 0x60) {
                        return new CommResponse(TransResult.ERR_RECV, null);
                    }
                    return new CommResponse(TransResult.SUCC, result);
                }
            }
            if (result == null || result.length == 0) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "", e);
        } catch (CommException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "", e);
        }

        return new CommResponse(TransResult.ERR_RECV, null);
    }

    @Override
    public void onClose() {
        try {
            if (modemClient != null) {
                modemClient.disconnect();
                modemClient = null;
            }
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }
    }

    public void cancelRecv() {
        // 在阻塞模式下取消接收数据
        modemClient.cancelRecv();
    }

    public CommResponse recvBlocking() {
        try {
            // 在阻塞模式下接收指定长度数据
            byte[] result = modemClient.recv(2048);
            if (result == null) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(result);
            result = baos.toByteArray();
            return new CommResponse(TransResult.SUCC, result);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } catch (CommException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "", e);
        }

        return new CommResponse(TransResult.ERR_RECV, null);
    }

    public void reset() {
        if (modemClient != null) {
            modemClient.reset();
        }

    }

    /**
     * MODEM 预拨号处理
     */
    public void preDial() {
        SysParam sysParam = FinancialApplication.getSysParam();
        String commType = sysParam.get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (commType.equals(SysParam.Constant.COMMTYPE_MODEM)) {
            if (sysParam.get(SysParam.PTAG_MODEM_PRE_DIAL).equals(SysParam.Constant.YES)) {
                try {

                    // 预拨号 ---拨telephone Number 1
                    modemClient = getModemClient(setModemCommParam(false));
                    modemClient.connect();
                } catch (CommException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "", e);
                }
            }
        }
    }

    /**
     * 获取ModemClient
     * 
     * @param param
     *            ModemParam 参数
     * @return
     */
    public IComm getModemClient(ModemParam param) {
        IDalCommManager commManager = FinancialApplication.getDal().getCommManager();
        if (modemClient != null) {
            modemClient = null;
        }
        modemClient = commManager.getModemComm(param);
        return modemClient;
    }

    /**
     * 设置Modem通讯的相关参数
     */
    public ModemParam setModemCommParam(boolean isDialBlocking) {

        SysParam sysParam = FinancialApplication.getSysParam();
        String commType = sysParam.get(SysParam.APP_COMM_TYPE_ACQUIRER);
        ModemParam modemParam = new ModemParam();
        if (commType.equals(SysParam.Constant.COMMTYPE_MODEM)) {
            // modem参数设置

            modemParam.setTelNo1(sysParam.get(SysParam.PTAG_MODEM_TELNO1)); // 电话号码1号
            modemParam.setTelNo2(sysParam.get(SysParam.PTAG_MODEM_TELNO2));// 电话号码2号
            modemParam.setTelNo3(sysParam.get(SysParam.PTAG_MODEM_TELNO3)); // 电话号码3号

            // 需要外线设置
            if (sysParam.get(SysParam.PTAG_MODEM_NEED_EXTER_LINE).equals(SysParam.Constant.YES)) {
                modemParam.setExtNum(sysParam.get(SysParam.PTAG_MODEM_PABX));// 外线号码
                modemParam.setDelayTime(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_PABXDELAY))); // 外线拨号延迟
            }
            modemParam.setTimeout(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_TIMEOUT))); // 通信超时
            modemParam.setAsyncMode(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_ASMODE))); // 异步通信模式
            modemParam.setSsetup(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_SSETUP))); // 通信字节设置

            String dialType = sysParam.get(SysParam.PTAG_MODEM_DP);
            int dp = 0;
            if (dialType.equals(SysParam.Constant.MODEM_DP_00)) {
                dp = 0x00;
            } else if (dialType.equals(SysParam.Constant.MODEM_DP_01)) {
                dp = 0x01;
            } else if (dialType.equals(SysParam.Constant.MODEM_DP_02)) {
                dp = 0x02;
            }
            modemParam.setDp(dp);// 音频拨号脉冲

            if (sysParam.get(SysParam.PTAG_MODEM_CHDT).equals(SysParam.Constant.YES)) {
                modemParam.setChdt(1);// 检查拨号音
            } else if (sysParam.get(SysParam.PTAG_MODEM_CHDT).equals(SysParam.Constant.NO)) {
                modemParam.setChdt(0);// 检查拨号音
            }

            modemParam.setDialBlocking(isDialBlocking);

            modemParam.setDt1(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_DT1)));// 从拿起电话到拨号的等待时间
            modemParam.setDt2(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_DT2)));// 当拨打外线等待的时间
            modemParam.setHt(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_HT)));// 一个数字双音频拨号的持续时间
            modemParam.setWt(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_WT)));// 双音频拨号时拨两个数字的时间间隔
            modemParam.setLevel(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_LEVEL)));// 电频设置
            modemParam.setRedialTimes(Integer.parseInt(sysParam.get(SysParam.PTAG_MODEM_DTIMES)));// 重拨次数
        }

        return modemParam;
    }

    @SuppressWarnings("unused")
    private int calcTimeoutBySsetup() {

        int timeout = 30;
        int ssetup = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.PTAG_MODEM_SSETUP));

        switch (ssetup & 0x07) {
            case 0x00:
                // 000
                timeout = 5;
                break;
            case 0x01:
                // 001
                timeout = 8;
                break;
            case 0x02:
                // 010
                timeout = 11;
                break;
            case 0x03:
                // 011
                timeout = 14;
                break;
            case 0x04:
                // 100
                timeout = 17;
                break;
            case 0x05:
                // 101
                timeout = 20;
                break;
            case 0x06:
                // 110
                timeout = 23;
                break;
            case 0x07:
                // 111
                timeout = 26;
                break;
            default:
                break;
        }
        return timeout;
    }
}
