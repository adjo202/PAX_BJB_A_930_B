package com.pax.pay.trans.transmit;

import android.util.Log;

//import com.pax.gl.comm.CommException;
import com.pax.gl.comm.ICommHelper;
//import com.pax.gl.comm.ISslKeyStore;
import com.pax.gl.commhelper.ISslKeyStore;
import com.pax.gl.commhelper.exception.CommException;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TcpCupSslCommunicate extends ATcpCommunicate {
    private static final String TAG = "TcpCupSslCommunicate";

    private InputStream keyStoreStream;

    public TcpCupSslCommunicate(InputStream keyStoreStream) {

        this.keyStoreStream = keyStoreStream;
    }

    @Override
    public int onConnect() {
        int ret = setTcpCommParam();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        int timeout = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT)) * 1000;
        // 启用主通讯地址
        hostIp = getMainHostIp();
        hostPort = getMainHostPort();
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_connect));
        ret = connectCupSLL(hostIp, hostPort, timeout);
        if (ret != TransResult.ERR_CONNECT) {
            return ret;
        }
        hostIp = getBackHostIp();
        hostPort = getbackHostPort();
        // 启用备用通讯地址
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_connect_other));
        ret = connectCupSLL(hostIp, hostPort, timeout);
        return ret;
    }

    @Override
    public int onSend(byte[] data) {

        try {
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_send));
            client.send(getCupSslPackage(data)); //发送数据
            return TransResult.SUCC;
        } catch (CommException e) {

            Log.e(TAG, "", e);
        }
        return TransResult.ERR_SEND;
    }

    @Override
    public CommResponse onRecv() {
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_recv));
        String sslType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_SSL);
        if (sslType.equals(SysParam.Constant.COMM_CUP_SSL)) {
            byte[] data = new byte[0];
            while (true) {
                byte[] temp = null;
                try {
                    temp = client.recv(1); //在阻塞模式下接收指定长度数据
                } catch (CommException e) {

                    Log.e(TAG, "", e);
                    return new CommResponse(TransResult.ERR_RECV, null);
                }
                if (temp != null && temp.length == 1) {
                    data = byteMerger(data, temp);
                    String s = new String(data);
                    if (s.contains("\r\n\r\n") && !s.contains("200 OK")) {
                        return new CommResponse(TransResult.ERR_RECV, null);
                    }
                }
            }
        }
        try {
            byte[] lenBuf = client.recv(2);
            if (lenBuf == null || lenBuf.length != 2) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = (((lenBuf[0] << 8) & 0xff00) | (lenBuf[1] & 0xff)); ////取出报文长度（前两个字节表示）
            byte[] rsp = client.recv(len);
            if (rsp == null || rsp.length != len) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }
            baos.write(rsp); //Writes the specified byte  to the OutputStream
            rsp = baos.toByteArray();
            return new CommResponse(TransResult.SUCC, rsp);
        } catch (IOException | CommException e) {
            Log.e(TAG, "", e);
        }
        return new CommResponse(TransResult.ERR_RECV, null);
    }

    @Override
    public void onClose() {

        try {
            client.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private int connectCupSLL(String hostIp, int port, int timeout) {
        if (hostIp == null || hostIp.length() == 0 || "0.0.0.0".equals(hostIp)) {
            return TransResult.ERR_CONNECT;
        }

        //ICommHelper commHelper = FinancialApplication.getGl().getCommHelper();
        PaxGLComm commHelper = FinancialApplication.getComm();
        ISslKeyStore keyStore = commHelper.createSslKeyStore(); //创建SSL/HTTPS密钥存储
        if (keyStoreStream != null) {
            try {
                keyStoreStream.reset(); //Resets this stream to the last marked location
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }
        keyStore.setTrustStore(keyStoreStream); //用X.509证书来设置用于服务验证的信任存储区
        client = commHelper.createSslClient(hostIp, port, keyStore); //创建一个SSL客户端
        client.setConnectTimeout(timeout); //设置连接超时时间，默认20000ms
        client.setRecvTimeout(timeout); //设置接收超时时间，默认20000ms
        try {
            client.connect(); //连接
            return TransResult.SUCC;
        } catch (CommException e) {

            Log.e(TAG, "", e);
        }
        return TransResult.ERR_CONNECT;
    }

    byte[] getCupSslPackage(byte[] req) {
        String cupHostName = hostIp + ":" + hostPort;
        String cupUrl = "http://" + cupHostName + "/unp/webtrans/VPB_lb";

        StringBuilder httpsReq = new StringBuilder();
        httpsReq.append("POST ");
        httpsReq.append(cupUrl);
        httpsReq.append(" HTTP/1.1");
        httpsReq.append("\r\n");
        httpsReq.append("HOST: ");
        httpsReq.append(cupHostName);
        httpsReq.append("\r\n");
        httpsReq.append("User-Agent: Donjin Http 0.1");
        httpsReq.append("\r\n");
        httpsReq.append("Cache-Control: no-cache");
        httpsReq.append("\r\n");
        httpsReq.append("Content-Type: x-ISO-TPDU/x-auth");
        httpsReq.append("\r\n");
        httpsReq.append("Accept: */*");
        httpsReq.append("\r\n");
        httpsReq.append("Content-Length: ");
        httpsReq.append(String.format("%d", req.length)); //Returns a formatted string, using the supplied format and arguments, using the default locale.
        httpsReq.append("\r\n");
        httpsReq.append("\r\n");
        byte[] header = httpsReq.toString().getBytes();
        byte[] bReq = byteMerger(header, req);
        return byteMerger(bReq, "\r\n".getBytes());
    }

    byte[] byteMerger(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        byte[] arrayOfByte = new byte[paramArrayOfByte1.length + paramArrayOfByte2.length];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, paramArrayOfByte1.length);
        System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, paramArrayOfByte1.length, paramArrayOfByte2.length);
        return arrayOfByte;
    }

}
