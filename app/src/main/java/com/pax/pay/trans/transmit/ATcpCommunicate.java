package com.pax.pay.trans.transmit;

//import com.pax.dal.IComm;
import com.pax.dal.IDalCommManager;
import com.pax.dal.entity.EChannelType;
import com.pax.dal.entity.LanParam;
import com.pax.dal.entity.MobileParam;
//import com.pax.gl.comm.IComm;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.gl.commhelper.IComm;

public abstract class ATcpCommunicate extends ACommunicate {

    protected IComm client;
    protected String hostIp;
    protected int hostPort;

    /**
     * 设置TCP通讯的相关参数
     * 
     * @return
     */
    public int setTcpCommParam() {
        IDalCommManager commManager = FinancialApplication.getDal().getCommManager();
        SysParam sysParam = FinancialApplication.getSysParam();
        String commType = sysParam.get(SysParam.APP_COMM_TYPE_ACQUIRER);
        EChannelType channelType = EChannelType.WIFI;
        if (commType.equals(SysParam.Constant.COMMTYPE_LAN)) {
            channelType = EChannelType.LAN;

            LanParam lanParam = new LanParam();
            lanParam.setDhcp(sysParam.get(SysParam.LAN_DHCP).equals(SysParam.Constant.YES) ? true : false);
            lanParam.setDns1(sysParam.get(SysParam.LAN_DNS1));
            lanParam.setDns2(sysParam.get(SysParam.LAN_DNS2));
            lanParam.setGateway(sysParam.get(SysParam.LAN_GATEWAY));
            lanParam.setLocalIp(sysParam.get(SysParam.LAN_LOCALIP));
            lanParam.setSubnetMask(sysParam.get(SysParam.LAN_SUBNETMASK));
            commManager.setLanParam(lanParam);

        } else if (commType.equals(SysParam.Constant.COMMTYPE_WIFI)) {
            channelType = EChannelType.WIFI;
        } else if (commType.equals(SysParam.Constant.COMMTYPE_MOBILE)) {
            channelType = EChannelType.MOBILE;
            // mobile参数设置
            MobileParam param = new MobileParam();
            param.setApn(sysParam.get(SysParam.MOBILE_APN));
            param.setPassword(sysParam.get(SysParam.MOBILE_PWD));
            param.setUsername(sysParam.get(SysParam.MOBILE_USER));
            commManager.setMobileParam(param);
        }
        onShowMsg(FinancialApplication.getAppContext().getString(R.string.wait_initialize_net));
        int timeout = Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT));
        int ret = commManager.enableChannelExclusive(channelType, timeout);
        if (ret != 0) {
            return TransResult.ERR_COMM_CHANNEL;
        }
        return TransResult.SUCC;
    }

    /**
     * 获取主机地址
     * 
     * @return
     */
    protected String getMainHostIp() {
        SysParam sysParam = FinancialApplication.getSysParam();
        String hostIp = sysParam.get(SysParam.LAN_HOSTIP);
        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (commType.equals(SysParam.Constant.COMMTYPE_MOBILE)) {
            hostIp = sysParam.get(SysParam.MOBILE_HOSTIP);
        }
        return hostIp;
    }

    /**
     * 获取主机端口
     * 
     * @return
     */
    protected int getMainHostPort() {
        SysParam sysParam = FinancialApplication.getSysParam();
        String hostPort = sysParam.get(SysParam.LAN_HOSTPORT);
        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (commType.equals(SysParam.Constant.COMMTYPE_MOBILE)) {
            hostPort = sysParam.get(SysParam.MOBILE_HOSTPORT);
        }
        if (hostPort == null || hostPort.length() == 0) {
            hostPort = "0";
        }
        return Integer.parseInt(hostPort);
    }

    /**
     * 获取备份主机地址
     * 
     * @return
     */
    protected String getBackHostIp() {
        SysParam sysParam = FinancialApplication.getSysParam();
        String hostIp = sysParam.get(SysParam.LAN_BAK_HOSTIP);
        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (commType.equals(SysParam.Constant.COMMTYPE_MOBILE)) {
            hostIp = sysParam.get(SysParam.MOBILE_BAK_HOSTIP);
        }
        return hostIp;
    }

    /**
     * 获取备份主机端口
     * 
     * @return
     */
    protected int getbackHostPort() {
        SysParam sysParam = FinancialApplication.getSysParam();
        String hostPort = sysParam.get(SysParam.LAN_BAK_HOSTPORT);
        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (commType.equals(SysParam.Constant.COMMTYPE_MOBILE)) {
            hostPort = sysParam.get(SysParam.MOBILE_BAK_HOSTPORT);
        }
        if (hostPort == null || hostPort.length() == 0) {
            hostPort = "0";
        }
        return Integer.parseInt(hostPort);
    }

}
