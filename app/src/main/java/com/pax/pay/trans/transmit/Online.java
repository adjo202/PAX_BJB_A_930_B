package com.pax.pay.trans.transmit;

import android.util.Log;

import com.pax.abl.core.ipacker.IPacker;
import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.pax.pay.trans.pack.PackIso8583.hex_dump_char;

public class Online {
    private static final String TAG = "Online";
    private static Online online;

    private TransProcessListener listener;
    private ACommunicate comm;

    private IPacker<TransData, byte[]> packager;
    private IPacker<TransData, byte[]> dupPackager;

    private Online() {

    }

    public static Online getInstance() {
        if (online == null) {
            online = new Online();
        }

        return online;
    }

    class PackListenerImpl implements PackListener {
        private TransProcessListener listener;

        public PackListenerImpl(TransProcessListener listener) {
            this.listener = listener;
        }

        @Override
        public byte[] onCalcMac(byte[] data) {

            if (listener != null) {
                return listener.onCalcMac(data);
            }
            return null;
        }

        @Override
        public byte[] onEncTrack(byte[] track) {

            if (listener != null) {
                return listener.onEncTrack(track);
            }
            return null;
        }

    }

    public int online(TransData transData, final TransProcessListener listener) {
        try {
            this.listener = listener;
            onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_process));
            ETransType transType = ETransType.valueOf(transData.getTransType());
            // 准备打包器
            packager = transType.getpackager(new PackListenerImpl(listener));
            dupPackager = transType.getDupPackager(new PackListenerImpl(listener));

            // 打包
            byte[] req;
            if (transData.getIsReversal() && dupPackager != null) {
                req = dupPackager.pack(transData);
            } else {
                req = packager.pack(transData);
            }

            if (FinancialApplication.getSysParam().getPrintDebug()) {
                //hex_dump_char("SEND TO HOST", req, req.length);
                try {
                    packager.dumpIso(req);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            if (req == null) {
                return TransResult.ERR_PACK;
            }

            // Sandy : Flushing
            //if dupPackager and the transaction is not reversal
            //then we should save it first to prepare the reversal
            if (dupPackager != null && !transData.getIsReversal()) {
                // Sandy : write flush file
                Log.d("teg", "6");
                TransData.deleteDupRecord();
                // Sandy : save
                transData.saveDup();
            }

            // 冲正交易不需要增加流水号
            if (!transData.getIsReversal()) {
                if (transType != ETransType.INQ_PULSA_DATA && transType != ETransType.PURCHASE_PULSA_DATA) // add abdul
                    Component.incTransNo(); //流水号+1，溢出时应该是从1重新开始
            }

            // ID transaksi online
            transData.setIsOnlineTrans(true);

            int ret;
            comm = getCommClient();
            comm.setTransProcessListener(listener);
            // connecting...
            ret = comm.onConnect();
            if (ret != 0) {
                return TransResult.ERR_CONNECT;
            }

            byte[] sendData = new byte[2 + req.length];
            sendData[0] = (byte) (req.length / 256);
            sendData[1] = (byte) (req.length % 256);
            System.arraycopy(req, 0, sendData, 2, req.length); //发送数据前两个字节存放数据长度

            // Sandy : start sending a data
            if (comm instanceof ModemCommunicate) {
                AppLog.i(TAG, "SEND:" + FinancialApplication.getConvert().bcdToStr(req));
                ret = comm.onSend(req);
            } else {
                AppLog.i(TAG, "SEND:" + FinancialApplication.getConvert().bcdToStr(sendData));
                ret = comm.onSend(sendData);
            }

            if (ret != 0) {
                return TransResult.ERR_SEND;
            }

            // 接收
            CommResponse commResponse = comm.onRecv();
            if (commResponse.getRetCode() != TransResult.SUCC) {
                // Sandy : Update justification reason
                if (dupPackager != null && !transData.getIsReversal()) {
                    TransData.updateDupReason(TransData.REASON_NO_RECV);
                }
                return TransResult.ERR_RECV;
            }
            AppLog.i(TAG, "RECV:" + FinancialApplication.getConvert().bcdToStr(commResponse.getData()));
            if (FinancialApplication.getSysParam().getPrintDebug()) {
                //hex_dump_char("RECV FROM HOST", commResponse.getData(), commResponse.getData().length);
                try {
                    packager.dumpIso(commResponse.getData());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (transData.getIsReversal()) {
                //return dupPackager.unpack(transData, commResponse.getData());
                int res = dupPackager.unpack(transData, commResponse.getData());

                if ((transType == ETransType.PASCABAYAR_OVERBOOKING ||
                        transType == ETransType.PDAM_OVERBOOKING )
                        && res == TransResult.ERR_TRACE_NO){
                    res = 0;
                }
                return res;
            }

            try {
                Log.i("abdul", "resp byte = " + FinancialApplication.getConvert().bcdToStr(commResponse.getData()));
                ret = packager.unpack(transData, commResponse.getData());
            } catch (Exception e) {
                e.printStackTrace();
                return ret;
            }


            // Sandy : Update reversal reason/time
            if (ret == TransResult.ERR_MAC && dupPackager != null && !transData.getIsReversal()) {
                TransData.updateDupReason(TransData.REASON_MACWRONG);
                TransData.updateDupDate(transData.getDate());
            }
            // Sandy: If the 39 field returns null,
            // delete the reversal file, or unpack the 3, 4, 11, 41, and 42 fields
            // if they are different from the request,
            // delete the reversal (BCTC requires the next transaction to not send the reversal)
            if (ret == TransResult.ERR_BAG
                    || ret == TransResult.ERR_PROC_CODE
                    || ret == TransResult.ERR_TRANS_AMT
                    || ret == TransResult.ERR_TRACE_NO
                    || ret == TransResult.ERR_TERM_ID
                    || ret == TransResult.ERR_MERCH_ID) {
                Log.d("teg", "7");
                TransData.deleteDupRecord();
            }
            // Sandy : Processing requirements for storing headers
            saveProcReq(transData.getHeader());

            return ret;
        } finally {
            if (comm != null) {
                if (comm instanceof ModemCommunicate) {
                    // If the communication method is MODEM; do not close the connection temporarily,
                    // and then close the connection after the transaction is settled
                    // do Nothing
                } else {
                    comm.onClose();

                }
            }
        }
    }

    /**
     * 保存报文头的处理要求
     * 
     * @param header
     *            ：报文头
     */
    private void saveProcReq(String header) {
        if (header == null || header.length() < 5) {
            return;
        }
        AppLog.i(TAG, "Header: " + header);
        byte[] bHeader = FinancialApplication.getConvert().strToBcd(header, EPaddingPosition.PADDING_LEFT);

        int procReq = bHeader[2] & 0x0f;
        if (procReq >= 1 && procReq <= 8) {
            int bit = FinancialApplication.getController().get(Controller.HEADER_PROC_REQ_A);
            bit |= 0x01 << (procReq - 1);  // <<运算符的优先级大于|=运算符
            FinancialApplication.getController().set(Controller.HEADER_PROC_REQ_A, bit);
        } else if (procReq >= 9 && procReq <= 15) {
            int bit = FinancialApplication.getController().get(Controller.HEADER_PROC_REQ_B);
            bit |= 0x01 << (procReq - 8 - 1);
            FinancialApplication.getController().set(Controller.HEADER_PROC_REQ_B, bit);
        }
    }

    private void onShowMsg(String msg) {
        if (listener != null) {
            listener.onShowProgress(msg, Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT)));
        }
    }

    private ACommunicate getCommClient() {
        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        String sslType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_SSL);
        // LAN,WIFI,MOBILE
        if (commType.equals(SysParam.Constant.COMMTYPE_LAN) || commType.equals(SysParam.Constant.COMMTYPE_MOBILE)
                || commType.equals(SysParam.Constant.COMMTYPE_WIFI)) {

            if (sslType.equals(SysParam.Constant.COMM_NO_SSL)) {
                return new TcpNoSslCommunicate();
            } else {
                InputStream inputStream = null;
                try {
                    File file = new File(Constants.CACERT_PATH);
                    if (file.exists()) {
                        inputStream = new FileInputStream(file);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
                return new TcpCupSslCommunicate(inputStream);
            }
        } else {// MODEM
            return ModemCommunicate.getInstance();
        }
    }

}
