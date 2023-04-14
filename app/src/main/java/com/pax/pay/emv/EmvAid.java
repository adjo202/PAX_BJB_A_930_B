package com.pax.pay.emv;

import android.annotation.SuppressLint;
import android.util.Log;

import com.pax.eemv.entity.AidParam;
import com.pax.gl.convert.IConvert;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.IDbListener;
import com.pax.pay.app.FinancialApplication;

import java.util.LinkedList;
import java.util.List;

@SuppressLint("DefaultLocale")
public class EmvAid extends AEntityBase {
    public static final String TAG = "EmvAid";
    /**
     * aid, 应用标志
     */
    @Column(canBeNull = true)
    private String aid;
    /**
     * 选择标志(PART_MATCH 部分匹配 FULL_MATCH 全匹配)
     */
    @Column
    private int selFlag;
    /**
     * 终端联机PIN支持能力
     */
    @Column
    private boolean onlinePin;
    /**
     * 电子现金终端交易限额(9F7F)
     */
    @Column
    private String ecTTLVal;
    /**
     * 读卡器非接触CVM限制(DF21)
     */
    @Column
    private String rdCVMLmt;
    /**
     * 读卡器非接触交易限额(DF20)
     */
    @Column(canBeNull = true)
    private String rdClssTxnLmt;
    /**
     * 读卡器非接触脱机最低限额(DF19)
     */
    @Column
    private String rdClssFLmt;
    /**
     * TTL存在? 1-存在 电子现金终端交易限额（EC Terminal Transaction Limit）(9F7B)
     */
    @Column
    private int ecTTLFlg;
    /**
     * 是否存在读卡器非接触脱机最低限额
     */
    @Column
    private int rdClssFLmtFlg;
    /**
     * 是否存在读卡器非接触交易限额
     */
    @Column
    private int rdClssTxnLmtFlg;
    /**
     * 是否存在读卡器非接触CVM限额
     */
    @Column
    private int rdCVMLmtFlg;

    /**
     * 目标百分比数
     */
    @Column
    private int targetPer;
    /**
     * 最大目标百分比数
     */
    @Column
    private int maxTargetPer;
    /**
     * 是否检查最低限额
     */
    @Column
    private int floorlimitCheck;
    /**
     * 是否进行随机交易选择
     */
    @Column
    private boolean randTransSel;
    /**
     * 是否进行频度检测
     */
    @Column
    private boolean velocityCheck;
    /**
     * 最低限额
     */
    @Column
    private String floorLimit;
    /**
     * 阀值
     */
    @Column
    private String threshold;
    /**
     * 终端行为代码(拒绝)
     */
    @Column(canBeNull = true)
    private String tacDenial;
    /**
     * 终端行为代码(联机)
     */
    @Column(canBeNull = true)
    private String tacOnline;
    /**
     * 终端行为代码(缺省)
     */
    @Column(canBeNull = true)
    private String tacDefualt; // 单词拼写错误,会导致表结构变化，暂时不改
    /**
     * 收单行标志־
     */
    @Column(canBeNull = true)
    private String acquierId;
    /**
     * 终端缺省DDOL
     */
    @Column(canBeNull = true)
    private String dDOL;
    /**
     * 终端缺省TDOL
     */
    @Column(canBeNull = true)
    private String tDOL;
    /**
     * 应用版本
     */
    @Column(canBeNull = true)
    private String version;
    /**
     * 风险管理数据
     */
    @Column(canBeNull = true)
    private String riskmanData;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getSelFlag() {
        return selFlag;
    }

    public void setSelFlag(int selFlag) {
        this.selFlag = selFlag;
    }

    public boolean getOnlinePin() {
        return onlinePin;
    }

    public void setOnlinePin(boolean onlinePin) {
        this.onlinePin = onlinePin;
    }

    public String getEcTTLVal() {
        return ecTTLVal;
    }

    public void setEcTTLVal(String ecTTLVal) {
        this.ecTTLVal = ecTTLVal;
    }

    public String getRdCVMLmt() {
        return rdCVMLmt;
    }

    public void setRdCVMLmt(String rdCVMLmt) {
        this.rdCVMLmt = rdCVMLmt;
    }

    public String getRdClssTxnLmt() {
        return rdClssTxnLmt;
    }

    public void setRdClssTxnLmt(String rdClssTxnLmt) {
        this.rdClssTxnLmt = rdClssTxnLmt;
    }

    public String getRdClssFLmt() {
        return rdClssFLmt;
    }

    public void setRdClssFLmt(String rdClssFLmt) {
        this.rdClssFLmt = rdClssFLmt;
    }

    public int getEcTTLFlg() {
        return ecTTLFlg;
    }

    public void setEcTTLFlg(int ecTTLFlg) {
        this.ecTTLFlg = ecTTLFlg;
    }

    public int getRdClssFLmtFlg() {
        return rdClssFLmtFlg;
    }

    public void setRdClssFLmtFlg(int rdClssFLmtFlg) {
        this.rdClssFLmtFlg = rdClssFLmtFlg;
    }

    public int getRdClssTxnLmtFlg() {
        return rdClssTxnLmtFlg;
    }

    public void setRdClssTxnLmtFlg(int rdClssTxnLmtFlg) {
        this.rdClssTxnLmtFlg = rdClssTxnLmtFlg;
    }

    public int getRdCVMLmtFlg() {
        return rdCVMLmtFlg;
    }

    public void setRdCVMLmtFlg(int rdCVMLmtFlg) {
        this.rdCVMLmtFlg = rdCVMLmtFlg;
    }

    public int getTargetPer() {
        return targetPer;
    }

    public void setTargetPer(int targetPer) {
        this.targetPer = targetPer;
    }

    public int getMaxTargetPer() {
        return maxTargetPer;
    }

    public void setMaxTargetPer(int maxTargetPer) {
        this.maxTargetPer = maxTargetPer;
    }

    public int getFloorlimitCheck() {
        return floorlimitCheck;
    }

    public void setFloorlimitCheck(int floorlimitCheck) {
        this.floorlimitCheck = floorlimitCheck;
    }

    public boolean getRandTransSel() {
        return randTransSel;
    }

    public void setRandTransSel(boolean randTransSel) {
        this.randTransSel = randTransSel;
    }

    public boolean getVelocityCheck() {
        return velocityCheck;
    }

    public void setVelocityCheck(boolean velocityCheck) {
        this.velocityCheck = velocityCheck;
    }

    public String getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(String floorLimit) {
        this.floorLimit = floorLimit;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getTacDenial() {
        return tacDenial;
    }

    public void setTacDenial(String tacDenial) {
        this.tacDenial = tacDenial;
    }

    public String getTacOnline() {
        return tacOnline;
    }

    public void setTacOnline(String tacOnline) {
        this.tacOnline = tacOnline;
    }

    public String getTacDefualt() {
        return tacDefualt;
    }

    public void setTacDefualt(String tacDefualt) {
        this.tacDefualt = tacDefualt;
    }

    public String getAcquierId() {
        return acquierId;
    }

    public void setAcquierId(String acquierId) {
        this.acquierId = acquierId;
    }

    public String getDDOL() {
        return dDOL;
    }

    public void setDDOL(String dDOL) {
        this.dDOL = dDOL;
    }

    public String getTDOL() {
        return tDOL;
    }

    public void setTDOL(String tDOL) {
        this.tDOL = tDOL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRiskmanData() {
        return riskmanData;
    }

    public void setRiskmanData(String riskmanData) {
        this.riskmanData = riskmanData;
    }

    /**************************************** AID数据库信息 *********************************************/

    /**
     * AID数据库信息
     * 
     * @author Steven.W
     * 
     */
    class DbInfo {
        /**
         * 版本号
         */
        public static final int VER = 1;
        /**
         * capk数据库名称
         */
        public static final String DB_NAME = "aid.db";
        /**
         * capk表名
         */
        public static final String TABLE_NAME = "aid";
    }

    /**************************************** AID数据库句柄 *********************************************/

    /**
     * 获取交易数据库句柄
     * 
     * @return
     * @throws DbException
     */
    private static IDao<EmvAid> getAidDao() throws DbException {
        IDao<EmvAid> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME, EmvAid.class,
                new IDbListener<EmvAid>() {

                    @Override //IDb.IDao<T> onUpdate(IDb.IDao<T> dao, int oldVersion, int newVersion)
                    public IDao<EmvAid> onUpdate(IDao<EmvAid> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME,
                                    EmvAid.class, null);
                        } catch (DbException e) {
                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }

    /***************************************** aid数据库操作 ******************************************/

    /**
     * 写一条aid
     * 
     * @param
     * @return
     */
    public boolean save() {
        try {

            IDao<EmvAid> dao = getAidDao();
            String aidF = this.getAid();
            String sql = String.format("aid = '" + aidF + "'");
            List<EmvAid> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) { //如果原来有这个ID的删除再保存，否则直接保存
                this.setId(list.get(0).getId());
                dao.delete(list.get(0).getId());
            }
            dao.save(this);

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return false;
        }

        return true;
    }

    /**
     * 读一条Aid
     * 
     * @param index
     *            :第几条, 从1开始
     * @return
     */
    public static EmvAid readAid(int index) {
        try {
            IDao<EmvAid> dao = getAidDao();
            String sql = String.format("id = %d", index);
            List<EmvAid> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 通过AID读取emv参数
     * @param aid
     * @return
     */
    public static EmvAid readAid(String aid) {
        try {
            IDao<EmvAid> dao = getAidDao();
            String sql = "aid = '" + aid + "'";
            List<EmvAid> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }
    
    /**
     * 读所有AID参数
     * 
     * @return
     */
    public static List<EmvAid> readAllAid() {
        try {
            IDao<EmvAid> dao = getAidDao();
            return dao.findAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;

    }

    /**
     * 删除所有AID
     */
    public static void deleteAll() {
        try {
            IDao<EmvAid> dao = getAidDao();
            dao.deleteAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
    }

    /*************************** EmvAidParam to AidParam ***********************************/
    public static List<AidParam> toAidParams() {
        IConvert convert = FinancialApplication.getConvert();
        List<AidParam> list = new LinkedList<AidParam>();

        List<EmvAid> aidList = readAllAid(); //从数据库中读取所有Aid
        if (aidList == null) {

            return null;
        }
        for (EmvAid emvAidParam : aidList) {
            Log.d(TAG, "Sandy=emvAidParam.getAid():" + emvAidParam.getAid());
            Log.d(TAG, "Sandy=emvAidParam.getOnlinePin():" + emvAidParam.getOnlinePin());



            AidParam aidParam = new AidParam();
            aidParam.setAid(convert.strToBcd(emvAidParam.getAid(), EPaddingPosition.PADDING_LEFT));
            aidParam.setSelFlag((byte) emvAidParam.getSelFlag());
            aidParam.setOnlinePin(emvAidParam.getOnlinePin());

//          aidParam.setEcTTLVal(emvAidParam.getEcTTLVal());
            aidParam.setRdCVMLmt(Long.parseLong(emvAidParam.getRdCVMLmt()));
            aidParam.setRdClssTxnLmt(Long.parseLong(emvAidParam.getRdClssTxnLmt()));
            aidParam.setRdClssFLmt(Long.parseLong(emvAidParam.getRdClssFLmt()));
//            aidParam.setEcTTLFlag((byte) emvAidParam.getEcTTLFlg());
            aidParam.setRdClssFLmtFlag(emvAidParam.getRdClssFLmtFlg());
            aidParam.setRdClssTxnLmtFlag(emvAidParam.getRdClssTxnLmtFlg());
            aidParam.setRdCVMLmtFlag(emvAidParam.getRdCVMLmtFlg());
            aidParam.setFloorLimit(Long.parseLong(emvAidParam.getFloorLimit()));
            aidParam.setFloorLimitCheckFlg(emvAidParam.getFloorlimitCheck());
            aidParam.setThreshold(Long.parseLong(emvAidParam.getThreshold()));
            aidParam.setTargetPer((byte) emvAidParam.getTargetPer());
            aidParam.setMaxTargetPer((byte) emvAidParam.getMaxTargetPer());
            aidParam.setRandTransSel(emvAidParam.getRandTransSel());
            aidParam.setVelocityCheck(emvAidParam.getVelocityCheck());
            aidParam.setTacDenial(convert.strToBcd(emvAidParam.getTacDenial(), EPaddingPosition.PADDING_LEFT));
            aidParam.setTacOnline(convert.strToBcd(emvAidParam.getTacOnline(), EPaddingPosition.PADDING_LEFT));
            aidParam.setTacDefault(convert.strToBcd(emvAidParam.getTacDefualt(), EPaddingPosition.PADDING_LEFT));
            if (emvAidParam.getAcquierId() != null) {
                aidParam.setAcquirerId(FinancialApplication.getConvert().strToBcd(emvAidParam.getAcquierId(), EPaddingPosition.PADDING_LEFT));
            }
            if (emvAidParam.getDDOL() != null) {
                aidParam.setdDol(convert.strToBcd(emvAidParam.getDDOL(), EPaddingPosition.PADDING_LEFT));
            }
            if (emvAidParam.getTDOL() != null) {
                aidParam.settDol(convert.strToBcd(emvAidParam.getTDOL(), EPaddingPosition.PADDING_LEFT));
            }
            aidParam.setVersion(convert.strToBcd(emvAidParam.getVersion(), EPaddingPosition.PADDING_LEFT));
            if (emvAidParam.getRiskmanData() != null) {
                aidParam.setRiskManData(convert.strToBcd(emvAidParam.getRiskmanData(), EPaddingPosition.PADDING_LEFT));
            }
            list.add(aidParam);
        }
        return list;
    }

}
