package com.pax.pay.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.pax.dal.IDAL;
import com.pax.device.GeneralParam;
import com.pax.eemv.EmvImpl;
import com.pax.eemv.IClss;
import com.pax.eemv.IEmv;
import com.pax.eemv.clss.ClssImpl;
import com.pax.gl.IGL;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.gl.convert.IConvert;
import com.pax.gl.db.IDb;
import com.pax.gl.impl.GLProxy;
import com.pax.gl.packer.IPacker;
import com.pax.neptunelite.api.DALProxyClient;
import com.pax.pay.constant.Constants;
import com.pax.pay.operator.Operator;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.utils.AppLog;
import com.pax.pay.utils.ResponseCode;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.squareup.leakcanary.RefWatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.pax.gl.commhelper.IComm;


public class FinancialApplication extends Application {
    private static final String TAG = "FinancialApplication";
    private static FinancialApplication sAppContext;
    /**
     * 系统参数
     */
    private static SysParam sysParam;
    /**
     * 系统控制参数
     */
    private static Controller controller;
    /**
     * 平台应答码解析
     */
    private static ResponseCode rspCode;


    private static GeneralParam generalParam;

    // 获取IPPI常用接口
    private static IDAL dal;
    private static IGL gl;
    private static IEmv emv;
    private static IConvert convert;
    private static IPacker packer;
    private static IDb db;
    private static IClss clss;
    private static PaxGLComm com;




    // 应用版本号
    public static String version;
    private Handler handler;

    private RefWatcher mRefWatcher;
    public static RefWatcher getRefWatcher(Context context) {
        FinancialApplication application = (FinancialApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //added by andy 20170512
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        mRefWatcher = LeakCanary.install(this);   //remove by richard 20170526, depend on if needed,
        //end add
        sAppContext = this;
        handler = new Handler();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(this);
        initData();
        version = getVersion();
        // init();

    }

    public static void init() throws Exception {
        // 获取IPPI常用接口
        if(dal != null)
            return;
        dal = DALProxyClient.getInstance().getDal(sAppContext);
        emv = new EmvImpl().getEmv();
        clss = new ClssImpl().getClss();
        gl = new GLProxy(sAppContext).getGL();
        convert = gl.getConvert();
        packer = gl.getPacker();
        db = gl.getDb();
        // 初始化是参数对象
        sysParam = SysParam.getInstance(sAppContext);
        // 初始化控制对象

        controller = Controller.getInstance(sAppContext);
        com = PaxGLComm.getInstance(sAppContext);


        // 操作员初始化
        Operator.init();
        // 初始化通用参数
        generalParam = GeneralParam.getInstance(sAppContext);
        // 初始化Log
//        AppLog.debug(EDebugLevel.DEBUG_I, true);
//        AppLog.debug(EDebugLevel.DEBUG_E, true);
        AppLog.debug(true);

        //sandy added here
        initLocalData();

    }

    public static void initData() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                initAppstoreTool();
                // 初始化平台应答码
                rspCode = ResponseCode.getInstance();
                try {
                    rspCode.init(FinancialApplication.getAppContext().getResources().getAssets().open("response_list.xml"));
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
                // 拷贝打印字体
                Utils.install(FinancialApplication.getAppContext(), Constants.FONT_NAME, Constants.FONT_PATH);
            }
        }).start();
    }

    public static FinancialApplication getAppContext() {
        return sAppContext;
    }

    public static SysParam getSysParam() {
        return sysParam;
    }

    public static Controller getController() {
        return controller;
    }

    public static ResponseCode getRspCode() {
        return rspCode;
    }

    public static GeneralParam getGeneralParam() {
        return generalParam;
    }

    public static IDAL getDal() {
        return dal;
    }

    public static IGL getGl() {
        return gl;
    }

    public static IEmv getEmv() {
        return emv;
    }

    public static IConvert getConvert() {
        return convert;
    }

    public static IPacker getPacker() {
        return packer;
    }

    public static IDb getDb() {
        return db;
    }

    public static IClss getClss() {
        return clss;
    }

    public static PaxGLComm getComm() {
        return com;
    }




    /**
     * 获取软件版本号
     */
    private String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }


    private static void initLocalData() {
        initProvinceData();
        initLocationData();
        initBranchData();
        initDistrictData();
    }



    private static void initDistrictData() {

        try {
            //if null then we assume, it is empty
            String pData = sysParam.get(SysParam.BPJS_DISTRICT_DATA);
            if( pData == null){
                String district = null;
                String fileName = "bpjs_district.json";
                AssetManager am = FinancialApplication.getAppContext().getResources().getAssets();
                InputStream is = null;
                BufferedReader bufferedReader = null;
                InputStreamReader inputStream = null;
                is = am.open(fileName);
                inputStream = new InputStreamReader(is);
                bufferedReader = new BufferedReader(inputStream);
                StringBuilder out= new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    out.append(line);
                    line = bufferedReader.readLine();
                }
                district = out.toString();

                //store it into context
                JSONObject province = new JSONObject(district);
                sysParam.set(SysParam.BPJS_DISTRICT_DATA, province.toString());
            }


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }



    private static void initBranchData(){

        try {
            //if null then we assume, it is empty
            String pData = sysParam.get(SysParam.BPJS_BRANCH_OFFICE_DATA);
            if( pData == null){
                String branch = null;
                String fileName = "bpjs_branch_data.json";
                AssetManager am = FinancialApplication.getAppContext().getResources().getAssets();
                InputStream is = null;
                BufferedReader bufferedReader = null;
                InputStreamReader inputStream=null;
                is = am.open(fileName);
                inputStream = new InputStreamReader(is);
                bufferedReader = new BufferedReader(inputStream);
                StringBuilder out= new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    out.append(line);
                    line = bufferedReader.readLine();
                }
                branch = out.toString();

                //Sandy : store it into context
                JSONObject province = new JSONObject(branch);
                sysParam.set(SysParam.BPJS_BRANCH_OFFICE_DATA, province.toString());
            }


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    private static void initLocationData() {

        try {
            //if null then we assume, it is empty
            String pData = sysParam.get(SysParam.BPJS_LOCATION_DATA);
            if( pData == null){
                String location = null;
                String fileName = "bpjs_location_data.json";
                AssetManager am = FinancialApplication.getAppContext().getResources().getAssets();
                InputStream is = null;
                BufferedReader bufferedReader = null;
                InputStreamReader inputStream=null;
                is = am.open(fileName);
                inputStream = new InputStreamReader(is);
                bufferedReader = new BufferedReader(inputStream);
                StringBuilder out= new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    out.append(line);
                    line = bufferedReader.readLine();
                }
                location = out.toString();

                //store it into context
                JSONObject province = new JSONObject(location);
                sysParam.set(SysParam.BPJS_LOCATION_DATA, province.toString());
            }


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }



    private static void initProvinceData() {

        String provinces = "{\n" +
                "\"data\": [\n" +
                "\t{\n" +
                "\t\t\"id\" : 8,\n" +
                "\t\t\"kodePropinsi\" : \"51\",\n" +
                "\t\t\"kodeLevel\" : \"51\",\n" +
                "\t\t\"namaPropinsi\" : \"BALI\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 9,\n" +
                "\t\t\"kodePropinsi\" : \"19\",\n" +
                "\t\t\"kodeLevel\" : \"19\",\n" +
                "\t\t\"namaPropinsi\" : \"BANGKA BELITUNG\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 10,\n" +
                "\t\t\"kodePropinsi\" : \"36\",\n" +
                "\t\t\"kodeLevel\" : \"36\",\n" +
                "\t\t\"namaPropinsi\" : \"BANTEN\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 11,\n" +
                "\t\t\"kodePropinsi\" : \"17\",\n" +
                "\t\t\"kodeLevel\" : \"17\",\n" +
                "\t\t\"namaPropinsi\" : \"BENGKULU\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 12,\n" +
                "\t\t\"kodePropinsi\" : \"34\",\n" +
                "\t\t\"kodeLevel\" : \"34\",\n" +
                "\t\t\"namaPropinsi\" : \"DI YOGYAKARTA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 13,\n" +
                "\t\t\"kodePropinsi\" : \"31\",\n" +
                "\t\t\"kodeLevel\" : \"31\",\n" +
                "\t\t\"namaPropinsi\" : \"DKI JAKARTA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 14,\n" +
                "\t\t\"kodePropinsi\" : \"75\",\n" +
                "\t\t\"kodeLevel\" : \"75\",\n" +
                "\t\t\"namaPropinsi\" : \"GORONTALO\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 15,\n" +
                "\t\t\"kodePropinsi\" : \"15\",\n" +
                "\t\t\"kodeLevel\" : \"15\",\n" +
                "\t\t\"namaPropinsi\" : \"JAMBI\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 16,\n" +
                "\t\t\"kodePropinsi\" : \"32\",\n" +
                "\t\t\"kodeLevel\" : \"32\",\n" +
                "\t\t\"namaPropinsi\" : \"JAWA BARAT\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 17,\n" +
                "\t\t\"kodePropinsi\" : \"33\",\n" +
                "\t\t\"kodeLevel\" : \"33\",\n" +
                "\t\t\"namaPropinsi\" : \"JAWA TENGAH\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 18,\n" +
                "\t\t\"kodePropinsi\" : \"35\",\n" +
                "\t\t\"kodeLevel\" : \"35\",\n" +
                "\t\t\"namaPropinsi\" : \"JAWA TIMUR\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 19,\n" +
                "\t\t\"kodePropinsi\" : \"61\",\n" +
                "\t\t\"kodeLevel\" : \"61\",\n" +
                "\t\t\"namaPropinsi\" : \"KALIMANTAN BARAT\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 20,\n" +
                "\t\t\"kodePropinsi\" : \"63\",\n" +
                "\t\t\"kodeLevel\" : \"63\",\n" +
                "\t\t\"namaPropinsi\" : \"KALIMANTAN SELATAN\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 21,\n" +
                "\t\t\"kodePropinsi\" : \"62\",\n" +
                "\t\t\"kodeLevel\" : \"62\",\n" +
                "\t\t\"namaPropinsi\" : \"KALIMANTAN TENGAH\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 22,\n" +
                "\t\t\"kodePropinsi\" : \"64\",\n" +
                "\t\t\"kodeLevel\" : \"64\",\n" +
                "\t\t\"namaPropinsi\" : \"KALIMANTAN TIMUR\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 23,\n" +
                "\t\t\"kodePropinsi\" : \"65\",\n" +
                "\t\t\"kodeLevel\" : \"65\",\n" +
                "\t\t\"namaPropinsi\" : \"KALIMANTAN UTARA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 24,\n" +
                "\t\t\"kodePropinsi\" : \"21\",\n" +
                "\t\t\"kodeLevel\" : \"21\",\n" +
                "\t\t\"namaPropinsi\" : \"KEPULAUAN RIAU\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 25,\n" +
                "\t\t\"kodePropinsi\" : \"18\",\n" +
                "\t\t\"kodeLevel\" : \"18\",\n" +
                "\t\t\"namaPropinsi\" : \"LAMPUNG\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 26,\n" +
                "\t\t\"kodePropinsi\" : \"81\",\n" +
                "\t\t\"kodeLevel\" : \"81\",\n" +
                "\t\t\"namaPropinsi\" : \"MALUKU\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 27,\n" +
                "\t\t\"kodePropinsi\" : \"82\",\n" +
                "\t\t\"kodeLevel\" : \"82\",\n" +
                "\t\t\"namaPropinsi\" : \"MALUKU UTARA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 28,\n" +
                "\t\t\"kodePropinsi\" : \"11\",\n" +
                "\t\t\"kodeLevel\" : \"11\",\n" +
                "\t\t\"namaPropinsi\" : \"NANGGROE ACEH DARUSSALAM (NAD)\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 29,\n" +
                "\t\t\"kodePropinsi\" : \"52\",\n" +
                "\t\t\"kodeLevel\" : \"52\",\n" +
                "\t\t\"namaPropinsi\" : \"NUSA TENGGARA BARAT (NTB)\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 30,\n" +
                "\t\t\"kodePropinsi\" : \"53\",\n" +
                "\t\t\"kodeLevel\" : \"53\",\n" +
                "\t\t\"namaPropinsi\" : \"NUSA TENGGARA TIMUR (NTT)\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 31,\n" +
                "\t\t\"kodePropinsi\" : \"91\",\n" +
                "\t\t\"kodeLevel\" : \"91\",\n" +
                "\t\t\"namaPropinsi\" : \"PAPUA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 32,\n" +
                "\t\t\"kodePropinsi\" : \"92\",\n" +
                "\t\t\"kodeLevel\" : \"92\",\n" +
                "\t\t\"namaPropinsi\" : \"PAPUA BARAT\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 33,\n" +
                "\t\t\"kodePropinsi\" : \"14\",\n" +
                "\t\t\"kodeLevel\" : \"14\",\n" +
                "\t\t\"namaPropinsi\" : \"RIAU\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 34,\n" +
                "\t\t\"kodePropinsi\" : \"76\",\n" +
                "\t\t\"kodeLevel\" : \"76\",\n" +
                "\t\t\"namaPropinsi\" : \"SULAWESI BARAT\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 35,\n" +
                "\t\t\"kodePropinsi\" : \"73\",\n" +
                "\t\t\"kodeLevel\" : \"73\",\n" +
                "\t\t\"namaPropinsi\" : \"SULAWESI SELATAN\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 36,\n" +
                "\t\t\"kodePropinsi\" : \"72\",\n" +
                "\t\t\"kodeLevel\" : \"72\",\n" +
                "\t\t\"namaPropinsi\" : \"SULAWESI TENGAH\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 37,\n" +
                "\t\t\"kodePropinsi\" : \"74\",\n" +
                "\t\t\"kodeLevel\" : \"74\",\n" +
                "\t\t\"namaPropinsi\" : \"SULAWESI TENGGARA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 38,\n" +
                "\t\t\"kodePropinsi\" : \"71\",\n" +
                "\t\t\"kodeLevel\" : \"71\",\n" +
                "\t\t\"namaPropinsi\" : \"SULAWESI UTARA\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 39,\n" +
                "\t\t\"kodePropinsi\" : \"13\",\n" +
                "\t\t\"kodeLevel\" : \"13\",\n" +
                "\t\t\"namaPropinsi\" : \"SUMATERA BARAT\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 40,\n" +
                "\t\t\"kodePropinsi\" : \"16\",\n" +
                "\t\t\"kodeLevel\" : \"16\",\n" +
                "\t\t\"namaPropinsi\" : \"SUMATERA SELATAN\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\" : 41,\n" +
                "\t\t\"kodePropinsi\" : \"12\",\n" +
                "\t\t\"kodeLevel\" : \"12\",\n" +
                "\t\t\"namaPropinsi\" : \"SUMATERA UTARA\"\n" +
                "\t}\n" +
                "]}";

        try {
            JSONObject province = new JSONObject(provinces);
            //if null then we assume, it is empty
            String pData = sysParam.get(SysParam.BPJS_PROVINCE_DATA);
            if( pData == null)
                sysParam.set(SysParam.BPJS_PROVINCE_DATA, province.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * AppstoreTool初始化
     */
    private static void initAppstoreTool() {
//        PaxAppStoreTool.init(sAppContext, new PaxAppStoreCallback() {
//            @Override
//            public void enableUpdateAndUninstall() {
//            	if (FinancialApplication.db==null) {
//					try {
//						init();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						return;
//
//					}
//				}
//
//                if (TransData.getTransCount() > 0) {
////                    PaxAppStoreTool.enableUpdateAndUninstall(sAppContext.getApplicationContext(), false, sAppContext
////                            .getApplicationContext().getResources().getString(R.string.app_need_update_please_settle));
//                    return;
//                }
////                PaxAppStoreTool.enableUpdateAndUninstall(sAppContext, true, null);
//            }
//
//            @Override
//            public void onParamChange() {
//
//            	if (FinancialApplication.db==null) {
//					try {
//						init();
//					} catch (Exception e) {
//						e.printStackTrace();
//						return;
//					}
//				}
//            	 if (TransData.getTransCount() > 0) {
////                     PaxAppStoreTool.enableUpdateAndUninstall(sAppContext.getApplicationContext(), false, sAppContext
////                             .getApplicationContext().getResources().getString(R.string.app_need_update_please_settle));
//                     return;
//                 }
//
//                SysParam.downloadParamOnline(sAppContext.getApplicationContext());
//            }
//        });
    }

}
