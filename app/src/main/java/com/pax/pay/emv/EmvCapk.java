package com.pax.pay.emv;

import android.annotation.SuppressLint;
import android.util.Log;

import com.pax.eemv.entity.Capk;
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
public class EmvCapk extends AEntityBase {
    
    public static final String TAG = "EmvCapk";

    // 应用注册服务商ID
    @Column(canBeNull = true)
    private String RID;
    // 密钥索引
    @Column
    private int KeyID;
    // HASH算法标志
    @Column
    private int HashInd;
    // RSA算法标志
    @Column
    private int arithInd;
    // 模
    @Column(canBeNull = true)
    private String modul;
    // 指数
    @Column(canBeNull = true)
    private String Exponent;
    // 有效期(YYMMDD)
    @Column(canBeNull = true)
    private String expDate;
    // 密钥校验和
    @Column(canBeNull = true)
    private String checkSum;

    public String getRID() {

        return RID;
    }

    public void setRID(String rID) {

        RID = rID;
    }

    public int getKeyID() {

        return KeyID;
    }

    public void setKeyID(int keyID) {

        KeyID = keyID;
    }

    public int getHashInd() {

        return HashInd;
    }

    public void setHashInd(int hashInd) {

        HashInd = hashInd;
    }

    public int getArithInd() {

        return arithInd;
    }

    public void setArithInd(int arithInd) {

        this.arithInd = arithInd;
    }

    public String getModul() {

        return modul;
    }

    public void setModul(String modul) {

        this.modul = modul;
    }

    public String getExponent() {

        return Exponent;
    }

    public void setExponent(String exponent) {

        Exponent = exponent;
    }

    public String getExpDate() {

        return expDate;
    }

    public void setExpDate(String expDate) {

        this.expDate = expDate;
    }

    public String getCheckSum() {

        return checkSum;
    }

    public void setCheckSum(String checkSum) {

        this.checkSum = checkSum;
    }

    /************************************************ CAPK数据库信息定义 *********************************************/
    /**
     * CAPK数据库信息
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
        public static final String DB_NAME = "capk.db";
        /**
         * capk表名
         */
        public static final String TABLE_NAME = "capk";
    }

    /************************************************ CAPK数据库句柄 *********************************************/

    /**
     * 获取交易数据库句柄
     * 
     * @return
     * @throws DbException
     */
    private static IDao<EmvCapk> getCapkDao() throws DbException {
        IDao<EmvCapk> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME, EmvCapk.class,
                new IDbListener<EmvCapk>() {

                    @Override
                    public IDao<EmvCapk> onUpdate(IDao<EmvCapk> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME,
                                    EmvCapk.class, null);
                        } catch (DbException e) {
                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }

    /***************************************** capk数据库操作 ******************************************/

    /**
     * 写一条capk
     * 
     * @param
     * @return
     */
    public boolean save() {
        try {
            IDao<EmvCapk> dao = getCapkDao();
            String rid = this.getRID();
            int keyId = this.getKeyID();
            String sql = String.format("RID = '" + rid + "'" + " and " + "KeyID = %d", keyId);
            List<EmvCapk> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
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
     * 读一条capk
     * 
     * @param index
     *            :第几条, 从1开始
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static EmvCapk readCapk(int index) {
        try {
            IDao<EmvCapk> dao = getCapkDao();
            String sql = String.format("id = %d", index);
            List<EmvCapk> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 读所有CAPK
     * 
     * @return
     */
    public static List<EmvCapk> readAllCapk() {
        try {
            IDao<EmvCapk> dao = getCapkDao();
            return dao.findAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    public static void deleteAll() {
        try {
            IDao<EmvCapk> dao = getCapkDao();
            dao.deleteAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

    }

    /******************************** EmvCapk to Capk *******************************/
    public static List<Capk> toCapk() {
        IConvert convert = FinancialApplication.getConvert();
        List<Capk> list = new LinkedList<Capk>();

        List<EmvCapk> capkList = readAllCapk(); //从数据库中读所有CAPK
        if (capkList == null) {
            return null;
        }
        for (EmvCapk readCapk : capkList) {
            Capk capk = new Capk();
            capk.setRid(convert.strToBcd(readCapk.getRID(), EPaddingPosition.PADDING_LEFT));
            capk.setKeyID((byte) readCapk.getKeyID());
            capk.setHashInd((byte) readCapk.getHashInd());
            capk.setArithInd((byte) readCapk.getArithInd());
            if (readCapk.getModul() == null)
                continue;
            capk.setModul(convert.strToBcd(readCapk.getModul(), EPaddingPosition.PADDING_LEFT));
            if (readCapk.getExponent() == null)
                continue;
            capk.setExponent(convert.strToBcd(readCapk.getExponent(), EPaddingPosition.PADDING_LEFT));
            capk.setExpDate(convert.strToBcd(readCapk.getExpDate(), EPaddingPosition.PADDING_LEFT));
            capk.setCheckSum(convert.strToBcd(readCapk.getCheckSum(), EPaddingPosition.PADDING_LEFT));
            list.add(capk);
        }
        return list;
    }
}
