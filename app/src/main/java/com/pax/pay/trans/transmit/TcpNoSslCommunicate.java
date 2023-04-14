package com.pax.pay.trans.transmit;

import android.util.Log;

//import com.pax.gl.comm.CommException;
import com.pax.gl.comm.ICommHelper;
import com.pax.gl.commhelper.IComm;
import com.pax.gl.commhelper.exception.CommException;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TcpNoSslCommunicate extends ATcpCommunicate {
    public static final String TAG = "TcpNoSslCommunicate";

    @Override
    public int onConnect() {
        int ret = setTcpCommParam();
        if (ret != TransResult.SUCC) {
            return ret;
        }
        int timeout = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT)) * 1000;
        // Sandy : Enable primary address
        hostIp = getMainHostIp();
        hostPort = getMainHostPort();
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_connect));
        ret = connectNoSLL(hostIp, hostPort, timeout);
        if (ret != TransResult.ERR_CONNECT) {
            return ret;
        }

        hostIp = getBackHostIp();
        hostPort = getbackHostPort();
        // 启用备用通讯地址
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_connect_other));
        ret = connectNoSLL(hostIp, hostPort, timeout);
        return ret;
    }

    @Override
    public int onSend(byte[] data) {
        try {
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_send));
            client.send(data); //发送数据
            return TransResult.SUCC;
        } catch (CommException e) {

            Log.e(TAG, "", e);
        }
        return TransResult.ERR_SEND;
    }



    @Override
    public CommResponse onRecv() {
        try {
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_recv));
            byte[] lenBuf = client.recv(2); //Receive data of specified length in blocking mode
            if (lenBuf == null || lenBuf.length != 2) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Take out the message length (represented by the first two bytes)
            int len = (((lenBuf[0] << 8) & 0xff00) | (lenBuf[1] & 0xff));
            byte[] rsp = client.recv(len);
            if (rsp == null || rsp.length != len) {
                return new CommResponse(TransResult.ERR_RECV, null);
            }
            baos.write(rsp);
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
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    private int connectNoSLL(String hostIp, int port, int timeout) {
        if (hostIp == null || hostIp.length() == 0 || "0.0.0.0".equals(hostIp)) {
            return TransResult.ERR_CONNECT;
        }

        //ICommHelper commHelper = FinancialApplication.getGl().getCommHelper();
        PaxGLComm commHelper = FinancialApplication.getComm();
        client = commHelper.createTcpClient(hostIp, port); //创建一个TCP客户端
        //client.setSendTimeout(timeout);
        client.setConnectTimeout(timeout); //Set the connection timeout time, the default is 20000ms
        client.setRecvTimeout(timeout); //Set the connection timeout time, the default is 20000ms
        try {
            client.connect();
            return TransResult.SUCC;
        } catch (CommException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return TransResult.ERR_CONNECT;
    }

}
