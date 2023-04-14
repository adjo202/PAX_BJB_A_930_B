package com.pax.pay.operator;

import android.util.Log;

import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.Unique;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.CollectionUtils;

import java.util.List;

public final class Operator extends AEntityBase {
    public static final String TAG = "Operator";

    /**
     * 操作员ID
     */
    @Column
    @Unique
    private String operId = "";

    /**
     * 操作员密码
     */
    @Column
    private String pd = "";

    /**
     * 操作员名字
     */
    @Column
    private String name = "";

    /**
     * 给DB调用
     */
    public Operator() {

    }

    /**
     * 初始化操作员
     * 
     * @param id
     *            操作员id
     * @param pwd
     *            操作员密码
     */
    public Operator(String id, String pwd) {
        this.operId = id;
        this.pd = pwd;
        this.name = "";
    }

    /**
     * 初始化操作员
     * 
     * @param id
     *            操作员id
     * @param pwd
     *            操作员密码
     */
    public Operator(String id, String pwd, String name) {
        this.operId = id;
        this.pd = pwd;
        this.name = name;
    }

    public String getOperId() {
        return operId;
    }

    public void setOperId(String operId) {
        this.operId = operId;
    }

    public String getPd() {
        return pd;
    }

    public void setPd(String pd) {
        this.pd = pd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    class DbInfo {
        private static final int DB_VER = 1;
        private static final String DB_NAME = "OperDb";
        private static final String TABLE_NAME = "operator";
    }

    private static IDao<Operator> getDao() {
        try {
            return FinancialApplication.getDb().getDb(DbInfo.DB_VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME, Operator.class,
                    new IDb.IDbListener<Operator>() {

                        @Override
                        public IDao<Operator> onUpdate(IDao<Operator> dao, int arg1, int arg2) {
                            try {
                                dao.dropTable();

                                return FinancialApplication.getDb().getDb(DbInfo.DB_VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME,
                                        Operator.class, null);
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                            }
                            return null;
                        }
                    });

        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 初始化基本操作员
     */
    public static void init() {
        try {
            IDao<Operator> dao = getDao();
            if (dao != null && dao.getCount() == 0) {
                dao.save(new Operator("01", "0000"));
                dao.save(new Operator("02", "0000"));
                dao.save(new Operator("03", "0000"));
                dao.save(new Operator("04", "0000"));
                dao.save(new Operator("05", "0000"));
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
    }

    public boolean add() {
        try {
            IDao<Operator> dao = getDao();
            if (dao != null) {
                dao.save(this);
                return true;
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    public static boolean delete(String operId) {
        try {
            IDao<Operator> dao = getDao();
            Operator oper = find(operId);
            if (oper == null) {
                return false;
            }
            if (dao != null) {
                dao.delete(oper.getId());
                return true;
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    public static Operator find(String operId) {
        try {
            IDao<Operator> dao = getDao();
            List<Operator> list = null;
            if (dao != null) {
                list = dao.findByCondition("operId = " + "\'" + operId + "\'");
            }
            if (!CollectionUtils.isEmpty(list)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static List<Operator> findAll() {

        try {
            IDao<Operator> dao = getDao();
            List<Operator> list = null;
            if (dao != null) {
                list= dao.findAll();
            }
            if (!CollectionUtils.isEmpty(list)) {
                return list;
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public void update() {
        try {
            Operator o = find(getOperId());
            if (o == null) {
                return;
            }
            setId(o.getId());
            IDao<Operator> dao = getDao();
            if (dao != null) {
                dao.update(this);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
    }

    public long getCount() {
        long result = 0;
        try {
            IDao<Operator> dao = getDao();
            if (dao != null) {
                result = dao.getCount();
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return result;
    }

}