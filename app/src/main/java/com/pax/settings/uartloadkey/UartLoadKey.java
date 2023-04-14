package com.pax.settings.uartloadkey;

import android.util.Log;

import com.pax.dal.IComm;
import com.pax.dal.IPed;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.EUartPort;
import com.pax.dal.entity.UartParam;
import com.pax.dal.exceptions.CommException;
import com.pax.dal.exceptions.PedDevException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.AppLog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UartLoadKey {
    private static final String TAG = "UartLoadKey";

    private static final int RET_ERR_START = -100; // 错误码起始值
    public static final int RET_ERR_END = -200; // 错误码结束值
    public static final int RET_ERR_PORT = RET_ERR_START - 1; // 打开端口出错
    public static final int RET_ERR_USER_CANCEL = RET_ERR_START - 2; // 用户取消
    public static final int RET_ERR_RECV = RET_ERR_START - 3; // 接收错误
    public static final int RET_ERR_VERIFY = RET_ERR_START - 4; // 校验取消
    public static final int RET_ERR_INDEX = RET_ERR_START - 5; // 密钥索引非法
    public static final int RET_ERR_MODE = RET_ERR_START - 6; // 加解密模式非法
    public static final int RET_ERR_TMK = RET_ERR_START - 7; // 主密钥不存在
    public static final int RET_ERR_OTHER = RET_ERR_START - 8; // 其他错误

    private static UartLoadKey aipUartLoadKey;

    private boolean isExit = false;
    private boolean isDebug = false;
    private String udpHostIp = "";
    private int udpPort = 0;

    private IComm uartComm;
    private IPed ped;

    private UartLoadKey() {
    }

    public static synchronized UartLoadKey getInstance() {
        if (aipUartLoadKey == null) {
            aipUartLoadKey = new UartLoadKey();
        }
        return aipUartLoadKey;
    }

    public boolean open(EUartPort uartPort, String param) {
        UartParam uartParam = new UartParam();
        uartParam.setAttr(param);
        uartParam.setPort(uartPort);
        try {
            uartComm = FinancialApplication.getDal().getCommManager().getUartComm(uartParam);
            ped = FinancialApplication.getDal().getPed(EPedType.INTERNAL);
            uartComm.connect();
            uartComm.reset();
            return true;
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }

        return false;
    }

    public void close() {
        try {
            uartComm.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public void cancel() {
        isExit = true;
        uartComm.cancelRecv();
        close();
    }

    public int loadKey() {
        isExit = false;
        int ret = RET_ERR_USER_CANCEL;
        while (!isExit) {
            byte[] recvData = new byte[100];
            ret = recvSecurePosCmd(recvData);
            if (ret != 0) {
                continue;
            }
            AppLog.i(TAG, "recvData:" + FinancialApplication.getConvert().bcdToStr(recvData));
            switch (recvData[0]) {
                case (byte) 0x80:
                    ret = loadmasterKey(recvData);
                    isExit = true;
                    break;
                case (byte) 0x81:
                case (byte) 0x82:
                case (byte) 0x8e:
                    sendErrorData(recvData[0], (byte) 0x00);
                    break;
                default:
                    sendErrorData(recvData[0], (byte) 0x00);
                    break;
            }

        }
        return ret;
    }

    private int recvSecurePosCmd(byte[] recvData) {
        byte[] recv;
        int iRecvLen = 0;

        recv = receive(1, 2000);

        if (recv == null || recv.length == 0)
            return RET_ERR_RECV;

        if (recv[0] < (byte) 0x80 || recv[0] > (byte) 0x95) {
            return RET_ERR_RECV;
        }

        recvData[0] = recv[0];
        recv = receive(1, 500);
        if (recv == null)
            return RET_ERR_RECV;

        iRecvLen = recv[0];
        recvData[1] = recv[0];
        switch (recvData[0]) {
            case (byte) 0x80:
                if (iRecvLen == 10 || iRecvLen == 18) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x81:
                if (iRecvLen == 11) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x82:
            case (byte) 0x92:
                if (iRecvLen == 4) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x83:
                if (iRecvLen == 12 || iRecvLen == 19 || iRecvLen == 22) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x84:
            case (byte) 0x95:
                if (iRecvLen == 19) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x87:
                if (iRecvLen == 10) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x88:
            case (byte) 0x8e:
            case (byte) 0x90:
                if (iRecvLen == 0) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x89:
                if (iRecvLen == 1) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x8c:
                if (iRecvLen == 3) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x8d:
            case (byte) 0x93:
                if (iRecvLen == 2) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x8f:
                if (iRecvLen == 0xf5) {
                    break;
                }
                return RET_ERR_RECV;

            case (byte) 0x85:
            case (byte) 0x86:
            case (byte) 0x8a:
            case (byte) 0x8b:
            case (byte) 0x91:
            case (byte) 0x94:
                break;

            default:
                return RET_ERR_RECV;
        }

        // 需要多收一个校验位
        recv = receive(iRecvLen + 1, 3000);
        if (recv == null)
            return RET_ERR_RECV;
        System.arraycopy(recv, 0, recvData, 2, recv.length);
        if (getEdc(recvData) != recvData[iRecvLen + 2]) {
            byte[] edc = new byte[1];
            edc[0] = getEdc(recvData);
            return RET_ERR_VERIFY;
        }
        return 0;
    }

    private byte getEdc(byte[] data) {
        byte edc;
        int len = data[1] + 2;
        edc = 0x00;
        for (int i = 0; i < len; i++) {
            edc ^= data[i];
        }

        return edc;
    }

    private int loadmasterKey(byte[] data) {

        byte keyID = data[2];
        byte mode = data[3];
        int ret = 0;
        // 如果指令和结果一起发送， 母pos会超时。 现在把发指令和结果分开处理。 如果写密钥失败， 则不给母pos发送结果信息。
        // 写密钥前先发正确指令，如果写密钥成功，再发结果
        sendHeader(data[0]);
        if ((keyID & 0x80) > 0) {
            // 写des密钥
            byte[] desKey;
            if (mode == 0x01) {
                desKey = new byte[8];
            } else {
                desKey = new byte[16];
            }
            System.arraycopy(data, 4, desKey, 0, desKey.length);
            try {
                ped.writeKey(EPedKeyType.TMK, (byte) 0, EPedKeyType.TDK, (byte) (keyID & 0x7f), desKey,
                        ECheckMode.KCV_NONE, null);
            } catch (PedDevException e) {
                Log.e(TAG, "", e);
                ret = e.getErrCode(); //modified by richard 20170518, merge APPT_V0509
            }
        } else {
            // 写主密钥
            byte[] mKey;
            if (mode == 0x01) {
                mKey = new byte[8];
            } else {
                mKey = new byte[16];
            }
            System.arraycopy(data, 4, mKey, 0, mKey.length);
            try {
                AppLog.i(TAG, "keyID:" + keyID + " , mKey :" + FinancialApplication.getConvert().bcdToStr(mKey));
                ped.writeKey(EPedKeyType.TLK, (byte) 0, EPedKeyType.TMK, (byte) keyID, mKey, ECheckMode.KCV_NONE, null);
            } catch (PedDevException e) {
                Log.e(TAG, "", e);
                ret = e.getErrCode();
            }
        }
        if (ret == 0) {
            sendResut(data[0]);
        }
        return ret;
    }

    private void sendErrorData(byte cmdHead, byte errCode) {
        byte[] sendData;
        if (errCode == 0) {
            sendData = new byte[3];
            sendData[0] = cmdHead;
            sendData[1] = 0;
            sendData[2] = getEdc(sendData);
        } else {
            sendData = new byte[4];
            sendData[0] = (byte) (cmdHead & 0x7f);
            sendData[1] = 1;
            sendData[2] = errCode;
            sendData[3] = getEdc(sendData);
        }

        try {
            uartComm.send(sendData);
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }
    }

    private void sendHeader(byte cmdHead) {
        byte[] sendData;
        sendData = new byte[1];
        sendData[0] = cmdHead;
        try {
            uartComm.send(sendData);
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }
    }

    private void sendResut(byte cmdHead) {
        byte[] sendData;
        sendData = new byte[3];
        sendData[0] = cmdHead;
        sendData[1] = 0;
        sendData[2] = getEdc(sendData);

        byte[] data = new byte[2];
        data[0] = sendData[1];
        data[1] = sendData[2];
        try {
            uartComm.send(data);
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }
    }

    private byte[] receive(int len, int timeout) {
        try {
            uartComm.setRecvTimeout(timeout);
            return uartComm.recv(len);
        } catch (CommException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public void setUDPDebug(boolean isDebug, String udpHostIp, int udpPort) {
        this.isDebug = isDebug;
        this.udpHostIp = udpHostIp;
        this.udpPort = udpPort;
    }

    public void udpSendLog(final String log) {
        if (!isDebug) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {

                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket(4567);
                    InetAddress serverAddress = InetAddress.getByName(udpHostIp);
                    DatagramPacket dp = new DatagramPacket(log.getBytes(), log.length(), serverAddress, udpPort);
                    socket.send(dp);
                } catch (SocketException | UnknownHostException e) {
                    Log.e(TAG, "", e);
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }

            }
        }).start();
    }
}
