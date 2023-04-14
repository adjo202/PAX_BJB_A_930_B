package com.pax.pay.trans.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;

public class Controller {
    public static class Constant {
        public static final int YES = 1;
        public static final int NO = 0;
        /**
         * 批上送类型
         */
        public static final int RMBLOG = 1;
        public static final int FRNLOG = 2;
        public static final int ALLLOG = 3;
        public static final int ICLOG = 4;
        /**
         * 批上送状态
         */
        public static final int WORKED = 0;
        public static final int BATCH_UP = 1;
    }

    /**
     * 报文头处理要求A(1-8)
     */
    public static final String HEADER_PROC_REQ_A = "head_proc_req_A";
    /**
     * 报文头处理要求B(9-16)
     */
    public static final String HEADER_PROC_REQ_B = "header_proc_req_B";
    /**
     * 是否需要下载capk NO:不需要 YES:需要
     */
    public static final String NEED_DOWN_CAPK = "need_down_capk";
    /**
     * 是否需要下载aid NO:不需要 YES:需要
     */
    public static final String NEED_DOWN_AID = "need_down_aid";
    /**
     * 是否需要下载非接业务参数
     */
    public static final String NEED_DOWN_CLPARA = "need_down_clpara";
    /**
     * 是否需要下载卡BINB
     */
    public static final String NEED_DOWN_CLBINB = "need_down_clbinb";
    /**
     * 是否需要下载卡BINC
     */
    public static final String NEED_DOWN_CLBINC = "need_down_clbinc";
    /**
     * 是否需要下载黑名单 NO:不需要 YES:需要
     */
    public static final String NEED_DOWN_BLACK = "need_down_black";
    /**
     * 终端签到状态 NO:未签到 YES:已签到
     */
    public static final String POS_LOGON_STATUS = "pos_logon_status";
    /**
     * 操作员签到状态 NO:未签到 YES:已签到
     */
    public static final String OPERATOR_LOGON_STATUS = "operator_logon_status";
    /**
     * 批上送状态 {@link Constant#WORKED}未进行批上送 , {@link Constant#BATCH_UP}:处于批上送状态
     */
    public static final String BATCH_UP_STATUS = "batch_up_status";
    /**
     * 批上送类型 RMBLOG: 上送内卡交易 FRNLOG 上送外卡交易 ALLLOG 上送所有交易 ICLOG 上送IC卡交易
     */
    public static final String BATCH_UP_TYPE = "batch_up_type";
    /**
     * 外卡对账结果
     */
    public static final String FRN_RESULT = "frnResult";
    /**
     * 内卡对账结果
     */
    public static final String RMB_RESULT = "rmbResult";
    /**
     * 批上送笔数
     */
    public static final String BATCH_NUM = "batch_num";
    /**
     * 是否需要清除交易记录: NO:不清除, YES:清除
     */
    public static final String CLEAR_LOG = "clearLog";

    private static Controller controller;
    private static Context context;

    private static final String fileName = "control";

    public static synchronized Controller getInstance(Context context) {
        if (controller == null) {
            Controller.context = context.getApplicationContext();
            controller = new Controller();
            //Log.i("Test",controller.get(Controller.POS_LOGON_STATUS)+"-----------1");
        }

        return controller;
    }

    private Controller() {
        super();
        Log.d("Controller","Sandy=Controller");
        if (isParamFileExist()) {

            return;
        }


        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        //Log.d("Controller","program is executing here.."); //第一次安装会执行到这里
        editor.putInt(HEADER_PROC_REQ_A, Constant.NO);
        editor.putInt(HEADER_PROC_REQ_B, Constant.NO);
        editor.putInt(NEED_DOWN_CAPK, Constant.YES);
        editor.putInt(NEED_DOWN_AID, Constant.YES);
        editor.putInt(NEED_DOWN_CLPARA, Constant.YES);
        editor.putInt(NEED_DOWN_CLBINB, Constant.YES);
        editor.putInt(NEED_DOWN_CLBINC, Constant.YES);
        editor.putInt(NEED_DOWN_BLACK, Constant.YES);
        editor.putInt(POS_LOGON_STATUS, Constant.NO);
        editor.putInt(OPERATOR_LOGON_STATUS, Constant.NO);
        editor.putInt(BATCH_UP_STATUS, Constant.NO);
        editor.commit();
    }

    public int get(String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        int value = sp.getInt(key, 0);
        Log.d("Controller","Sandy=get-key:" + fileName.toString());
        Log.d("Controller","Sandy=get-value:" + value);

        return value;
    }

    public void set(String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Log.d("Controller","Sandy=set:" + fileName.toString());
        Log.d("Controller","Sandy=set-key:" + key);
        Log.d("Controller","Sandy=set-value:" + value);
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private boolean isParamFileExist() {

        String dir = "/data/data/" + context.getPackageName() + File.separator + "shared_prefs/" + fileName + ".xml";
        File file = new File(dir);
        if (file.exists()) {
            Log.d("Controller","Sandy=dir:" + file.toString());
            return true;
        }
        return false;
    }

}
