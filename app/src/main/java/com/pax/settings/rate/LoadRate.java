package com.pax.settings.rate;

import android.util.Log;

import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.IDbListener;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.AppLog;

import java.util.List;

/**
 * Created by yanglj on 2017-03-29.
 */

public class LoadRate extends AEntityBase {
    private static final String TAG = "LoadRate";
    /********************************** 汇率数据库信息 *****************************************/
    /**
     * 汇率数据信息
     *
     * @author Steven.W
     */
    class DbInfo {
        /**
         * 版本号
         */
        public static final int VER = 1;
        /**
         * 汇率数据库名称
         */
        public static final String DB_NAME = "CurrencyRate.db";
        /**
         * 货币汇率表名
         */
        public static final String TABLE_NAME_RATE = "tbRate";
    }

    // Currency Code
    @Column(canBeNull = true)
    private String transCode;
    @Column(canBeNull = true)
    private String cardCode;
    @Column(canBeNull = true)
    private String rate;

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public LoadRate() {

    }

    public LoadRate(String transCode, String cardCode, String rate) {
        this.transCode = transCode;
        this.cardCode = cardCode;
        this.rate = rate;
    }

    // ********************************获取数据句柄********************************/

    /**
     * 获取汇率数据库句柄
     *
     * @return
     * @throws DbException
     */
    private static IDao<LoadRate> getLoadRateDao() throws DbException {
        return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_RATE,
                LoadRate.class, new IDbListener<LoadRate>() {
                    @Override
                    public IDao<LoadRate> onUpdate(IDao<LoadRate> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME,
                                    DbInfo.TABLE_NAME_RATE,
                                    LoadRate.class, null);
                        } catch (DbException e) {
                            Log.e(TAG, "", e);
                        }
                        return null;
                    }
                });

    }

/********************************************** 汇率数据库操作
 * *****************************************************/
    /**
     * 写一条汇率记录
     *
     * @param
     * @return
     */
    public boolean saveRate() {
        try {
            IDao<LoadRate> dao = getLoadRateDao();
            String transCodeData = getTransCode();
            String cardCodeData = getCardCode();
            String sql = String.format("transCode = '" + transCodeData + "' and " + "cardCode = '" +
                    cardCodeData + "'");

            AppLog.i(TAG, "sql:" + sql);

            List<LoadRate> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                setId(list.get(0).getId());
                dao.update(this);
                return true;
            }
            dao.save(this);
        } catch (DbException e) {
            AppLog.e(TAG, "", e);
            return false;
        }
        return true;
    }

    /**
     * 通过currency code 读取rate
     *
     * @param transCode,cardCode
     * @return
     */
    public static LoadRate readRate(String transCode, String cardCode) {
        try {
            IDao<LoadRate> dao = getLoadRateDao();
            String sql = "transCode = '" + transCode + "' and " + "cardCode = '" + cardCode + "'";
            List<LoadRate> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 读所有汇率记录
     *
     * @return
     */
    public static List<LoadRate> readAllRate() {
        try {
            IDao<LoadRate> dao = getLoadRateDao();
            return dao.findAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 删除所有汇率记录
     */
    public static void deleteAll() {
        try {
            IDao<LoadRate> dao = getLoadRateDao();
            dao.deleteAll();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
    }
}
