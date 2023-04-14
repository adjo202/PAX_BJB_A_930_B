package com.pax.pay.trans.transmit;

import com.pax.pay.app.FinancialApplication;
import com.pax.settings.SysParam;

public abstract class ACommunicate {
    /**
     * 建立连接
     * 
     * @return
     */
    public abstract int onConnect();

    /**
     * 发送数据
     * 
     * @param data
     * @return
     */
    public abstract int onSend(byte[] data);

    /**
     * 接收数据
     * 
     * @return
     */
    public abstract CommResponse onRecv();

    /**
     * 关闭连接
     */
    public abstract void onClose();

    protected TransProcessListener transProcessListener;

    /**
     * 设置监听器
     * 
     * @param listener
     */
    protected void setTransProcessListener(TransProcessListener listener) {
        this.transProcessListener = listener;
    }

    protected void onShowMsg(String msg) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg,
                    Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT)));
        }
    }

    protected void onShowMsg(String msg, int timeout) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg, timeout);
        }
    }

}
