package com.pax.pay.emv;

import android.util.Log;

import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.IDbListener;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class CardBin extends AEntityBase {
    public static final String TAG = "CardBin";

    private static final String[] BIN_A = new String[]{"16622211", "16622212", "16622213",
            "16622214", "16622215",
            "16622220", "16622223", "16622224", "16622225", "16622229", "16622206", "16622230",
            "16622231", "16622232",
            "16622233", "16622234", "16622235", "16622236", "16622237", "16622238", "16622239",
            "16622240", "16622245",
            "16622246", "16622597", "16622599", "16622910", "16625160", "16625161", "16625162",
            "16625330", "16625331",
            "16625332", "16625650", "16625708", "16625709", "16625858", "16625859", "16625860",
            "16625865", "16625866",
            "16625899", "16628286", "16628288", "16622210", "16620514", "16622752", "16622753",
            "16622759", "16622760",
            "16622761", "16622788", "16625834", "16625333", "16625337", "16625338", "16625568",
            "16625905", "16625906",
            "16625907", "16625908", "16625909", "16625910", "16628312", "16628313", "16628388",
            "19620060", "19620525",
            "16621080", "19621081", "16621082", "19621284", "16621466", "19621467", "16621488",
            "16621499", "19621598",
            "19621621", "19621673", "19621700", "19622280", "19622700", "19623094", "16622966",
            "16622988", "19623211",
            "16623251", "19623668", "16625955", "16625956", "16622168", "19622260", "19622262",
            "16622252", "16622253",
            "16622656", "16628216", "16628218", "16622285", "16622588", "16621483", "16622609",
            "16621485", "16621286",
            "16621486", "16623126", "16623136", "16622575", "16622576", "16622577", "16622578",
            "16628290", "16622581",
            "16622582", "16625802", "16625803", "16628362", "19623699", "19623698", "19621798",
            "19621797", "19621799",
            "16625367", "16625368", "16625978", "16622685", "16622687", "16625339", "16622658",
            "16622570", "16625979",
            "16622659", "16625975", "16622650", "16628201", "16622161", "16625981", "16622657",
            "16625976", "16622655",
            "16628202", "16622801", "16622600", "16622601", "16622602", "16622623", "16625911",
            "16625912", "16625913",
            "16625188", "16621791", "16621792", "16621793", "16621795", "16621796", "16622516",
            "16622522", "16622518",
            "16621390", "16621351", "16622521", "16621352", "16622523", "16622916", "16622918",
            "16622919", "16622680",
            "16622688", "16622689", "16628206", "16628208", "16628209", "16628370", "16628371",
            "16628372", "16621468",
            "16621420", "16623111", "19620550", "16622889", "16625900", "16625801", "16625941",
            "16625136", "16625040",
            "16625042", "16625017", "16625018", "16625019", "16623074", "17623074", "18623074",
            "19623074", "16625824",
            "17625824", "18625824", "19625824", "16620136", "17620136", "18620136", "19620136"};

    private static final String[] BIN_B = new String[]{"19622260", "19622262", "16622588",
            "16621483", "16622609",
            "16621485", "16621286", "16621486", "16623126", "16623136", "19623699", "19623698",
            "19621798", "19621797",
            "19621799", "16621791", "16621792", "16621793", "16621795", "16621796", "16622516",
            "16622522", "16622518",
            "16621390", "16621351", "16622521", "16621352", "16622523", "16621468", "16621420",
            "16623111", "19601382",
            "19621663", "19620061", "19621283", "19621661", "19621725", "19621660", "19621666",
            "19621668", "19621669",
            "19621330", "19621331", "19621332", "19621333", "19621568", "19621569", "19621756",
            "19621757", "19621758",
            "19621759", "19621785", "19621786", "19621787", "19621788", "19621789", "19621790",
            "19621620", "19623208",
            "19621672", "19623575", "19623573", "19623572", "19623571", "19623184", "19623569",
            "19623586", "19621665",
            "19620060", "19620525", "16621080", "19621081", "16621082", "19621284", "16621466",
            "19621467", "16621488",
            "16621499", "19621598", "19621621", "19621673", "19621700", "19622280", "19622700",
            "19623094", "16622966",
            "16622988", "19623211", "16623251", "19623668", "16625955", "16625956", "19622568",
            "19621462", "19623506",
            "19620550", "16623074", "17623074", "18623074", "19623074", "16620136", "17620136",
            "18620136", "19620136",
            "16627000", "16627001", "16627002", "16627003", "16627004", "16627005", "16627006",
            "16627007", "16627008",
            "16627009", "16627010", "16627011", "16627012", "16627013", "16627014", "16627015",
            "16627016", "16627017",
            "16627018", "16627019", "19627020", "19627021", "19627022", "19627023", "19627024",
            "19627025", "19627026",
            "19627027", "19627028", "19627029", "19627030", "19627031", "19627032", "19627033",
            "19627034", "19627035",
            "19627036", "19627037", "19627038", "19627039", "19627040", "19627041", "19627042",
            "19627043", "19627044",
            "19627045", "19627046", "19627047", "19627048", "19627049", "19627050", "19627051",
            "19627052", "19627053",
            "19627054", "19627055", "19627056", "19627057", "19627058", "19627059"};

    // 卡号
    @Column(canBeNull = true)
    private String bin;
    @Column(canBeNull = true)
    private int cardNoLen;

    public CardBin() {

    }

    public CardBin(String bin, int cardNoLen) {
        this.bin = bin;
        this.cardNoLen = cardNoLen;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public int getCardNoLen() {
        return cardNoLen;
    }

    public void setCardNoLen(int cardNoLen) {
        this.cardNoLen = cardNoLen;
    }

    /********************************** 卡BIN数据库信息 *****************************************/
    /**
     * 卡bin数据信息
     *
     * @author Steven.W
     */
    class DbInfo {
        /**
         * 版本号
         */
        public static final int VER = 1;
        /**
         * 卡bin数据库名称
         */
        public static final String DB_NAME = "cardBin.db";
        /**
         * 卡bin黑名单表名
         */
        public static final String TABLE_NAME_BLACK = "black";
        /**
         * 卡binA表名
         */
        public static final String TABLE_NAME_BIN_A = "binA";
        /**
         * 卡binB表名
         */
        public static final String TABLE_NAME_BIN_B = "binB";
        /**
         * 卡binC表名
         */
        public static final String TABLE_NAME_BIN_C = "binC";
    }

    /********************************** 卡bin数据库句柄 ********************************************/
    /**
     * 获取卡BIN数据库句柄N
     *
     * @return
     * @throws DbException
     */
    private static IDao<CardBin> getCardBinDao(final String tableName) throws DbException {
        return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, tableName, CardBin.class,
                new IDbListener<CardBin>() {

                    @Override
                    public IDao<CardBin> onUpdate(IDao<CardBin> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_BIN_A,
                                    CardBin.class, null).dropTable();

                            FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_BIN_B,
                                    CardBin.class, null).dropTable();

                            FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_BIN_C,
                                    CardBin.class, null).dropTable();

                            FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_BLACK,
                                    CardBin.class, null).dropTable();

                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, tableName, CardBin.class, null);
                        } catch (DbException e) {
                            Log.e(TAG, "", e);
                        }
                        return null;
                    }
                });
    }

    /********************************************** 卡BIN数据库操作
     * *****************************************************/
    /**
     * 写一条黑名单卡bin
     *
     * @param
     * @return
     */
    public boolean saveBlack() {
        try {
            IDao<CardBin> iBlack = getCardBinDao(DbInfo.TABLE_NAME_BLACK);
            String cardBin = getBin();
            String sql = String.format("bin = '" + cardBin + "'");
            List<CardBin> list = iBlack.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                // 已有则不重复保存
                return true;
            }
            iBlack.save(this);
        } catch (DbException e) {
            Log.e(TAG, "", e);
            return false;
        }

        return true;
    }

    /**
     * 写卡BIN B, BIN B分两部分, 一部分是默认的写死在程序中, 另一部分通过下载的保存在数据库中
     *
     * @param binList
     */
    public static void saveBinB(ArrayList<CardBin> binList) {
        if (CollectionUtils.isEmpty(binList)) {
            return;
        }

        for (CardBin cardBin : binList) {
            String bin = cardBin.getBin();
            int len = cardBin.getCardNoLen();
            for (String defaultBin : BIN_B) {
                if (defaultBin == null || defaultBin.length() < 8) {
                    continue;
                }
                // 已有则不重复保存
                if (Integer.parseInt(defaultBin.substring(0, 2)) == len && defaultBin.substring
                        (2, 8).equals(bin)) {
                    return;
                }
            }
            saveCardBin(cardBin, DbInfo.TABLE_NAME_BIN_B);
        }
    }

    /**
     * 写卡BIN C
     *
     * @param binList
     */
    public static void saveBinC(ArrayList<CardBin> binList) {
        if (CollectionUtils.isEmpty(binList)) {
            return;
        }

        for (CardBin cardBin : binList) {
            saveCardBin(cardBin, DbInfo.TABLE_NAME_BIN_C);
        }
    }

    /**
     * 存储单条 卡bin B/C
     *
     * @param cardBin   :卡bin
     * @param tableName ：表名称
     * @return
     */
    private static boolean saveCardBin(CardBin cardBin, String tableName) {
        try {
            IDao<CardBin> dao = getCardBinDao(tableName);
            if (cardBin != null) {
                String bin = cardBin.getBin();
                String sql = String.format("bin = '" + bin + "'");
                List<CardBin> list = dao.findByCondition(sql);
                if ((list != null) && (list.size() == 1) && list.contains(cardBin)) {
                    // 已有则不重复保存
                    return true;
                }
                dao.save(cardBin);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
            return false;
        }

        return true;
    }

    /**
     * 删除卡bin C
     */
    public static void deleteBinC() {
        deleteBin(DbInfo.TABLE_NAME_BIN_C);
    }

    /**
     * 删除黑名单
     */
    public static void deleteBlack() {
        deleteBin(DbInfo.TABLE_NAME_BLACK);
    }

    /**
     * 删除卡bin
     */
    private static boolean deleteBin(String tableName) {
        try {
            IDao<CardBin> dao = getCardBinDao(tableName);
            if (dao == null) {
                return false;
            }
            dao.deleteAll();
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    /**
     * 判断卡号是否在黑名单中
     *
     * @param cardNo : 卡号
     * @return
     */
    public static boolean isInBlack(String cardNo) {
        try {
            IDao<CardBin> dao = getCardBinDao(DbInfo.TABLE_NAME_BLACK);
            List<CardBin> list = dao.findAll();
            if ((cardNo != null) && (list != null)) {
                for (int i = 0; i < list.size(); i++) {
                    CardBin cardBin = list.get(i);
                    if (cardBin == null)
                        continue;
                    if (cardNo.regionMatches(0, cardBin.getBin(), 0, cardBin.getBin().length())) {
                        return true;
                    }
                }
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * 检查卡号是否在BINA中, 注：BINA写死在程序中，和BINB,BINC有区别
     *
     * @param cardNo
     * @return
     */
    public static boolean isInCardBinA(String cardNo) {
        if (cardNo == null || cardNo.length() < 6) {
            return false;
        }
        for (String bin : BIN_A) {
            if (bin == null || bin.length() < 8) {
                continue;
            }
            if (Integer.parseInt(bin.substring(0, 2)) == cardNo.length()
                    && cardNo.substring(0, 6).equals(bin.substring(2, 8))) {
                return true;
            }
            continue;
        }

        return false;
    }

    /**
     * 检查卡号是否在BIN表B中, BIN B分两部分, 一部分是默认的写死在程序中, 另一部分通过下载的保存在数据库中
     *
     * @param cardNo
     * @return
     */
    public static boolean isInCardBinB(String cardNo) {
        if (cardNo == null || cardNo.length() < 6) {
            return false;
        }
        // 先从默认的里找
        for (String bin : BIN_B) {
            if (bin == null || bin.length() < 8) {
                continue;
            }
            if (Integer.parseInt(bin.substring(0, 2)) == cardNo.length()
                    && cardNo.substring(0, 6).equals(bin.substring(2, 8))) {
                return true;
            }
        }
        // 从数据库里找
        CardBin cardBin = getCardBin(cardNo.substring(0, 6), DbInfo.TABLE_NAME_BIN_B);
        if (cardBin == null) {
            return false;
        }

        return cardBin.getCardNoLen() == cardNo.length();
    }

    /**
     * 检查卡号是否在BIN表C中
     *
     * @param cardNo
     * @return
     */
    public static boolean isInCardBinC(String cardNo) {
        if (cardNo == null || cardNo.length() < 6) {
            return false;
        }
        CardBin cardBin = getCardBin(cardNo.substring(0, 6), DbInfo.TABLE_NAME_BIN_C);
        if (cardBin == null) {
            return false;
        }

        return cardBin.getCardNoLen() == cardNo.length();
    }

    /**
     * 检查卡bin是否在数据库中
     *
     * @param bin       ：
     * @param tableName ：数据库表名
     * @return
     */
    private static CardBin getCardBin(String bin, String tableName) {
        try {
            IDao<CardBin> dao = getCardBinDao(tableName);
            if (bin != null) {
                String sql = String.format("bin = '" + bin + "'");
                List<CardBin> list = dao.findByCondition(sql);
                if (!CollectionUtils.isEmpty(list)) {
                    return list.get(0);
                }
            }

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        }

        return null;
    }
}
