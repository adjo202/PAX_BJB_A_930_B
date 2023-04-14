package com.pax.pay.constant;

import com.pax.pay.app.FinancialApplication;

import java.io.File;

public class Constants {
    /**
     * 交易成功，弹出对话框的显示时间, 单位秒
     */
    public final static int SUCCESS_DIALOG_SHOW_TIME = 2;
    /**
     * 失败 时弹出框的显示时间, 单位秒
     */
    public final static int FAILED_DIALOG_SHOW_TIME = 5;

    /**
     * 自定义软键盘取消键值定义
     */
    public final static int KEY_EVENT_CANCEL = 65535;
    /**
     * 自定义软件盘隐藏键值定义
     */
    public final static int KEY_EVENT_HIDE = 65534;

    /**
     * mac密钥索引
     */
    public final static byte INDEX_TAK = 0x01;
    /**
     * pin密钥索引
     */
    public static final byte INDEX_TPK = 0x03;
    /**
     * des密钥索引
     */
    public static final byte INDEX_TDK = 0x05;

    /**
     * SSL通讯证书
     */
    public static final String CACERT_PATH = FinancialApplication.getAppContext().getFilesDir() + File.separator + "cacert.pem";
    /**
     * 打印字库路径
     */
    public static final String FONT_PATH = FinancialApplication.getAppContext().getFilesDir().getAbsolutePath() + File.separator;
    /**
     * 字库名称
     */
    public static final String FONT_NAME = "Fangsong.ttf";
    //public static final String FONT_NAME = "Monaco.ttf";

    /**
     * date pattern of storage
     */
    public static final String TIME_PATTERN_TRANS = "yyyyMMddHHmmss";
    public static final String TIME_PATTERN_TRANS2 = "yyMMddHHmmss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm";
    public static final String TIME_PATTERN_NEW = "dd MMM yyyy, HH:mm"; // tambahan abdul
    /**
     * date pattern of display
     */
    public static final String TIME_PATTERN_DISPLAY = "yyyy/MM/dd HH:mm:ss";

    public static final String TIME_PATTERN_DISPLAY2 = "MMM d, yyyy HH:mm";

}
